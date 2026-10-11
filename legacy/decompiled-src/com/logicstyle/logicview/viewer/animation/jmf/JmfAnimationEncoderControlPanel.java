/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.animation.jmf;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

public class JmfAnimationEncoderControlPanel
extends JPanel {
    private static final ResourceBundle res = ResourceBundle.getBundle(JmfAnimationEncoderControlPanel.class.getName());
    private JFileChooser file_chooser;
    private JButton browseButton;
    private JTextField fileTextField;
    private JLabel jLabel1;

    public JmfAnimationEncoderControlPanel() {
        this.initComponents();
        this.file_chooser = new JFileChooser();
        this.file_chooser.setFileFilter(new FileFilter(){

            public String getDescription() {
                return res.getString("fileChooserDescription");
            }

            public boolean accept(File f) {
                return !f.isDirectory() && f.getName().endsWith(".mov");
            }
        });
        this.fileTextField.setText("anim1.mov");
    }

    private void initComponents() {
        this.jLabel1 = new JLabel();
        this.fileTextField = new JTextField();
        this.browseButton = new JButton();
        this.jLabel1.setText(res.getString("fileLabel"));
        this.add(this.jLabel1);
        this.fileTextField.setColumns(15);
        this.add(this.fileTextField);
        this.browseButton.setText(res.getString("browseLabel"));
        this.browseButton.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent evt) {
                JmfAnimationEncoderControlPanel.this.browseButtonActionPerformed(evt);
            }
        });
        this.add(this.browseButton);
    }

    private void browseButtonActionPerformed(ActionEvent evt) {
        if (this.file_chooser.showOpenDialog(this) == 0) {
            this.fileTextField.setText(this.file_chooser.getSelectedFile().getAbsolutePath());
        }
    }

    public String getOutputFilename() {
        return this.fileTextField.getText();
    }
}

