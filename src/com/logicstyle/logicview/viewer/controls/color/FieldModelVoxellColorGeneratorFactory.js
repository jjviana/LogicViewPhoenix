import { ResourceBundle } from '../../../../../../compat/util/ResourceBundle.js';
import { VoxellColorGeneratorControlFactory } from './VoxellColorGeneratorControlFactory.js';
import { PrintPageElementFactory } from '../../printing/PrintPageElementFactory.js';
import { FieldModelVoxellColorGeneratorControl } from './FieldModelVoxellColorGeneratorControl.js';
import { TransformedVoxellModel } from '../../../model/TransformedVoxellModel.js';
import { FieldModel } from '../../../model/FieldModel.js';
import { MainWindow } from '../../../MainWindow/MainWindow.js';

const res = await ResourceBundle.getBundle(new URL('./FieldModelVoxellColorGeneratorFactory.properties', import.meta.url));

/**
 * One color control per field model, shared by every request for it (the
 * geometry generator asks again, with the transformed model, each time it
 * rebuilds the surface, which refreshes the color range).
 */
export class FieldModelVoxellColorGeneratorFactory {
    static implements = [VoxellColorGeneratorControlFactory, PrintPageElementFactory];

    constructor() {
        this.active = false;
        this.rep = null;
        this.control_map = new Map();
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

    getViewControl(m) {
        const orig = m;
        while (m instanceof TransformedVoxellModel) {
            m = m.getSource();
        }
        let c = this.control_map.get(m);
        if (c != null) {
            c.setModel(orig);
            return c;
        }
        if (m instanceof FieldModel) {
            const w = this.rep.findComponentByInterface(MainWindow);
            c = new FieldModelVoxellColorGeneratorControl(orig, m, w);
            this.control_map.set(m, c);
            return c;
        }
        return null;
    }

    getPageElements(v) {
        let m = v.getModel();
        while (m instanceof TransformedVoxellModel) {
            m = m.getSource();
        }
        const c = this.control_map.get(m);
        if (c == null) {
            return null;
        }
        return [c.getPrintPageElement()];
    }
}
