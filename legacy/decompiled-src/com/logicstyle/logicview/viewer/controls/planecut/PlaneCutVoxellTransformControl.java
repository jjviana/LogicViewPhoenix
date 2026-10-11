/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.controls.planecut;

import com.logicstyle.logicview.MainWindow.MainWindow;
import com.logicstyle.logicview.model.VoxellModel;
import com.logicstyle.logicview.viewer.animation.Interpolator;
import com.logicstyle.logicview.viewer.controls.ViewControl;
import com.logicstyle.logicview.viewer.controls.ViewControlListener;
import com.logicstyle.logicview.viewer.controls.VoxellTransformControl;
import com.logicstyle.logicview.viewer.controls.planecut.PlaneCutVoxellTransformControlPanel;
import com.logicstyle.logicview.viewer.util.WrapperVoxellModel;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.EventListenerList;

public class PlaneCutVoxellTransformControl
implements VoxellTransformControl,
Interpolator {
    private static ResourceBundle res = ResourceBundle.getBundle(PlaneCutVoxellTransformControl.class.getName());
    private EventListenerList listenerList = null;
    private PlaneCutVoxellTransformControlPanel control_panel;
    private JButton control_component;
    private InterpolatorControlPanel interpolatorControlPanel;
    private PlaneCutVoxellModel cmodel;

    public PlaneCutVoxellTransformControl(MainWindow main, VoxellModel mod) {
        this.cmodel = new PlaneCutVoxellModel(mod);
        this.control_panel = new PlaneCutVoxellTransformControlPanel((Frame)main.getParentFrame(), this);
        this.control_component = new JButton(res.getString("controlButtonLabel"));
        this.control_component.setFont(new Font("Dialog", 0, 10));
        this.control_component.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent evt) {
                PlaneCutVoxellTransformControl.this.control_panel.show();
            }
        });
    }

    public void controlComponentHidden(Component c) {
    }

    public void controlComponentShown(Component c) {
    }

    public Component getControlComponent() {
        return this.control_component;
    }

    public String getName() {
        return res.getString("name");
    }

    public boolean isActive() {
        return true;
    }

    public VoxellModel processModel(VoxellModel source) {
        this.cmodel.setSource(source);
        return this.cmodel;
    }

    public synchronized void addViewControlListener(ViewControlListener listener) {
        if (this.listenerList == null) {
            this.listenerList = new EventListenerList();
        }
        this.listenerList.add(ViewControlListener.class, listener);
    }

    public synchronized void removeViewControlListener(ViewControlListener listener) {
        this.listenerList.remove(ViewControlListener.class, listener);
    }

    private void fireViewControlListenerControlDeactivated(ViewControl event) {
        if (this.listenerList == null) {
            return;
        }
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ViewControlListener.class) continue;
            ((ViewControlListener)listeners[i + 1]).controlDeactivated(event);
        }
    }

    private void fireViewControlListenerControlActivated(ViewControl event) {
        if (this.listenerList == null) {
            return;
        }
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ViewControlListener.class) continue;
            ((ViewControlListener)listeners[i + 1]).controlActivated(event);
        }
    }

    private void fireViewControlListenerControlStateChanged(ViewControl event) {
        if (this.listenerList == null) {
            return;
        }
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ViewControlListener.class) continue;
            ((ViewControlListener)listeners[i + 1]).controlStateChanged(event);
        }
    }

    public PlaneCutVoxellModel getVoxellModel() {
        return this.cmodel;
    }

    public void modelUpdated() {
        this.fireViewControlListenerControlStateChanged(this);
    }

    public Component getConfigurationComponent() {
        if (this.interpolatorControlPanel == null) {
            this.interpolatorControlPanel = new InterpolatorControlPanel();
        }
        return this.interpolatorControlPanel;
    }

    public String getInterpolatorName() {
        return "Plane Cut";
    }

    public void setInterpolationStep(float step) {
        int startX = Integer.parseInt(this.interpolatorControlPanel.startX.getText());
        int startY = Integer.parseInt(this.interpolatorControlPanel.startY.getText());
        int startZ = Integer.parseInt(this.interpolatorControlPanel.startZ.getText());
        int endX = Integer.parseInt(this.interpolatorControlPanel.endX.getText());
        int endY = Integer.parseInt(this.interpolatorControlPanel.endY.getText());
        int endZ = Integer.parseInt(this.interpolatorControlPanel.endZ.getText());
        int sx = startX != -1 ? (int)((float)startX + (float)(endX - startX) * step) : this.cmodel.getStartX();
        int sy = startY != -1 ? (int)((float)startY + (float)(endY - startY) * step) : this.cmodel.getStartY();
        int sz = startZ != -1 ? (int)((float)startZ + (float)(endZ - startZ) * step) : this.cmodel.getStartZ();
        this.cmodel.setStartX(sx);
        this.cmodel.setStartY(sy);
        this.cmodel.setStartZ(sz);
        System.err.println("sx=" + sx + ",sy=" + sy + ",sz=" + sz);
        this.modelUpdated();
    }

    static class PlaneCutVoxellModel
    extends WrapperVoxellModel {
        private int startX;
        private int startY;
        private int startZ;
        private int endX;
        private int endY;
        private int endZ;

        PlaneCutVoxellModel(VoxellModel start) {
            super(start);
            this.setStartX(0);
            this.setStartY(0);
            this.setStartZ(0);
            this.setEndX(start.getWidth());
            this.setEndY(start.getHeight());
            this.setEndZ(start.getDepth());
        }

        public boolean isEnabled(int x, int y, int z) {
            if (x >= this.startX && x <= this.endX && y >= this.startY && y <= this.endY && z >= this.startZ && z <= this.endZ) {
                boolean retValue = super.isEnabled(x, y, z);
                return retValue;
            }
            return false;
        }

        public int getStartX() {
            return this.startX;
        }

        public void setStartX(int startX) {
            this.startX = startX;
        }

        public int getStartY() {
            return this.startY;
        }

        public void setStartY(int startY) {
            this.startY = startY;
        }

        public int getStartZ() {
            return this.startZ;
        }

        public void setStartZ(int startZ) {
            this.startZ = startZ;
        }

        public int getEndX() {
            return this.endX;
        }

        public void setEndX(int endX) {
            this.endX = endX;
        }

        public int getEndY() {
            return this.endY;
        }

        public void setEndY(int endY) {
            this.endY = endY;
        }

        public int getEndZ() {
            return this.endZ;
        }

        public void setEndZ(int endZ) {
            this.endZ = endZ;
        }
    }

    class InterpolatorControlPanel
    extends JPanel {
        JTextField startX;
        JTextField endX;
        JTextField startY;
        JTextField endY;
        JTextField startZ;
        JTextField endZ;

        public InterpolatorControlPanel() {
            this.add(new JLabel("Start and end plane cuts for each axis:"));
            this.startX = new JTextField("-1");
            this.add(new JLabel("X from "));
            this.add(this.startX);
            this.endX = new JTextField("-1");
            this.add(new JLabel(" to "));
            this.add(this.endX);
            this.startY = new JTextField("-1");
            this.add(new JLabel("Y from "));
            this.add(this.startY);
            this.endY = new JTextField("-1");
            this.add(new JLabel(" to "));
            this.add(this.endY);
            this.startZ = new JTextField("-1");
            this.add(new JLabel("Z from "));
            this.add(this.startZ);
            this.endZ = new JTextField("-1");
            this.add(new JLabel(" to "));
            this.add(this.endZ);
        }
    }
}

