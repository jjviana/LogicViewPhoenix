/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.controls.animation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class AnimationControlPanel
extends JDialog {
    private ResourceBundle res = ResourceBundle.getBundle(AnimationControlPanel.class.getName());
    private JPanel animationPropertiesPanel;
    private JPanel interpolatorPanel;
    private JList interpolatorsList;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JTextField jTextField1;
    private JTextField jTextField2;
    private JPanel propertiesPanel;

    public AnimationControlPanel() {
        this.initComponents();
    }

    private void initComponents() {
        this.interpolatorsList = new JList();
        this.animationPropertiesPanel = new JPanel();
        this.jLabel1 = new JLabel();
        this.jTextField1 = new JTextField();
        this.jLabel2 = new JLabel();
        this.jTextField2 = new JTextField();
        this.interpolatorPanel = new JPanel();
        this.propertiesPanel = new JPanel();
        this.addWindowListener(new WindowAdapter(){

            public void windowClosing(WindowEvent evt) {
                AnimationControlPanel.this.closeDialog(evt);
            }
        });
        this.getContentPane().add((Component)this.interpolatorsList, "West");
        this.animationPropertiesPanel.setLayout(new GridBagLayout());
        this.animationPropertiesPanel.setBorder(new TitledBorder(null, this.res.getString("animationPropertiesTitle"), 0, 0, new Font("Dialog", 1, 12)));
        this.jLabel1.setText(this.res.getString("framesPerSecondLabel"));
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = 17;
        this.animationPropertiesPanel.add((Component)this.jLabel1, gridBagConstraints);
        this.jTextField1.setColumns(4);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 0;
        this.animationPropertiesPanel.add((Component)this.jTextField1, gridBagConstraints);
        this.jLabel2.setText(this.res.getString("durationLabel"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = 17;
        this.animationPropertiesPanel.add((Component)this.jLabel2, gridBagConstraints);
        this.jTextField2.setColumns(4);
        this.animationPropertiesPanel.add((Component)this.jTextField2, new GridBagConstraints());
        this.getContentPane().add((Component)this.animationPropertiesPanel, "North");
        this.interpolatorPanel.setLayout(new BorderLayout());
        this.propertiesPanel.setLayout(new BorderLayout());
        this.interpolatorPanel.add((Component)this.propertiesPanel, "Center");
        this.getContentPane().add((Component)this.interpolatorPanel, "Center");
        this.pack();
    }

    private void closeDialog(WindowEvent evt) {
        this.setVisible(false);
        this.dispose();
    }

    public static void main(String[] args) {
    }
}

