import * as THREE from 'three';
import { ResourceBundle } from '../../../../../compat/util/ResourceBundle.js';
import { Dimension } from '../../../../../compat/awt/Dimension.js';
import { Color } from '../../../../../compat/awt/Color.js';
import { el, ProgressMonitor, JOptionPane, nextFrame } from '../../../../../compat/swing/Swing.js';
import { System } from '../../../../../compat/util/Logger.js';
import {
    BranchGroup, TransformGroup, SpotLight, LightingEnvironment, createLitMaterial, MouseEventId, toAwtMouseEvent,
} from '../../../../../compat/j3d/J3D.js';
import { createText3DGeometry } from '../../../../../compat/j3d/Text3D.js';
import { View } from '../../model/View.js';
import { PrintPageElement } from '../printing/PrintPageElement.js';
import { ViewControlFactory } from '../controls/ViewControlFactory.js';
import { VoxellTransformControl } from '../controls/VoxellTransformControl.js';
import { SceneControl } from '../controls/SceneControl.js';
import { VoxellColorGeneratorControl } from '../controls/color/VoxellColorGeneratorControl.js';
import { VoxellGeometryGenerator } from '../controls/geometry/VoxellGeometryGenerator.js';
import { ViewControlPanel } from '../util/ViewControlPanel.js';

const res = await ResourceBundle.getBundle(new URL('./VoxellModelView.properties', import.meta.url));

// Java 3D's default field of view: 45 degrees, measured horizontally.
const FIELD_OF_VIEW = Math.PI / 4.0;
// ViewingPlatform.setNominalViewingTransform(): the eye is moved back so that
// [-1, 1] at z = 0 spans the width of the window.
const NOMINAL_VIEWING_DISTANCE = 1.0 / Math.tan(FIELD_OF_VIEW / 2.0);

/**
 * The 3D view of a voxel model.
 *
 * Builds the view's control panel from every ViewControlFactory that accepts
 * the model, and a scene graph laid out exactly like the Java 3D original:
 *
 *   rootHolder
 *     root (BranchGroup)
 *       globalTransformGroup        <- zoom acts here (and holds the lights)
 *         4 SpotLights
 *         shapeTransformGroup       <- rotation acts here
 *           shape (BranchGroup): the voxel surface + the coordinate axis
 *
 * Whenever a transform or color control changes, the model is pushed through
 * every active VoxellTransformControl (in repository order) and the surface is
 * rebuilt by the active VoxellGeometryGenerator. Rebuilds are serialized, as the
 * original serialized its rebuild threads on viewUpdateLock.
 */
export class VoxellModelView {
    static implements = [View, PrintPageElement];

    constructor(m, v) {
        this.mod = m;
        this.viewer = v;
        this.view_controls = [];
        this.viewUpdateLock = Promise.resolve();
        this.printing_size = new Dimension(320, 200);
        this.minimum_printing_size = new Dimension(100, 100);
        this.lighting = new LightingEnvironment();
        this.renderPending = false;
        this.disposed = false;
    }

    getViewComponent() {
        if (this.panel) {
            return this.panel;
        }
        const p = el('div', { className: 'lv-voxellview' });
        this.panel = p;
        const rep = this.viewer.getRepository();
        const viewControls = rep.findByInterface(ViewControlFactory);
        if (viewControls.length > 0) {
            for (const factory of viewControls) {
                const v = factory.getViewControl(this.mod);
                if (v == null) {
                    continue;
                }
                this.view_controls.push(v);
                const listener = {
                    controlActivated: (control) => this.reconstructView(control),
                    controlDeactivated: (control) => listener.controlActivated(control),
                    controlStateChanged: (control) => listener.controlActivated(control),
                };
                v.addViewControlListener(listener);
            }
            const vp = new ViewControlPanel(this.view_controls);
            p.append(vp.element);
        }
        this.canvasHolder = el('div', { className: 'lv-canvas3d' });
        this.c3d = el('canvas', { className: 'lv-canvas', tabindex: 0 });
        this.canvasHolder.append(this.c3d);
        p.append(this.canvasHolder);

        this.renderer = new THREE.WebGLRenderer({ canvas: this.c3d, antialias: true });
        this.renderer.outputColorSpace = THREE.LinearSRGBColorSpace;
        this.renderer.setPixelRatio(window.devicePixelRatio || 1);
        this.camera = new THREE.PerspectiveCamera(45, 4 / 3, 0.01, 100);
        this.camera.position.set(0, 0, NOMINAL_VIEWING_DISTANCE);
        this.scene = new THREE.Scene();
        this.rootHolder = new BranchGroup();
        this.scene.add(this.rootHolder);
        this.installMouseHandling();
        new ResizeObserver(() => this.updateSize()).observe(this.canvasHolder);

        this.ready = this.viewUpdateLock = this.constructScene();
        return p;
    }

