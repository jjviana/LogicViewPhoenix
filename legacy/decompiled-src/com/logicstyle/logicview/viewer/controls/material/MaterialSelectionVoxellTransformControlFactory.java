/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.controls.material;

import com.logicstyle.logicview.model.MaterialModel;
import com.logicstyle.logicview.model.Materialized;
import com.logicstyle.logicview.model.Model;
import com.logicstyle.logicview.model.TransformedVoxellModel;
import com.logicstyle.logicview.model.View;
import com.logicstyle.logicview.model.VoxellModel;
import com.logicstyle.logicview.viewer.controls.ViewControl;
import com.logicstyle.logicview.viewer.controls.ViewControlFactory;
import com.logicstyle.logicview.viewer.controls.material.MaterialSelectionVoxellTransformControl;
import com.logicstyle.logicview.viewer.printing.PrintPageElementFactory;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
import com.logicstyle.mosaic.interfaces.DependencyException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.ResourceBundle;

public class MaterialSelectionVoxellTransformControlFactory
implements ViewControlFactory,
PrintPageElementFactory {
    private static ResourceBundle res = ResourceBundle.getBundle(MaterialSelectionVoxellTransformControlFactory.class.getName());
    private ComponentRepository rep;
    private boolean active = false;
    private HashMap control_map = new HashMap();

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
        MaterialSelectionVoxellTransformControl c;
        if (!(model instanceof VoxellModel)) {
            return null;
        }
        VoxellModel m = (VoxellModel)model;
        while (m instanceof TransformedVoxellModel) {
            m = ((TransformedVoxellModel)m).getSource();
        }
        if (!(m instanceof MaterialModel)) {
            if (m instanceof Materialized) {
                if ((m = ((Materialized)((Object)m)).getMaterialModel()) == null) {
                    return null;
                }
            } else {
                return null;
            }
        }
        if ((c = (MaterialSelectionVoxellTransformControl)this.control_map.get(m)) == null) {
            c = new MaterialSelectionVoxellTransformControl((MaterialModel)m, this.rep);
            this.control_map.put(m, c);
        }
        return c;
    }

    public boolean init(ComponentRepository c) throws DependencyException {
        this.rep = c;
        this.active = true;
        return true;
    }

    public boolean isActive() {
        return this.active;
    }

    public Collection getPageElements(View v) {
        Model m = v.getModel();
        VoxellModel orig = (VoxellModel)m;
        while (m instanceof TransformedVoxellModel) {
            m = ((TransformedVoxellModel)m).getSource();
        }
        MaterialSelectionVoxellTransformControl c = (MaterialSelectionVoxellTransformControl)this.control_map.get(m);
        if (c == null) {
            return null;
        }
        return Collections.singleton(c.getPrintPageElement());
    }
}

