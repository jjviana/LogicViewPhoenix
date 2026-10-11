/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.printing;

import com.logicstyle.logicview.model.View;
import com.logicstyle.logicview.viewer.printing.DefaultViewPrintConstructor;
import com.logicstyle.logicview.viewer.printing.ViewPrintConstructor;
import com.logicstyle.logicview.viewer.printing.ViewPrintConstructorFactory;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
import com.logicstyle.mosaic.interfaces.DependencyException;
import java.util.ResourceBundle;

public class DefaultViewPrintConstructorFactory
implements ViewPrintConstructorFactory {
    private static final ResourceBundle res = ResourceBundle.getBundle(DefaultViewPrintConstructorFactory.class.getName());
    private boolean active = false;
    private ComponentRepository rep;

    public void deactivate() {
        this.active = false;
    }

    public String getDescription() {
        return res.getString("description");
    }

    public String getName() {
        return res.getString("name");
    }

    public ViewPrintConstructor getViewPrintConstructor(View v) {
        return new DefaultViewPrintConstructor(this.rep, v);
    }

    public boolean init(ComponentRepository c) throws DependencyException {
        this.rep = c;
        this.active = true;
        return true;
    }

    public boolean isActive() {
        return this.active;
    }
}

