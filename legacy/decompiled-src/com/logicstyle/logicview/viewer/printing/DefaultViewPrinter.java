/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.printing;

import com.logicstyle.logicview.model.View;
import com.logicstyle.logicview.viewer.printing.ImagePrinter;
import com.logicstyle.logicview.viewer.printing.ImagePrinterFactory;
import com.logicstyle.logicview.viewer.printing.ViewPrintConstructor;
import com.logicstyle.logicview.viewer.printing.ViewPrintConstructorFactory;
import com.logicstyle.logicview.viewer.printing.ViewPrinter;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
import com.logicstyle.mosaic.interfaces.DependencyException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

public class DefaultViewPrinter
implements ViewPrinter {
    private static ResourceBundle res = ResourceBundle.getBundle(DefaultViewPrinter.class.getName());
    private boolean active = false;
    private ComponentRepository rep;

    public boolean canPrint(View v) {
        return true;
    }

    public void deactivate() {
        this.active = false;
    }

    public String getDescription() {
        return res.getString("description");
    }

    public String getName() {
        return res.getString("name");
    }

    public boolean init(ComponentRepository c) throws DependencyException {
        this.rep = c;
        return true;
    }

    public boolean isActive() {
        return this.active;
    }

    public void print(View v) {
        PrintOptionsFrame pf = new PrintOptionsFrame(this.rep, v);
        pf.setVisible(true);
    }

    private class PrintOptionsFrame
    extends JFrame
    implements Observer {
        private ViewPrintConstructor printConstructor;
        private DestinationPanel destp;

        PrintOptionsFrame(ComponentRepository rep, View v) {
            super(res.getString("PrintOptionsTitle"));
            this.getContentPane().setLayout(new BorderLayout());
            ViewPrintConstructorFactory factory = (ViewPrintConstructorFactory)rep.findComponentByInterface(ViewPrintConstructorFactory.class);
            this.printConstructor = factory.getViewPrintConstructor(v);
            this.destp = new DestinationPanel();
            this.getContentPane().add((Component)this.destp, "North");
            this.getContentPane().add(this.printConstructor.getPrintConstructorComponent(), "Center");
            JButton printButton = new JButton();
            printButton.setText(res.getString("printbuttonlabel"));
            this.getContentPane().add((Component)printButton, "South");
            printButton.addActionListener(new ActionListener(){

                public void actionPerformed(ActionEvent evt) {
                    PrintOptionsFrame.this.printSnapshot();
                }
            });
            this.setSize(640, 480);
        }

        private void printSnapshot() {
            ImagePrinter p = this.destp.getSelectedPrinter();
            Dimension size = p.getPreferredSampleSize();
            BufferedImage out = new BufferedImage(size.width, size.height, 1);
            this.printConstructor.printView(out.getGraphics(), size);
            if (p.printImage(out)) {
                JOptionPane.showMessageDialog(this, res.getString("printOkmessage"), res.getString("printOkmessagetitle"), 1);
            }
        }

        public void update(Observable o, Object arg) {
            this.printConstructor.getPrintConstructorComponent().repaint();
        }

        class DestinationPanel
        extends JPanel {
            JComboBox destinationsComboBox;
            List destinations;
            Component current_config;

            DestinationPanel() {
                this.setLayout(new GridBagLayout());
                GridBagConstraints gc = new GridBagConstraints();
                this.setBorder(new TitledBorder(res.getString("destinationPanelTitle")));
                JLabel destination_label = new JLabel(res.getString("destinationlabel"));
                this.add((Component)destination_label, gc);
                this.destinations = new ArrayList();
                List destination_factories = DefaultViewPrinter.this.rep.findByInterface(ImagePrinterFactory.class);
                this.destinationsComboBox = new JComboBox();
                for (ImagePrinterFactory f : destination_factories) {
                    ImagePrinter im = f.createImagePrinter();
                    this.destinationsComboBox.addItem(im.getDescription());
                    this.destinations.add(im);
                }
                this.add((Component)this.destinationsComboBox, gc);
                gc.fill = 2;
                ImagePrinter first = (ImagePrinter)this.destinations.get(0);
                this.current_config = first.getConfigurationComponent();
                this.add(this.current_config, gc);
                this.destinationsComboBox.addItemListener(new ItemListener(){

                    public void itemStateChanged(ItemEvent evt) {
                        DestinationPanel.this.remove(DestinationPanel.this.current_config);
                        GridBagConstraints gc = new GridBagConstraints();
                        gc.fill = 2;
                        ImagePrinter p = (ImagePrinter)DestinationPanel.this.destinations.get(DestinationPanel.this.destinationsComboBox.getSelectedIndex());
                        DestinationPanel.this.current_config = p.getConfigurationComponent();
                        DestinationPanel.this.add(DestinationPanel.this.current_config, gc);
                    }
                });
            }

            public ImagePrinter getSelectedPrinter() {
                return (ImagePrinter)this.destinations.get(this.destinationsComboBox.getSelectedIndex());
            }
        }
    }
}