    /** Resolves once the first surface has been built. */
    whenReady() {
        return this.ready;
    }

    /** Resolves when no rebuild is pending. */
    async whenIdle() {
        let p;
        do {
            p = this.viewUpdateLock;
            await p;
        } while (p !== this.viewUpdateLock);
    }

    async constructScene() {
        await this.generateShape((percent) => this.viewer.fireViewConstructionListenerViewConstructionProgress(res.getString('Model.BuildingSurfaceActionName'), percent, true));
        this.setupScene();
        this.requestRender();
    }

    reconstructView(v) {
        this.viewUpdateLock = this.viewUpdateLock.catch(() => {}).then(async () => {
            if (this.disposed) {
                return;
            }
            const monitor = new ProgressMonitor({ root: this.panel.closest('.lv-window') }, 'Reconstructing view', null, 0, 100);
            monitor.setMillisToDecideToPopup(0);
            monitor.setMillisToPopup(0);
            monitor.setProgress(10);
            await nextFrame();
            try {
                if (v instanceof VoxellTransformControl || v instanceof VoxellColorGeneratorControl) {
                    await this.generateShape((percent) => {
                        monitor.setNote(res.getString('Model.BuildingSurfaceActionName'));
                        monitor.setProgress(10 + 0.85 * percent);
                    });
                    monitor.setProgress(95);
                    this.disposeShape(this.shapeTransformGroup);
                    this.shapeTransformGroup.removeAllChildren();
                    this.shapeTransformGroup.addChild(this.shape);
                }
                if (v instanceof SceneControl) {
                    this.setupScene();
                }
            } catch (e) {
                System.err.printStackTrace(e);
                JOptionPane.showMessageDialog(null, 'Error reconstructing the view:\n' + e.message, 'Error', JOptionPane.ERROR_MESSAGE);
            } finally {
                monitor.setProgress(100);
                this.requestRender();
            }
        });
        return this.viewUpdateLock;
    }

    async generateShape(progress) {
        let current = this.mod;
        for (const v of this.view_controls) {
            if (!(v instanceof VoxellTransformControl) || !v.isActive()) {
                continue;
            }
            current = v.processModel(current);
        }
        this.shape = await this.getCurrentSurface(current, progress);
    }

    setupScene() {
        this.rootHolder.removeAllChildren();
        this.root = new BranchGroup();
        const white = Color.white;
        const keyLight = new SpotLight(white, [-0.707, 0.707, 1.0], [0.0, 0.75, 0.0], [0.707, -0.707, -1.0], 1.0471976, 100.0);
        const keyLight2 = new SpotLight(white, [0.707, -0.707, 1.0], [0.0, 0.75, 0.0], [-0.707, 0.707, -1.0], 1.0471976, 100.0);
        const fillLight = new SpotLight(white, [1.06, 1.06, 1.0], [0.0, 0.75, 0.0], [-1.06, 1.06, -1.0], 1.0471976, 100.0);
        const backLight = new SpotLight(white, [-0.707, 0.707, -1.0], [0.0, 1.0, 0.0], [0.707, -0.707, 1.0], 1.0471976, 100.0);
        this.shapeTransformGroup = new TransformGroup();
        this.shapeTransformGroup.addEventListener('transformChanged', () => this.requestRender());
        const globalTransformGroup = new TransformGroup();
        globalTransformGroup.addEventListener('transformChanged', () => this.requestRender());
        globalTransformGroup.addChild(keyLight);
        globalTransformGroup.addChild(keyLight2);
        globalTransformGroup.addChild(fillLight);
        globalTransformGroup.addChild(backLight);
        globalTransformGroup.addChild(this.shapeTransformGroup);
        this.shapeTransformGroup.addChild(this.shape);
        this.root.addChild(globalTransformGroup);
        for (const v of this.view_controls) {
            if (v instanceof SceneControl) {
                v.processScene(this.root, globalTransformGroup, this.shapeTransformGroup);
            }
        }
        // A leftover of the original: the light positions drawn as white
        // points (POLYGON_POINT), invisible on the white background.
        const points = new THREE.BufferGeometry().setFromPoints([
            new THREE.Vector3(-0.707, 0.707, 1.0), new THREE.Vector3(1.06, 1.06, 1.0), new THREE.Vector3(0.707, 0.707, -1.0)]);
        globalTransformGroup.addChild(new THREE.Points(points, new THREE.PointsMaterial({ color: 0xffffff, size: 1, sizeAttenuation: false })));
        // Background(Color.white)
        this.scene.background = new THREE.Color(1, 1, 1);
        this.rootHolder.addChild(this.root);
    }

