/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer;

import com.logicstyle.logicview.model.Model;
import com.logicstyle.logicview.model.View;
import com.logicstyle.logicview.viewer.ViewConstructionListener;
import com.logicstyle.mosaic.interfaces.Component;

public interface Viewer
extends Component {
    public View getView(Model var1);

    public boolean isModelSupported(Model var1);

    public void addViewConstructionListener(ViewConstructionListener var1);

    public void removeViewConstructionListener(ViewConstructionListener var1);

    public String getViewName();
}

