import { ResourceBundle } from '../../../../../../compat/util/ResourceBundle.js';
import { JOptionPane, el, button, textField, comboBox } from '../../../../../../compat/swing/Swing.js';

const res = await ResourceBundle.getBundle(new URL('./ValueSelectionVoxellTransformControlPanel.properties', import.meta.url));

/** "[x] Display voxels where [value|module] is [v] +/- [d] [%|absolute] [Update]" */
export class ValueSelectionVoxellTransformControlPanel {
    static MODULE = 0;
    static VALUE = 1;
    static LESS_THAN = 3;
    static GREATER_THAN = 4;
    static PERCENT = 5;
    static ABSOLUTE = 6;

    constructor() {
        this.changeListeners = [];
        this.initComponents();
        this.valueTextField.value = '0.0';
    }

    initComponents() {
        this.activateCheckBox = el('input', { type: 'checkbox' });
        this.modulecombo = comboBox(['value', 'module']);
        this.valueTextField = textField(4);
        this.deltaTextField = textField(4);
        this.relativeCombo = comboBox(['%', 'absolute']);
        this.updateButton = button(res.getString('updateButtonLabel'), () => this.updateButtonActionPerformed());
        this.element = el('div', { className: 'lv-flow' },
            el('label', { className: 'lv-checkbox' }, this.activateCheckBox, el('span', {}, res.getString('valueSelectLabel1'))),
            this.modulecombo,
            el('span', {}, res.getString('isLabel')),
            this.valueTextField,
            el('span', {}, '+/-'),
            this.deltaTextField,
            this.relativeCombo,
            this.updateButton);
    }

    async updateButtonActionPerformed() {
        if (!isFloat(this.valueTextField.value)) {
            await JOptionPane.showMessageDialog(this.element, res.getString('invalidValueMessage'), res.getString('invaludValueTitle'), JOptionPane.ERROR_MESSAGE);
            this.valueTextField.focus();
            return;
        }
        if (!isFloat(this.deltaTextField.value)) {
            await JOptionPane.showMessageDialog(this.element, res.getString('invalidDeltaValue'), res.getString('invaludValueTitle'), JOptionPane.ERROR_MESSAGE);
            this.deltaTextField.focus();
            return;
        }
        this.fireChangeListenerStateChanged();
    }

    isActive() {
        return this.activateCheckBox.checked;
    }

    getValueMode() {
        return this.modulecombo.selectedIndex === 0 ? ValueSelectionVoxellTransformControlPanel.VALUE : ValueSelectionVoxellTransformControlPanel.MODULE;
    }

    getValueScope() {
        return this.relativeCombo.selectedIndex === 0 ? ValueSelectionVoxellTransformControlPanel.PERCENT : ValueSelectionVoxellTransformControlPanel.ABSOLUTE;
    }

    getDelta() {
        return Number.parseFloat(this.deltaTextField.value);
    }

    getValue() {
        return Number.parseFloat(this.valueTextField.value);
    }

    addChangeListener(listener) {
        this.changeListeners.push(listener);
    }

    removeChangeListener(listener) {
        this.changeListeners = this.changeListeners.filter((l) => l !== listener);
    }

    fireChangeListenerStateChanged() {
        for (const l of [...this.changeListeners].reverse()) {
            l({ source: this });
        }
    }
}

function isFloat(s) {
    return /^\s*[+-]?(\d+\.?\d*|\.\d+)([eE][+-]?\d+)?[fFdD]?\s*$/.test(s);
}
