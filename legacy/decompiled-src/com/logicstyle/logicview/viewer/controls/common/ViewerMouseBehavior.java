/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.j3d.utils.behaviors.mouse.MouseBehavior
 *  javax.media.j3d.TransformGroup
 */
package com.logicstyle.logicview.viewer.controls.common;

import com.sun.j3d.utils.behaviors.mouse.MouseBehavior;
import java.awt.Component;
import java.lang.ref.SoftReference;
import javax.media.j3d.TransformGroup;

public abstract class ViewerMouseBehavior
extends MouseBehavior {
    private SoftReference tgref;
    private boolean active;

    public ViewerMouseBehavior(Component c, int format) {
        super(c, format);
    }

    public ViewerMouseBehavior(Component c, TransformGroup g) {
        super(c, g);
    }

    public ViewerMouseBehavior(int format) {
        super(format);
    }

    public ViewerMouseBehavior(TransformGroup g) {
        super(0);
        this.tgref = new SoftReference<TransformGroup>(g);
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setTransformGroup(TransformGroup transformGroup) {
        this.tgref = new SoftReference<TransformGroup>(transformGroup);
    }

    public TransformGroup getTransformGroup() {
        return (TransformGroup)this.tgref.get();
    }
}

