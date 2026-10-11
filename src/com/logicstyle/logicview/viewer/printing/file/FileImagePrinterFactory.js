import { ResourceBundle } from '../../../../../../compat/util/ResourceBundle.js';
import { ImagePrinterFactory } from '../ImagePrinterFactory.js';
import { FileImagePrinter } from './FileImagePrinter.js';

const res = await ResourceBundle.getBundle(new URL('./FileImagePrinterFactory.properties', import.meta.url));

export class FileImagePrinterFactory {
    static implements = [ImagePrinterFactory];

    constructor() {
        this.rep = null;
        this.active = false;
    }

    createImagePrinter() {
        return new FileImagePrinter();
    }

    deactivate() {
        this.active = false;
    }

    getDescription() {
        return res.getString('description');
    }

    getName() {
        return res.getString('name');
    }

    init(c) {
        this.rep = c;
        this.active = true;
        return true;
    }

    isActive() {
        return this.active;
    }
}
