/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.printing;

import com.logicstyle.logicview.model.View;
import com.logicstyle.logicview.viewer.printing.ViewPrintConstructor;
import com.logicstyle.mosaic.interfaces.Component;

public interface ViewPrintConstructorFactory
extends Component {
    public ViewPrintConstructor getViewPrintConstructor(View var1);
}

