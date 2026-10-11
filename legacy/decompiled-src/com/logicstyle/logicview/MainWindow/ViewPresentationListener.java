/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.MainWindow;

import com.logicstyle.logicview.model.Model;
import com.logicstyle.logicview.viewer.Viewer;
import java.util.EventListener;

public interface ViewPresentationListener
extends EventListener {
    public void viewPresentationStarted(Model var1, Viewer[] var2);

    public void viewPresentationFinished(Model var1, Viewer[] var2);
}

