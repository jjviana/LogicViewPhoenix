import { ResourceBundle } from '../../../../../../compat/util/ResourceBundle.js';
import { JOptionPane, el, textField } from '../../../../../../compat/swing/Swing.js';
import { VoxellTransformControl } from '../VoxellTransformControl.js';
import { ViewControlListenerSupport } from '../ViewControlSupport.js';
import { Interpolator } from '../../animation/Interpolator.js';
import { TransformedVoxellModel } from '../../../model/TransformedVoxellModel.js';
import { FieldModel } from '../../../model/FieldModel.js';

const res = await ResourceBundle.getBundle(new URL('./TimeStepSelectorVoxelTransformControl.properties', import.meta.url));

/** Selects which time step of a field model is displayed. */
export class TimeStepSelectorVoxelTransformControl {
    static implements = [VoxellTransformControl, Interpolator];

    constructor() {
        this.model = null;
        this.listenerList = null;
        this.control_panel = null;
    }

    controlComponentHidden() {
    }

    controlComponentShown() {
    }

    initControlPanel() {
        if (this.control_panel == null) {
            this.control_panel = el('div', { className: 'lv-flow' });
        } else {
            this.control_panel.replaceChildren();
        }
        const skip = this.model.getTimeStepSkip();
        this.tf = textField(4, String(this.model.getCurrentTimeStep() * skip));
        const timeStepFinal = skip * Math.trunc((this.model.getTimeStepCount() - 1) / skip);
        this.slider = el('input', { type: 'range', className: 'lv-slider lv-timestep-slider', min: 0, max: timeStepFinal, step: skip, value: this.model.getCurrentTimeStep() });
        this.slider.disabled = timeStepFinal === 0;
        this.control_panel.append(el('span', {}, 'Time Step:'), this.tf, el('span', {}, ' of ' + timeStepFinal), this.slider);
        this.slider.addEventListener('input', () => {
            this.tf.value = this.slider.value;
        });
        // JSlider fired once the user let go of the knob (getValueIsAdjusting() == false).
        this.slider.addEventListener('change', async () => {
            const newval = Number(this.slider.value);
            this.tf.value = String(newval);
            await this.model.setCurrentTimeStep(Math.trunc(newval / this.model.getTimeStepSkip()));
            this.fireViewControlListenerControlStateChanged(this);
        });
        this.tf.addEventListener('change', async () => {
            const newval = Number(this.tf.value);
            if (!Number.isInteger(newval)) {
                await JOptionPane.showMessageDialog(null, res.getString('timestepnumberformaterror'), res.getString('timestepformaterrortitle'), JOptionPane.ERROR_MESSAGE);
                this.tf.select();
                return;
            }
            this.slider.value = String(newval);
            this.slider.dispatchEvent(new Event('change'));
        });
    }

    getControlComponent() {
        if (this.control_panel == null) {
            this.control_panel = el('div', { className: 'lv-flow' });
        }
        return this.control_panel;
    }

    getName() {
        return res.getString('name');
    }

    isActive() {
        return true;
    }

    processModel(source) {
        const orig = source;
        while (source instanceof TransformedVoxellModel) {
            source = source.getSource();
        }
        if (source instanceof FieldModel) {
            if (this.model == null || this.model !== source) {
                this.model = source;
                this.initControlPanel();
            }
            return orig;
        }
        return null;
    }

    getConfigurationComponent() {
        return null;
    }

    getInterpolatorName() {
        return res.getString('interpolatorName');
    }

    async setInterpolationStep(step) {
        const new_step = Math.trunc((this.model.getTimeStepCount() - 1) * step);
        if (new_step !== this.model.getCurrentTimeStep()) {
            await this.model.setCurrentTimeStep(new_step);
            this.slider.value = String(new_step * this.model.getTimeStepSkip());
            this.tf.value = this.slider.value;
        }
        this.fireViewControlListenerControlStateChanged(this);
    }
}

Object.assign(TimeStepSelectorVoxelTransformControl.prototype, ViewControlListenerSupport);
