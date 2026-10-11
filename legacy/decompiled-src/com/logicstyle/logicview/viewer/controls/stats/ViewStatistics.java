/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.controls.stats;

import com.logicstyle.logicview.model.FieldModel;
import com.logicstyle.logicview.model.MaterialModel;
import com.logicstyle.logicview.model.Model;
import com.logicstyle.logicview.model.VoxellModel;
import com.logicstyle.logicview.viewer.controls.ViewControl;
import com.logicstyle.logicview.viewer.controls.ViewControlFactory;
import com.logicstyle.logicview.viewer.controls.ViewControlListener;
import com.logicstyle.logicview.viewer.controls.VoxellTransformControl;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
import com.logicstyle.mosaic.interfaces.DependencyException;
import java.awt.Component;

public class ViewStatistics
implements ViewControlFactory {
    private ViewStatsHack vh = new ViewStatsHack();

    public void deactivate() {
    }

    public String getDescription() {
        return "Statistics viewer ugly hack";
    }

    public String getName() {
        return "An uuuuugly hack pretending to be a statistics viewer.";
    }

    public ViewControl getViewControl(Model model) {
        FieldModel fm;
        if (model instanceof FieldModel && (fm = (FieldModel)model).getMaterialModel() != null) {
            return this.vh;
        }
        return null;
    }

    public boolean init(ComponentRepository c) throws DependencyException {
        return true;
    }

    public boolean isActive() {
        return true;
    }

    public class ViewStatsHack
    implements VoxellTransformControl {
        public void addViewControlListener(ViewControlListener listener) {
        }

        public void controlComponentHidden(Component c) {
        }

        public void controlComponentShown(Component c) {
        }

        public Component getControlComponent() {
            return null;
        }

        public String getName() {
            return "View Statistics";
        }

        public boolean isActive() {
            return true;
        }

        public VoxellModel processModel(VoxellModel source) {
            int i;
            if (!(source instanceof FieldModel)) {
                System.err.println("Warn: can't compute stats for model " + source.getClass().getName());
                return source;
            }
            FieldModel fm = (FieldModel)source;
            MaterialModel mm = fm.getMaterialModel();
            if (mm == null) {
                System.err.println("Warn: material model not found for model " + source.getClass().getName());
                return source;
            }
            MaterialModel.Material[] mats = mm.getMaterials();
            double[] max = new double[mats.length];
            double[] min = new double[mats.length];
            double[] avg = new double[mats.length];
            int[] nv = new int[mats.length];
            for (i = 0; i < max.length; ++i) {
                max[i] = -1.7976931348623157E308;
            }
            for (i = 0; i < min.length; ++i) {
                min[i] = Double.MAX_VALUE;
            }
            for (i = 0; i < avg.length; ++i) {
                avg[i] = 0.0;
            }
            for (i = 0; i < nv.length; ++i) {
                nv[i] = 0;
            }
            for (i = 0; i < source.getWidth(); ++i) {
                for (int j = 0; j < source.getHeight(); ++j) {
                    for (int k = 0; k < source.getDepth(); ++k) {
                        int mat;
                        float val = fm.getFloatValueAt(i, j, k);
                        if ((double)val < min[mat = mm.getIntValueAt(i, j, k) - 1]) {
                            min[mat] = val;
                        }
                        if ((double)val > max[mat]) {
                            max[mat] = val;
                        }
                        avg[mat] = avg[mat] + (double)val;
                        int n = mat;
                        nv[n] = nv[n] + 1;
                    }
                }
            }
            System.err.println("Model Statistics");
            System.err.println("Material\tMin\tAvg\tMax");
            for (i = 0; i < mats.length; ++i) {
                if (nv[i] == 0) continue;
                avg[i] = avg[i] / (double)nv[i];
                System.err.println(mats[i].name + "\t" + min[i] + "\t" + avg[i] + "\t" + max[i]);
            }
            return source;
        }

        public void removeViewControlListener(ViewControlListener listener) {
        }
    }
}

