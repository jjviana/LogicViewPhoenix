/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.util;

import com.logicstyle.logicview.model.TransformedVoxellModel;
import com.logicstyle.logicview.model.VoxellModel;

public class WrapperVoxellModel
implements TransformedVoxellModel {
    VoxellModel orig;

    public WrapperVoxellModel() {
    }

    public WrapperVoxellModel(VoxellModel source) {
        this.orig = source;
    }

    public void setSource(VoxellModel source) {
        this.orig = source;
    }

    public VoxellModel getSource() {
        return this.orig;
    }

    public double getDeltaX() {
        return this.orig.getDeltaX();
    }

    public double getDeltaY() {
        return this.orig.getDeltaY();
    }

    public double getDeltaZ() {
        return this.orig.getDeltaZ();
    }

    public int getDepth() {
        return this.orig.getDepth();
    }

    public String getDescription() {
        return this.orig.getDescription();
    }

    public double getDoubleValueAt(int x, int y, int z) {
        return this.orig.getDoubleValueAt(x, y, z);
    }

    public float getFloatValueAt(int x, int y, int z) {
        return this.orig.getFloatValueAt(x, y, z);
    }

    public int getHeight() {
        return this.orig.getHeight();
    }

    public int getIntValueAt(int x, int y, int z) {
        return this.orig.getIntValueAt(x, y, z);
    }

    public String getName() {
        return this.orig.getName();
    }

    public Object getObjectValueAt(int x, int y, int z) {
        return this.orig.getObjectValueAt(x, y, z);
    }

    public int getWidth() {
        return this.orig.getWidth();
    }

    public boolean isEnabled(int x, int y, int z) {
        return this.orig.isEnabled(x, y, z);
    }
}

