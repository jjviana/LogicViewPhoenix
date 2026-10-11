import { ResourceBundle } from '../../../../../../compat/util/ResourceBundle.js';
import { Color } from '../../../../../../compat/awt/Color.js';
import { Dimension } from '../../../../../../compat/awt/Dimension.js';
import { Font } from '../../../../../../compat/awt/Font.js';
import { Graphics } from '../../../../../../compat/awt/Graphics.js';
import { el } from '../../../../../../compat/swing/Swing.js';
import { PrintPageElement } from '../../printing/PrintPageElement.js';

const res = await ResourceBundle.getBundle(new URL('./MaterialsPanel.properties', import.meta.url));

const ICON_SIZE = 16;

/** One check box per material, with a swatch of its color. */
export class MaterialsPanel {
    constructor(m, c) {
        this.model = m;
        this.parent = c;
        this.page_element = null;
        const mat = this.model.getMaterials();
        this.element = el('div', { className: 'lv-materials' });
        for (let i = 0; i < mat.length; ++i) {
            const input = el('input', { type: 'checkbox', checked: mat[i].enabled });
            const icon = el('canvas', { width: ICON_SIZE, height: ICON_SIZE, className: 'lv-material-icon' });
            const paintIcon = () => {
                const g = new Graphics(icon.getContext('2d'));
                g.ctx.clearRect(0, 0, ICON_SIZE, ICON_SIZE);
                this.drawIcon(mat[i].color, input.checked, g, 0, 0, 15);
            };
            input.addEventListener('change', () => {
                this.parent.setMaterialState(i, input.checked);
                paintIcon();
            });
            paintIcon();
            this.element.append(el('label', { className: 'lv-material' }, input, icon, el('span', {}, mat[i].name)));
        }
    }

    drawIcon(c, selected, g, x, y, size = 15) {
        g.setColor(c);
        g.fillRect(x, y, size, size);
        g.setColor(Color.black);
        g.setStroke(1);
        g.drawRect(x, y, size, size);
        if (selected) {
            g.setColor(c.equals(Color.black) ? Color.white : Color.black);
            g.setStroke(2.0);
            g.drawLine(x, y + 10, x + 5, y + 15);
            g.drawLine(x + 5, y + 15, x + 15, y + 3);
            g.setStroke(1);
        }
    }

    getPageElement() {
        if (this.page_element == null) {
            this.page_element = new MaterialsPageElement(this);
        }
        return this.page_element;
    }
}

const BORDER_SIZE = 20;
const INTERNAL_SPACING = 4;
const TITLE_SPACING = 10;

/** The material legend of printed snapshots. */
class MaterialsPageElement {
    static implements = [PrintPageElement];

    constructor(panel) {
        this.panel = panel;
        this.size = null;
        this.labelFont = new Font('Dialog', Font.BOLD, 14);
        this.titleFont = new Font('Dialog', Font.BOLD, 18);
        this.title = res.getString('printPageElementTitle');
        this.titleDimension = this.titleFont.getStringBounds(this.title);
        this.titleAscent = this.titleFont.getLineMetrics(this.title).ascent;
        let minWidth = 0.0;
        let minHeight = Math.trunc(this.titleDimension.height) + 20;
        for (const m of panel.model.getMaterials()) {
            const ret = this.labelFont.getStringBounds(m.name);
            if (minWidth < ret.width) {
                minWidth = ret.width;
            }
            minHeight += ret.height < 16.0 ? 16.0 + INTERNAL_SPACING : ret.height;
        }
        minWidth = minWidth + 16.0 + 2 * BORDER_SIZE + INTERNAL_SPACING;
        minHeight += 2 * BORDER_SIZE;
        this.minimumSize = new Dimension(Math.floor(minWidth), Math.floor(minHeight));
    }

    getMinimumSize() {
        return this.minimumSize;
    }

    getName() {
        return res.getString('printPageElementName');
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
        g.setColor(Color.white);
        g.fillRect(0, 0, this.size.width, this.size.height);
        let cur_y = Math.trunc(this.size.height / 2) - Math.trunc(this.minimumSize.height / 2);
        g.setColor(Color.black);
        const title_x = Math.trunc(this.size.width / 2) - Math.trunc(this.titleDimension.width / 2);
        g.setFont(this.titleFont);
        g.drawString(this.title, title_x, cur_y + Math.trunc(this.titleAscent));
        const cur_x = BORDER_SIZE;
        cur_y = cur_y + Math.trunc(this.titleDimension.height) + TITLE_SPACING;
        g.setFont(this.labelFont);
        const mat = this.panel.model.getMaterials();
        for (let i = 0; i < mat.length; ++i) {
            this.panel.drawIcon(mat[i].color, this.panel.parent.getMaterialState(i), g, cur_x, cur_y);
            const metrics = this.labelFont.getLineMetrics(mat[i].name);
            g.setColor(Color.black);
            g.setFont(this.labelFont);
            g.drawString(mat[i].name, cur_x + 16 + INTERNAL_SPACING, cur_y + Math.trunc(metrics.ascent));
            cur_y = metrics.height > 16.0 ? cur_y + Math.trunc(metrics.height) + INTERNAL_SPACING : cur_y + 16 + INTERNAL_SPACING;
        }
    }

    setOrientation() {
    }

    setSize(size) {
        this.size = size;
    }
}
