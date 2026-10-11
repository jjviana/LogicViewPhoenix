/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer;

import com.logicstyle.logicview.viewer.Viewer;
import java.util.EventListener;

public interface ViewConstructionListener
extends EventListener {
    public void viewConstructionStarting(Viewer var1);

    public void viewConstructionFinished(Viewer var1);

    public void viewConstructionProgress(String var1, float var2, boolean var3);
}

