import { ResourceBundle } from '../../../../../compat/util/ResourceBundle.js';
import { JDialog, JTabbedPane, JOptionPane, el, button, textField, checkBox, titledPanel } from '../../../../../compat/swing/Swing.js';
import { FieldModel } from '../../model/FieldModel.js';
import { ViewPrintConstructorFactory } from '../printing/ViewPrintConstructorFactory.js';
import { AnimationEncoderFactory } from './AnimationEncoderFactory.js';
import { InterpolatorFactory } from './InterpolatorFactory.js';
import { Animation } from './Animation.js';
import { Animator } from './Animator.js';
import { AnimationDialog } from './AnimationDialog.js';

const res = await ResourceBundle.getBundle(new URL('./AnimationControlPanel.properties', import.meta.url));

/**
 * View > Animate...: frame rate, duration and frame size; the page layout of
 * the frames; the output (encoder) and one tab per interpolator offered for
 * the view (rotation, plane cut, time steps).
 */
export class AnimationControlPanel extends JDialog {
    constructor(rep, v) {
        super(null, false, { maximizable: true });
        this.repository = rep;
        this.view = v;
        this.animationEncoder = null;
        this.setTitle(res.getString('animationPropertiesTitle'));
        this.initComponents();
        this.constructPropertiesPane();
        this.framesPerSecondTextField.value = '1';
        const model = this.view.getModel();
        if (model instanceof FieldModel) {
            this.durationTextField.value = String(model.getTimeStepCount());
        } else {
            this.durationTextField.value = '60';
        }
        this.frameWidthTextField.value = '640';
        this.frameHeightTextField.value = '480';
        this.setSize(640, 520);
    }

    initComponents() {
        this.framesPerSecondTextField = textField(4);
        this.durationTextField = textField(4);
        this.frameWidthTextField = textField(4);
        this.frameHeightTextField = textField(4);
        this.startButton = button(res.getString('startButtonLabel'), () => this.startButtonActionPerformed());
        const row = (label, field) => el('div', { className: 'lv-form-row' }, el('label', {}, label), el('span', {}, field));
        this.animationPropertiesPanel = titledPanel(res.getString('animationPropertiesTitle'),
            el('div', { className: 'lv-animation-properties' },
                el('div', {}, row(res.getString('framesPerSecondLabel'), this.framesPerSecondTextField), row(res.getString('durationLabel'), this.durationTextField)),
                el('div', {}, row(res.getString('frameWidthLabel'), this.frameWidthTextField), row(res.getString('frameHeightLabel'), this.frameHeightTextField)),
                el('div', { className: 'lv-animation-start' }, this.startButton)));
        this.PropertiesPane = new JTabbedPane();
        this.PropertiesPane.element.classList.add('lv-fill');
        this.getContentPane().classList.add('lv-column');
        this.getContentPane().append(this.animationPropertiesPanel, this.PropertiesPane.element);
        this.addWindowListener({ windowClosing: () => this.dispose() });
    }

    async startButtonActionPerformed() {
        const check = async (value, test, message, field) => {
            if (!test(value)) {
                await JOptionPane.showMessageDialog(this, res.getString(message), res.getString('errorMessageTitle'), JOptionPane.ERROR_MESSAGE);
                field.focus();
                return false;
            }
            return true;
        };
        const isInt = (s) => /^\s*\d+\s*$/.test(s) && Number(s) > 0;
        const isNum = (s) => s.trim() !== '' && !Number.isNaN(Number(s)) && Number(s) > 0;
        if (!(await check(this.framesPerSecondTextField.value, isInt, 'framesPerSecondError', this.framesPerSecondTextField))
            || !(await check(this.durationTextField.value, isNum, 'durationError', this.durationTextField))
            || !(await check(this.frameWidthTextField.value, isInt, 'frameWidthError', this.frameWidthTextField))
            || !(await check(this.frameHeightTextField.value, isInt, 'frameHeightError', this.frameHeightTextField))) {
            return;
        }
        await this.startAnimation(Number(this.frameWidthTextField.value), Number(this.frameHeightTextField.value),
            Number(this.framesPerSecondTextField.value), Number(this.durationTextField.value));
    }

    constructPropertiesPane() {
        const constructorFactory = this.repository.findComponentByInterface(ViewPrintConstructorFactory);
        this.viewPrintConstructor = constructorFactory.getViewPrintConstructor(this.view);
        this.PropertiesPane.add(res.getString('viewConstructorPaneTitle'), this.viewPrintConstructor.getPrintConstructorComponent());
        const encoderFactory = this.repository.findComponentByInterface(AnimationEncoderFactory);
        if (encoderFactory != null) {
            this.shouldWriteAnimation = checkBox(res.getString('enableEncodingLabel'), true);
            this.animationEncoder = encoderFactory.getAnimationEncoder();
            const encoderPanel = el('div', { className: 'lv-padded' }, this.shouldWriteAnimation, this.animationEncoder.getAnimationEncodingControlCoponent());
            this.PropertiesPane.add(res.getString('encodePanetitle'), encoderPanel);
        }
        const factories = this.repository.findByInterface(InterpolatorFactory);
        this.interpolators = [];
        this.interpolator_checkboxes = [];
        for (const f of factories) {
            const inter = f.getInterpolator(this.view);
            if (inter == null) {
                continue;
            }
            this.interpolators.push(inter);
            const check = checkBox(res.getString('interpolatorCheckboxLabel'));
            this.interpolator_checkboxes.push(check);
            const inner_panel = el('div', { className: 'lv-padded' }, check, inter.getConfigurationComponent());
            this.PropertiesPane.add(inter.getInterpolatorName(), inner_panel);
        }
    }

    async startAnimation(frameWidth, frameHeight, frames_per_second, duration) {
        const animation = new Animation();
        animation.setFrameWidth(frameWidth);
        animation.setFrameHeight(frameHeight);
        animation.setNumberOfFrames(Math.trunc(frames_per_second * duration));
        animation.setFramesPerSecond(frames_per_second);
        animation.setDuration(duration);
        const enabledInterpolators = this.interpolators.filter((inter, i) => this.interpolator_checkboxes[i].input.checked);
        if (enabledInterpolators.length === 0) {
            const reply = await JOptionPane.showConfirmDialog(this, res.getString('noInterpolatorsWarning'), res.getString('warningTitle'), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (reply === JOptionPane.NO_OPTION) {
                return;
            }
        }
        const animator = new Animator(animation, enabledInterpolators, this.viewPrintConstructor);
        animation.setAnimator(animator);
        const dialog = new AnimationDialog(this, animation);
        animator.addAnimatorListener(dialog);
        // (The original recorded even with "Record animation results" unchecked.)
        if (this.animationEncoder != null && this.shouldWriteAnimation.input.checked) {
            this.animationEncoder.setAnimation(animation);
            animator.addAnimatorListener(this.animationEncoder);
        }
        dialog.setVisible(true);
        try {
            await animator.start();
        } catch (e) {
            JOptionPane.showMessageDialog(dialog, String(e.message || e), res.getString('errorMessageTitle'), JOptionPane.ERROR_MESSAGE);
        }
    }
}
