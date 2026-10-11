/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.controls.rotate;

import com.logicstyle.logicview.model.Model;
import com.logicstyle.logicview.model.View;
import com.logicstyle.logicview.viewer.animation.Interpolator;
import com.logicstyle.logicview.viewer.animation.InterpolatorFactory;
import com.logicstyle.logicview.viewer.controls.ViewControl;
import com.logicstyle.logicview.viewer.controls.ViewControlFactory;
import com.logicstyle.logicview.viewer.controls.rotate.RotateSceneControl;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
import com.logicstyle.mosaic.interfaces.DependencyException;
import java.util.HashMap;
import java.util.ResourceBundle;

public class RotateSceneControlFactory
implements ViewControlFactory,
InterpolatorFactory {
    private ResourceBundle res = ResourceBundle.getBundle(RotateSceneControlFactory.class.getName());
    private boolean active = false;
    private ComponentRepository rep;
    private HashMap controlsByModel = new HashMap();

    public void deactivate() {
        this.active = false;
    }

    public String getDescription() {
        return this.res.getString("description");
    }

    public String getName() {
        return this.res.getString("name");
    }

    public ViewControl getViewControl(Model model) {
        RotateSceneControl control = new RotateSceneControl();
        this.controlsByModel.put(model, control);
        return control;
    }

    public boolean init(ComponentRepository c) throws DependencyException {
        this.rep = c;
        this.active = true;
        return true;
    }

    public boolean isActive() {
        return this.active;
    }

    public Interpolator getInterpolator(View v) {
        return (Interpolator)this.controlsByModel.get(v.getModel());
    }
}

