import { ResourceBundle } from '../../../../../compat/util/ResourceBundle.js';
import { Dimension } from '../../../../../compat/awt/Dimension.js';
import { Graphics } from '../../../../../compat/awt/Graphics.js';
import { System } from '../../../../../compat/util/Logger.js';
import { el, titledPanel, checkBox } from '../../../../../compat/swing/Swing.js';
import { ViewPrintConstructor } from './ViewPrintConstructor.js';
import { PrintPageElementFactory } from './PrintPageElementFactory.js';
import { PrintPageElement } from './PrintPageElement.js';

const res = await ResourceBundle.getBundle(new URL('./DefaultViewPrintConstructor.properties', import.meta.url));

/**
 * Lays out the page elements of a view (from every PrintPageElementFactory)
 * on a page: CENTER elements side by side on the left, PORTRAIT elements
 * stacked to their right at full height, LANDSCAPE elements below. Also the
 * "Elements" check boxes and the live preview shown in the snapshot and
 * animation dialogs.
 */
export class DefaultViewPrintConstructor {
    static implements = [ViewPrintConstructor];

    constructor(comp, v) {
        this.rep = comp;
        this.view = v;
        const pe_factories = this.rep.findByInterface(PrintPageElementFactory);
        if (pe_factories.length === 0) {
            throw new Error(res.getString('noPageElementFactoryExceptionMessage'));
        }
        this.elements = [];
        for (const factory of pe_factories) {
            const factoryElements = factory.getPageElements(v);
            if (factoryElements == null) {
                continue;
            }
            for (const page_element of factoryElements) {
                this.elements.push(page_element);
            }
        }
        this.elp = new ElementsPanel(this.elements, this);
        this.preview = new PrintPreview(this);
        this.element = el('div', { className: 'lv-printconstructor' }, this.elp.element, this.preview.element);
    }

    update() {
        this.preview.repaint();
    }

    isElementSelected(i) {
        return this.elp.isElementSelected(i);
    }

    async printView(g, size) {
        let cur_x = 0;
        let ocupX = 0;
        let ocupY = 0;
        let numCenterElements = 0;
        for (let i = 0; i < this.elements.length; ++i) {
            if (!this.isElementSelected(i)) {
                continue;
            }
            const ppe = this.elements[i];
            if (ppe.getOrientation() === PrintPageElement.PORTRAIT) {
                ocupX += ppe.getMinimumSize().width;
            } else if (ppe.getOrientation() === PrintPageElement.LANDSCAPE) {
                ocupY += ppe.getMinimumSize().height;
            } else {
                ++numCenterElements;
            }
        }
        let remainingWidth = size.width - ocupX;
        let remainingHeight = size.height - ocupY;
        if (numCenterElements > 0 && (remainingWidth < 1 || remainingHeight < 1)) {
            System.err.println('Warn: no room to fit all print page elements! Remaining width=' + remainingWidth + ', remaining height=' + remainingHeight);
            remainingWidth = Math.max(remainingWidth, 1);
            remainingHeight = Math.max(remainingHeight, 1);
        }
        const printElement = async (ppe, x, y, w, h) => {
            ppe.setSize(new Dimension(w, h));
            const sub = g.create(x, y, w, h);
            try {
                await ppe.print(sub);
            } finally {
                sub.dispose();
            }
        };
        if (numCenterElements > 0) {
            const centerElementWidth = Math.trunc(remainingWidth / numCenterElements);
            for (let i = 0; i < this.elements.length; ++i) {
                const ppe = this.elements[i];
                if (!this.isElementSelected(i) || ppe.getOrientation() !== PrintPageElement.CENTER) {
                    continue;
                }
                const min = ppe.getMinimumSize();
                let newwidth = centerElementWidth;
                let newheight = remainingHeight;
                if (min.width > centerElementWidth) {
                    System.err.println("Warn: can't fit element " + ppe.getName() + ' width into page.');
                    newwidth = min.width;
                }
                if (min.height > remainingHeight) {
                    System.err.println("Warn: can't fit element " + ppe.getName() + ' height into page.');
                    newheight = min.height;
                }
                await printElement(ppe, cur_x, 0, newwidth, newheight);
                cur_x += newwidth;
            }
        }
        const total_center_width = cur_x;
        let cur_y = remainingHeight;
        for (let i = 0; i < this.elements.length; ++i) {
            if (!this.isElementSelected(i)) {
                continue;
            }
            const ppe = this.elements[i];
            const min = ppe.getMinimumSize();
            if (ppe.getOrientation() === PrintPageElement.PORTRAIT) {
                let newheight = size.height;
                if (min.height > size.height) {
                    System.err.println('Warn: min height of element ' + ppe.getName() + '  (' + min.height + ')is larger than page height');
                    newheight = min.height;
                }
                await printElement(ppe, cur_x, 0, min.width, newheight);
                cur_x += min.width;
            } else if (ppe.getOrientation() === PrintPageElement.LANDSCAPE) {
                let newwidth = total_center_width;
                if (min.width > total_center_width) {
                    System.err.println('Warn: min width of element ' + ppe.getName() + ' (' + min.width + ') is larger than available page width');
                    newwidth = min.width;
                }
                await printElement(ppe, 0, cur_y, newwidth, min.height);
                cur_y += min.height;
            }
        }
    }

    getPrintConstructorComponent() {
        return this.element;
    }
}

class PrintPreview {
    constructor(owner) {
        this.owner = owner;
        this.canvas = el('canvas', { className: 'lv-preview-canvas' });
        this.holder = el('div', { className: 'lv-preview-holder' }, this.canvas);
        this.element = titledPanel(res.getString('previewLabel'), this.holder);
        this.element.classList.add('lv-preview');
        this.painting = false;
        this.again = false;
        new ResizeObserver(() => this.repaint()).observe(this.holder);
        this.element.addEventListener('pointerenter', () => this.repaint());
    }

    async repaint() {
        if (this.painting) {
            this.again = true;
            return;
        }
        const w = this.holder.clientWidth;
        const h = this.holder.clientHeight;
        if (w < 2 || h < 2) {
            return;
        }
        this.painting = true;
        try {
            const off = document.createElement('canvas');
            off.width = w;
            off.height = h;
            const ctx = off.getContext('2d');
            ctx.fillStyle = '#fff';
            ctx.fillRect(0, 0, w, h);
            await this.owner.printView(new Graphics(ctx), new Dimension(w, h));
            this.canvas.width = w;
            this.canvas.height = h;
            this.canvas.getContext('2d').drawImage(off, 0, 0);
        } catch (e) {
            System.err.printStackTrace(e);
        } finally {
            this.painting = false;
        }
        if (this.again) {
            this.again = false;
            this.repaint();
        }
    }
}

class ElementsPanel {
    constructor(elements, observer) {
        this.observer = observer;
        this.element_boxes = elements.map((elem) => {
            const box = checkBox(elem.getName(), true);
            box.input.addEventListener('change', () => this.observer.update());
            return box;
        });
        this.element = titledPanel(res.getString('ElementsPanelTitle'), ...this.element_boxes.map((b) => el('div', {}, b)));
        this.element.classList.add('lv-elements');
    }

    isElementSelected(i) {
        return this.element_boxes[i].input.checked;
    }
}
