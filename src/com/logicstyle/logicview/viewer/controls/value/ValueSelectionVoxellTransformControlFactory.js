import { ResourceBundle } from '../../../../../../compat/util/ResourceBundle.js';
import { ViewControlFactory } from '../ViewControlFactory.js';
import { VoxellModel } from '../../../model/VoxellModel.js';
import { TransformedVoxellModel } from '../../../model/TransformedVoxellModel.js';
import { FieldModel } from '../../../model/FieldModel.js';
import { ValueSelectionVoxellTransformControl } from './ValueSelectionVoxellTransformControl.js';

const res = await ResourceBundle.getBundle(new URL('./ValueSelectionVoxellTransformControlFactory.properties', import.meta.url));

/** Field models only. (The original shared a single control between all views.) */
export class ValueSelectionVoxellTransformControlFactory {
    static implements = [ViewControlFactory];

    constructor() {
        this.active = true;
        this.controls = new Map();
    }

    deactivate() {
        this.active = false;
    }

    getDescription() {
        return res.getString('descriptiopn');
    }

    getName() {
        return res.getString('name');
    }

    getViewControl(model) {
        if (!(model instanceof VoxellModel)) {
            return null;
        }
        let m = model;
        while (m instanceof TransformedVoxellModel) {
            m = m.getSource();
        }
        if (m instanceof FieldModel) {
            if (!this.controls.has(m)) {
                this.controls.set(m, new ValueSelectionVoxellTransformControl());
            }
            return this.controls.get(m);
        }
        return null;
    }

    init() {
        return true;
    }

    isActive() {
        return this.active;
    }
}
