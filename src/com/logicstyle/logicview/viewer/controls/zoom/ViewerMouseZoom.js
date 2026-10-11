import * as THREE from 'three';
import { MouseEventId } from '../../../../../../compat/j3d/J3D.js';
import { ViewerMouseBehavior } from '../common/ViewerMouseBehavior.js';

/**
 * Moves the global transform group along Z while the left mouse button is
 * dragged vertically. (Phoenix: the mouse wheel zooms too, whichever tab is shown.)
 */
export class ViewerMouseZoom extends ViewerMouseBehavior {
    constructor(flags = 0) {
        super(flags);
        this.translation = new THREE.Vector3();
        this.callback = null;
    }

    initialize() {
        super.initialize();
        this.z_factor = 0.004;
        if ((this.flags & 2) === 2) {
            this.z_factor *= -1.0;
            this.invert = true;
        }
    }

    getFactor() {
        return this.z_factor;
    }

    setFactor(factor) {
        this.z_factor = factor;
    }

    doProcess(evt) {
        this.processMouseEvent(evt);
        const id = evt.getID();
        if (id === MouseEventId.MOUSE_WHEEL) {
            this.translate(-Math.sign(evt.wheelRotation) * 10);
            return;
        }
        if ((this.buttonPress && (this.flags & 1) === 0) || (this.wakeUp && (this.flags & 1) !== 0)) {
            if (id === MouseEventId.MOUSE_DRAGGED && !evt.isAltDown() && !evt.isMetaDown() && this.isActive()) {
                this.x = evt.getX();
                this.y = evt.getY();
                const dy = this.y - this.y_last;
                if (!this.reset) {
                    this.translate(dy);
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

    translate(dy) {
        const tg = this.getTransformGroup();
        tg.getTransform(this.currXform);
        this.translation.set(0, 0, dy * this.z_factor);
        this.transformX.makeTranslation(this.translation.x, this.translation.y, this.translation.z);
        if (this.invert) {
            this.currXform.multiplyMatrices(this.currXform, this.transformX);
        } else {
            this.currXform.multiplyMatrices(this.transformX, this.currXform);
        }
        tg.setTransform(this.currXform);
        this.transformChanged(this.currXform);
        if (this.callback != null) {
            this.callback.transformChanged(2, this.currXform);
        }
    }

    transformChanged() {
    }

    setupCallback(callback) {
        this.callback = callback;
    }

    getZoom() {
        this.getTransformGroup().getTransform(this.currXform);
        this.translation.setFromMatrixPosition(this.currXform);
        return this.translation.z;
    }

    setZoom(zoom) {
        this.translation.set(0, 0, zoom);
        this.getTransformGroup().getTransform(this.currXform);
        this.currXform.makeTranslation(0, 0, zoom);
        this.getTransformGroup().setTransform(this.currXform);
    }
}
