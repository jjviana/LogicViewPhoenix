/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.controls.zoom;

import com.logicstyle.logicview.model.Model;
import com.logicstyle.logicview.viewer.controls.ViewControl;
import com.logicstyle.logicview.viewer.controls.ViewControlFactory;
import com.logicstyle.logicview.viewer.controls.zoom.ZoomSceneControl;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
import com.logicstyle.mosaic.interfaces.DependencyException;
import java.util.ResourceBundle;

public class ZoomSceneControlFactory
implements ViewControlFactory {
    private ComponentRepository rep;
    private boolean active = false;
    private ResourceBundle res = ResourceBundle.getBundle(ZoomSceneControlFactory.class.getName());

    public void deactivate() {
        this.active = false;
    }

    public String getDescription() {
        return this.res.getString("name");
    }

    public String getName() {
        return this.res.getString("name");
    }

    public ViewControl getViewControl(Model model) {
        return new ZoomSceneControl();
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

