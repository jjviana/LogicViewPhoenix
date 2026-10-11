import * as THREE from 'three';
import { MouseEventId } from '../../../../../../compat/j3d/J3D.js';
import { ViewerMouseBehavior } from '../common/ViewerMouseBehavior.js';

/**
 * Rotates the shape transform group while the left mouse button is dragged:
 * vertical motion rotates around X, horizontal motion around Y.
 */
export class ViewerMouseRotate extends ViewerMouseBehavior {
    constructor(flags = 0) {
        super(flags);
        this.transformZ = new THREE.Matrix4();
        this.callback = null;
    }

    initialize() {
        super.initialize();
        this.x_angle = 0.0;
        this.y_angle = 0.0;
        this.x_factor = 0.03;
        this.y_factor = 0.03;
        if ((this.flags & 2) === 2) {
            this.invert = true;
            this.x_factor *= -1.0;
            this.y_factor *= -1.0;
        }
    }

    getXFactor() {
        return this.x_factor;
    }

    getYFactor() {
        return this.y_factor;
    }

    setFactor(xFactor, yFactor = xFactor) {
        this.x_factor = xFactor;
        this.y_factor = yFactor;
    }

    doProcess(evt) {
        this.processMouseEvent(evt);
        if ((this.buttonPress && (this.flags & 1) === 0) || (this.wakeUp && (this.flags & 1) !== 0)) {
            const id = evt.getID();
            if (id === MouseEventId.MOUSE_DRAGGED && !evt.isMetaDown() && !evt.isAltDown() && this.isActive()) {
                this.x = evt.getX();
                this.y = evt.getY();
                const dx = this.x - this.x_last;
                const dy = this.y - this.y_last;
                if (!this.reset) {
                    this.x_angle = dy * this.y_factor;
                    this.y_angle = dx * this.x_factor;
                    this.transformX.makeRotationX(this.x_angle);
                    this.transformY.makeRotationY(this.y_angle);
                    const tg = this.getTransformGroup();
                    tg.getTransform(this.currXform);
                    const mat = this.currXform.clone();
                    this.currXform.setPosition(0, 0, 0);
                    if (this.invert) {
                        this.currXform.multiplyMatrices(this.currXform, this.transformX);
                        this.currXform.multiplyMatrices(this.currXform, this.transformY);
                    } else {
                        this.currXform.multiplyMatrices(this.transformX, this.currXform);
                        this.currXform.multiplyMatrices(this.transformY, this.currXform);
                    }
                    this.currXform.copyPosition(mat);
                    tg.setTransform(this.currXform);
                    this.transformChanged(this.currXform);
                    if (this.callback != null) {
                        this.callback.transformChanged(0, this.currXform);
                    }
                } else {
                    this.reset = false;
                }
                this.x_last = this.x;
                this.y_last = this.y;
            } else if (id === MouseEventId.MOUSE_PRESSED) {
                this.x_last = evt.getX();
                this.y_last = evt.getY();
            }
        }
    }

    transformChanged() {
    }

    setupCallback(callback) {
        this.callback = callback;
    }

    /** Euler angles (x, y, z) of the current rotation, each in [0, 2*PI). */
    getRotationAngles() {
        const e = this.currXform.elements;
        // Matrix3d.getElement(row, col) of the rotation part (three.js is column major).
        const m = (row, col) => e[col * 4 + row];
        const scale = new THREE.Vector3().setFromMatrixColumn(this.currXform, 0).length() || 1;
        const angles = { x: 0, y: 0, z: 0 };
        angles.y = Math.fround(Math.asin(Math.max(-1, Math.min(1, m(0, 2) / scale))));
        const c = Math.cos(angles.y);
        if (Math.abs(c) > 1.0E-5) {
            let tRx = m(2, 2) / scale / c;
            let tRy = -m(1, 2) / scale / c;
            angles.x = Math.atan2(tRy, tRx);
            tRx = m(0, 0) / scale / c;
            tRy = -m(0, 1) / scale / c;
            angles.z = Math.atan2(tRy, tRx);
        } else {
            angles.x = 0.0;
            const tRx = m(1, 1) / scale / c;
            const tRy = m(1, 0) / scale / c;
            angles.z = Math.atan2(tRy, tRx);
        }
        for (const k of ['x', 'y', 'z']) {
            if (angles[k] < 0.0) {
                angles[k] += Math.PI * 2;
            } else if (angles[k] > Math.PI * 2) {
                angles[k] -= Math.PI * 2;
            }
            if (angles[k] < 0.001 && angles[k] > -0.001) {
                angles[k] = 0.0;
            }
            if (angles[k] === Math.PI * 2) {
                angles[k] = 0.0;
            }
        }
        return angles;
    }

    setRotationAngles(rx, ry, rz) {
        this.transformX.makeRotationX(rx);
        this.transformY.makeRotationY(ry);
        this.transformZ.makeRotationZ(rz);
        this.currXform.identity();
        this.currXform.multiply(this.transformX);
        this.currXform.multiply(this.transformY);
        this.currXform.multiply(this.transformZ);
        this.getTransformGroup().setTransform(this.currXform);
    }
}
