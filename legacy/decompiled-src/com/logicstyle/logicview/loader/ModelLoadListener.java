/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.loader;

import com.logicstyle.logicview.loader.ModelLoader;
import java.util.EventListener;

public interface ModelLoadListener
extends EventListener {
    public void loadStarting(ModelLoader var1);

    public void loadFinished(ModelLoader var1);

    public void loadingPercentComplete(ModelLoader var1, float var2);
}

