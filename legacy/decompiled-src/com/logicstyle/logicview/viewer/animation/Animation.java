/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.animation;

import com.logicstyle.logicview.viewer.animation.Animator;

public class Animation {
    private int frameWidth;
    private int frameHeight;
    private int numberOfFrames;
    private int framesPerSecond;
    private float duration;
    private Animator animator;

    public int getFrameWidth() {
        return this.frameWidth;
    }

    public void setFrameWidth(int frameWidth) {
        this.frameWidth = frameWidth;
    }

    public int getFrameHeight() {
        return this.frameHeight;
    }

    public void setFrameHeight(int frameHeight) {
        this.frameHeight = frameHeight;
    }

    public int getNumberOfFrames() {
        return this.numberOfFrames;
    }

    public void setNumberOfFrames(int numberOfFrames) {
        this.numberOfFrames = numberOfFrames;
    }

    public int getFramesPerSecond() {
        return this.framesPerSecond;
    }

    public void setFramesPerSecond(int framesPerSecond) {
        this.framesPerSecond = framesPerSecond;
    }

    public float getDuration() {
        return this.duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public Animator getAnimator() {
        return this.animator;
    }

    public void setAnimator(Animator animator) {
        this.animator = animator;
    }
}

