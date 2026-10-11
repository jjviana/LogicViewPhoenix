/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.controls.planecut;

import com.logicstyle.logicview.viewer.controls.planecut.PlaneCutVoxellTransformControl;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class PlaneCutVoxellTransformControlPanel
extends JDialog {
    private static ResourceBundle res = ResourceBundle.getBundle(PlaneCutVoxellTransformControlPanel.class.getName());
    private PlaneCutVoxellTransformControl client;
    private JLabel jLabel4;
    private JSlider endZslider;
    private JLabel jLabel1;
    private JLabel jLabel3;
    private JPanel cutXpanel;
    private JLabel jLabel2;
    private JSlider startZslider;
    private JPanel cutYpanel;
    private JSlider startYslider;
    private JSlider startXslider;
    private JSlider endYslider;
    private JButton applyButton;
    private JLabel jLabel6;
    private JSlider endXslider;
    private JPanel cutZpanel;
    private JLabel jLabel5;
    private TitledBorder titleX;
    private TitledBorder titleY;
    private TitledBorder titleZ;

    private void initComponents() {
        this.cutXpanel = new JPanel();
        this.jLabel1 = new JLabel();
        this.startXslider = new JSlider();
        this.jLabel2 = new JLabel();
        this.endXslider = new JSlider();
        this.cutYpanel = new JPanel();
        this.jLabel3 = new JLabel();
        this.startYslider = new JSlider();
        this.jLabel4 = new JLabel();
        this.endYslider = new JSlider();
        this.cutZpanel = new JPanel();
        this.jLabel5 = new JLabel();
        this.startZslider = new JSlider();
        this.jLabel6 = new JLabel();
        this.endZslider = new JSlider();
        this.applyButton = new JButton();
        this.getContentPane().setLayout(new GridBagLayout());
        this.setTitle(res.getString("title"));
        this.addWindowListener(new WindowAdapter(){

            public void windowClosing(WindowEvent evt) {
                PlaneCutVoxellTransformControlPanel.this.closeDialog(evt);
            }
        });
        this.cutXpanel.setLayout(new GridBagLayout());
        this.cutXpanel.setBorder(new TitledBorder(this.titleX));
        this.jLabel1.setText(res.getString("minText"));
        this.cutXpanel.add((Component)this.jLabel1, new GridBagConstraints());
        this.startXslider.setMajorTickSpacing(1);
        this.startXslider.setSnapToTicks(true);
        this.startXslider.addChangeListener(new ChangeListener(){

            public void stateChanged(ChangeEvent evt) {
                PlaneCutVoxellTransformControlPanel.this.startXsliderStateChanged(evt);
            }
        });
        this.startXslider.addPropertyChangeListener(new PropertyChangeListener(){

            public void propertyChange(PropertyChangeEvent evt) {
                PlaneCutVoxellTransformControlPanel.this.startXsliderPropertyChange(evt);
            }
        });
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 0;
        this.cutXpanel.add((Component)this.startXslider, gridBagConstraints);
        this.jLabel2.setText(res.getString("maxText"));
        this.cutXpanel.add((Component)this.jLabel2, new GridBagConstraints());
        this.endXslider.setMajorTickSpacing(1);
        this.endXslider.setSnapToTicks(true);
        this.endXslider.addChangeListener(new ChangeListener(){

            public void stateChanged(ChangeEvent evt) {
                PlaneCutVoxellTransformControlPanel.this.endXsliderStateChanged(evt);
            }
        });
        this.cutXpanel.add((Component)this.endXslider, new GridBagConstraints());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 0;
        this.getContentPane().add((Component)this.cutXpanel, gridBagConstraints);
        this.cutYpanel.setLayout(new GridBagLayout());
        this.cutYpanel.setBorder(new TitledBorder(this.titleY));
        this.jLabel3.setText(res.getString("minText"));
        this.cutYpanel.add((Component)this.jLabel3, new GridBagConstraints());
        this.startYslider.setMajorTickSpacing(1);
        this.startYslider.setSnapToTicks(true);
        this.startYslider.addChangeListener(new ChangeListener(){

            public void stateChanged(ChangeEvent evt) {
                PlaneCutVoxellTransformControlPanel.this.startYsliderStateChanged(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 0;
        this.cutYpanel.add((Component)this.startYslider, gridBagConstraints);
        this.jLabel4.setText(res.getString("maxText"));
        this.cutYpanel.add((Component)this.jLabel4, new GridBagConstraints());
        this.endYslider.setMajorTickSpacing(1);
        this.endYslider.setSnapToTicks(true);
        this.endYslider.addChangeListener(new ChangeListener(){

            public void stateChanged(ChangeEvent evt) {
                PlaneCutVoxellTransformControlPanel.this.endYsliderStateChanged(evt);
            }
        });
        this.cutYpanel.add((Component)this.endYslider, new GridBagConstraints());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 0;
        this.getContentPane().add((Component)this.cutYpanel, gridBagConstraints);
        this.cutZpanel.setLayout(new GridBagLayout());
        this.cutZpanel.setBorder(new TitledBorder(this.titleZ));
        this.jLabel5.setText(res.getString("minText"));
        this.cutZpanel.add((Component)this.jLabel5, new GridBagConstraints());
        this.startZslider.setMajorTickSpacing(1);
        this.startZslider.setSnapToTicks(true);
        this.startZslider.addChangeListener(new ChangeListener(){

            public void stateChanged(ChangeEvent evt) {
                PlaneCutVoxellTransformControlPanel.this.startZsliderStateChanged(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 0;
        this.cutZpanel.add((Component)this.startZslider, gridBagConstraints);
        this.jLabel6.setText(res.getString("maxText"));
        this.cutZpanel.add((Component)this.jLabel6, new GridBagConstraints());
        this.endZslider.setSnapToTicks(true);
        this.endZslider.addChangeListener(new ChangeListener(){

            public void stateChanged(ChangeEvent evt) {
                PlaneCutVoxellTransformControlPanel.this.endZsliderStateChanged(evt);
            }
        });
        this.cutZpanel.add((Component)this.endZslider, new GridBagConstraints());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 0;
        this.getContentPane().add((Component)this.cutZpanel, gridBagConstraints);
        this.applyButton.setText(res.getString("applyButtonLabel"));
        this.applyButton.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent evt) {
                PlaneCutVoxellTransformControlPanel.this.applyButtonActionPerformed(evt);
            }
        });
        this.getContentPane().add((Component)this.applyButton, new GridBagConstraints());
        this.pack();
    }

    private void endZsliderStateChanged(ChangeEvent evt) {
        this.updateTitleBorderZ();
    }

    private void endXsliderStateChanged(ChangeEvent evt) {
        this.updateTitleBorderX();
    }

    private void endYsliderStateChanged(ChangeEvent evt) {
        this.updateTitleBorderY();
    }

    private void startXsliderPropertyChange(PropertyChangeEvent evt) {
        this.updateTitleBorderX();
    }

    private void applyButtonActionPerformed(ActionEvent evt) {
        PlaneCutVoxellTransformControl.PlaneCutVoxellModel cmod = this.client.getVoxellModel();
        if (this.startXslider.getValue() >= this.endXslider.getValue()) {
            JOptionPane.showMessageDialog(this, res.getString("invalidXCut"), res.getString("invalidCutTitle"), 0);
            return;
        }
        if (this.startYslider.getValue() >= this.endYslider.getValue()) {
            JOptionPane.showMessageDialog(this, res.getString("invalidYCut"), res.getString("invalidCutTitle"), 0);
            return;
        }
        if (this.startZslider.getValue() >= this.endZslider.getValue()) {
            JOptionPane.showMessageDialog(this, res.getString("invalidZCut"), res.getString("invalidCutTitle"), 0);
            return;
        }
        cmod.setStartX(this.startXslider.getValue());
        cmod.setEndX(this.endXslider.getValue());
        cmod.setStartY(this.startYslider.getValue());
        cmod.setEndY(this.endYslider.getValue());
        cmod.setStartZ(this.startZslider.getValue());
        cmod.setEndZ(this.endZslider.getValue());
        this.client.modelUpdated();
    }

    private void startZsliderStateChanged(ChangeEvent evt) {
        this.updateTitleBorderZ();
    }

    private void startYsliderStateChanged(ChangeEvent evt) {
        this.updateTitleBorderY();
    }

    private void startXsliderStateChanged(ChangeEvent evt) {
        this.updateTitleBorderX();
    }

    private void closeDialog(WindowEvent evt) {
        this.setVisible(false);
        this.dispose();
    }

    private void updateTitleBorderX() {
        this.titleX.setTitle(res.getString("cutPlaneTitle") + "X:" + this.startXslider.getValue() + ":" + this.endXslider.getValue());
        this.repaint();
    }

    private void updateTitleBorderY() {
        this.titleY.setTitle(res.getString("cutPlaneTitle") + "Y:" + this.startYslider.getValue() + ":" + this.endYslider.getValue());
        this.repaint();
    }

    private void updateTitleBorderZ() {
        this.titleZ.setTitle(res.getString("cutPlaneTitle") + "Z:" + this.startZslider.getValue() + ":" + this.endZslider.getValue());
        this.repaint();
    }

    public void show() {
        PlaneCutVoxellTransformControl.PlaneCutVoxellModel cmod = this.client.getVoxellModel();
        this.startXslider.setMinimum(0);
        this.startYslider.setMinimum(0);
        this.startZslider.setMinimum(0);
        this.endXslider.setMinimum(0);
        this.endYslider.setMinimum(0);
        this.endZslider.setMinimum(0);
        this.startXslider.setMaximum(cmod.getSource().getWidth());
        this.startYslider.setMaximum(cmod.getSource().getHeight());
        this.startZslider.setMaximum(cmod.getSource().getDepth());
        this.startXslider.setValue(cmod.getStartX());
        this.startYslider.setValue(cmod.getStartY());
        this.startZslider.setValue(cmod.getStartZ());
        this.endXslider.setMaximum(cmod.getSource().getWidth());
        this.endYslider.setMaximum(cmod.getSource().getHeight());
        this.endZslider.setMaximum(cmod.getSource().getDepth());
        this.endXslider.setValue(cmod.getEndX());
        this.endYslider.setValue(cmod.getEndY());
        this.endZslider.setValue(cmod.getEndZ());
        this.updateTitleBorderX();
        this.updateTitleBorderY();
        this.updateTitleBorderZ();
        this.pack();
        super.show();
    }

    public PlaneCutVoxellTransformControlPanel(Frame parent, PlaneCutVoxellTransformControl client) {
        super(parent, false);
        this.client = client;
        this.titleX = new TitledBorder("");
        this.titleY = new TitledBorder("");
        this.titleZ = new TitledBorder("");
        this.initComponents();
        this.validate();
    }
}

