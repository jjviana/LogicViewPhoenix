/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.controls.timestep;

import com.logicstyle.logicview.model.FieldModel;
import com.logicstyle.logicview.model.TransformedVoxellModel;
import com.logicstyle.logicview.model.VoxellModel;
import com.logicstyle.logicview.viewer.animation.Interpolator;
import com.logicstyle.logicview.viewer.controls.ViewControl;
import com.logicstyle.logicview.viewer.controls.ViewControlListener;
import com.logicstyle.logicview.viewer.controls.VoxellTransformControl;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

public class TimeStepSelectorVoxelTransformControl
implements VoxellTransformControl,
Interpolator {
    private static ResourceBundle res = ResourceBundle.getBundle(TimeStepSelectorVoxelTransformControl.class.getName());
    private FieldModel model;
    private EventListenerList listenerList = null;
    private JPanel control_panel;
    private JSlider slider;
    private JTextField tf;

    public void controlComponentHidden(Component c) {
    }

    public void controlComponentShown(Component c) {
    }

    private void initControlPanel() {
        if (this.control_panel == null) {
            this.control_panel = new JPanel();
        } else {
            this.control_panel.removeAll();
        }
        this.control_panel.add(new JLabel("Time Step:"));
        this.tf = new JTextField(4);
        this.tf.setText(String.valueOf(this.model.getCurrentTimeStep() * this.model.getTimeStepSkip()));
        this.control_panel.add(this.tf);
        int timeStepFinal = this.model.getTimeStepSkip() * ((this.model.getTimeStepCount() - 1) / this.model.getTimeStepSkip());
        this.control_panel.add(new JLabel(" of " + timeStepFinal));
        this.slider = new JSlider(0, timeStepFinal, this.model.getCurrentTimeStep());
        this.slider.setMajorTickSpacing(this.model.getTimeStepSkip());
        this.slider.setPaintLabels(true);
        this.slider.setSnapToTicks(true);
        this.slider.setPaintTicks(true);
        this.control_panel.add(this.slider);
        this.slider.addChangeListener(new ChangeListener(){

            public void stateChanged(ChangeEvent evt) {
                if (!TimeStepSelectorVoxelTransformControl.this.slider.getValueIsAdjusting()) {
                    int newval = TimeStepSelectorVoxelTransformControl.this.slider.getValue();
                    TimeStepSelectorVoxelTransformControl.this.tf.setText(String.valueOf(newval));
                    TimeStepSelectorVoxelTransformControl.this.model.setCurrentTimeStep(newval / TimeStepSelectorVoxelTransformControl.this.model.getTimeStepSkip());
                    TimeStepSelectorVoxelTransformControl.this.fireViewControlListenerControlStateChanged(TimeStepSelectorVoxelTransformControl.this);
                }
            }
        });
        this.tf.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent evt) {
                int newval;
                try {
                    newval = Integer.valueOf(TimeStepSelectorVoxelTransformControl.this.tf.getText());
                }
                catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, res.getString("timestepnumberformaterror"), res.getString("timestepformaterrortitle"), 0);
                    TimeStepSelectorVoxelTransformControl.this.tf.selectAll();
                    return;
                }
                TimeStepSelectorVoxelTransformControl.this.slider.setValue(newval);
            }
        });
    }

    public Component getControlComponent() {
        if (this.control_panel == null) {
            this.control_panel = new JPanel();
        }
        return this.control_panel;
    }

    public String getName() {
        return res.getString("name");
    }

    public boolean isActive() {
        return true;
    }

    public VoxellModel processModel(VoxellModel source) {
        VoxellModel orig = source;
        while (source instanceof TransformedVoxellModel) {
            source = ((TransformedVoxellModel)source).getSource();
        }
        if (source instanceof FieldModel) {
            FieldModel m = (FieldModel)source;
            if (this.model == null || !this.model.equals(m)) {
                this.model = m;
                this.initControlPanel();
            }
            return orig;
        }
        return null;
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

    public Component getConfigurationComponent() {
        return null;
    }

    public String getInterpolatorName() {
        return res.getString("interpolatorName");
    }

    public void setInterpolationStep(float step) {
        int new_step = (int)((float)(this.model.getTimeStepCount() - 1) * step);
        this.model.setCurrentTimeStep(new_step);
        this.fireViewControlListenerControlStateChanged(this);
    }
}

