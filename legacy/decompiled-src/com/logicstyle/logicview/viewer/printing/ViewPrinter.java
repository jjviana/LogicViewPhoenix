/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.printing;

import com.logicstyle.logicview.model.View;
import com.logicstyle.mosaic.interfaces.Component;

public interface ViewPrinter
extends Component {
    public boolean canPrint(View var1);

    public void print(View var1);
}

