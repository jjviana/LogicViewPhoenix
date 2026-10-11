/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.util;

import com.logicstyle.logicview.viewer.controls.ViewControl;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ViewControlPanel
extends JPanel {
    private ViewControl[] controls;
    private boolean initialized = false;
    private JTabbedPane controlsPane;

    private void initComponents() {
        this.controlsPane = new JTabbedPane();
        this.setLayout(new BorderLayout());
        this.setBorder(new BevelBorder(0));
        this.add((Component)this.controlsPane, "Center");
    }

    public ViewControlPanel(ViewControl[] c) {
        this.controls = c;
        this.initComponents();
        this.controlsPane.addChangeListener(new ChangeListener(){

            public void stateChanged(ChangeEvent e) {
                int sel = ViewControlPanel.this.controlsPane.getSelectedIndex();
                ViewControlPanel.this.controls[sel].controlComponentShown(ViewControlPanel.this.controls[sel].getControlComponent());
                for (int i = 0; i < ViewControlPanel.this.controls.length; ++i) {
                    if (i == sel) continue;
                    ViewControlPanel.this.controls[i].controlComponentHidden(ViewControlPanel.this.controls[i].getControlComponent());
                }
            }
        });
        for (int i = 0; i < this.controls.length; ++i) {
            if (this.controls[i].getControlComponent() == null) continue;
            this.controlsPane.addTab(this.controls[i].getName(), this.controls[i].getControlComponent());
        }
        this.controls[0].controlComponentShown(this.controls[0].getControlComponent());
        this.initialized = true;
    }
}

