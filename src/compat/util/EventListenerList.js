/**
 * javax.swing.event.EventListenerList: listeners stored together with the
 * "class" (here: any key, usually an Interface) they were registered for.
 */
export class EventListenerList {
    constructor() {
        this.listeners = [];
    }

    add(type, listener) {
        this.listeners.push([type, listener]);
    }

    remove(type, listener) {
        const i = this.listeners.findIndex(([t, l]) => t === type && l === listener);
        if (i >= 0) {
            this.listeners.splice(i, 1);
        }
    }

    /** Listeners registered for `type`, most recently added first (Swing's firing order). */
    getListeners(type) {
        return this.listeners.filter(([t]) => t === type).map(([, l]) => l).reverse();
    }

    getListenerCount() {
        return this.listeners.length;
    }
}
