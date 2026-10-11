/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.animation;

import com.logicstyle.logicview.model.View;
import com.logicstyle.logicview.viewer.animation.Interpolator;
import com.logicstyle.mosaic.interfaces.Component;

public interface InterpolatorFactory
extends Component {
    public Interpolator getInterpolator(View var1);
}

