import { ResourceBundle } from '../../../../../../compat/util/ResourceBundle.js';
import { Color } from '../../../../../../compat/awt/Color.js';
import { JDialog, JTabbedPane, JOptionPane, el, button, textField, checkBox } from '../../../../../../compat/swing/Swing.js';
import { ColorBar } from './ColorBar.js';
import { formatDecimal4 } from '../common/Format.js';

const res = await ResourceBundle.getBundle(new URL('./FieldModelVoxellColorGeneratorControl.properties', import.meta.url));

/**
 * The color scale dialog of a field model: shows the color bar and lets the
 * user pick the min/max colors, the number of colors and the value range.
 * Unless "Hold color scale" is checked, the range follows the min and max of
 * the voxels currently displayed every time the surface is rebuilt.
 */
export class ColorControlDialog extends JDialog {
    constructor(owner, parent, minC, maxC, currMod, numC, units) {
        super(parent, false);
        this.owner = owner;
        this.initialized = false;
        this.fixed_scale = false;
        this.colors = [];
        this.colorBar = null;
        this.currentModel = null;
        this.averageValue = 0;
        this.i_numColors = 0;
        this.f_minValue = 0;
        this.f_maxValue = 0;
        this.initComponents();
        this.setMinColor(minC);
        this.o_minColor = minC;
        this.setMaxColor(maxC);
        this.o_maxColor = maxC;
        this.setNumColors(numC);
        this.oi_numColors = numC;
        this.units = units;
        this.setTitle(res.getString('name'));
        this.setSize(300, 420);
    }

    initComponents() {
        this.tabPanel = new JTabbedPane();
        this.colorPanel = el('div', { className: 'lv-colorpanel' });
        this.minValButton = button(res.getString('maxButtonTitle'), () => this.minValButtonActionPerformed(), { className: 'lv-button lv-colorbutton' });
        this.maxValButton = button(res.getString('minButtonTitle'), () => this.maxValButtonActionPerformed(), { className: 'lv-button lv-colorbutton' });
        this.numColors = textField(4);
        this.minVal = textField(8);
        this.maxVal = textField(8);
        this.avgLabel = el('span', {}, '<unknown>');
        this.fixColorScaleCheckBox = checkBox(res.getString('fixedScaleLabel'));
        this.fixColorScaleCheckBox.input.addEventListener('change', () => {
            this.fixed_scale = this.fixColorScaleCheckBox.input.checked;
        });
        this.calculateButton = button(res.getString('calculateTitle'), () => this.computeStats());
        this.applyButton = button(res.getString('previewButtonTitle'), () => this.applyButtonActionPerformed());
        this.applyModelButton = button(res.getString('applyButtonTitle'), () => this.applyModelButtonActionPerformed());
        this.resetButton = button(res.getString('resetButtonTitle'), () => this.resetButtonActionPerformed());
        const row = (label, field) => el('div', { className: 'lv-form-row' }, el('label', {}, label), el('span', {}, field));
        this.propertiesPanel = el('div', { className: 'lv-form' },
            row('Min color:', this.minValButton),
            row('Max color:', this.maxValButton),
            row('Number of colors:', this.numColors),
            row('Min value:', this.minVal),
            row('Max value:', this.maxVal),
            row('Average:', this.avgLabel),
            el('div', { className: 'lv-form-wide' }, this.fixColorScaleCheckBox),
            el('div', { className: 'lv-form-wide' }, this.calculateButton),
            el('div', { className: 'lv-button-row' }, this.applyButton, this.applyModelButton, this.resetButton));
        this.tabPanel.addTab(res.getString('colorTabTitle'), this.colorPanel);
        this.tabPanel.addTab(res.getString('propertiesTabTitle'), this.propertiesPanel);
        this.tabPanel.element.classList.add('lv-fill');
        this.getContentPane().append(this.tabPanel.element);
        this.getContentPane().classList.add('lv-column');
    }

