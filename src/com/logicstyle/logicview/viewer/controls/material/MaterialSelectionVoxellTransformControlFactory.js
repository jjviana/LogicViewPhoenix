import { ResourceBundle } from '../../../../../../compat/util/ResourceBundle.js';
import { ViewControlFactory } from '../ViewControlFactory.js';
import { PrintPageElementFactory } from '../../printing/PrintPageElementFactory.js';
import { VoxellModel } from '../../../model/VoxellModel.js';
import { TransformedVoxellModel } from '../../../model/TransformedVoxellModel.js';
import { MaterialModel } from '../../../model/MaterialModel.js';
import { Materialized } from '../../../model/Materialized.js';
import { MaterialSelectionVoxellTransformControl } from './MaterialSelectionVoxellTransformControl.js';

const res = await ResourceBundle.getBundle(new URL('./MaterialSelectionVoxellTransformControlFactory.properties', import.meta.url));

export class MaterialSelectionVoxellTransformControlFactory {
    static implements = [ViewControlFactory, PrintPageElementFactory];

    constructor() {
        this.rep = null;
        this.active = false;
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

    getViewControl(model) {
        if (!(model instanceof VoxellModel)) {
            return null;
        }
        let m = model;
        while (m instanceof TransformedVoxellModel) {
            m = m.getSource();
        }
        if (!(m instanceof MaterialModel)) {
            if (m instanceof Materialized) {
                m = m.getMaterialModel();
                if (m == null) {
                    return null;
                }
            } else {
                return null;
            }
        }
        let c = this.control_map.get(m);
        if (c == null) {
            c = new MaterialSelectionVoxellTransformControl(m, this.rep);
            this.control_map.set(m, c);
        }
        return c;
    }

    init(c) {
        this.rep = c;
        this.active = true;
        return true;
    }

    isActive() {
        return this.active;
    }

    getPageElements(v) {
        let m = v.getModel();
        while (m instanceof TransformedVoxellModel) {
            m = m.getSource();
        }
        if (!(m instanceof MaterialModel) && m instanceof Materialized) {
            m = m.getMaterialModel();
        }
        const c = this.control_map.get(m);
        if (c == null) {
            return null;
        }
        return [c.getPrintPageElement()];
    }
}
