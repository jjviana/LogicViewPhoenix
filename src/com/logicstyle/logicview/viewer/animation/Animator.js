import { Dimension } from '../../../../../compat/awt/Dimension.js';
import { Graphics } from '../../../../../compat/awt/Graphics.js';

/**
 * Produces the frames of an animation: for frame i of n, every enabled
 * interpolator is moved to step i/n and the view is printed into a new image.
 * (A Thread in the original; start() runs it asynchronously.)
 */
export class Animator {
    constructor(animation, interpolators, constructor) {
        this.interpolators = interpolators;
        this.view_constructor = constructor;
        this.keepRunning = true;
        this.animation = animation;
        this.animatorListenerList = [];
    }

    start() {
        this.finished = this.run();
        return this.finished;
    }

    async run() {
        await this.fireAnimatorListenerAnimationStarting(this);
        const frameSize = new Dimension(this.animation.getFrameWidth(), this.animation.getFrameHeight());
        const numberOfFrames = this.animation.getNumberOfFrames();
        try {
            for (let i = 0; i <= numberOfFrames && this.keepRunning; ++i) {
                const currentVal = numberOfFrames > 0 ? i / numberOfFrames : 0;
                for (const inter of this.interpolators) {
                    await inter.setInterpolationStep(currentVal);
                }
                const currentFrame = document.createElement('canvas');
                currentFrame.width = frameSize.width;
                currentFrame.height = frameSize.height;
                const ctx = currentFrame.getContext('2d');
                ctx.fillStyle = '#000';
                ctx.fillRect(0, 0, frameSize.width, frameSize.height);
                await this.view_constructor.printView(new Graphics(ctx), frameSize);
                await this.fireAnimatorListenerFrameProduced(currentFrame, i);
            }
        } finally {
            await this.fireAnimatorListenerAnimationFinishing(this);
        }
    }

    addAnimatorListener(listener) {
        this.animatorListenerList.push(listener);
    }

    removeAnimatorListener(listener) {
        this.animatorListenerList = this.animatorListenerList.filter((l) => l !== listener);
    }

    async fireAnimatorListenerAnimationStarting(event) {
        for (const l of [...this.animatorListenerList]) {
            await l.animationStarting(event);
        }
    }

    async fireAnimatorListenerAnimationFinishing(event) {
        for (const l of [...this.animatorListenerList]) {
            await l.animationFinishing(event);
        }
    }

    async fireAnimatorListenerFrameProduced(event, frameCount) {
        for (const l of [...this.animatorListenerList]) {
            await l.frameProduced(event, frameCount);
        }
    }

    setKeepRunning(value) {
        this.keepRunning = value;
    }
}
