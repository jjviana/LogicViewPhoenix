/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.controls.color;

import com.logicstyle.logicview.MainWindow.MainWindow;
import com.logicstyle.logicview.model.FieldModel;
import com.logicstyle.logicview.model.VoxellModel;
import com.logicstyle.logicview.viewer.controls.ViewControl;
import com.logicstyle.logicview.viewer.controls.ViewControlListener;
import com.logicstyle.logicview.viewer.controls.color.ColorControlDialog;
import com.logicstyle.logicview.viewer.controls.color.VoxellColorGeneratorControl;
import com.logicstyle.logicview.viewer.printing.PrintPageElement;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.event.EventListenerList;

public class FieldModelVoxellColorGeneratorControl
implements VoxellColorGeneratorControl {
    private ResourceBundle res = ResourceBundle.getBundle(FieldModelVoxellColorGeneratorControl.class.getName());
    private float max;
    private float min;
    private ColorControlDialog cdialog;
    private JButton control = new JButton(this.res.getString("controlButtonLabel"));
    private Color[] colorvals;
    private float step;
    private MainWindow main;
    private EventListenerList listenerList = null;

    public FieldModelVoxellColorGeneratorControl(VoxellModel currentModel, FieldModel originalModel, MainWindow main) {
        this.control.setFont(new Font("Dialog", 0, 10));
        this.control.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent evt) {
                FieldModelVoxellColorGeneratorControl.this.cdialog.validate();
                FieldModelVoxellColorGeneratorControl.this.cdialog.show();
            }
        });
        this.main = main;
        this.createColorDialog(originalModel, currentModel);
        this.setModel(currentModel);
    }

    public void controlComponentHidden(Component c) {
    }

    public void controlComponentShown(Component c) {
    }

    public Color getColorForPoint(int x, int y, int z, VoxellModel model) {
        float val = model.getFloatValueAt(x, y, z);
        return this.cdialog.getColorforValue(val);
    }

    public Component getControlComponent() {
        return this.control;
    }

    public String getName() {
        return this.res.getString("name");
    }

    public boolean isActive() {
        return true;
    }

    public int getIntegerInterpolatedRGB(int c1, int c2, int coef) {
        if (coef == 0) {
            return c1;
        }
        if (coef == 255) {
            return c2;
        }
        if (c1 == c2) {
            return c1;
        }
        int c1A = c1 & 0xFF000000;
        int c1R = (c1 & 0xFF0000) >>> 16;
        int c1G = (c1 & 0xFF00) >>> 8;
        int c1B = c1 & 0xFF;
        int c2A = c2 & 0xFF000000;
        int c2R = (c2 & 0xFF0000) >>> 16;
        int c2G = (c2 & 0xFF00) >>> 8;
        int c2B = c2 & 0xFF;
        int nA = c1A + ((c2A - c1A) * coef >> 8);
        int nR = c1R + ((c2R - c1R) * coef >> 8);
        int nG = c1G + ((c2G - c1G) * coef >> 8);
        int nB = c1B + ((c2B - c1B) * coef >> 8);
        int newRGB = nA | nR << 16 | nG << 8 | nB;
        return newRGB;
    }

    private void createColorDialog(FieldModel originalModel, VoxellModel currentModel) {
        Color color1 = Color.blue;
        Color color2 = Color.red;
        this.cdialog = new ColorControlDialog(this, this.main.getParentFrame(), color1, color2, currentModel, 256, originalModel.getUnits());
    }

    public void setModel(VoxellModel m) {
        this.cdialog.setCurrentModel(m);
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

    public void applyColorScale() {
        this.fireViewControlListenerControlStateChanged(this);
    }

    public PrintPageElement getPrintPageElement() {
        return this.cdialog.getPrintPageElement();
    }
}

