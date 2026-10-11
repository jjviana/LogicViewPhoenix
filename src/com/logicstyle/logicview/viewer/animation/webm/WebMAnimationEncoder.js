import { ResourceBundle } from '../../../../../../compat/util/ResourceBundle.js';
import { System } from '../../../../../../compat/util/Logger.js';
import { Thread } from '../../../../../../compat/lang/Thread.js';
import { el, textField } from '../../../../../../compat/swing/Swing.js';
import { AnimationEncoder } from '../AnimationEncoder.js';

const res = await ResourceBundle.getBundle(new URL('./WebMAnimationEncoderControlPanel.properties', import.meta.url));

/**
 * Records the animation frames into a video with the browser's MediaRecorder
 * (replacing the Java Media Framework QuickTime encoder). The recorder is
 * paused while a frame is being rendered and resumed for exactly 1/fps seconds
 * per frame, so the video plays at the requested frame rate whatever the
 * rendering speed. The file is downloaded when the animation finishes.
 */
export class WebMAnimationEncoder {
    static implements = [AnimationEncoder];

    constructor() {
        this.controlPanel = new WebMAnimationEncoderControlPanel();
        this.animation = null;
    }

    setAnimation(anim) {
        this.animation = anim;
    }

    getAnimationEncodingControlCoponent() {
        return this.controlPanel.element;
    }

    static pickMimeType() {
        const candidates = ['video/webm;codecs=vp9', 'video/webm;codecs=vp8', 'video/webm', 'video/mp4'];
        return candidates.find((t) => typeof MediaRecorder !== 'undefined' && MediaRecorder.isTypeSupported(t)) || '';
    }

    async animationStarting() {
        this.canvas = document.createElement('canvas');
        this.canvas.width = this.animation.getFrameWidth();
        this.canvas.height = this.animation.getFrameHeight();
        this.ctx = this.canvas.getContext('2d');
        this.ctx.fillStyle = '#fff';
        this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
        this.stream = this.canvas.captureStream(0);
        this.track = this.stream.getVideoTracks()[0];
        this.mimeType = WebMAnimationEncoder.pickMimeType();
        this.chunks = [];
        this.recorder = new MediaRecorder(this.stream, { mimeType: this.mimeType, videoBitsPerSecond: 8_000_000 });
        this.recorder.ondataavailable = (e) => e.data.size > 0 && this.chunks.push(e.data);
        this.recorder.start();
        System.err.println('start processing... (' + (this.recorder.mimeType || this.mimeType) + ')');
        this.recorder.pause();
        this.frames = 0;
    }

    async frameProduced(frame) {
        this.ctx.drawImage(frame, 0, 0);
        this.recorder.resume();
        if (this.track.requestFrame) {
            this.track.requestFrame();
        }
        await Thread.sleep(1000 / this.animation.getFramesPerSecond());
        this.recorder.pause();
        this.frames++;
    }

    async animationFinishing() {
        if (!this.recorder) {
            return;
        }
        const stopped = new Promise((resolve) => {
            this.recorder.onstop = resolve;
        });
        this.recorder.resume();
        this.recorder.stop();
        await stopped;
        this.track.stop();
        if (this.frames === 0) {
            return;
        }
        const type = (this.recorder.mimeType || this.mimeType || 'video/webm').split(';')[0];
        const blob = new Blob(this.chunks, { type });
        let name = this.controlPanel.getOutputFilename() || 'anim1.webm';
        if (type === 'video/mp4') {
            name = name.replace(/\.webm$/i, '') + (name.endsWith('.mp4') ? '' : '.mp4');
        }
        const a = el('a', { href: URL.createObjectURL(blob), download: name });
        document.body.append(a);
        a.click();
        a.remove();
        setTimeout(() => URL.revokeObjectURL(a.href), 60000);
        System.err.println('Animation written: ' + name + ' (' + blob.size + ' bytes)');
    }
}

class WebMAnimationEncoderControlPanel {
    constructor() {
        this.fileTextField = textField(20, 'anim1.webm');
        this.element = el('div', { className: 'lv-flow' },
            el('span', {}, res.getString('fileLabel')), this.fileTextField,
            el('span', { className: 'lv-hint' }, '(' + res.getString('fileChooserDescription') + ', downloaded when the animation ends)'));
    }

    getOutputFilename() {
        return this.fileTextField.value.trim();
    }
}
