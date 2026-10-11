import { ResourceBundle } from '../../../../../../compat/util/ResourceBundle.js';
import { el, textField } from '../../../../../../compat/swing/Swing.js';
import { SceneControl } from '../SceneControl.js';
import { Interpolator } from '../../animation/Interpolator.js';
import { formatDecimal2 } from '../common/Format.js';
import { ViewerMouseRotate } from './ViewerMouseRotate.js';

const res = await ResourceBundle.getBundle(new URL('./RotateSceneControl.properties', import.meta.url));

// The original converted between radians and degrees with 6.283 rather than 2*PI.
const TWO_PI_ISH = 6.283;

/** Rotates the shape with the mouse (while its tab is shown) or from typed angles. */
export class RotateSceneControl {
    static implements = [SceneControl, Interpolator];

    constructor() {
        this.active = false;
        this.mrot = new ViewerMouseRotate();
        this.interpolatorControlPanel = null;
        this.rot_panel = new RotationControlPanel(this.mrot);
    }

    addViewControlListener() {
    }

    removeViewControlListener() {
    }

    controlComponentHidden() {
        this.mrot.setActive(false);
    }

    controlComponentShown() {
        this.mrot.setActive(true);
    }

    getControlComponent() {
        return this.rot_panel.element;
    }

    getName() {
        return res.getString('name');
    }

    isActive() {
        return true;
    }

    processScene(root, global, shape) {
        this.mrot.setTransformGroup(shape);
        const oldRoot = this.mrot.getParent();
        if (oldRoot != null) {
            oldRoot.remove(this.mrot);
        }
        global.add(this.mrot);
        this.rot_panel.updateFieldsFromTransform();
    }

    getConfigurationComponent() {
        if (this.interpolatorControlPanel == null) {
            this.interpolatorControlPanel = new InterpolatorControlPanel('Start and end angles for each axis:');
        }
        return this.interpolatorControlPanel.element;
    }

    getInterpolatorName() {
        return 'Rotation';
    }

    setInterpolationStep(step) {
        const p = this.interpolatorControlPanel;
        const startx = parseFloat(p.startX.value);
        const starty = parseFloat(p.startY.value);
        const startz = parseFloat(p.startZ.value);
        const endx = parseFloat(p.endX.value);
        const endy = parseFloat(p.endY.value);
        const endz = parseFloat(p.endZ.value);
        const currentAngles = this.rot_panel.rot.getRotationAngles();
        let rx = startx !== -1.0 ? startx + (endx - startx) * step : currentAngles.x * 360.0 / TWO_PI_ISH;
        let ry = starty !== -1.0 ? starty + (endy - starty) * step : currentAngles.y * 360.0 / TWO_PI_ISH;
        // (The original interpolated Z from the X start angle here.)
        let rz = startz !== -1.0 ? startz + (endz - startz) * step : currentAngles.z * 360.0 / TWO_PI_ISH;
        rx = rx / 360.0 * TWO_PI_ISH;
        ry = ry / 360.0 * TWO_PI_ISH;
        rz = rz / 360.0 * TWO_PI_ISH;
        this.rot_panel.rot.setRotationAngles(rx, ry, rz);
        this.rot_panel.updateFieldsFromTransform();
    }
}

class RotationControlPanel {
    constructor(r) {
        this.rotX = textField(5);
        this.rotY = textField(5);
        this.rotZ = textField(5);
        this.element = el('div', { className: 'lv-flow' },
            el('span', {}, res.getString('rotationLabelBegin') + ' X='), this.rotX,
            el('span', {}, ', Y='), this.rotY,
            el('span', {}, ', Z='), this.rotZ);
        this.rot = r;
        this.updating_from_fields = false;
        this.rot.setupCallback({
            transformChanged: () => {
                if (!this.updating_from_fields) {
                    this.updateFieldsFromTransform();
                }
            },
        });
        const focusLost = () => {
            this.updating_from_fields = true;
            this.updateTransformFromFields();
            this.updating_from_fields = false;
        };
        for (const f of [this.rotX, this.rotY, this.rotZ]) {
            f.addEventListener('change', focusLost);
        }
    }

    updateFieldsFromTransform() {
        const angles = this.rot.getRotationAngles();
        this.rotX.value = formatDecimal2(360.0 * (angles.x / TWO_PI_ISH));
        this.rotY.value = formatDecimal2(360.0 * (angles.y / TWO_PI_ISH));
        this.rotZ.value = formatDecimal2(360.0 * (angles.z / TWO_PI_ISH));
    }

    updateTransformFromFields() {
        let rX = Number(this.rotX.value);
        let rY = Number(this.rotY.value);
        let rZ = Number(this.rotZ.value);
        if ([rX, rY, rZ].some((v) => Number.isNaN(v)) || !this.rot.getTransformGroup()) {
            return;
        }
        rX = rX / 360.0 * TWO_PI_ISH;
        rY = rY / 360.0 * TWO_PI_ISH;
        rZ = rZ / 360.0 * TWO_PI_ISH;
        this.rot.setRotationAngles(rX, rY, rZ);
    }
}

/** "Start and end ... for each axis": six text fields, -1 meaning "leave as is". */
export class InterpolatorControlPanel {
    constructor(title) {
        this.startX = textField(4, '-1');
        this.endX = textField(4, '-1');
        this.startY = textField(4, '-1');
        this.endY = textField(4, '-1');
        this.startZ = textField(4, '-1');
        this.endZ = textField(4, '-1');
        this.element = el('div', { className: 'lv-interpolator-panel' },
            el('div', {}, title),
            el('div', { className: 'lv-flow' }, 'X from ', this.startX, ' to ', this.endX),
            el('div', { className: 'lv-flow' }, 'Y from ', this.startY, ' to ', this.endY),
            el('div', { className: 'lv-flow' }, 'Z from ', this.startZ, ' to ', this.endZ));
    }
}
