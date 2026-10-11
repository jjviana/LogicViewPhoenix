import { ResourceBundle } from '../../../../../../compat/util/ResourceBundle.js';
import { System } from '../../../../../../compat/util/Logger.js';
import { VoxellTransformControl } from '../VoxellTransformControl.js';
import { ViewControlListenerSupport } from '../ViewControlSupport.js';
import { WrapperVoxellModel } from '../../util/WrapperVoxellModel.js';
import { ValueSelectionVoxellTransformControlPanel as Panel } from './ValueSelectionVoxellTransformControlPanel.js';

const res = await ResourceBundle.getBundle(new URL('./ValueSelectionVoxellTransformControl.properties', import.meta.url));

/**
 * Keeps only the voxels whose value (or absolute value) lies within a band,
 * given either in absolute terms or as a percentage of the maximum.
 */
export class ValueSelectionVoxellTransformControl {
    static implements = [VoxellTransformControl];

    constructor() {
        this.listenerList = null;
        this.cpanel = new Panel();
        this.cpanel.addChangeListener(() => this.fireViewControlListenerControlStateChanged(this));
    }

    controlComponentHidden() {
    }

    controlComponentShown() {
    }

    getControlComponent() {
        return this.cpanel.element;
    }

    getName() {
        return res.getString('name');
    }

    isActive() {
        return this.cpanel.isActive();
    }

    processModel(source) {
        if (this.isActive()) {
            return new ValueTransformWrapper(source, this.cpanel);
        }
        return source;
    }
}

Object.assign(ValueSelectionVoxellTransformControl.prototype, ViewControlListenerSupport);

class ValueTransformWrapper extends WrapperVoxellModel {
    constructor(source, cpanel) {
        super(source);
        this.cpanel = cpanel;
        this.module = cpanel.getValueMode() === Panel.MODULE;
        this.computeMaxValues();
        this.computeLimits();
        System.err.println('Value selection: maxValue= ' + this.maxValue + ', upperLimit=' + this.upperLimit + ', lowerLimit=' + this.lowerLimit);
    }

    computeMaxValues() {
        this.maxAbsValue = -3.4028235E38;
        this.maxValue = -3.4028235E38;
        const model = this.getSource();
        const w = model.getWidth();
        const h = model.getHeight();
        const d = model.getDepth();
        for (let i = 0; i < w; ++i) {
            for (let j = 0; j < h; ++j) {
                for (let k = 0; k < d; ++k) {
                    if (!model.isEnabled(i, j, k)) {
                        continue;
                    }
                    const val = model.getFloatValueAt(i, j, k);
                    if (val > this.maxValue) {
                        this.maxValue = val;
                    }
                    const absVal = Math.abs(val);
                    if (absVal > this.maxAbsValue) {
                        this.maxAbsValue = absVal;
                    }
                }
            }
        }
    }

    computeLimits() {
        const val = this.module ? this.maxAbsValue : this.maxValue;
        if (this.cpanel.getValueScope() === Panel.PERCENT) {
            const center = val * (this.cpanel.getValue() / 100.0);
            this.upperLimit = center + center * (this.cpanel.getDelta() / 100.0);
            this.lowerLimit = center - center * (this.cpanel.getDelta() / 100.0);
        } else {
            const center = this.cpanel.getValue();
            this.upperLimit = center + this.cpanel.getDelta();
            this.lowerLimit = center - this.cpanel.getDelta();
        }
    }

    isEnabled(x, y, z) {
        const orig = this.getSource();
        let value = orig.getFloatValueAt(x, y, z);
        if (this.module) {
            value = Math.abs(value);
        }
        const enable = this.lowerLimit <= value && value <= this.upperLimit;
        return enable && orig.isEnabled(x, y, z);
    }
}
