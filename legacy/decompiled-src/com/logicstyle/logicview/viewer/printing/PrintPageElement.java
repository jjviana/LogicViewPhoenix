/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.printing;

import java.awt.Dimension;
import java.awt.Graphics;

public interface PrintPageElement {
    public static final int PORTRAIT = 1;
    public static final int LANDSCAPE = 2;
    public static final int CENTER = 3;

    public String getName();

    public Dimension getMinimumSize();

    public Dimension getSize();

    public void setSize(Dimension var1);

    public int getSupportedOrientations();

    public int getOrientation();

    public void setOrientation(int var1);

    public void print(Graphics var1);
}

