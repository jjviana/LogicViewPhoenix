/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.model;

import com.logicstyle.logicview.model.Model;

public interface VoxellModel
extends Model {
    public int getWidth();

    public int getHeight();

    public int getDepth();

    public double getDeltaX();

    public double getDeltaY();

    public double getDeltaZ();

    public int getIntValueAt(int var1, int var2, int var3);

    public float getFloatValueAt(int var1, int var2, int var3);

    public double getDoubleValueAt(int var1, int var2, int var3);

    public Object getObjectValueAt(int var1, int var2, int var3);

    public boolean isEnabled(int var1, int var2, int var3);
}

