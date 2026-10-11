/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.printing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.RenderedImage;

public interface ImagePrinter {
    public String getDescription();

    public Dimension getPreferredSampleSize();

    public Component getConfigurationComponent();

    public boolean printImage(RenderedImage var1);
}

