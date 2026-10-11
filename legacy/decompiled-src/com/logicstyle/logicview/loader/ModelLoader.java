/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.loader;

import com.logicstyle.logicview.loader.ModelLoadListener;
import com.logicstyle.logicview.model.Model;
import com.logicstyle.mosaic.interfaces.Component;
import java.net.URL;

public interface ModelLoader
extends Component {
    public Model LoadModel(URL var1);

    public String getTypeName();

    public String getFilenameExtension();

    public void addModelLoadListener(ModelLoadListener var1);

    public void removeModelLoadListener(ModelLoadListener var1);
}