    async getCurrentSurface(cur, progress = () => {}) {
        this.viewer.fireViewConstructionListenerViewConstructionProgress(res.getString('Model.BuildingSurfaceActionName'), 0.0, true);
        let g = null;
        for (const v of this.view_controls) {
            if (!(v instanceof VoxellGeometryGenerator) || !v.isActive()) {
                continue;
            }
            g = await v.getGeometry(cur, (fraction) => progress(100 * fraction));
            break;
        }
        const bgr = new BranchGroup();
        // Material: shininess 1, specular 0.02, color target AMBIENT_AND_DIFFUSE.
        const material = createLitMaterial(this.lighting, {
            vertexColors: true,
            specular: new THREE.Vector3(0.02, 0.02, 0.02),
            shininess: 1.0,
        });
        if (g != null) {
            bgr.addChild(new THREE.Mesh(g, material));
        }
        bgr.addChild(this.generateCoordinateAxis(cur));
        return bgr;
    }

    getViewName() {
        return res.getString('viewName');
    }

    /* PrintPageElement ------------------------------------------------ */

    getMinimumSize() {
        return this.minimum_printing_size;
    }

    getName() {
        return res.getString('printElementName');
    }

    getOrientation() {
        return PrintPageElement.CENTER;
    }

    getSize() {
        return this.printing_size;
    }

    getSupportedOrientations() {
        return PrintPageElement.CENTER;
    }

    setOrientation() {
    }

    setSize(size) {
        this.printing_size = size;
    }

    /** Renders the scene off screen at the printing size and draws it into `g`. */
    async print(g) {
        await this.whenIdle();
        g.drawImage(this.renderImage(this.printing_size.width, this.printing_size.height), 0, 0);
    }

    renderImage(width, height) {
        const max = this.renderer.capabilities.maxTextureSize;
        width = Math.max(1, Math.min(Math.round(width), max));
        height = Math.max(1, Math.min(Math.round(height), max));
        const target = new THREE.WebGLRenderTarget(width, height, { samples: 4 });
        const camera = this.camera.clone();
        this.setupProjection(camera, width / height);
        this.scene.updateMatrixWorld();
        camera.updateMatrixWorld();
        this.lighting.update(this.scene, camera);
        this.renderer.setRenderTarget(target);
        this.renderer.render(this.scene, camera);
        const pixels = new Uint8Array(width * height * 4);
        this.renderer.readRenderTargetPixels(target, 0, 0, width, height, pixels);
        this.renderer.setRenderTarget(null);
        target.dispose();
        const image = new ImageData(width, height);
        const row = width * 4;
        for (let y = 0; y < height; y++) {
            image.data.set(pixels.subarray((height - 1 - y) * row, (height - y) * row), y * row);
        }
        const canvas = document.createElement('canvas');
        canvas.width = width;
        canvas.height = height;
        canvas.getContext('2d').putImageData(image, 0, 0);
        this.requestRender();
        return canvas;
    }

    getModel() {
        return this.mod;
    }

    /* Coordinate axis --------------------------------------------------- */

