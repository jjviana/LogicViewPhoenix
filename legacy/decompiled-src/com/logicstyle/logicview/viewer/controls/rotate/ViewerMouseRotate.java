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
 *  javax.vecmath.Matrix3d
 *  javax.vecmath.Matrix4d
 *  javax.vecmath.Vector3d
 *  javax.vecmath.Vector3f
 */
package com.logicstyle.logicview.viewer.controls.rotate;

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
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

public class ViewerMouseRotate
extends ViewerMouseBehavior {
    double x_angle;
    double y_angle;
    double x_factor = 0.03;
    double y_factor = 0.03;
    private Transform3D transformZ = new Transform3D();
    private MouseBehaviorCallback callback = null;

    public ViewerMouseRotate(TransformGroup transformGroup) {
        super(transformGroup);
    }

    public ViewerMouseRotate() {
        super(0);
    }

    public ViewerMouseRotate(int flags) {
        super(flags);
    }

    public ViewerMouseRotate(Component c) {
        super(c, 0);
    }

    public ViewerMouseRotate(Component c, TransformGroup transformGroup) {
        super(c, transformGroup);
    }

    public ViewerMouseRotate(Component c, int flags) {
        super(c, flags);
    }

    public void initialize() {
        super.initialize();
        this.x_angle = 0.0;
        this.y_angle = 0.0;
        if ((this.flags & 2) == 2) {
            this.invert = true;
            this.x_factor *= -1.0;
            this.y_factor *= -1.0;
        }
    }

    public double getXFactor() {
        return this.x_factor;
    }

    public double getYFactor() {
        return this.y_factor;
    }

    public void setFactor(double factor) {
        this.x_factor = this.y_factor = factor;
    }

    public void setFactor(double xFactor, double yFactor) {
        this.x_factor = xFactor;
        this.y_factor = yFactor;
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
            if (id == 506 && !evt.isMetaDown() && !evt.isAltDown() && this.isActive()) {
                this.x = evt.getX();
                this.y = evt.getY();
                int dx = this.x - this.x_last;
                int dy = this.y - this.y_last;
                if (!this.reset) {
                    this.x_angle = (double)dy * this.y_factor;
                    this.y_angle = (double)dx * this.x_factor;
                    this.transformX.rotX(this.x_angle);
                    this.transformY.rotY(this.y_angle);
                    this.getTransformGroup().getTransform(this.currXform);
                    Matrix4d mat = new Matrix4d();
                    this.currXform.get(mat);
                    this.currXform.setTranslation(new Vector3d(0.0, 0.0, 0.0));
                    if (this.invert) {
                        this.currXform.mul(this.currXform, this.transformX);
                        this.currXform.mul(this.currXform, this.transformY);
                    } else {
                        this.currXform.mul(this.transformX, this.currXform);
                        this.currXform.mul(this.transformY, this.currXform);
                    }
                    Vector3d translation = new Vector3d(mat.m03, mat.m13, mat.m23);
                    this.currXform.setTranslation(translation);
                    this.getTransformGroup().setTransform(this.currXform);
                    this.transformChanged(this.currXform);
                    if (this.callback != null) {
                        this.callback.transformChanged(0, this.currXform);
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

    public Vector3f getRotationAngles() {
        Transform3D t3D = this.currXform;
        Matrix3d m1 = new Matrix3d();
        Vector3f Angles = new Vector3f();
        t3D.get(m1);
        Angles.y = (float)Math.asin(m1.getElement(0, 2));
        double c = Math.cos(Angles.y);
        if (Math.abs(c) > 1.0E-5) {
            double tRx = m1.getElement(2, 2) / c;
            double tRy = -m1.getElement(1, 2) / c;
            Angles.x = (float)Math.atan2(tRy, tRx);
            tRx = m1.getElement(0, 0) / c;
            tRy = -m1.getElement(0, 1) / c;
            Angles.z = (float)Math.atan2(tRy, tRx);
        } else {
            Angles.x = 0.0f;
            double tRx = m1.getElement(1, 1) / c;
            double tRy = m1.getElement(1, 0) / c;
            Angles.z = (float)Math.atan2(tRy, tRx);
        }
        if ((double)Angles.x < 0.0) {
            Angles.x = (float)((double)Angles.x + Math.PI * 2);
        } else if ((double)Angles.x > Math.PI * 2) {
            Angles.x = (float)((double)Angles.x - Math.PI * 2);
        }
        if ((double)Angles.y < 0.0) {
            Angles.y = (float)((double)Angles.y + Math.PI * 2);
        } else if ((double)Angles.y > Math.PI * 2) {
            Angles.y = (float)((double)Angles.y - Math.PI * 2);
        }
        if ((double)Angles.z < 0.0) {
            Angles.z = (float)((double)Angles.z + Math.PI * 2);
        } else if ((double)Angles.z > Math.PI * 2) {
            Angles.z = (float)((double)Angles.z - Math.PI * 2);
        }
        if ((double)Angles.x < 0.001 && (double)Angles.x > -0.001) {
            Angles.x = 0.0f;
        }
        if ((double)Angles.y < 0.001 && (double)Angles.y > -0.001) {
            Angles.y = 0.0f;
        }
        if ((double)Angles.z < 0.001 && (double)Angles.z > -0.001) {
            Angles.z = 0.0f;
        }
        if ((double)Angles.x == 0.0) {
            Angles.x = Math.abs(Angles.x);
        }
        if ((double)Angles.y == 0.0) {
            Angles.y = Math.abs(Angles.y);
        }
        if ((double)Angles.z == 0.0) {
            Angles.z = Math.abs(Angles.z);
        }
        if ((double)Angles.x == Math.PI * 2) {
            Angles.x = 0.0f;
        }
        if ((double)Angles.y == Math.PI * 2) {
            Angles.y = 0.0f;
        }
        if ((double)Angles.z == Math.PI * 2) {
            Angles.z = 0.0f;
        }
        return Angles;
    }

    public void setRotationAngles(double rx, double ry, double rz) {
        this.transformX.rotX(rx);
        this.transformY.rotY(ry);
        this.transformZ.rotZ(rz);
        this.currXform.setIdentity();
        this.currXform.mul(this.currXform, this.transformX);
        this.currXform.mul(this.currXform, this.transformY);
        this.currXform.mul(this.currXform, this.transformZ);
        this.getTransformGroup().setTransform(this.currXform);
    }
}

