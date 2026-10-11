import { ResourceBundle } from '../../../../../../compat/util/ResourceBundle.js';
import { Dimension } from '../../../../../../compat/awt/Dimension.js';
import { JDialog, JOptionPane, el, button, textField, comboBox } from '../../../../../../compat/swing/Swing.js';

const res = await ResourceBundle.getBundle(new URL('./ConfigurationDialog.properties', import.meta.url));

const MIME_TYPES = { png: 'image/png', gif: 'image/gif', tiff: 'image/tiff', jpg: 'image/jpeg', webp: 'image/webp' };

/** File name, image format and size of saved snapshots. */
export class ConfigurationDialog extends JDialog {
    constructor(parent, modal) {
        super(parent, modal, { resizable: false });
        // The formats the browser can encode (ImageIO.getImageWritersBySuffix()).
        const probe = document.createElement('canvas');
        probe.width = probe.height = 1;
        this.sufixes = Object.keys(MIME_TYPES).filter((s) => probe.toDataURL(MIME_TYPES[s]).startsWith('data:' + MIME_TYPES[s]));
        this.initComponents();
        this.current_dimension = new Dimension(320, 200);
        this.widthTextField.value = String(this.current_dimension.width);
        this.heightTextField.value = String(this.current_dimension.height);
        this.fileNameTextField.value = 'snapshot1.png';
        this.listeners = [];
    }

    initComponents() {
        this.setTitle(res.getString('title'));
        this.fileNameTextField = textField(20);
        this.formatComboBox = comboBox(this.sufixes);
        this.formatComboBox.addEventListener('change', () => {
            this.fileNameTextField.value = this.fileNameTextField.value.replace(/\.(png|gif|tiff?|jpe?g|webp)$/i, '') + '.' + this.getFormat();
        });
        this.widthTextField = textField(4);
        this.heightTextField = textField(4);
        this.okButton = button(res.getString('okbuttonlabel'), () => this.okButtonActionPerformed());
        const row = (label, field) => el('div', { className: 'lv-form-row' }, el('label', {}, label), el('span', {}, field));
        this.getContentPane().classList.add('lv-padded');
        this.getContentPane().append(
            row(res.getString('filenamelabel'), this.fileNameTextField),
            row(res.getString('fileformatlabel'), this.formatComboBox),
            row(res.getString('widthlabel'), this.widthTextField),
            row(res.getString('heightlafbel'), this.heightTextField),
            el('div', { className: 'lv-button-row' }, this.okButton));
        this.addWindowListener({ windowClosing: () => this.setVisible(false) });
    }

    async okButtonActionPerformed() {
        const width = Number(this.widthTextField.value);
        if (!Number.isInteger(width) || width < 1) {
            await JOptionPane.showMessageDialog(this, res.getString('invalidwidtherror'), res.getString('invalidwidtherrortitle'), JOptionPane.ERROR_MESSAGE);
            return;
        }
        const height = Number(this.heightTextField.value);
        if (!Number.isInteger(height) || height < 1) {
            await JOptionPane.showMessageDialog(this, res.getString('invalidheighterror'), res.getString('invalidheighterrortitle'), JOptionPane.ERROR_MESSAGE);
            return;
        }
        this.current_dimension = new Dimension(width, height);
        this.setVisible(false);
        for (const l of this.listeners) {
            l();
        }
    }

    getFile() {
        return this.fileNameTextField.value;
    }

    getSize() {
        return this.current_dimension;
    }

    getFormat() {
        return this.formatComboBox.value;
    }

    getMimeType() {
        return MIME_TYPES[this.getFormat()];
    }
}
