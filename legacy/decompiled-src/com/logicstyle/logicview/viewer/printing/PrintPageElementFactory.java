/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.printing;

import com.logicstyle.logicview.model.View;
import com.logicstyle.mosaic.interfaces.Component;
import java.util.Collection;

public interface PrintPageElementFactory
extends Component {
    public Collection getPageElements(View var1);
}

