/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.animation;

import com.logicstyle.logicview.viewer.animation.Animation;
import com.logicstyle.logicview.viewer.animation.AnimatorListener;
import java.awt.Component;

public interface AnimationEncoder
extends AnimatorListener {
    public Component getAnimationEncodingControlCoponent();

    public void setAnimation(Animation var1);
}

