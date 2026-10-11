/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.controls.color;

import com.logicstyle.logicview.model.VoxellModel;
import com.logicstyle.logicview.viewer.controls.ViewControl;
import java.awt.Color;

public interface VoxellColorGeneratorControl
extends ViewControl {
    public Color getColorForPoint(int var1, int var2, int var3, VoxellModel var4);
}

