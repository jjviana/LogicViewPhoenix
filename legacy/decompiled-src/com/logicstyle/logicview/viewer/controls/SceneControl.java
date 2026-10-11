/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.media.j3d.BranchGroup
 *  javax.media.j3d.TransformGroup
 */
package com.logicstyle.logicview.viewer.controls;

import com.logicstyle.logicview.viewer.controls.ViewControl;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.TransformGroup;

public interface SceneControl
extends ViewControl {
    public void processScene(BranchGroup var1, TransformGroup var2, TransformGroup var3);
}

