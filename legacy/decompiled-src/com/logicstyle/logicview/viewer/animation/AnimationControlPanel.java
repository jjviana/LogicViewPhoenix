/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.animation;

import com.logicstyle.logicview.model.FieldModel;
import com.logicstyle.logicview.model.Model;
import com.logicstyle.logicview.model.View;
import com.logicstyle.logicview.viewer.animation.Animation;
import com.logicstyle.logicview.viewer.animation.AnimationDialog;
import com.logicstyle.logicview.viewer.animation.AnimationEncoder;
import com.logicstyle.logicview.viewer.animation.AnimationEncoderFactory;
import com.logicstyle.logicview.viewer.animation.Animator;
import com.logicstyle.logicview.viewer.animation.Interpolator;
import com.logicstyle.logicview.viewer.animation.InterpolatorFactory;
import com.logicstyle.logicview.viewer.printing.ViewPrintConstructor;
import com.logicstyle.logicview.viewer.printing.ViewPrintConstructorFactory;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class AnimationControlPanel
extends JDialog {
    private ResourceBundle res = ResourceBundle.getBundle(AnimationControlPanel.class.getName());
    private ComponentRepository repository;
    private View view;
    private ViewPrintConstructor viewPrintConstructor;
    private ArrayList interpolators;
    private ArrayList interpolator_checkboxes;
    private AnimationEncoder animationEncoder;
    private JCheckBox shouldWriteAnimation;
    private JTabbedPane PropertiesPane;
    private JPanel animationPropertiesPanel;
    private JTextField durationTextField;
    private JTextField frameHeightTextField;
    private JLabel frameWidthLabel;
    private JTextField frameWidthTextField;
    private JTextField framesPerSecondTextField;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JPanel leftPanel;
    private JPanel rigntPanel;
    private JButton startButton;

    public AnimationControlPanel(ComponentRepository rep, View v) {
        this.repository = rep;
        this.view = v;
        this.initComponents();
        this.constructPropertiesPane();
        this.framesPerSecondTextField.setText("1");
        Model model = this.view.getModel();
        if (model instanceof FieldModel) {
            FieldModel fmodel = (FieldModel)model;
            this.durationTextField.setText(fmodel.getTimeStepCount() + "");
        } else {
            this.durationTextField.setText("60");
        }
        this.frameWidthTextField.setText("640");
        this.frameHeightTextField.setText("480");
        this.setSize(640, 480);
        this.validate();
    }

    private void initComponents() {
        this.animationPropertiesPanel = new JPanel();
        this.leftPanel = new JPanel();
        this.jLabel1 = new JLabel();
        this.framesPerSecondTextField = new JTextField();
        this.jLabel2 = new JLabel();
        this.durationTextField = new JTextField();
        this.rigntPanel = new JPanel();
        this.jLabel3 = new JLabel();
        this.frameWidthTextField = new JTextField();
        this.frameWidthLabel = new JLabel();
        this.frameHeightTextField = new JTextField();
        this.startButton = new JButton();
        this.PropertiesPane = new JTabbedPane();
        this.addWindowListener(new WindowAdapter(){

            public void windowClosing(WindowEvent evt) {
                AnimationControlPanel.this.closeDialog(evt);
            }
        });
        this.animationPropertiesPanel.setLayout(new GridBagLayout());
        this.animationPropertiesPanel.setBorder(new TitledBorder(null, this.res.getString("animationPropertiesTitle"), 0, 0, new Font("Dialog", 1, 12)));
        this.leftPanel.setLayout(new GridBagLayout());
        this.jLabel1.setText(this.res.getString("framesPerSecondLabel"));
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = 17;
        this.leftPanel.add((Component)this.jLabel1, gridBagConstraints);
        this.framesPerSecondTextField.setColumns(4);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 0;
        this.leftPanel.add((Component)this.framesPerSecondTextField, gridBagConstraints);
        this.jLabel2.setText(this.res.getString("durationLabel"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = 17;
        this.leftPanel.add((Component)this.jLabel2, gridBagConstraints);
        this.durationTextField.setColumns(4);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 0;
        this.leftPanel.add((Component)this.durationTextField, gridBagConstraints);
        this.animationPropertiesPanel.add((Component)this.leftPanel, new GridBagConstraints());
        this.rigntPanel.setLayout(new GridBagLayout());
        this.jLabel3.setText(this.res.getString("frameWidthLabel"));
        this.rigntPanel.add((Component)this.jLabel3, new GridBagConstraints());
        this.frameWidthTextField.setColumns(4);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 0;
        this.rigntPanel.add((Component)this.frameWidthTextField, gridBagConstraints);
        this.frameWidthLabel.setText(this.res.getString("frameHeightLabel"));
        this.rigntPanel.add((Component)this.frameWidthLabel, new GridBagConstraints());
        this.frameHeightTextField.setColumns(4);
        this.rigntPanel.add((Component)this.frameHeightTextField, new GridBagConstraints());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 0;
        this.animationPropertiesPanel.add((Component)this.rigntPanel, gridBagConstraints);
        this.startButton.setText(this.res.getString("startButtonLabel"));
        this.startButton.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent evt) {
                AnimationControlPanel.this.startButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 0;
        this.animationPropertiesPanel.add((Component)this.startButton, gridBagConstraints);
        this.getContentPane().add((Component)this.animationPropertiesPanel, "North");
        this.getContentPane().add((Component)this.PropertiesPane, "Center");
        this.pack();
    }

    private void startButtonActionPerformed(ActionEvent evt) {
        int frameHeight;
        int frameWidth;
        float duration;
        int frames_per_second;
        try {
            frames_per_second = Integer.parseInt(this.framesPerSecondTextField.getText());
        }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, this.res.getString("framesPerSecondError"), this.res.getString("errorMessageTitle"), 0);
            this.framesPerSecondTextField.grabFocus();
            return;
        }
        try {
            duration = Float.parseFloat(this.durationTextField.getText());
        }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, this.res.getString("durationError"), this.res.getString("errorMessageTitle"), 0);
            this.durationTextField.grabFocus();
            return;
        }
        try {
            frameWidth = Integer.parseInt(this.frameWidthTextField.getText());
        }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, this.res.getString("frameWidthError"), this.res.getString("errorMessageTitle"), 0);
            this.frameWidthTextField.grabFocus();
            return;
        }
        try {
            frameHeight = Integer.parseInt(this.frameHeightTextField.getText());
        }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, this.res.getString("frameHeightError"), this.res.getString("errorMessageTitle"), 0);
            this.frameHeightTextField.grabFocus();
            return;
        }
        this.startAnimation(frameWidth, frameHeight, frames_per_second, duration);
    }

    private void closeDialog(WindowEvent evt) {
        this.setVisible(false);
        this.dispose();
    }

    public static void main(String[] args) {
    }

    private void constructPropertiesPane() {
        ViewPrintConstructorFactory constructorFactory = (ViewPrintConstructorFactory)this.repository.findComponentByInterface(ViewPrintConstructorFactory.class);
        this.viewPrintConstructor = constructorFactory.getViewPrintConstructor(this.view);
        this.PropertiesPane.add(this.res.getString("viewConstructorPaneTitle"), this.viewPrintConstructor.getPrintConstructorComponent());
        AnimationEncoderFactory encoderFactory = (AnimationEncoderFactory)this.repository.findComponentByInterface(AnimationEncoderFactory.class);
        if (encoderFactory != null) {
            JPanel encoderPanel = new JPanel();
            encoderPanel.setLayout(new BorderLayout());
            this.shouldWriteAnimation = new JCheckBox(this.res.getString("enableEncodingLabel"));
            this.shouldWriteAnimation.setSelected(true);
            encoderPanel.add((Component)this.shouldWriteAnimation, "North");
            this.animationEncoder = encoderFactory.getAnimationEncoder();
            if (this.animationEncoder.getAnimationEncodingControlCoponent() != null) {
                encoderPanel.add(this.animationEncoder.getAnimationEncodingControlCoponent(), "Center");
            }
            this.PropertiesPane.add(this.res.getString("encodePanetitle"), encoderPanel);
        }
        List factories = this.repository.findByInterface(InterpolatorFactory.class);
        this.interpolators = new ArrayList();
        this.interpolator_checkboxes = new ArrayList();
        for (int i = 0; i < factories.size(); ++i) {
            InterpolatorFactory f = (InterpolatorFactory)factories.get(i);
            Interpolator inter = f.getInterpolator(this.view);
            if (inter == null) continue;
            this.interpolators.add(inter);
            JCheckBox check = new JCheckBox(this.res.getString("interpolatorCheckboxLabel"));
            this.interpolator_checkboxes.add(check);
            JPanel inner_panel = new JPanel();
            inner_panel.setLayout(new BorderLayout());
            inner_panel.add((Component)check, "North");
            if (inter.getConfigurationComponent() != null) {
                inner_panel.add(inter.getConfigurationComponent(), "Center");
            }
            this.PropertiesPane.add(inter.getInterpolatorName(), inner_panel);
        }
    }

    private void startAnimation(int frameWidth, int frameHeight, int frames_per_second, float duration) {
        int reply;
        Animation animation = new Animation();
        animation.setFrameWidth(frameWidth);
        animation.setFrameHeight(frameHeight);
        animation.setNumberOfFrames((int)((float)frames_per_second * duration));
        animation.setFramesPerSecond(frames_per_second);
        animation.setDuration(duration);
        ArrayList enabledInterpolators = new ArrayList();
        for (int i = 0; i < this.interpolator_checkboxes.size(); ++i) {
            JCheckBox check = (JCheckBox)this.interpolator_checkboxes.get(i);
            if (!check.isSelected()) continue;
            enabledInterpolators.add(this.interpolators.get(i));
        }
        if (enabledInterpolators.size() == 0 && (reply = JOptionPane.showConfirmDialog(this, this.res.getString("noInterpolatorsWarning"), this.res.getString("warningTitle"), 0, 2)) == 1) {
            return;
        }
        Animator animator = new Animator(animation, enabledInterpolators, this.viewPrintConstructor);
        animation.setAnimator(animator);
        AnimationDialog dialog = new AnimationDialog((Dialog)this, animation);
        animator.addAnimatorListener(dialog);
        if (this.animationEncoder != null) {
            this.animationEncoder.setAnimation(animation);
            animator.addAnimatorListener(this.animationEncoder);
        }
        animator.start();
        dialog.show();
    }
}

