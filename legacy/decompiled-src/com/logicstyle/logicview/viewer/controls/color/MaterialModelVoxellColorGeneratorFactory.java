/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.controls.color;

import com.logicstyle.logicview.model.MaterialModel;
import com.logicstyle.logicview.model.Model;
import com.logicstyle.logicview.model.TransformedVoxellModel;
import com.logicstyle.logicview.viewer.controls.ViewControl;
import com.logicstyle.logicview.viewer.controls.color.MaterialModelVoxellColorGeneratorControl;
import com.logicstyle.logicview.viewer.controls.color.VoxellColorGeneratorControlFactory;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
import com.logicstyle.mosaic.interfaces.DependencyException;
import java.util.ResourceBundle;

public class MaterialModelVoxellColorGeneratorFactory
implements VoxellColorGeneratorControlFactory {
    private boolean active = false;
    private ResourceBundle res = ResourceBundle.getBundle(MaterialModelVoxellColorGeneratorFactory.class.getName());
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
        this.rep = c;
        this.active = true;
        return true;
    }

    public boolean isActive() {
        return this.active;
    }

    public ViewControl getViewControl(Model m) {
        while (m instanceof TransformedVoxellModel) {
            m = ((TransformedVoxellModel)m).getSource();
        }
        if (!(m instanceof MaterialModel)) {
            return null;
        }
        return new MaterialModelVoxellColorGeneratorControl();
    }
}

