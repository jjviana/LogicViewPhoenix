/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.controls.value;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ResourceBundle;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

public class ValueSelectionVoxellTransformControlPanel
extends JPanel {
    private static ResourceBundle res = ResourceBundle.getBundle(ValueSelectionVoxellTransformControlPanel.class.getName());
    public static final int MODULE = 0;
    public static final int VALUE = 1;
    public static final int LESS_THAN = 3;
    public static final int GREATER_THAN = 4;
    public static final int PERCENT = 5;
    public static final int ABSOLUTE = 6;
    private JCheckBox activateCheckBox;
    private JTextField deltaTextField;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JComboBox modulecombo;
    private JComboBox relativeCombo;
    private JButton updateButton;
    private JTextField valueTextField;

    public ValueSelectionVoxellTransformControlPanel() {
        this.initComponents();
        this.valueTextField.setText("0.0");
    }

    private void initComponents() {
        this.activateCheckBox = new JCheckBox();
        this.jLabel1 = new JLabel();
        this.modulecombo = new JComboBox();
        this.jLabel2 = new JLabel();
        this.valueTextField = new JTextField();
        this.jLabel3 = new JLabel();
        this.deltaTextField = new JTextField();
        this.relativeCombo = new JComboBox();
        this.updateButton = new JButton();
        this.add(this.activateCheckBox);
        this.jLabel1.setText(res.getString("valueSelectLabel1"));
        this.add(this.jLabel1);
        this.modulecombo.setModel(new DefaultComboBoxModel<String>(new String[]{"value", "module"}));
        this.modulecombo.setLightWeightPopupEnabled(false);
        this.add(this.modulecombo);
        this.jLabel2.setText(res.getString("isLabel"));
        this.add(this.jLabel2);
        this.valueTextField.setColumns(4);
        this.valueTextField.addFocusListener(new FocusAdapter(){

            public void focusLost(FocusEvent evt) {
                ValueSelectionVoxellTransformControlPanel.this.valueTextFieldFocusLost(evt);
            }
        });
        this.add(this.valueTextField);
        this.jLabel3.setText("+/-");
        this.add(this.jLabel3);
        this.deltaTextField.setColumns(4);
        this.add(this.deltaTextField);
        this.relativeCombo.setModel(new DefaultComboBoxModel<String>(new String[]{"%", "absolute"}));
        this.relativeCombo.setLightWeightPopupEnabled(false);
        this.add(this.relativeCombo);
        this.updateButton.setText(res.getString("updateButtonLabel"));
        this.updateButton.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent evt) {
                ValueSelectionVoxellTransformControlPanel.this.updateButtonActionPerformed(evt);
            }
        });
        this.add(this.updateButton);
    }

    private void updateButtonActionPerformed(ActionEvent evt) {
        try {
            Float.parseFloat(this.valueTextField.getText());
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this, res.getString("invalidValueMessage"), res.getString("invaludValueTitle"), 0);
            this.valueTextField.grabFocus();
            return;
        }
        try {
            Float.parseFloat(this.deltaTextField.getText());
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this, res.getString("invalidDeltaValue"), res.getString("invaludValueTitle"), 0);
            this.deltaTextField.grabFocus();
            return;
        }
        this.fireChangeListenerStateChanged();
    }

    private void valueTextFieldFocusLost(FocusEvent evt) {
    }

    public boolean isActive() {
        return this.activateCheckBox.isSelected();
    }

    public int getValueMode() {
        if (this.modulecombo.getSelectedIndex() == 0) {
            return 1;
        }
        return 0;
    }

    public int getValueScope() {
        if (this.relativeCombo.getSelectedIndex() == 0) {
            return 5;
        }
        return 6;
    }

    public float getDelta() {
        return Float.parseFloat(this.deltaTextField.getText());
    }

    public float getValue() {
        return Float.parseFloat(this.valueTextField.getText());
    }

    public synchronized void addChangeListener(ChangeListener listener) {
        if (this.listenerList == null) {
            this.listenerList = new EventListenerList();
        }
        this.listenerList.add(ChangeListener.class, listener);
    }

    public synchronized void removeChangeListener(ChangeListener listener) {
        this.listenerList.remove(ChangeListener.class, listener);
    }

    private void fireChangeListenerStateChanged() {
        ChangeEvent evt = new ChangeEvent(this);
        if (this.listenerList == null) {
            return;
        }
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ChangeListener.class) continue;
            ((ChangeListener)listeners[i + 1]).stateChanged(evt);
        }
    }
}

