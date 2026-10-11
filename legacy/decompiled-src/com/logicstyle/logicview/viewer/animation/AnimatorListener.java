/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.animation;

import com.logicstyle.logicview.viewer.animation.Animator;
import java.awt.image.BufferedImage;
import java.util.EventListener;

public interface AnimatorListener
extends EventListener {
    public void animationStarting(Animator var1);

    public void animationFinishing(Animator var1);

    public void frameProduced(BufferedImage var1, int var2);
}

