import * as THREE from 'three';
import { Behavior, MouseEventId } from '../../../../../../compat/j3d/J3D.js';

/**
 * Base of LogicView's mouse behaviors (com.sun.j3d.utils.behaviors.mouse.MouseBehavior
 * plus the original ViewerMouseBehavior): keeps the transform group it acts on
 * and an "active" flag, set while the control's tab is shown.
 */
export class ViewerMouseBehavior extends Behavior {
    static INVERT_INPUT = 2;

    constructor(flags = 0) {
        super();
        this.flags = flags;
        this.active = false;
        this.tgref = null;
        this.x = 0;
        this.y = 0;
        this.x_last = 0;
        this.y_last = 0;
        this.buttonPress = false;
        this.wakeUp = false;
        this.reset = true;
        this.invert = false;
        this.currXform = new THREE.Matrix4();
        this.transformX = new THREE.Matrix4();
        this.transformY = new THREE.Matrix4();
        this.initialize();
    }

    initialize() {
        this.reset = true;
    }

    isActive() {
        return this.active;
    }

    setActive(active) {
        this.active = active;
    }

    setTransformGroup(transformGroup) {
        this.tgref = new WeakRef(transformGroup);
    }

    getTransformGroup() {
        return this.tgref ? this.tgref.deref() : null;
    }

    /** MouseBehavior.processMouseEvent: tracks the button state. */
    processMouseEvent(evt) {
        if (evt.id === MouseEventId.MOUSE_PRESSED) {
            this.buttonPress = true;
        } else if (evt.id === MouseEventId.MOUSE_RELEASED) {
            this.buttonPress = false;
        }
    }

    processStimulus(criteria) {
        // Successive drag events are coalesced, as the original did with its queue.
        let pendingDrag = null;
        for (const evt of criteria) {
            if (evt.id === MouseEventId.MOUSE_DRAGGED) {
                pendingDrag = evt;
                continue;
            }
            if (pendingDrag) {
                this.doProcess(pendingDrag);
                pendingDrag = null;
            }
            this.doProcess(evt);
        }
        if (pendingDrag) {
            this.doProcess(pendingDrag);
        }
    }

    doProcess() {
    }
}
