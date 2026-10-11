/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.controls.planecut;

import com.logicstyle.logicview.MainWindow.MainWindow;
import com.logicstyle.logicview.model.Model;
import com.logicstyle.logicview.model.View;
import com.logicstyle.logicview.model.VoxellModel;
import com.logicstyle.logicview.viewer.animation.Interpolator;
import com.logicstyle.logicview.viewer.animation.InterpolatorFactory;
import com.logicstyle.logicview.viewer.controls.ViewControl;
import com.logicstyle.logicview.viewer.controls.ViewControlFactory;
import com.logicstyle.logicview.viewer.controls.planecut.PlaneCutVoxellTransformControl;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
import com.logicstyle.mosaic.interfaces.DependencyException;
import java.util.HashMap;
import java.util.ResourceBundle;

public class PlaneCutVoxellTransformControlFactory
implements ViewControlFactory,
InterpolatorFactory {
    private static ResourceBundle res = ResourceBundle.getBundle(PlaneCutVoxellTransformControlFactory.class.getName());
    private ComponentRepository rep;
    private boolean active = false;
    private HashMap controlsByModel = new HashMap();

    public void deactivate() {
        this.active = false;
    }

    public String getDescription() {
        return res.getString("description");
    }

    public String getName() {
        return res.getString("name");
    }

    public ViewControl getViewControl(Model model) {
        MainWindow main = (MainWindow)this.rep.findComponentByInterface(MainWindow.class);
        if (model instanceof VoxellModel) {
            PlaneCutVoxellTransformControl control = new PlaneCutVoxellTransformControl(main, (VoxellModel)model);
            this.controlsByModel.put(model, control);
            return control;
        }
        return null;
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

