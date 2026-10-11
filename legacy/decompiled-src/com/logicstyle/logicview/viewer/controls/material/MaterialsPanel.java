/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.controls.material;

import com.logicstyle.logicview.model.MaterialModel;
import com.logicstyle.logicview.viewer.controls.material.MaterialSelectionVoxellTransformControl;
import com.logicstyle.logicview.viewer.printing.PrintPageElement;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class MaterialsPanel
extends JPanel {
    private static ResourceBundle res = ResourceBundle.getBundle(MaterialsPanel.class.getName());
    private MaterialModel model;
    private HashMap matmap;
    private MaterialSelectionVoxellTransformControl parent;
    private static final int ICON_SIZE = 16;
    private MaterialsPageElement page_element;

    public MaterialsPanel(MaterialModel m, MaterialSelectionVoxellTransformControl c) {
        this.model = m;
        this.parent = c;
        this.matmap = new HashMap();
        MaterialModel.Material[] mat = this.model.getMaterials();
        this.setLayout(new GridLayout(mat.length, 1));
        for (int i = 0; i < mat.length; ++i) {
            JCheckBox button = new JCheckBox(mat[i].name, this.createSolidIcon(mat[i].color, false));
            button.setBackground(Color.white);
            button.setSelectedIcon(this.createSolidIcon(mat[i].color, true));
            button.setSelected(mat[i].enabled);
            this.matmap.put(button, new Integer(i));
            button.addItemListener(new ItemListener(){

                public void itemStateChanged(ItemEvent evt) {
                    Integer ind = (Integer)MaterialsPanel.this.matmap.get(evt.getSource());
                    if (ind == null) {
                        return;
                    }
                    MaterialsPanel.this.parent.setMaterialState(ind, evt.getStateChange() == 1);
                }
            });
            this.add(button);
        }
        this.setBackground(Color.white);
    }

    private void drawIcon(Color c, boolean selected, Graphics gr, int x, int y) {
        Graphics2D g = (Graphics2D)gr;
        g.setColor(c);
        g.fillRect(x, y, 15, 15);
        g.setColor(Color.black);
        g.drawRect(x, y, 15, 15);
        g.setColor(c);
        if (selected) {
            if (c.equals(Color.black)) {
                g.setColor(Color.white);
            } else {
                g.setColor(Color.black);
            }
            g.setStroke(new BasicStroke(2.0f));
            g.drawLine(x, y + 10, x + 5, y + 15);
            g.drawLine(x + 5, y + 15, x + 15, y + 3);
        }
    }

    private Icon createSolidIcon(Color c, boolean selected) {
        BufferedImage img = new BufferedImage(16, 16, 1);
        Graphics2D g = (Graphics2D)img.getGraphics();
        this.drawIcon(c, selected, g, 0, 0);
        ImageIcon icon = new ImageIcon(img);
        return icon;
    }

    public PrintPageElement getPageElement() {
        if (this.page_element == null) {
            this.page_element = new MaterialsPageElement();
        }
        return this.page_element;
    }

    class MaterialsPageElement
    implements PrintPageElement {
        private Dimension minimumSize;
        private Dimension size;
        private Font labelFont = new Font("Dialog", 1, 14);
        private Font titleFont = new Font("Dialog", 1, 18);
        private FontRenderContext ctx = new FontRenderContext(null, false, true);
        private String title = MaterialsPanel.access$000().getString("printPageElementTitle");
        private Rectangle2D titleDimension = this.titleFont.getStringBounds(this.title, this.ctx);
        private float titleAscent;
        private static final int BORDER_SIZE = 20;
        private static final int INTERNAL_SPACING = 4;
        private static final int TITLE_SPACING = 10;

        MaterialsPageElement() {
            LineMetrics titleMetrics = this.titleFont.getLineMetrics(this.title, this.ctx);
            this.titleAscent = titleMetrics.getAscent();
            double minWidth = 0.0;
            double minHeight = (int)this.titleDimension.getHeight() + 20;
            MaterialModel.Material[] mat = MaterialsPanel.this.model.getMaterials();
            for (int i = 0; i < mat.length; ++i) {
                Rectangle2D ret = this.labelFont.getStringBounds(mat[i].name, this.ctx);
                if (minWidth < ret.getWidth()) {
                    minWidth = ret.getWidth();
                }
                if (ret.getHeight() < 16.0) {
                    minHeight = minHeight + 16.0 + 4.0;
                    continue;
                }
                minHeight += ret.getHeight();
            }
            minWidth = minWidth + 16.0 + 40.0 + 4.0;
            this.minimumSize = new Dimension((int)Math.floor(minWidth), (int)Math.floor(minHeight += 40.0));
        }

        public Dimension getMinimumSize() {
            return this.minimumSize;
        }

        public String getName() {
            return res.getString("printPageElementName");
        }

        public int getOrientation() {
            return 1;
        }

        public Dimension getSize() {
            return this.size;
        }

        public int getSupportedOrientations() {
            return 1;
        }

        public void print(Graphics g) {
            g.setColor(Color.white);
            g.fillRect(0, 0, this.size.width, this.size.height);
            int cur_y = this.size.height / 2 - this.minimumSize.height / 2;
            g.setColor(Color.black);
            int title_x = this.size.width / 2 - (int)this.titleDimension.getWidth() / 2;
            g.setFont(this.titleFont);
            g.drawString(this.title, title_x, cur_y + (int)this.titleAscent);
            int cur_x = 20;
            cur_y = cur_y + (int)this.titleDimension.getHeight() + 10;
            g.setFont(this.labelFont);
            MaterialModel.Material[] mat = MaterialsPanel.this.model.getMaterials();
            for (int i = 0; i < mat.length; ++i) {
                MaterialsPanel.this.drawIcon(mat[i].color, MaterialsPanel.this.parent.getMaterialState(i), g, cur_x, cur_y);
                LineMetrics metrics = this.labelFont.getLineMetrics(mat[i].name, this.ctx);
                g.setColor(Color.black);
                g.drawString(mat[i].name, cur_x + 16 + 4, cur_y + (int)metrics.getAscent());
                cur_y = metrics.getHeight() > 16.0f ? cur_y + (int)metrics.getHeight() + 4 : cur_y + 16 + 4;
            }
        }

        public void setOrientation(int orientation) {
        }

        public void setSize(Dimension size) {
            this.size = size;
        }
    }
}

