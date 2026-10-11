/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.printing.file;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Iterator;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

public class ConfigurationDialog
extends JDialog {
    private static ResourceBundle res = ResourceBundle.getBundle(ConfigurationDialog.class.getName());
    private File current_file;
    private Dimension current_dimension;
    private JFileChooser file_chooser;
    String[] sufixes = new String[]{"png", "gif", "tiff", "jpg"};
    private JLabel jLabel4;
    private JTextField widthTextField;
    private JTextField fileNameTextField;
    private JLabel jLabel1;
    private JTextField heightTextField;
    private JLabel jLabel3;
    private JLabel jLabel2;
    private JComboBox formatComboBox;
    private JButton okButton;
    private JButton chooseFileButton;

    public ConfigurationDialog(Frame parent, boolean modal) {
        super(parent, modal);
        this.file_chooser = new JFileChooser();
        this.file_chooser.setFileFilter(new FileFilter(){

            public String getDescription() {
                return res.getString("filechooserdescription");
            }

            public boolean accept(File f) {
                return !f.isDirectory();
            }
        });
        this.initComponents();
        this.current_dimension = new Dimension(320, 200);
        this.widthTextField.setText(this.current_dimension.width + "");
        this.heightTextField.setText(this.current_dimension.height + "");
        this.fileNameTextField.setText("snapshot1.png");
        this.current_file = new File("snapshot1.png");
        for (int i = 0; i < this.sufixes.length; ++i) {
            Iterator<ImageWriter> it = ImageIO.getImageWritersBySuffix(this.sufixes[i]);
            if (!it.hasNext()) continue;
            this.formatComboBox.addItem(this.sufixes[i]);
        }
    }

    private void initComponents() {
        this.jLabel1 = new JLabel();
        this.fileNameTextField = new JTextField();
        this.chooseFileButton = new JButton();
        this.jLabel4 = new JLabel();
        this.formatComboBox = new JComboBox();
        this.jLabel2 = new JLabel();
        this.widthTextField = new JTextField();
        this.jLabel3 = new JLabel();
        this.heightTextField = new JTextField();
        this.okButton = new JButton();
        this.getContentPane().setLayout(new GridBagLayout());
        this.setTitle(res.getString("title"));
        this.addWindowListener(new WindowAdapter(){

            public void windowClosing(WindowEvent evt) {
                ConfigurationDialog.this.closeDialog(evt);
            }
        });
        this.jLabel1.setText(res.getString("filenamelabel"));
        this.getContentPane().add((Component)this.jLabel1, new GridBagConstraints());
        this.fileNameTextField.setColumns(20);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = 17;
        this.getContentPane().add((Component)this.fileNameTextField, gridBagConstraints);
        this.chooseFileButton.setText(res.getString("filechooserbuttonlabel"));
        this.chooseFileButton.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent evt) {
                ConfigurationDialog.this.chooseFileButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 0;
        gridBagConstraints.anchor = 17;
        this.getContentPane().add((Component)this.chooseFileButton, gridBagConstraints);
        this.jLabel4.setText(res.getString("fileformatlabel"));
        this.getContentPane().add((Component)this.jLabel4, new GridBagConstraints());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 0;
        gridBagConstraints.anchor = 17;
        this.getContentPane().add((Component)this.formatComboBox, gridBagConstraints);
        this.jLabel2.setText(res.getString("widthlabel"));
        this.getContentPane().add((Component)this.jLabel2, new GridBagConstraints());
        this.widthTextField.setColumns(4);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 0;
        gridBagConstraints.anchor = 17;
        this.getContentPane().add((Component)this.widthTextField, gridBagConstraints);
        this.jLabel3.setText(res.getString("heightlafbel"));
        this.getContentPane().add((Component)this.jLabel3, new GridBagConstraints());
        this.heightTextField.setColumns(4);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 0;
        gridBagConstraints.anchor = 17;
        this.getContentPane().add((Component)this.heightTextField, gridBagConstraints);
        this.okButton.setText(res.getString("okbuttonlabel"));
        this.okButton.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent evt) {
                ConfigurationDialog.this.okButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        this.getContentPane().add((Component)this.okButton, gridBagConstraints);
        this.pack();
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        int height;
        int width;
        try {
            width = Integer.parseInt(this.widthTextField.getText());
        }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, res.getString("invalidwidtherror"), res.getString("invalidwidtherrortitle"), 0);
            return;
        }
        try {
            height = Integer.parseInt(this.heightTextField.getText());
        }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, res.getString("invalidheighterror"), res.getString("invalidheighterrortitle"), 0);
            return;
        }
        this.current_dimension = new Dimension(width, height);
        this.setVisible(false);
    }

    private void chooseFileButtonActionPerformed(ActionEvent evt) {
        if (this.file_chooser.showOpenDialog(this) == 0) {
            this.fileNameTextField.setText(this.file_chooser.getSelectedFile().getAbsolutePath());
            this.current_file = this.file_chooser.getSelectedFile();
        }
    }

    private void closeDialog(WindowEvent evt) {
        this.setVisible(false);
        this.dispose();
    }

    public File getFile() {
        return new File(this.fileNameTextField.getText());
    }

    public Dimension getSize() {
        return this.current_dimension;
    }

    public String getFormat() {
        return (String)this.formatComboBox.getSelectedItem();
    }
}

