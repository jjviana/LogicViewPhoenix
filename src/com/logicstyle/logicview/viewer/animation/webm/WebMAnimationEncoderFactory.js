import { ResourceBundle } from '../../../../../../compat/util/ResourceBundle.js';
import { DependencyException } from '../../../../mosaic/interfaces/DependencyException.js';
import { AnimationEncoderFactory } from '../AnimationEncoderFactory.js';
import { WebMAnimationEncoder } from './WebMAnimationEncoder.js';

const res = await ResourceBundle.getBundle(new URL('./WebMAnimationEncoderFactory.properties', import.meta.url));

export class WebMAnimationEncoderFactory {
    static implements = [AnimationEncoderFactory];

    constructor() {
        this.active = false;
    }

    deactivate() {
        this.active = false;
    }

    getAnimationEncoder() {
        return new WebMAnimationEncoder();
    }

    getDescription() {
        return res.getString('description');
    }

    getName() {
        return res.getString('name');
    }

    init() {
        // The JMF factory checked that the Java Media Framework was installed.
        if (typeof MediaRecorder === 'undefined' || !HTMLCanvasElement.prototype.captureStream || !WebMAnimationEncoder.pickMimeType()) {
            throw new DependencyException('MediaRecorder is not available - animations cannot be recorded', AnimationEncoderFactory);
        }
        this.active = true;
        return true;
    }

    isActive() {
        return this.active;
    }
}
