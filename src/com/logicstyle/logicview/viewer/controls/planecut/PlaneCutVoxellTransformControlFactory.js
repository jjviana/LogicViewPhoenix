import { ResourceBundle } from '../../../../../../compat/util/ResourceBundle.js';
import { ViewControlFactory } from '../ViewControlFactory.js';
import { InterpolatorFactory } from '../../animation/InterpolatorFactory.js';
import { VoxellModel } from '../../../model/VoxellModel.js';
import { MainWindow } from '../../../MainWindow/MainWindow.js';
import { PlaneCutVoxellTransformControl } from './PlaneCutVoxellTransformControl.js';

const res = await ResourceBundle.getBundle(new URL('./PlaneCutVoxellTransformControlFactory.properties', import.meta.url));

export class PlaneCutVoxellTransformControlFactory {
    static implements = [ViewControlFactory, InterpolatorFactory];

    constructor() {
        this.rep = null;
        this.active = false;
        this.controlsByModel = new Map();
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
        const main = this.rep.findComponentByInterface(MainWindow);
        if (model instanceof VoxellModel) {
            const control = new PlaneCutVoxellTransformControl(main, model);
            this.controlsByModel.set(model, control);
            return control;
        }
        return null;
    }

    init(c) {
        this.rep = c;
        this.active = true;
        return true;
    }

    isActive() {
        return this.active;
    }

    getInterpolator(v) {
        return this.controlsByModel.get(v.getModel()) || null;
    }
}
