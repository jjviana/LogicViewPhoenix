/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.controls;

import com.logicstyle.logicview.model.Model;
import com.logicstyle.logicview.viewer.controls.ViewControl;
import com.logicstyle.mosaic.interfaces.Component;

public interface ViewControlFactory
extends Component {
    public ViewControl getViewControl(Model var1);
}

