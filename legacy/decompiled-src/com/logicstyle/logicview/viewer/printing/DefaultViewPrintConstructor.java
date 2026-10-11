/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.printing;

import com.logicstyle.logicview.model.View;
import com.logicstyle.logicview.viewer.printing.PrintPageElement;
import com.logicstyle.logicview.viewer.printing.PrintPageElementFactory;
import com.logicstyle.logicview.viewer.printing.ViewPrintConstructor;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

public class DefaultViewPrintConstructor
extends JPanel
implements Observer,
ViewPrintConstructor {
    private static ResourceBundle res = ResourceBundle.getBundle(DefaultViewPrintConstructor.class.getName());
    private PrintPreview preview;
    private ElementsPanel elp;
    private Vector elements;
    private ComponentRepository rep;
    private View view;

    DefaultViewPrintConstructor(ComponentRepository comp, View v) {
        this.rep = comp;
        this.view = v;
        List pe_factories = this.rep.findByInterface(PrintPageElementFactory.class);
        if (pe_factories.size() == 0) {
            throw new RuntimeException(res.getString("noPageElementFactoryExceptionMessage"));
        }
        this.elements = new Vector();
        for (PrintPageElementFactory factory : pe_factories) {
            Collection factoryElements = factory.getPageElements(v);
            if (factoryElements == null) continue;
            for (PrintPageElement page_element : factoryElements) {
                this.elements.add(page_element);
            }
        }
        this.setLayout(new BorderLayout());
        this.elp = new ElementsPanel(this.elements, this);
        this.preview = new PrintPreview(this.elements);
        this.preview.setBorder(new TitledBorder("Preview"));
        this.add((Component)this.preview, "Center");
        this.add((Component)this.elp, "West");
    }

    public void update(Observable o, Object arg) {
        this.preview.repaint();
    }

    public boolean isElementSelected(int i) {
        return this.elp.isElementSelected(i);
    }

    public void printView(Graphics g, Dimension size) {
        int newwidth;
        Dimension min;
        PrintPageElement ppe;
        int i;
        int cur_x = 0;
        int cur_y = 0;
        int ocupX = 0;
        int ocupY = 0;
        int numCenterElements = 0;
        for (int i2 = 0; i2 < this.elements.size(); ++i2) {
            if (!this.isElementSelected(i2)) continue;
            PrintPageElement ppe2 = (PrintPageElement)this.elements.get(i2);
            if (ppe2.getOrientation() == 1) {
                ocupX += ppe2.getMinimumSize().width;
                continue;
            }
            if (ppe2.getOrientation() == 2) {
                ocupY += ppe2.getMinimumSize().height;
                continue;
            }
            ++numCenterElements;
        }
        int remainingWidth = size.width - ocupX;
        int remainingHeight = size.height - ocupY;
        if (numCenterElements > 0 && (remainingWidth < 1 || remainingHeight < 1)) {
            System.err.println("Warn: no room to fit all print page elements! Remaining width=" + remainingWidth + ", remaining height=" + remainingHeight);
            if (remainingWidth < 1) {
                remainingWidth = 1;
            }
            if (remainingHeight < 1) {
                remainingHeight = 1;
            }
        }
        if (numCenterElements > 0) {
            int centerElementWidth = remainingWidth / numCenterElements;
            for (i = 0; i < this.elements.size(); ++i) {
                int newheight;
                if (!this.isElementSelected(i) || (ppe = (PrintPageElement)this.elements.get(i)).getOrientation() != 3) continue;
                min = ppe.getMinimumSize();
                if (min.width > centerElementWidth) {
                    System.err.println("Warn: can't fit element " + ppe.getName() + " width into page.");
                    newwidth = min.width;
                } else {
                    newwidth = centerElementWidth;
                }
                if (min.height > remainingHeight) {
                    System.err.println("Warn: can't fit element " + ppe.getName() + " height into page.");
                    newheight = min.height;
                } else {
                    newheight = remainingHeight;
                }
                ppe.setSize(new Dimension(newwidth, newheight));
                ppe.print(g.create(cur_x, 0, newwidth, newheight));
                cur_x += newwidth;
            }
        }
        int total_center_width = cur_x;
        cur_y = remainingHeight;
        for (i = 0; i < this.elements.size(); ++i) {
            if (!this.isElementSelected(i)) continue;
            ppe = (PrintPageElement)this.elements.get(i);
            if (ppe.getOrientation() == 1) {
                int newheight;
                min = ppe.getMinimumSize();
                if (min.height > size.height) {
                    System.err.println("Warn: min height of element " + ppe.getName() + "  (" + min.height + ")is larger than page height");
                    newheight = min.height;
                } else {
                    newheight = size.height;
                }
                ppe.setSize(new Dimension(min.width, newheight));
                ppe.print(g.create(cur_x, 0, min.width, newheight));
                cur_x += min.width;
                continue;
            }
            if (ppe.getOrientation() != 2) continue;
            min = ppe.getMinimumSize();
            if (min.width > total_center_width) {
                System.err.println("Warn: min width of element " + ppe.getName() + " (" + min.width + ") is larger than available page width");
                newwidth = min.width;
            } else {
                newwidth = total_center_width;
            }
            ppe.setSize(new Dimension(newwidth, min.height));
            ppe.print(g.create(0, cur_y, newwidth, min.height));
            cur_y += min.height;
        }
    }

    public Component getPrintConstructorComponent() {
        return this;
    }

    class PrintPreview
    extends JComponent {
        private Vector elements;
        private Dimension prefered;

        PrintPreview(Vector elements) {
            this.elements = elements;
            this.prefered = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }

        public void paintComponent(Graphics g) {
            DefaultViewPrintConstructor.this.printView(g, this.getSize());
        }

        public Dimension getPreferredSize() {
            return this.prefered;
        }
    }

    class ElementsPanel
    extends JPanel {
        JCheckBox[] element_boxes;
        Observer observer;

        ElementsPanel(Vector elements, Observer obs) {
            this.observer = obs;
            this.setBorder(new TitledBorder(res.getString("ElementsPanelTitle")));
            this.setLayout(new GridBagLayout());
            GridBagConstraints ct = new GridBagConstraints();
            ct.gridwidth = 0;
            ct.anchor = 17;
            int size = elements.size();
            this.element_boxes = new JCheckBox[size];
            for (int i = 0; i < size; ++i) {
                PrintPageElement elem = (PrintPageElement)elements.get(i);
                this.element_boxes[i] = new JCheckBox(elem.getName());
                this.element_boxes[i].setSelected(true);
                this.element_boxes[i].addActionListener(new ActionListener(){

                    public void actionPerformed(ActionEvent evt) {
                        ElementsPanel.this.observer.update(null, null);
                    }
                });
                this.add((Component)this.element_boxes[i], ct);
            }
        }

        public boolean isElementSelected(int i) {
            return this.element_boxes[i].isSelected();
        }
    }

    class ImageContainer
    extends JComponent {
        Image im;

        public ImageContainer(Image i) {
            this.setPreferredSize(new Dimension(i.getWidth(null), i.getHeight(null)));
            this.im = i;
        }

        public void paintComponent(Graphics g) {
            g.drawImage(this.im, 0, 0, null);
        }

        public Dimension getPreferredSize() {
            return new Dimension(this.im.getWidth(null), this.im.getHeight(null));
        }
    }
}

