/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.MainWindow;

import com.logicstyle.logicview.MainWindow.MainWindow;
import com.logicstyle.logicview.MainWindow.ViewPresentationListener;
import com.logicstyle.logicview.UIAction.ActionGroup;
import com.logicstyle.logicview.model.Model;
import com.logicstyle.logicview.viewer.Viewer;
import com.logicstyle.mosaic.interfaces.Component;

public interface ViewPresenter
extends Component {
    public static final ActionGroup VIEW_ACTION_GROUP = new ActionGroup("View");

    public void addViews(Model var1, Viewer[] var2, MainWindow var3);

    public void addViewPresentationListener(ViewPresentationListener var1);

    public void removeViewPresentationListener(ViewPresentationListener var1);
}

