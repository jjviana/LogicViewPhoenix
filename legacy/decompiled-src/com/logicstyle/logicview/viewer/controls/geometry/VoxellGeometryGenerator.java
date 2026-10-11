/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.media.j3d.GeometryArray
 */
package com.logicstyle.logicview.viewer.controls.geometry;

import com.logicstyle.logicview.model.VoxellModel;
import com.logicstyle.logicview.viewer.controls.ViewControl;
import javax.media.j3d.GeometryArray;

public interface VoxellGeometryGenerator
extends ViewControl {
    public GeometryArray getGeometry(VoxellModel var1);
}