    async applyModelButtonActionPerformed() {
        const before = [this.f_minValue, this.f_maxValue];
        if (!(await this.updateFromFields())) {
            return;
        }
        // Phoenix: a typed range would be replaced by the computed one as soon
        // as the surface is rebuilt, so applying it also holds the scale.
        if (before[0] !== this.f_minValue || before[1] !== this.f_maxValue) {
            this.fixed_scale = true;
            this.fixColorScaleCheckBox.input.checked = true;
        }
        this.computeColorTab();
        this.owner.applyColorScale();
    }

    resetButtonActionPerformed() {
        this.setMinValue(this.of_minValue);
        this.setMaxValue(this.of_maxValue);
        this.setNumColors(this.oi_numColors);
        this.setMinColor(this.o_minColor);
        this.setMaxColor(this.o_maxColor);
    }

    async updateFromFields() {
        const n = Number(this.numColors.value);
        if (!Number.isInteger(n) || n < 2) {
            await JOptionPane.showMessageDialog(this, res.getString('invalidValue'), res.getString('invalidNumColors'), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        this.setNumColors(n);
        let mv = Number(this.maxVal.value);
        if (this.maxVal.value.trim() === '' || Number.isNaN(mv)) {
            await JOptionPane.showMessageDialog(this, res.getString('invalidValue'), res.getString('invalidMaxTitle'), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        const minTyped = Number(this.minVal.value);
        if (mv <= minTyped) {
            await JOptionPane.showMessageDialog(this, res.getString('maxlessThanMin'), res.getString('invalidValue'), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        this.setMaxValue(Math.fround(mv));
        mv = minTyped;
        if (this.minVal.value.trim() === '' || Number.isNaN(mv)) {
            await JOptionPane.showMessageDialog(this, res.getString('invalidValue'), res.getString('invalidMinTitle'), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        this.setMinValue(Math.fround(mv));
        return true;
    }

    async applyButtonActionPerformed() {
        if (!(await this.updateFromFields())) {
            return;
        }
        this.computeColorTab();
        this.tabPanel.setSelectedIndex(0);
    }

    chooseColor(current, set) {
        // JColorChooser.showDialog: the browser's color picker.
        const input = el('input', { type: 'color', value: current.toHex(), className: 'lv-hidden-color' });
        document.body.append(input);
        input.addEventListener('change', () => {
            set(Color.fromHex(input.value));
            input.remove();
        });
        input.addEventListener('blur', () => setTimeout(() => input.remove(), 1000));
        input.click();
    }

    maxValButtonActionPerformed() {
        if (this.initialized) {
            this.chooseColor(this.maxColor, (c) => this.setMaxColor(c));
        }
    }

    minValButtonActionPerformed() {
        if (this.initialized) {
            this.chooseColor(this.minColor, (c) => this.setMinColor(c));
        }
    }

    getMinColor() {
        return this.minColor;
    }

    setMinColor(minColor) {
        this.minColor = minColor;
        this.minValButton.style.background = minColor.toCss();
        this.minValButton.style.color = contrast(minColor);
    }

    getMaxColor() {
        return this.maxColor;
    }

    setMaxColor(maxColor) {
        this.maxColor = maxColor;
        this.maxValButton.style.background = maxColor.toCss();
        this.maxValButton.style.color = contrast(maxColor);
    }

    getNumColors() {
        return this.i_numColors;
    }

    setNumColors(numColors) {
        this.i_numColors = numColors;
        this.numColors.value = String(numColors);
    }

    getMinValue() {
        return this.f_minValue;
    }

    setMinValue(minValue) {
        this.f_minValue = minValue;
        this.minVal.value = String(minValue);
        if (this.initialized && this.colorBar) {
            this.colorBar.setMin(minValue);
        }
    }

    getMaxValue() {
        return this.f_maxValue;
    }

    setMaxValue(maxValue) {
        this.f_maxValue = maxValue;
        this.maxVal.value = String(maxValue);
    }

    computeColorTab() {
        this.colors = new Array(this.i_numColors);
        this.step = (this.f_maxValue - this.f_minValue) / (this.i_numColors - 1);
        for (let i = 0; i < this.i_numColors; ++i) {
            // The original used i itself as the 0..255 coefficient, which is
            // only right for the default 256 colors; scale it to the table size.
            const coef = Math.round(i * 255 / (this.i_numColors - 1));
            this.colors[i] = new Color(this.getIntegerInterpolatedRGB(this.minColor.getRGB(), this.maxColor.getRGB(), coef));
        }
        this.setColorBar(new ColorBar(this.colors, this.step, this.f_minValue, this.units));
    }

    getIntegerInterpolatedRGB(c1, c2, coef) {
        if (coef === 0) {
            return c1;
        }
        if (coef === 255) {
            return c2;
        }
        if (c1 === c2) {
            return c1;
        }
        const c1R = (c1 & 0xFF0000) >>> 16;
        const c1G = (c1 & 0xFF00) >>> 8;
        const c1B = c1 & 0xFF;
        const c2R = (c2 & 0xFF0000) >>> 16;
        const c2G = (c2 & 0xFF00) >>> 8;
        const c2B = c2 & 0xFF;
        const nR = c1R + (((c2R - c1R) * coef) >> 8);
        const nG = c1G + (((c2G - c1G) * coef) >> 8);
        const nB = c1B + (((c2B - c1B) * coef) >> 8);
        return (nR << 16) | (nG << 8) | nB;
    }

    getColorBar() {
        return this.colorBar;
    }

    setColorBar(colorBar) {
        this.colorBar = colorBar;
        this.colorPanel.replaceChildren(colorBar.element);
    }

    getColorTable() {
        return this.colors;
    }

    getCurrentModel() {
        return this.currentModel;
    }

    setCurrentModel(currentModel) {
        this.currentModel = currentModel;
        this.computeStats();
        this.of_minValue = this.f_minValue;
        this.of_maxValue = this.f_maxValue;
        this.computeColorTab();
        this.initialized = true;
    }

    computeStats() {
        if (this.fixed_scale) {
            return;
        }
        let min = 3.4028235e38;
        let max = -3.4028235e38;
        let avg = 0.0;
        let nvox = 0;
        const m = this.currentModel;
        const w = m.getWidth();
        const h = m.getHeight();
        const d = m.getDepth();
        for (let i = 0; i < w; ++i) {
            for (let j = 0; j < h; ++j) {
                for (let k = 0; k < d; ++k) {
                    if (!m.isEnabled(i, j, k)) {
                        continue;
                    }
                    const val = m.getFloatValueAt(i, j, k);
                    if (val < min) {
                        min = val;
                    }
                    if (val > max) {
                        max = val;
                    }
                    avg += val;
                    ++nvox;
                }
            }
        }
        if (nvox === 0) {
            min = 0;
            max = 1;
        } else if (max <= min) {
            max = min + 1;
        }
        this.setMinValue(Math.fround(min));
        this.setMaxValue(Math.fround(max));
        this.setAverageValue(Math.fround(avg / nvox));
    }

    getColorforValue(val) {
        let cindex = Math.trunc((val - this.f_minValue) / this.step);
        if (!(cindex >= 0)) {
            cindex = 0;
        } else if (cindex > this.colors.length - 1) {
            cindex = this.colors.length - 1;
        }
        return this.colors[cindex];
    }

    getAverageValue() {
        return this.averageValue;
    }

    setAverageValue(averageValue) {
        this.averageValue = averageValue;
        this.avgLabel.textContent = Number.isNaN(averageValue) ? '<unknown>' : formatDecimal4(averageValue);
    }

    getPrintPageElement() {
        return this.colorBar.getPrintPageElement();
    }
}

function contrast(c) {
    return c.r * 0.299 + c.g * 0.587 + c.b * 0.114 > 140 ? '#000' : '#fff';
}
