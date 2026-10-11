/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.controls;

import com.logicstyle.logicview.viewer.controls.ViewControlListener;
import java.awt.Component;

public interface ViewControl {
    public Component getControlComponent();

    public void addViewControlListener(ViewControlListener var1);

    public void removeViewControlListener(ViewControlListener var1);

    public boolean isActive();

    public String getName();

    public void controlComponentShown(Component var1);

    public void controlComponentHidden(Component var1);
}

