import { ResourceBundle } from '../../../../../../compat/util/ResourceBundle.js';
import { Color } from '../../../../../../compat/awt/Color.js';
import { Dimension } from '../../../../../../compat/awt/Dimension.js';
import { Font } from '../../../../../../compat/awt/Font.js';
import { Graphics } from '../../../../../../compat/awt/Graphics.js';
import { el } from '../../../../../../compat/swing/Swing.js';
import { PrintPageElement } from '../../printing/PrintPageElement.js';
import { formatDecimal4 } from '../common/Format.js';

const res = await ResourceBundle.getBundle(new URL('./ColorBar.properties', import.meta.url));

/**
 * The color scale: a vertical bar of the color table with value labels and the
 * units as title. Drawn on a canvas, and printable as a page element.
 */
export class ColorBar {
    constructor(colors, step, min, title) {
        this.colors = colors;
        this.step = step;
        this.min = min;
        this.title = title;
        this.print_element = null;
        this.leftMargin = 20;
        this.rightMargin = 20;
        this.titleFont = new Font('Dialog', Font.BOLD, 14);
        const titleSize = this.titleFont.getStringBounds(title);
        this.minSize = new Dimension(Math.trunc(titleSize.width + 50.0) + this.leftMargin + this.rightMargin, colors.length);
        this.canvas = el('canvas', { className: 'lv-colorbar' });
        this.element = el('div', { className: 'lv-colorbar-holder' }, this.canvas);
        new ResizeObserver(() => this.repaint()).observe(this.element);
    }

    repaint() {
        const w = this.element.clientWidth;
        const h = this.element.clientHeight;
        if (w === 0 || h === 0) {
            return;
        }
        const dpr = window.devicePixelRatio || 1;
        this.canvas.width = Math.round(w * dpr);
        this.canvas.height = Math.round(h * dpr);
        this.canvas.style.width = w + 'px';
        this.canvas.style.height = h + 'px';
        const ctx = this.canvas.getContext('2d');
        ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
        this.paint(new Graphics(ctx), new Dimension(w, h));
    }

    paint(g2, d) {
        const labels = new Array(this.colors.length + 1);
        g2.setColor(Color.white);
        g2.fillRect(0, 0, d.width, d.height);
        g2.setColor(Color.black);
        g2.setFont(this.titleFont);
        const tr = this.titleFont.getStringBounds(this.getTitle());
        g2.drawString(this.getTitle(), d.width / 2 - Math.trunc(tr.width / 2.0), 30);
        const startLine = Math.trunc(tr.height) + 30;
        const labelFont = new Font('Dialog', Font.BOLD, 14);
        g2.setFont(labelFont);
        let maxWidth = 0;
        let maxHeight = 0;
        for (let i = 0; i < this.colors.length + 1; ++i) {
            const val = this.min + this.step * i;
            labels[i] = formatDecimal4(val);
            const rec = labelFont.getStringBounds(labels[i]);
            if (rec.width > maxWidth) {
                maxWidth = Math.trunc(rec.width);
            }
            if (rec.height > maxHeight) {
                maxHeight = Math.trunc(rec.height);
            }
        }
        maxWidth += 10;
        maxHeight += 10;
        const bottom_margin = maxHeight + 10;
        const incy = (d.height - startLine - bottom_margin) / this.colors.length;
        const numberOfLabels = 8;
        let labelDrawInterval = Math.trunc(this.colors.length / numberOfLabels);
        if (labelDrawInterval === 0) {
            labelDrawInterval = 1;
        }
        for (let i = 0; i < this.colors.length; ++i) {
            g2.setColor(this.colors[i]);
            g2.fillRect(this.leftMargin, Math.trunc((this.colors.length - i) * incy + startLine), d.width - maxWidth - (this.leftMargin + this.rightMargin), Math.ceil(incy));
            if (i !== 0 && (i + 1) % labelDrawInterval !== 0) {
                continue;
            }
            const metrics = labelFont.getLineMetrics(labels[i]);
            g2.setColor(Color.black);
            g2.drawString(labels[i], d.width - maxWidth - this.rightMargin, (this.colors.length - i) * incy + metrics.height / 2.0 + startLine);
        }
    }

    getMin() {
        return this.min;
    }

    setMin(min) {
        this.min = min;
        this.repaint();
    }

    getStep() {
        return this.step;
    }

    setStep(step) {
        this.step = step;
        this.repaint();
    }

    getTitle() {
        return this.title;
    }

    setTitle(title) {
        this.title = title;
    }

    getMinimumSize() {
        return this.minSize;
    }

    getPrintPageElement() {
        if (this.print_element == null) {
            this.print_element = new ColorBarPageElement(this);
        }
        return this.print_element;
    }
}

class ColorBarPageElement {
    static implements = [PrintPageElement];

    constructor(bar) {
        this.bar = bar;
        this.size = new Dimension(100, 300);
    }

    getMinimumSize() {
        return this.bar.minSize;
    }

    getName() {
        return res.getString('PrintElementName');
    }

    getOrientation() {
        return PrintPageElement.PORTRAIT;
    }

    getSize() {
        return this.size;
    }

    getSupportedOrientations() {
        return PrintPageElement.PORTRAIT;
    }

    print(g) {
        this.bar.paint(g, this.size);
    }

    setOrientation() {
    }

    setSize(size) {
        this.size = size;
    }
}
