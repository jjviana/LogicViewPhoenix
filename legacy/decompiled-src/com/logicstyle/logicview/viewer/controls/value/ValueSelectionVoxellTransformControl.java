/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.controls.value;

import com.logicstyle.logicview.model.VoxellModel;
import com.logicstyle.logicview.viewer.controls.ViewControl;
import com.logicstyle.logicview.viewer.controls.ViewControlListener;
import com.logicstyle.logicview.viewer.controls.VoxellTransformControl;
import com.logicstyle.logicview.viewer.controls.value.ValueSelectionVoxellTransformControlPanel;
import com.logicstyle.logicview.viewer.util.WrapperVoxellModel;
import java.awt.Component;
import java.util.ResourceBundle;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

public class ValueSelectionVoxellTransformControl
implements VoxellTransformControl {
    private static ResourceBundle res = ResourceBundle.getBundle(ValueSelectionVoxellTransformControl.class.getName());
    private ValueSelectionVoxellTransformControlPanel cpanel = new ValueSelectionVoxellTransformControlPanel();
    private EventListenerList listenerList = null;

    public ValueSelectionVoxellTransformControl() {
        this.cpanel.addChangeListener(new ChangeListener(){

            public void stateChanged(ChangeEvent evt) {
                ValueSelectionVoxellTransformControl.this.fireViewControlListenerControlStateChanged(ValueSelectionVoxellTransformControl.this);
            }
        });
    }

    public void controlComponentHidden(Component c) {
    }

    public void controlComponentShown(Component c) {
    }

    public Component getControlComponent() {
        return this.cpanel;
    }

    public String getName() {
        return res.getString("name");
    }

    public boolean isActive() {
        return this.cpanel.isActive();
    }

    public VoxellModel processModel(VoxellModel source) {
        if (this.isActive()) {
            return new ValueTransformWrapper(source);
        }
        return source;
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

    private class ValueTransformWrapper
    extends WrapperVoxellModel {
        private float maxValue;
        private float lowerLimit;
        private float upperLimit;
        private float maxAbsValue;

        public ValueTransformWrapper(VoxellModel source) {
            super(source);
            this.computeMaxValues();
            this.computeLimits();
            System.err.println("Value selection: maxValue= " + this.maxValue + ", upperLimit=" + this.upperLimit + ", lowerLimit=" + this.lowerLimit);
        }

        private void computeMaxValues() {
            this.maxAbsValue = -3.4028235E38f;
            this.maxValue = -3.4028235E38f;
            VoxellModel model = this.getSource();
            for (int i = 0; i < model.getWidth(); ++i) {
                for (int j = 0; j < model.getHeight(); ++j) {
                    for (int k = 0; k < model.getDepth(); ++k) {
                        float absVal;
                        if (!model.isEnabled(i, j, k)) continue;
                        float val = model.getFloatValueAt(i, j, k);
                        if (val > this.maxValue) {
                            this.maxValue = val;
                        }
                        if (!((absVal = Math.abs(val)) > this.maxAbsValue)) continue;
                        this.maxAbsValue = absVal;
                    }
                }
            }
        }

        private void computeLimits() {
            int n = ValueSelectionVoxellTransformControl.this.cpanel.getValueMode();
            ValueSelectionVoxellTransformControl.this.cpanel;
            float val = n == 0 ? this.maxAbsValue : this.maxValue;
            int n2 = ValueSelectionVoxellTransformControl.this.cpanel.getValueScope();
            ValueSelectionVoxellTransformControl.this.cpanel;
            if (n2 == 5) {
                float center = val * (ValueSelectionVoxellTransformControl.this.cpanel.getValue() / 100.0f);
                this.upperLimit = center + center * (ValueSelectionVoxellTransformControl.this.cpanel.getDelta() / 100.0f);
                this.lowerLimit = center - center * (ValueSelectionVoxellTransformControl.this.cpanel.getDelta() / 100.0f);
            } else {
                float center = ValueSelectionVoxellTransformControl.this.cpanel.getValue();
                this.upperLimit = center + ValueSelectionVoxellTransformControl.this.cpanel.getDelta();
                this.lowerLimit = center - ValueSelectionVoxellTransformControl.this.cpanel.getDelta();
            }
        }

        public boolean isEnabled(int x, int y, int z) {
            VoxellModel orig = this.getSource();
            float value = orig.getFloatValueAt(x, y, z);
            int n = ValueSelectionVoxellTransformControl.this.cpanel.getValueMode();
            ValueSelectionVoxellTransformControl.this.cpanel;
            if (n == 0) {
                value = Math.abs(value);
            }
            boolean enable = this.lowerLimit <= value && value <= this.upperLimit;
            return enable & orig.isEnabled(x, y, z);
        }
    }
}

