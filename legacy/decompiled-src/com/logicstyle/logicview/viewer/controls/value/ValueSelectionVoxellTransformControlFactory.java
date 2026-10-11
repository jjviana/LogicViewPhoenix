/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.controls.value;

import com.logicstyle.logicview.model.FieldModel;
import com.logicstyle.logicview.model.Model;
import com.logicstyle.logicview.model.TransformedVoxellModel;
import com.logicstyle.logicview.model.VoxellModel;
import com.logicstyle.logicview.viewer.controls.ViewControl;
import com.logicstyle.logicview.viewer.controls.ViewControlFactory;
import com.logicstyle.logicview.viewer.controls.value.ValueSelectionVoxellTransformControl;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
import com.logicstyle.mosaic.interfaces.DependencyException;
import java.util.ResourceBundle;

public class ValueSelectionVoxellTransformControlFactory
implements ViewControlFactory {
    private ResourceBundle res = ResourceBundle.getBundle(ValueSelectionVoxellTransformControlFactory.class.getName());
    private boolean active = true;
    private ValueSelectionVoxellTransformControl controlInstance = new ValueSelectionVoxellTransformControl();

    public void deactivate() {
        this.active = false;
    }

    public String getDescription() {
        return this.res.getString("descriptiopn");
    }

    public String getName() {
        return this.res.getString("name");
    }

    public ViewControl getViewControl(Model model) {
        if (!(model instanceof VoxellModel)) {
            return null;
        }
        VoxellModel m = (VoxellModel)model;
        while (m instanceof TransformedVoxellModel) {
            m = ((TransformedVoxellModel)m).getSource();
        }
        if (m instanceof FieldModel) {
            return this.controlInstance;
        }
        return null;
    }

    public boolean init(ComponentRepository c) throws DependencyException {
        return true;
    }

    public boolean isActive() {
        return this.active;
    }
}

