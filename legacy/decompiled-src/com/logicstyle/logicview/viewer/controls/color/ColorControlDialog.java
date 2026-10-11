/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.controls.color;

import com.logicstyle.logicview.model.VoxellModel;
import com.logicstyle.logicview.viewer.controls.color.ColorBar;
import com.logicstyle.logicview.viewer.controls.color.FieldModelVoxellColorGeneratorControl;
import com.logicstyle.logicview.viewer.printing.PrintPageElement;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ColorControlDialog
extends JDialog {
    private ResourceBundle res = ResourceBundle.getBundle(FieldModelVoxellColorGeneratorControl.class.getName());
    private int i_numColors;
    private int oi_numColors;
    private float f_minValue;
    private float of_minValue;
    private float f_maxValue;
    private float of_maxValue;
    private Color[] colors;
    private boolean initialized = false;
    private String units;
    FieldModelVoxellColorGeneratorControl owner;
    private float step;
    private boolean fixed_scale = false;
    private JLabel jLabel4;
    private JCheckBox fixColorScaleCheckBox;
    private JLabel jLabel1;
    private JLabel jLabel3;
    private JTextField minVal;
    private JLabel jLabel2;
    private JTextField numColors;
    private JPanel propertiesPanel;
    private JTabbedPane tabPanel;
    private JTextField maxVal;
    private JButton calculateButton;
    private JButton resetButton;
    private JLabel avgLabel;
    private JButton applyButton;
    private JButton applyModelButton;
    private JLabel jLabel6;
    private JPanel colorPanel;
    private JButton maxValButton;
    private JButton minValButton;
    private JLabel jLabel5;
    private Color minColor;
    private Color o_minColor;
    private Color maxColor;
    private Color o_maxColor;
    private ColorBar colorBar;
    private VoxellModel currentModel;
    private float averageValue;

    public ColorControlDialog(FieldModelVoxellColorGeneratorControl owner, Frame parent, Color minC, Color maxC, VoxellModel currMod, int numC, String units) {
        super(parent, false);
        this.owner = owner;
        this.initComponents();
        this.setMinColor(minC);
        this.o_minColor = minC;
        this.setMaxColor(maxC);
        this.o_maxColor = maxC;
        this.setNumColors(numC);
        this.oi_numColors = numC;
        this.units = units;
        this.setSize(300, 380);
    }

    private void initComponents() {
        this.tabPanel = new JTabbedPane();
        this.colorPanel = new JPanel();
        this.propertiesPanel = new JPanel();
        this.jLabel1 = new JLabel();
        this.minValButton = new JButton();
        this.jLabel2 = new JLabel();
        this.maxValButton = new JButton();
        this.jLabel3 = new JLabel();
        this.numColors = new JTextField();
        this.jLabel4 = new JLabel();
        this.minVal = new JTextField();
        this.jLabel5 = new JLabel();
        this.maxVal = new JTextField();
        this.jLabel6 = new JLabel();
        this.avgLabel = new JLabel();
        this.fixColorScaleCheckBox = new JCheckBox();
        this.calculateButton = new JButton();
        this.applyButton = new JButton();
        this.applyModelButton = new JButton();
        this.resetButton = new JButton();
        this.addWindowListener(new WindowAdapter(){

            public void windowClosing(WindowEvent evt) {
                ColorControlDialog.this.closeDialog(evt);
            }
        });
        this.tabPanel.setMinimumSize(new Dimension(254, 360));
        this.tabPanel.setPreferredSize(new Dimension(272, 360));
        this.tabPanel.addTab(this.res.getString("colorTabTitle"), this.colorPanel);
        this.propertiesPanel.setLayout(new GridBagLayout());
        this.jLabel1.setText("Min color:");
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = 13;
        this.propertiesPanel.add((Component)this.jLabel1, gridBagConstraints);
        this.minValButton.setFont(new Font("Dialog", 1, 8));
        this.minValButton.setText(this.res.getString("maxButtonTitle"));
        this.minValButton.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent evt) {
                ColorControlDialog.this.minValButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 0;
        this.propertiesPanel.add((Component)this.minValButton, gridBagConstraints);
        this.jLabel2.setText("Max color:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = 13;
        this.propertiesPanel.add((Component)this.jLabel2, gridBagConstraints);
        this.maxValButton.setFont(new Font("Dialog", 1, 8));
        this.maxValButton.setText(this.res.getString("minButtonTitle"));
        this.maxValButton.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent evt) {
                ColorControlDialog.this.maxValButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 0;
        this.propertiesPanel.add((Component)this.maxValButton, gridBagConstraints);
        this.jLabel3.setText("Number of colors:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = 13;
        this.propertiesPanel.add((Component)this.jLabel3, gridBagConstraints);
        this.numColors.setColumns(4);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 0;
        gridBagConstraints.anchor = 17;
        this.propertiesPanel.add((Component)this.numColors, gridBagConstraints);
        this.jLabel4.setText("Min value:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = 13;
        this.propertiesPanel.add((Component)this.jLabel4, gridBagConstraints);
        this.minVal.setColumns(8);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 0;
        gridBagConstraints.anchor = 17;
        this.propertiesPanel.add((Component)this.minVal, gridBagConstraints);
        this.jLabel5.setText("Max value:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = 13;
        this.propertiesPanel.add((Component)this.jLabel5, gridBagConstraints);
        this.maxVal.setColumns(8);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 0;
        gridBagConstraints.anchor = 17;
        this.propertiesPanel.add((Component)this.maxVal, gridBagConstraints);
        this.jLabel6.setText("Average:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = 13;
        this.propertiesPanel.add((Component)this.jLabel6, gridBagConstraints);
        this.avgLabel.setFont(new Font("Dialog", 0, 12));
        this.avgLabel.setText("<unknown>");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 0;
        this.propertiesPanel.add((Component)this.avgLabel, gridBagConstraints);
        this.fixColorScaleCheckBox.setText(this.res.getString("fixedScaleLabel"));
        this.fixColorScaleCheckBox.addChangeListener(new ChangeListener(){

            public void stateChanged(ChangeEvent evt) {
                ColorControlDialog.this.fixColorScaleCheckBoxStateChanged(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 0;
        this.propertiesPanel.add((Component)this.fixColorScaleCheckBox, gridBagConstraints);
        this.calculateButton.setFont(new Font("Dialog", 0, 12));
        this.calculateButton.setText(this.res.getString("calculateTitle"));
        this.calculateButton.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent evt) {
                ColorControlDialog.this.calculateButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 0;
        gridBagConstraints.insets = new Insets(0, 27, 0, 0);
        gridBagConstraints.anchor = 17;
        this.propertiesPanel.add((Component)this.calculateButton, gridBagConstraints);
        this.applyButton.setText(this.res.getString("previewButtonTitle"));
        this.applyButton.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent evt) {
                ColorControlDialog.this.applyButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = 13;
        this.propertiesPanel.add((Component)this.applyButton, gridBagConstraints);
        this.applyModelButton.setText(this.res.getString("applyButtonTitle"));
        this.applyModelButton.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent evt) {
                ColorControlDialog.this.applyModelButtonActionPerformed(evt);
            }
        });
        this.propertiesPanel.add((Component)this.applyModelButton, new GridBagConstraints());
        this.resetButton.setText(this.res.getString("resetButtonTitle"));
        this.resetButton.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent evt) {
                ColorControlDialog.this.resetButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = 17;
        this.propertiesPanel.add((Component)this.resetButton, gridBagConstraints);
        this.tabPanel.addTab(this.res.getString("propertiesTabTitle"), this.propertiesPanel);
        this.getContentPane().add((Component)this.tabPanel, "Center");
    }

    private void fixColorScaleCheckBoxStateChanged(ChangeEvent evt) {
        this.fixed_scale = this.fixColorScaleCheckBox.isSelected();
    }

    private void applyModelButtonActionPerformed(ActionEvent evt) {
        this.updateFromFields();
        this.computeColorTab();
        this.owner.applyColorScale();
    }

    private void calculateButtonActionPerformed(ActionEvent evt) {
        this.computeStats();
    }

    private void resetButtonActionPerformed(ActionEvent evt) {
        this.setMinValue(this.of_minValue);
        this.setMaxValue(this.of_maxValue);
        this.setNumColors(this.oi_numColors);
        this.setMinColor(this.o_minColor);
        this.setMaxColor(this.o_maxColor);
    }

    private void updateFromFields() {
        float mv;
        try {
            this.setNumColors(Integer.valueOf(this.numColors.getText()));
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this, this.res.getString("invalidValue"), this.res.getString("invalidNumColors"), 0);
            return;
        }
        try {
            mv = Float.valueOf(this.maxVal.getText()).floatValue();
            if (mv <= this.getMinValue()) {
                JOptionPane.showMessageDialog(this, this.res.getString("maxlessThanMin"), this.res.getString("invalidValue"), 0);
                return;
            }
            this.setMaxValue(mv);
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this, this.res.getString("invalidValue"), this.res.getString("invalidMaxTitle"), 0);
            return;
        }
        try {
            mv = Float.valueOf(this.minVal.getText()).floatValue();
            if (mv >= this.getMaxValue()) {
                JOptionPane.showMessageDialog(this, this.res.getString("minlessthanMax"), this.res.getString("invalidValue"), 0);
                return;
            }
            this.setMinValue(mv);
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this, this.res.getString("invalidValue"), this.res.getString("invalidMinTitle"), 0);
            return;
        }
    }

    private void applyButtonActionPerformed(ActionEvent evt) {
        this.updateFromFields();
        this.computeColorTab();
        this.tabPanel.setSelectedIndex(0);
    }

    private void maxValButtonActionPerformed(ActionEvent evt) {
        if (!this.initialized) {
            return;
        }
        Color newColor = JColorChooser.showDialog(this, this.res.getString("colorChooserTitle"), this.maxColor);
        this.setMaxColor(newColor);
    }

    private void minValButtonActionPerformed(ActionEvent evt) {
        if (!this.initialized) {
            return;
        }
        Color newColor = JColorChooser.showDialog(this, this.res.getString("colorChooserTitle"), this.minColor);
        this.setMinColor(newColor);
    }

    private void closeDialog(WindowEvent evt) {
        this.setVisible(false);
        this.dispose();
    }

    public Color getMinColor() {
        return this.minColor;
    }

    public void setMinColor(Color minColor) {
        this.minColor = minColor;
        this.minValButton.setBackground(minColor);
    }

    public Color getMaxColor() {
        return this.maxColor;
    }

    public void setMaxColor(Color maxColor) {
        this.maxColor = maxColor;
        this.maxValButton.setBackground(maxColor);
    }

    public int getNumColors() {
        return this.i_numColors;
    }

    public void setNumColors(int numColors) {
        this.i_numColors = numColors;
        this.numColors.setText(String.valueOf(numColors));
    }

    public float getMinValue() {
        return this.f_minValue;
    }

    public void setMinValue(float minValue) {
        this.f_minValue = minValue;
        this.minVal.setText(String.valueOf(minValue));
        if (this.initialized) {
            this.colorBar.setMin(minValue);
        }
    }

    public float getMaxValue() {
        return this.f_maxValue;
    }

    public void setMaxValue(float maxValue) {
        this.f_maxValue = maxValue;
        this.maxVal.setText(String.valueOf(maxValue));
    }

    private void computeColorTab() {
        this.colors = new Color[this.i_numColors];
        this.step = (this.f_maxValue - this.f_minValue) / (float)(this.i_numColors - 1);
        for (int i = 0; i < this.i_numColors; ++i) {
            Color temp;
            int col = this.getIntegerInterpolatedRGB(this.minColor.getRGB(), this.maxColor.getRGB(), i);
            this.colors[i] = temp = new Color(col);
        }
        ColorBar cb = new ColorBar(this.colors, this.step, this.f_minValue, this.units);
        this.setColorBar(cb);
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

    public ColorBar getColorBar() {
        return this.colorBar;
    }

    public void setColorBar(ColorBar colorBar) {
        this.colorBar = colorBar;
        this.colorPanel.removeAll();
        this.colorPanel.setLayout(new BorderLayout());
        this.colorPanel.add((Component)colorBar, "Center");
    }

    public Color[] getColorTable() {
        return this.colors;
    }

    public VoxellModel getCurrentModel() {
        return this.currentModel;
    }

    public void setCurrentModel(VoxellModel currentModel) {
        this.currentModel = currentModel;
        this.computeStats();
        this.of_minValue = this.f_minValue;
        this.of_maxValue = this.f_maxValue;
        this.computeColorTab();
        this.initialized = true;
    }

    private void computeStats() {
        if (this.fixed_scale) {
            return;
        }
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        double avg = 0.0;
        int nvox = 0;
        for (int i = 0; i < this.currentModel.getWidth(); ++i) {
            for (int j = 0; j < this.currentModel.getHeight(); ++j) {
                for (int k = 0; k < this.currentModel.getDepth(); ++k) {
                    if (!this.currentModel.isEnabled(i, j, k)) continue;
                    float val = this.currentModel.getFloatValueAt(i, j, k);
                    if (val < min) {
                        min = val;
                    }
                    if (val > max) {
                        max = val;
                    }
                    avg += (double)val;
                    ++nvox;
                }
            }
        }
        this.setMinValue(min);
        this.setMaxValue(max);
        this.setAverageValue((float)(avg /= (double)nvox));
    }

    public Color getColorforValue(float val) {
        int cindex = (int)((val - this.f_minValue) / this.step);
        if (cindex < 0) {
            cindex = 0;
        } else if (cindex > this.colors.length - 1) {
            cindex = this.colors.length - 1;
        }
        return this.colors[cindex];
    }

    public float getAverageValue() {
        return this.averageValue;
    }

    public void setAverageValue(float averageValue) {
        this.averageValue = averageValue;
        DecimalFormat nf = new DecimalFormat();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(4);
        this.avgLabel.setText(nf.format(averageValue));
    }

    public PrintPageElement getPrintPageElement() {
        return this.colorBar.getPrintPageElement();
    }
}

