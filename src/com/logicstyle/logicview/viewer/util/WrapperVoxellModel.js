import { TransformedVoxellModel } from '../../model/TransformedVoxellModel.js';

/** A TransformedVoxellModel delegating everything to its source; subclasses override isEnabled(). */
export class WrapperVoxellModel {
    static implements = [TransformedVoxellModel];

    constructor(source = null) {
        this.orig = source;
    }

    setSource(source) {
        this.orig = source;
    }

    getSource() {
        return this.orig;
    }

    getDeltaX() {
        return this.orig.getDeltaX();
    }

    getDeltaY() {
        return this.orig.getDeltaY();
    }

    getDeltaZ() {
        return this.orig.getDeltaZ();
    }

    getDepth() {
        return this.orig.getDepth();
    }

    getDescription() {
        return this.orig.getDescription();
    }

    getDoubleValueAt(x, y, z) {
        return this.orig.getDoubleValueAt(x, y, z);
    }

    getFloatValueAt(x, y, z) {
        return this.orig.getFloatValueAt(x, y, z);
    }

    getHeight() {
        return this.orig.getHeight();
    }

    getIntValueAt(x, y, z) {
        return this.orig.getIntValueAt(x, y, z);
    }

    getName() {
        return this.orig.getName();
    }

    getObjectValueAt(x, y, z) {
        return this.orig.getObjectValueAt(x, y, z);
    }

    getWidth() {
        return this.orig.getWidth();
    }

    isEnabled(x, y, z) {
        return this.orig.isEnabled(x, y, z);
    }
}
