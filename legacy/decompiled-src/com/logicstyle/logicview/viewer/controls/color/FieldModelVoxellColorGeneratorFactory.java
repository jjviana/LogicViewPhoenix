/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.controls.color;

import com.logicstyle.logicview.MainWindow.MainWindow;
import com.logicstyle.logicview.model.FieldModel;
import com.logicstyle.logicview.model.Model;
import com.logicstyle.logicview.model.TransformedVoxellModel;
import com.logicstyle.logicview.model.View;
import com.logicstyle.logicview.model.VoxellModel;
import com.logicstyle.logicview.viewer.controls.ViewControl;
import com.logicstyle.logicview.viewer.controls.color.FieldModelVoxellColorGeneratorControl;
import com.logicstyle.logicview.viewer.controls.color.VoxellColorGeneratorControlFactory;
import com.logicstyle.logicview.viewer.printing.PrintPageElementFactory;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
import com.logicstyle.mosaic.interfaces.DependencyException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.ResourceBundle;

public class FieldModelVoxellColorGeneratorFactory
implements VoxellColorGeneratorControlFactory,
PrintPageElementFactory {
    private boolean active = false;
    private static ResourceBundle res = ResourceBundle.getBundle(FieldModelVoxellColorGeneratorFactory.class.getName());
    private ComponentRepository rep;
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

    public boolean init(ComponentRepository c) throws DependencyException {
        this.rep = c;
        this.active = true;
        return true;
    }

    public boolean isActive() {
        return this.active;
    }

    public ViewControl getViewControl(Model m) {
        VoxellModel orig = (VoxellModel)m;
        while (m instanceof TransformedVoxellModel) {
            m = ((TransformedVoxellModel)m).getSource();
        }
        FieldModelVoxellColorGeneratorControl c = (FieldModelVoxellColorGeneratorControl)this.control_map.get(m);
        if (c != null) {
            c.setModel(orig);
            return c;
        }
        if (m instanceof FieldModel) {
            MainWindow w = (MainWindow)this.rep.findComponentByInterface(MainWindow.class);
            c = new FieldModelVoxellColorGeneratorControl(orig, (FieldModel)m, w);
            this.control_map.put(m, c);
            return c;
        }
        return null;
    }

    public Collection getPageElements(View v) {
        Model m = v.getModel();
        VoxellModel orig = (VoxellModel)m;
        while (m instanceof TransformedVoxellModel) {
            m = ((TransformedVoxellModel)m).getSource();
        }
        FieldModelVoxellColorGeneratorControl c = (FieldModelVoxellColorGeneratorControl)this.control_map.get(m);
        if (c == null) {
            return null;
        }
        return Collections.singleton(c.getPrintPageElement());
    }
}

