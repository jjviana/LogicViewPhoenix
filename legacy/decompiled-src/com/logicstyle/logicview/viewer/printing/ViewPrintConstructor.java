/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.printing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

public interface ViewPrintConstructor {
    public Component getPrintConstructorComponent();

    public void printView(Graphics var1, Dimension var2);
}

