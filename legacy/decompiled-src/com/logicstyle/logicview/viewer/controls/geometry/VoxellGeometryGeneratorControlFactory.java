/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.controls.geometry;

import com.logicstyle.logicview.model.VoxellModel;
import com.logicstyle.logicview.viewer.controls.ViewControlFactory;
import com.logicstyle.logicview.viewer.controls.geometry.VoxellGeometryGenerator;

public interface VoxellGeometryGeneratorControlFactory
extends ViewControlFactory {
    public VoxellGeometryGenerator getGeometryGenerator(VoxellModel var1);
}

