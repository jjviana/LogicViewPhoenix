/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.animation;

import com.logicstyle.logicview.viewer.animation.AnimationEncoder;
import com.logicstyle.mosaic.interfaces.Component;

public interface AnimationEncoderFactory
extends Component {
    public AnimationEncoder getAnimationEncoder();
}

