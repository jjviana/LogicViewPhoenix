import { ResourceBundle } from '../../../../../../compat/util/ResourceBundle.js';
import { JOptionPane, el, button } from '../../../../../../compat/swing/Swing.js';
import { ImagePrinter } from '../ImagePrinter.js';
import { ConfigurationDialog } from './ConfigurationDialog.js';

const res = await ResourceBundle.getBundle(new URL('./FileImagePrinter.properties', import.meta.url));

/** Saves snapshots as image files (downloaded by the browser). */
export class FileImagePrinter {
    static implements = [ImagePrinter];

    constructor() {
        this.config = new ConfigurationDialog(null, true);
        this.config_component = button('', () => {
            this.config.setVisible(true);
        });
        this.config.listeners.push(() => this.updateConfigComponent());
        this.updateConfigComponent();
    }

    updateConfigComponent() {
        const dim = this.config.getSize();
        this.config_component.textContent = res.getString('filelabel') + ' ' + this.config.getFile() + '; ' + res.getString('sizelabel') + ' ' + dim.width + 'x' + dim.height;
    }

    getConfigurationComponent() {
        return this.config_component;
    }

    getDescription() {
        return res.getString('description');
    }

    getPreferredSampleSize() {
        return this.config.getSize();
    }

    async printImage(canvas) {
        const blob = await new Promise((resolve) => canvas.toBlob(resolve, this.config.getMimeType(), 0.92));
        if (!blob) {
            await JOptionPane.showMessageDialog(null, res.getString('imageencodeerror'), res.getString('imageencodeerrortitle'), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        const a = el('a', { href: URL.createObjectURL(blob), download: this.config.getFile() });
        document.body.append(a);
        a.click();
        a.remove();
        setTimeout(() => URL.revokeObjectURL(a.href), 60000);
        return true;
    }
}
