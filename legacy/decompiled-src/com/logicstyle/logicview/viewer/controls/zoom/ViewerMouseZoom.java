/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.j3d.utils.behaviors.mouse.MouseBehaviorCallback
 *  javax.media.j3d.Transform3D
 *  javax.media.j3d.TransformGroup
 *  javax.media.j3d.WakeupCondition
 *  javax.media.j3d.WakeupCriterion
 *  javax.media.j3d.WakeupOnAWTEvent
 *  javax.media.j3d.WakeupOnBehaviorPost
 *  javax.vecmath.Vector3d
 */
package com.logicstyle.logicview.viewer.controls.zoom;

import com.logicstyle.logicview.viewer.controls.common.ViewerMouseBehavior;
import com.sun.j3d.utils.behaviors.mouse.MouseBehaviorCallback;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.LinkedList;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.WakeupCondition;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnAWTEvent;
import javax.media.j3d.WakeupOnBehaviorPost;
import javax.vecmath.Vector3d;

public class ViewerMouseZoom
extends ViewerMouseBehavior {
    double z_factor = 0.004;
    Vector3d translation = new Vector3d();
    private MouseBehaviorCallback callback = null;

    public ViewerMouseZoom(TransformGroup transformGroup) {
        super(transformGroup);
    }

    public ViewerMouseZoom() {
        super(0);
    }

    public ViewerMouseZoom(int flags) {
        super(flags);
    }

    public ViewerMouseZoom(Component c) {
        super(c, 0);
    }

    public ViewerMouseZoom(Component c, TransformGroup transformGroup) {
        super(c, transformGroup);
    }

    public ViewerMouseZoom(Component c, int flags) {
        super(c, flags);
    }

    public void initialize() {
        super.initialize();
        if ((this.flags & 2) == 2) {
            this.z_factor *= -1.0;
            this.invert = true;
        }
    }

    public double getFactor() {
        return this.z_factor;
    }

    public void setFactor(double factor) {
        this.z_factor = factor;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void processStimulus(Enumeration criteria) {
        block3: while (criteria.hasMoreElements()) {
            MouseEvent evt;
            WakeupCriterion wakeup = (WakeupCriterion)criteria.nextElement();
            if (wakeup instanceof WakeupOnAWTEvent) {
                AWTEvent[] events = ((WakeupOnAWTEvent)wakeup).getAWTEvent();
                if (events.length <= 0) continue;
                evt = (MouseEvent)events[events.length - 1];
                this.doProcess(evt);
                continue;
            }
            if (!(wakeup instanceof WakeupOnBehaviorPost)) continue;
            while (true) {
                LinkedList linkedList = this.mouseq;
                synchronized (linkedList) {
                    if (this.mouseq.isEmpty()) {
                        continue block3;
                    }
                    evt = (MouseEvent)this.mouseq.remove(0);
                    while (evt.getID() == 506 && !this.mouseq.isEmpty() && ((MouseEvent)this.mouseq.get(0)).getID() == 506) {
                        evt = (MouseEvent)this.mouseq.remove(0);
                    }
                }
                this.doProcess(evt);
            }
        }
        this.wakeupOn((WakeupCondition)this.mouseCriterion);
    }

    void doProcess(MouseEvent evt) {
        this.processMouseEvent(evt);
        if (this.buttonPress && (this.flags & 1) == 0 || this.wakeUp && (this.flags & 1) != 0) {
            int id = evt.getID();
            if (id == 506 && !evt.isAltDown() && !evt.isMetaDown() && this.isActive()) {
                this.x = evt.getX();
                this.y = evt.getY();
                int dx = this.x - this.x_last;
                int dy = this.y - this.y_last;
                if (!this.reset) {
                    this.getTransformGroup().getTransform(this.currXform);
                    this.translation.z = (double)dy * this.z_factor;
                    this.transformX.set(this.translation);
                    if (this.invert) {
                        this.currXform.mul(this.currXform, this.transformX);
                    } else {
                        this.currXform.mul(this.transformX, this.currXform);
                    }
                    this.getTransformGroup().setTransform(this.currXform);
                    this.transformChanged(this.currXform);
                    if (this.callback != null) {
                        this.callback.transformChanged(2, this.currXform);
                    }
                } else {
                    this.reset = false;
                }
                this.x_last = this.x;
                this.y_last = this.y;
            } else if (id == 501) {
                this.x_last = evt.getX();
                this.y_last = evt.getY();
            }
        }
    }

    public void transformChanged(Transform3D transform) {
    }

    public void setupCallback(MouseBehaviorCallback callback) {
        this.callback = callback;
    }

    public double getZoom() {
        this.getTransformGroup().getTransform(this.currXform);
        this.currXform.get(this.translation);
        return this.translation.z;
    }

    public void setZoom(double zoom) {
        this.translation.z = zoom;
        this.getTransformGroup().getTransform(this.currXform);
        this.currXform.set(this.translation);
        this.getTransformGroup().setTransform(this.currXform);
    }
}

