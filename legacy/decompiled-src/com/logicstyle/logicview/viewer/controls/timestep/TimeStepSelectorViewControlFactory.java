/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.controls.timestep;

import com.logicstyle.logicview.model.FieldModel;
import com.logicstyle.logicview.model.Model;
import com.logicstyle.logicview.model.TransformedVoxellModel;
import com.logicstyle.logicview.model.View;
import com.logicstyle.logicview.viewer.animation.Interpolator;
import com.logicstyle.logicview.viewer.animation.InterpolatorFactory;
import com.logicstyle.logicview.viewer.controls.ViewControl;
import com.logicstyle.logicview.viewer.controls.ViewControlFactory;
import com.logicstyle.logicview.viewer.controls.timestep.TimeStepSelectorVoxelTransformControl;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
import com.logicstyle.mosaic.interfaces.DependencyException;
import java.util.ResourceBundle;
import java.util.WeakHashMap;

public class TimeStepSelectorViewControlFactory
implements ViewControlFactory,
InterpolatorFactory {
    private static ResourceBundle res = ResourceBundle.getBundle(TimeStepSelectorViewControlFactory.class.getName());
    private boolean active = false;
    private WeakHashMap modelMap = new WeakHashMap();

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
        while (model instanceof TransformedVoxellModel) {
            model = ((TransformedVoxellModel)model).getSource();
        }
        if (model instanceof FieldModel) {
            TimeStepSelectorVoxelTransformControl c = new TimeStepSelectorVoxelTransformControl();
            this.modelMap.put(model, c);
            return c;
        }
        return null;
    }

    public boolean init(ComponentRepository c) throws DependencyException {
        this.active = true;
        return true;
    }

    public boolean isActive() {
        return this.active;
    }

    public Interpolator getInterpolator(View v) {
        Model m = v.getModel();
        TimeStepSelectorVoxelTransformControl c = (TimeStepSelectorVoxelTransformControl)this.modelMap.get(m);
        return c;
    }
}

