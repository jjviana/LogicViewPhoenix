import { ResourceBundle } from '../../../../../../compat/util/ResourceBundle.js';
import { ViewControlFactory } from '../ViewControlFactory.js';
import { InterpolatorFactory } from '../../animation/InterpolatorFactory.js';
import { RotateSceneControl } from './RotateSceneControl.js';

const res = await ResourceBundle.getBundle(new URL('./RotateSceneControlFactory.properties', import.meta.url));

export class RotateSceneControlFactory {
    static implements = [ViewControlFactory, InterpolatorFactory];

    constructor() {
        this.active = false;
        this.rep = null;
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
        const control = new RotateSceneControl();
        this.controlsByModel.set(model, control);
        return control;
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
