import { ResourceBundle } from '../../../../../compat/util/ResourceBundle.js';
import { JDialog, el, button, progressBar } from '../../../../../compat/swing/Swing.js';
import { AnimatorListener } from './AnimatorListener.js';

const res = await ResourceBundle.getBundle(new URL('./AnimationDialog.properties', import.meta.url));

/** Shows the frames as they are rendered: "Rendering frame i of n". */
export class AnimationDialog extends JDialog {
    static implements = [AnimatorListener];

    constructor(parent, animation) {
        super(parent, true);
        this.animation = animation;
        this.animating = false;
        this.initComponents();
        this.frameDisplay = el('canvas', { className: 'lv-framedisplay', width: animation.getFrameWidth(), height: animation.getFrameHeight() });
        this.framePane.append(this.frameDisplay);
        this.totalFramesLabel.textContent = String(animation.getNumberOfFrames());
        this.currentFrameLabel.textContent = '1';
        this.animationProgressBar.setMinimum(0);
        this.animationProgressBar.setMaximum(Math.max(1, animation.getNumberOfFrames()));
        this.setTitle('Animation');
        this.setSize(680, 560);
    }

    initComponents() {
        this.currentFrameLabel = el('span', {}, '1');
        this.totalFramesLabel = el('span', {}, '?');
        this.animationProgressBar = progressBar();
        this.cancelButton = button(res.getString('stopButtonLabel'), () => this.cancelButtonActionPerformed());
        this.framePane = el('div', { className: 'lv-framepane' });
        this.getContentPane().classList.add('lv-column');
        this.getContentPane().append(
            el('div', { className: 'lv-padded' },
                el('div', {}, res.getString('labelPart1') + ' ', this.currentFrameLabel, res.getString('labelPart2'), this.totalFramesLabel),
                this.animationProgressBar,
                el('div', { className: 'lv-button-row' }, this.cancelButton)),
            this.framePane);
        this.addWindowListener({ windowClosing: () => this.cancelButtonActionPerformed() });
    }

    cancelButtonActionPerformed() {
        if (this.animating) {
            this.animation.getAnimator().setKeepRunning(false);
            this.cancelButton.textContent = res.getString('cancelingAnimationLabel');
        } else {
            this.dispose();
        }
    }

    animationFinishing() {
        this.cancelButton.textContent = res.getString('closeWindowButtonLabel');
        this.animating = false;
    }

    animationStarting() {
        this.animating = true;
    }

    frameProduced(frame, timeStep) {
        this.currentFrameLabel.textContent = String(timeStep);
        this.animationProgressBar.setValue(timeStep);
        this.frameDisplay.getContext('2d').drawImage(frame, 0, 0);
    }
}
