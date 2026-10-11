/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.controls.color;

import com.logicstyle.logicview.model.MaterialModel;
import com.logicstyle.logicview.model.TransformedVoxellModel;
import com.logicstyle.logicview.model.VoxellModel;
import com.logicstyle.logicview.viewer.controls.ViewControlListener;
import com.logicstyle.logicview.viewer.controls.color.VoxellColorGeneratorControl;
import java.awt.Color;
import java.awt.Component;
import java.util.ResourceBundle;

public class MaterialModelVoxellColorGeneratorControl
implements VoxellColorGeneratorControl {
    private static ResourceBundle res = ResourceBundle.getBundle(MaterialModelVoxellColorGeneratorControl.class.getName());

    public void addViewControlListener(ViewControlListener listener) {
    }

    public void controlComponentHidden(Component c) {
    }

    public void controlComponentShown(Component c) {
    }

    public Color getColorForPoint(int x, int y, int z, VoxellModel model) {
        VoxellModel orig = model;
        while (model instanceof TransformedVoxellModel) {
            model = ((TransformedVoxellModel)model).getSource();
        }
        MaterialModel m = (MaterialModel)model;
        MaterialModel.Material[] mat = m.getMaterials();
        return mat[orig.getIntValueAt((int)x, (int)y, (int)z) - 1].color;
    }

    public Component getControlComponent() {
        return null;
    }

    public String getName() {
        return res.getString("name");
    }

    public boolean isActive() {
        return true;
    }

    public void removeViewControlListener(ViewControlListener listener) {
    }
}

