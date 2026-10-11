/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.controls.animation;

import java.awt.Component;

public interface Interpolator {
    public void setInterpolationStep(float var1);

    public String getInterpolatorName();

    public Component getConfigurationComponent();
}

