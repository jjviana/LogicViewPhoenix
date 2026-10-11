/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.printing.file;

import com.logicstyle.logicview.viewer.printing.ImagePrinter;
import com.logicstyle.logicview.viewer.printing.file.ConfigurationDialog;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JOptionPane;

public class FileImagePrinter
implements ImagePrinter {
    private static ResourceBundle res = ResourceBundle.getBundle(FileImagePrinter.class.getName());
    private ConfigurationDialog config = new ConfigurationDialog(null, true);
    private JButton config_component = new JButton();

    public FileImagePrinter() {
        this.config_component.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent evt) {
                FileImagePrinter.this.config.show();
                FileImagePrinter.this.updateConfigComponent();
            }
        });
        this.updateConfigComponent();
    }

    private void updateConfigComponent() {
        Dimension dim = this.config.getSize();
        this.config_component.setText(res.getString("filelabel") + " " + this.config.getFile().getAbsolutePath() + "; " + res.getString("sizelabel") + " " + dim.width + "x" + dim.height);
    }

    public Component getConfigurationComponent() {
        return this.config_component;
    }

    public String getDescription() {
        return res.getString("description");
    }

    public Dimension getPreferredSampleSize() {
        return this.config.getSize();
    }

    public boolean printImage(RenderedImage im) {
        File destination = this.config.getFile();
        boolean fileok = false;
        try {
            fileok = (!destination.exists() || destination.canWrite()) && (destination.exists() || destination.createNewFile());
        }
        catch (IOException e) {
            e.printStackTrace();
            fileok = false;
        }
        if (!fileok) {
            JOptionPane.showMessageDialog(null, res.getString("cantwritefileerror"), res.getString("cantwritefileerrortitle"), 0);
            return false;
        }
        try {
            ImageIO.write(im, this.config.getFormat(), destination);
        }
        catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, res.getString("imageencodeerror"), res.getString("imageencodeerrortitle"), 0);
            return false;
        }
        return true;
    }
}

