/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.model;

import com.logicstyle.logicview.model.Model;
import java.awt.Component;

public interface View {
    public Component getViewComponent();

    public Model getModel();
}

