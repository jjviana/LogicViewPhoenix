/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.controls.geometry;

import com.logicstyle.logicview.model.Model;
import com.logicstyle.logicview.viewer.controls.ViewControl;
import com.logicstyle.logicview.viewer.controls.ViewControlFactory;
import com.logicstyle.logicview.viewer.controls.geometry.PolyedronGeometryGenerator;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
import com.logicstyle.mosaic.interfaces.DependencyException;
import java.util.ResourceBundle;

public class PolyedronGeometryGeneratorFactory
implements ViewControlFactory {
    private ResourceBundle res = ResourceBundle.getBundle(PolyedronGeometryGeneratorFactory.class.getName());
    private boolean active = false;
    private ComponentRepository rep;

    public void deactivate() {
        this.active = false;
    }

    public String getDescription() {
        return this.res.getString("description");
    }

    public String getName() {
        return this.res.getString("name");
    }

    public boolean init(ComponentRepository c) throws DependencyException {
        this.active = true;
        this.rep = c;
        return true;
    }

    public boolean isActive() {
        return this.active;
    }

    public ViewControl getViewControl(Model model) {
        return new PolyedronGeometryGenerator(this.rep);
    }
}

