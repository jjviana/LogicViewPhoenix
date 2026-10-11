/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.controls.color;

import com.logicstyle.logicview.viewer.printing.PrintPageElement;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import javax.swing.JComponent;

public class ColorBar
extends JComponent {
    private static ResourceBundle res = ResourceBundle.getBundle(ColorBar.class.getName());
    private Color[] colors;
    private float step;
    private float min;
    private String title;
    private ColorBarPageElement print_element;
    Dimension minSize;
    Font titleFont;
    int leftMargin = 20;
    int rightMargin = 20;

    public ColorBar(Color[] colors, float step, float min, String title) {
        this.colors = colors;
        this.step = step;
        this.min = min;
        this.title = title;
        this.setBackground(Color.white);
        this.titleFont = new Font("Dialog", 1, 14);
        FontRenderContext ctx = new FontRenderContext(null, false, true);
        Rectangle2D titleSize = this.titleFont.getStringBounds(title, ctx);
        this.minSize = new Dimension((int)(titleSize.getWidth() + 50.0) + this.leftMargin + this.rightMargin, colors.length);
    }

    public void paint(Graphics g) {
        this.paint(g, this.getSize());
    }

    public void paint(Graphics g, Dimension size) {
        Graphics2D g2 = (Graphics2D)g;
        String[] labels = new String[this.colors.length + 1];
        Dimension d = size;
        g2.setBackground(Color.white);
        g2.setColor(Color.white);
        g2.fillRect(0, 0, d.width, d.height);
        g2.setColor(Color.black);
        DecimalFormat nf = new DecimalFormat();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(4);
        g2.setFont(this.titleFont);
        FontRenderContext ctx = g2.getFontRenderContext();
        Rectangle2D tr = this.titleFont.getStringBounds(this.getTitle(), ctx);
        g2.drawString(this.getTitle(), d.width / 2 - (int)(tr.getWidth() / 2.0), 30);
        int startLine = (int)tr.getHeight() + 30;
        Font labelFont = new Font("Dialog", 1, 14);
        g2.setFont(labelFont);
        int maxWidth = 0;
        int maxHeight = 0;
        for (int i = 0; i < this.colors.length + 1; ++i) {
            float val = this.min + this.step * (float)i;
            labels[i] = nf.format(val);
            Rectangle2D rec = labelFont.getStringBounds(labels[i], ctx);
            if (rec.getWidth() > (double)maxWidth) {
                maxWidth = (int)rec.getWidth();
            }
            if (!(rec.getHeight() > (double)maxHeight)) continue;
            maxHeight = (int)rec.getHeight();
        }
        maxWidth += 10;
        float total = this.step * (float)this.colors.length;
        int bottom_margin = (maxHeight += 10) + 10;
        float incy = ((float)d.height - (float)startLine - (float)bottom_margin) / (float)this.colors.length;
        int numberOfLabels = 8;
        int labelDrawInterval = this.colors.length / numberOfLabels;
        if (labelDrawInterval == 0) {
            labelDrawInterval = 1;
        }
        for (int i = 0; i < this.colors.length; ++i) {
            g2.setColor(this.colors[i]);
            g2.fillRect(this.leftMargin, (int)((float)(this.colors.length - i) * incy + (float)startLine), d.width - maxWidth - (this.leftMargin + this.rightMargin), (int)Math.ceil(incy));
            if (i != 0 && (i + 1) % labelDrawInterval != 0) continue;
            LineMetrics metrics = labelFont.getLineMetrics(labels[i], ctx);
            g2.setColor(Color.black);
            g2.drawString(labels[i], (float)(d.width - maxWidth - this.rightMargin), (float)(this.colors.length - i) * incy + metrics.getHeight() / 2.0f + (float)startLine);
        }
    }

    public float getMin() {
        return this.min;
    }

    public void setMin(float min) {
        this.min = min;
        this.repaint();
    }

    public float getStep() {
        return this.step;
    }

    public void setStep(float step) {
        this.step = step;
        this.repaint();
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Dimension getMinimumSize() {
        return this.minSize;
    }

    public Dimension getPreferredSize() {
        return this.getMinimumSize();
    }

    public PrintPageElement getPrintPageElement() {
        if (this.print_element == null) {
            this.print_element = new ColorBarPageElement();
        }
        return this.print_element;
    }

    class ColorBarPageElement
    implements PrintPageElement {
        Dimension size = new Dimension(100, 300);

        ColorBarPageElement() {
        }

        public Dimension getMinimumSize() {
            return ColorBar.this.minSize;
        }

        public String getName() {
            return res.getString("PrintElementName");
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
            ColorBar.this.paint(g, this.size);
        }

        public void setOrientation(int orientation) {
        }

        public void setSize(Dimension size) {
            this.size = size;
        }
    }
}

