/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.animation;

import com.logicstyle.logicview.viewer.animation.Animation;
import com.logicstyle.logicview.viewer.animation.AnimatorListener;
import com.logicstyle.logicview.viewer.animation.Interpolator;
import com.logicstyle.logicview.viewer.printing.ViewPrintConstructor;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Animator
extends Thread {
    private List interpolators;
    private ViewPrintConstructor view_constructor;
    private boolean keepRunning;
    private Animation animation;
    private transient ArrayList animatorListenerList;

    public Animator(Animation animation, List interpolators, ViewPrintConstructor constructor) {
        this.interpolators = interpolators;
        this.view_constructor = constructor;
        this.keepRunning = true;
        this.animation = animation;
    }

    public void run() {
        this.fireAnimatorListenerAnimationStarting(this);
        Dimension frameSize = new Dimension(this.animation.getFrameWidth(), this.animation.getFrameHeight());
        int numberOfFrames = this.animation.getNumberOfFrames();
        for (int i = 0; i <= numberOfFrames && this.keepRunning; ++i) {
            float currentVal = (float)i / (float)numberOfFrames;
            for (Interpolator inter : this.interpolators) {
                inter.setInterpolationStep(currentVal);
            }
            BufferedImage currentFrame = new BufferedImage(this.animation.getFrameWidth(), this.animation.getFrameHeight(), 1);
            this.view_constructor.printView(currentFrame.getGraphics(), frameSize);
            this.fireAnimatorListenerFrameProduced(currentFrame, i);
        }
        this.fireAnimatorListenerAnimationFinishing(this);
    }

    public synchronized void addAnimatorListener(AnimatorListener listener) {
        if (this.animatorListenerList == null) {
            this.animatorListenerList = new ArrayList();
        }
        this.animatorListenerList.add(listener);
    }

    public synchronized void removeAnimatorListener(AnimatorListener listener) {
        if (this.animatorListenerList != null) {
            this.animatorListenerList.remove(listener);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void fireAnimatorListenerAnimationStarting(Animator event) {
        ArrayList list;
        Animator animator = this;
        synchronized (animator) {
            if (this.animatorListenerList == null) {
                return;
            }
            list = (ArrayList)this.animatorListenerList.clone();
        }
        for (int i = 0; i < list.size(); ++i) {
            ((AnimatorListener)list.get(i)).animationStarting(event);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void fireAnimatorListenerAnimationFinishing(Animator event) {
        ArrayList list;
        Animator animator = this;
        synchronized (animator) {
            if (this.animatorListenerList == null) {
                return;
            }
            list = (ArrayList)this.animatorListenerList.clone();
        }
        for (int i = 0; i < list.size(); ++i) {
            ((AnimatorListener)list.get(i)).animationFinishing(event);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void fireAnimatorListenerFrameProduced(BufferedImage event, int frameCount) {
        ArrayList list;
        Animator animator = this;
        synchronized (animator) {
            if (this.animatorListenerList == null) {
                return;
            }
            list = (ArrayList)this.animatorListenerList.clone();
        }
        for (int i = 0; i < list.size(); ++i) {
            ((AnimatorListener)list.get(i)).frameProduced(event, frameCount);
        }
    }

    public void setKeepRunning(boolean value) {
        this.keepRunning = value;
    }
}

