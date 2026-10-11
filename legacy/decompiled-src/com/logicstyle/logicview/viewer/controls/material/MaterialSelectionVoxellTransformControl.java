/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.controls.material;

import com.logicstyle.logicview.MainWindow.MainWindow;
import com.logicstyle.logicview.model.MaterialModel;
import com.logicstyle.logicview.model.VoxellModel;
import com.logicstyle.logicview.viewer.controls.ViewControl;
import com.logicstyle.logicview.viewer.controls.ViewControlListener;
import com.logicstyle.logicview.viewer.controls.VoxellTransformControl;
import com.logicstyle.logicview.viewer.controls.material.MaterialsPanel;
import com.logicstyle.logicview.viewer.printing.PrintPageElement;
import com.logicstyle.logicview.viewer.util.WrapperVoxellModel;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.event.EventListenerList;

public class MaterialSelectionVoxellTransformControl
implements VoxellTransformControl {
    private static ResourceBundle res = ResourceBundle.getBundle(MaterialSelectionVoxellTransformControl.class.getName());
    private ComponentRepository rep;
    JButton label_component;
    JDialog matdialog;
    private MaterialsPanel mp;
    private boolean[] matState;
    private MaterialSelectionWrapper model_wrapper;
    private EventListenerList listenerList = null;

    public MaterialSelectionVoxellTransformControl(MaterialModel m, ComponentRepository c) {
        MaterialModel.Material[] mats = m.getMaterials();
        this.matState = new boolean[mats.length];
        for (int i = 0; i < mats.length; ++i) {
            this.matState[i] = mats[i].enabled;
        }
        this.model_wrapper = new MaterialSelectionWrapper(m);
        this.mp = new MaterialsPanel(m, this);
        MainWindow mw = (MainWindow)c.findComponentByInterface(MainWindow.class);
        this.matdialog = new JDialog((Frame)mw.getParentFrame(), false);
        JScrollPane jsp = new JScrollPane();
        jsp.setHorizontalScrollBarPolicy(30);
        jsp.setVerticalScrollBarPolicy(20);
        jsp.setViewportView(this.mp);
        this.matdialog.getContentPane().setLayout(new BorderLayout());
        this.matdialog.getContentPane().add((Component)jsp, "Center");
        JButton updateButton = new JButton(res.getString("updateButtonTitle"));
        updateButton.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent evt) {
                MaterialSelectionVoxellTransformControl.this.controlUpdated();
            }
        });
        this.matdialog.getContentPane().add((Component)updateButton, "South");
        this.matdialog.setTitle(res.getString("dialogTitle"));
        this.matdialog.setSize(200, 300);
        this.label_component = new JButton(res.getString("buttonTitle"));
        this.label_component.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent evt) {
                MaterialSelectionVoxellTransformControl.this.matdialog.show();
            }
        });
    }

    public void controlComponentHidden(Component c) {
    }

    public void controlComponentShown(Component c) {
    }

    public Component getControlComponent() {
        return this.label_component;
    }

    public String getName() {
        return res.getString("name");
    }

    public boolean isActive() {
        return true;
    }

    public VoxellModel processModel(VoxellModel source) {
        this.model_wrapper.setSource(source);
        return this.model_wrapper;
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

    public void controlUpdated() {
        this.fireViewControlListenerControlStateChanged(this);
    }

    void setMaterialState(int index, boolean state) {
        this.matState[index] = state;
    }

    boolean getMaterialState(int index) {
        return this.matState[index];
    }

    public PrintPageElement getPrintPageElement() {
        return this.mp.getPageElement();
    }

    class MaterialSelectionWrapper
    extends WrapperVoxellModel {
        private MaterialModel matmod;

        MaterialSelectionWrapper(MaterialModel m) {
            super(m);
            this.matmod = m;
        }

        public boolean isEnabled(int x, int y, int z) {
            int matCode = this.matmod.getIntValueAt(x, y, z);
            return MaterialSelectionVoxellTransformControl.this.matState[matCode - 1];
        }
    }
}

