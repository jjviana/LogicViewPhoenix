/** The parameters of an animation. */
export class Animation {
    getFrameWidth() {
        return this.frameWidth;
    }

    setFrameWidth(frameWidth) {
        this.frameWidth = frameWidth;
    }

    getFrameHeight() {
        return this.frameHeight;
    }

    setFrameHeight(frameHeight) {
        this.frameHeight = frameHeight;
    }

    getNumberOfFrames() {
        return this.numberOfFrames;
    }

    setNumberOfFrames(numberOfFrames) {
        this.numberOfFrames = numberOfFrames;
    }

    getFramesPerSecond() {
        return this.framesPerSecond;
    }

    setFramesPerSecond(framesPerSecond) {
        this.framesPerSecond = framesPerSecond;
    }

    getDuration() {
        return this.duration;
    }

    setDuration(duration) {
        this.duration = duration;
    }

    getAnimator() {
        return this.animator;
    }

    setAnimator(animator) {
        this.animator = animator;
    }
}
