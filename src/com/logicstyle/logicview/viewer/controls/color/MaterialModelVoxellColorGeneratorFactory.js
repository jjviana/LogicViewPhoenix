import { ResourceBundle } from '../../../../../../compat/util/ResourceBundle.js';
import { VoxellColorGeneratorControlFactory } from './VoxellColorGeneratorControlFactory.js';
import { MaterialModelVoxellColorGeneratorControl } from './MaterialModelVoxellColorGeneratorControl.js';
import { TransformedVoxellModel } from '../../../model/TransformedVoxellModel.js';
import { MaterialModel } from '../../../model/MaterialModel.js';

const res = await ResourceBundle.getBundle(new URL('./MaterialModelVoxellColorGeneratorFactory.properties', import.meta.url));

export class MaterialModelVoxellColorGeneratorFactory {
    static implements = [VoxellColorGeneratorControlFactory];

    constructor() {
        this.active = false;
        this.rep = null;
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
        while (m instanceof TransformedVoxellModel) {
            m = m.getSource();
        }
        if (!(m instanceof MaterialModel)) {
            return null;
        }
        return new MaterialModelVoxellColorGeneratorControl();
    }
}