    generateCoordinateAxis(cur) {
        let smallerDimension;
        let smallerDumensionDelta;
        if (cur.getWidth() < cur.getHeight()) {
            smallerDimension = cur.getWidth();
            smallerDumensionDelta = cur.getDeltaX();
        } else {
            smallerDimension = cur.getHeight();
            smallerDumensionDelta = cur.getDeltaY();
        }
        // (sic: the original compared a voxel size with a voxel count here, so
        // the axis length always follows the depth.)
        if (cur.getDeltaZ() < smallerDimension) {
            smallerDimension = cur.getDepth();
            smallerDumensionDelta = cur.getDeltaZ();
        }
        let axisSize = Math.trunc(smallerDimension / 4) * smallerDumensionDelta;
        if (axisSize < smallerDumensionDelta) {
            axisSize = smallerDumensionDelta;
        }
        const xLeft = Math.trunc(-cur.getWidth() / 2) * cur.getDeltaX() - cur.getDeltaX();
        const yBottom = Math.trunc(-cur.getHeight() / 2) * cur.getDeltaY() - cur.getDeltaY();
        const zBack = Math.trunc(-cur.getDepth() / 2) * cur.getDeltaZ() - cur.getDeltaZ();
        const lineEnds = [
            xLeft, yBottom, zBack, xLeft + axisSize, yBottom, zBack,
            xLeft, yBottom, zBack, xLeft, yBottom + axisSize, zBack,
            xLeft, yBottom, zBack, xLeft, yBottom, zBack + axisSize,
        ];
        // black, black, red, red, blue, blue
        const colors = [0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1];
        const lineArray = new THREE.BufferGeometry();
        lineArray.setAttribute('position', new THREE.Float32BufferAttribute(lineEnds, 3));
        lineArray.setAttribute('color', new THREE.Float32BufferAttribute(colors, 3));
        const gr = new BranchGroup();
        gr.addChild(new THREE.LineSegments(lineArray, new THREE.LineBasicMaterial({ vertexColors: true })));
        const label = (text, color, x, y, z) => {
            const c = new THREE.Vector3(color.r / 255, color.g / 255, color.b / 255);
            // new Material(color, color, color, color, 0.0)
            const ap = createLitMaterial(this.lighting, { ambient: c, emissive: c, diffuse: c, specular: c, shininess: 0.0 });
            const labelTransformgroup = new TransformGroup();
            labelTransformgroup.setTransform(new THREE.Matrix4().makeTranslation(x, y, z).scale(new THREE.Vector3(axisSize / 4.0, axisSize / 4.0, axisSize / 4.0)));
            labelTransformgroup.addChild(new THREE.Mesh(createText3DGeometry(text), ap));
            gr.addChild(labelTransformgroup);
        };
        label('x', Color.black, xLeft + axisSize, yBottom, zBack);
        label('y', Color.red, xLeft, yBottom + axisSize, zBack);
        label('z', Color.blue, xLeft, yBottom, zBack + axisSize);
        return gr;
    }

    /* Rendering ---------------------------------------------------------- */

    setupProjection(camera, aspect) {
        camera.aspect = aspect;
        // Java 3D keeps the horizontal field of view; three.js wants the vertical one.
        camera.fov = THREE.MathUtils.radToDeg(2 * Math.atan(Math.tan(FIELD_OF_VIEW / 2) / aspect));
        camera.updateProjectionMatrix();
    }

    updateSize() {
        const w = this.canvasHolder.clientWidth;
        const h = this.canvasHolder.clientHeight;
        if (w === 0 || h === 0) {
            return;
        }
        this.renderer.setSize(w, h, false);
        this.setupProjection(this.camera, w / h);
        this.requestRender();
    }

    requestRender() {
        if (this.renderPending || this.disposed) {
            return;
        }
        this.renderPending = true;
        requestAnimationFrame(() => {
            this.renderPending = false;
            this.render();
        });
    }

    render() {
        if (this.disposed) {
            return;
        }
        this.scene.updateMatrixWorld();
        this.camera.updateMatrixWorld();
        this.lighting.update(this.scene, this.camera);
        this.renderer.render(this.scene, this.camera);
    }

    /** Java 3D delivered AWT mouse events to the Behaviors of the scene graph. */
    installMouseHandling() {
        const canvas = this.c3d;
        const deliver = (evt) => {
            const behaviors = [];
            this.scene.traverse((o) => o.isBehavior && behaviors.push(o));
            for (const b of behaviors) {
                b.processStimulus([evt]);
            }
        };
        canvas.addEventListener('pointerdown', (e) => {
            canvas.setPointerCapture(e.pointerId);
            canvas.focus();
            deliver(toAwtMouseEvent(e, MouseEventId.MOUSE_PRESSED, canvas));
        });
        canvas.addEventListener('pointermove', (e) => {
            if (e.buttons !== 0) {
                deliver(toAwtMouseEvent(e, MouseEventId.MOUSE_DRAGGED, canvas));
            }
        });
        const release = (e) => deliver(toAwtMouseEvent(e, MouseEventId.MOUSE_RELEASED, canvas));
        canvas.addEventListener('pointerup', release);
        canvas.addEventListener('pointercancel', release);
        canvas.addEventListener('wheel', (e) => {
            e.preventDefault();
            deliver(toAwtMouseEvent(e, MouseEventId.MOUSE_WHEEL, canvas));
        }, { passive: false });
        canvas.addEventListener('contextmenu', (e) => e.preventDefault());
    }

    disposeShape(group) {
        group.traverse((o) => {
            if (o.geometry) {
                o.geometry.dispose();
            }
            if (o.material) {
                o.material.dispose();
            }
        });
    }

    dispose() {
        this.disposed = true;
        if (this.scene) {
            this.disposeShape(this.scene);
        }
        if (this.renderer) {
            this.renderer.dispose();
            this.renderer.forceContextLoss();
        }
    }
}
