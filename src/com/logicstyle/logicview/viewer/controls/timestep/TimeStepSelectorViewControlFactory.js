import { ResourceBundle } from '../../../../../../compat/util/ResourceBundle.js';
import { ViewControlFactory } from '../ViewControlFactory.js';
import { InterpolatorFactory } from '../../animation/InterpolatorFactory.js';
import { TransformedVoxellModel } from '../../../model/TransformedVoxellModel.js';
import { FieldModel } from '../../../model/FieldModel.js';
import { TimeStepSelectorVoxelTransformControl } from './TimeStepSelectorVoxelTransformControl.js';

const res = await ResourceBundle.getBundle(new URL('./TimeStepSelectorViewControlFactory.properties', import.meta.url));

export class TimeStepSelectorViewControlFactory {
    static implements = [ViewControlFactory, InterpolatorFactory];

    constructor() {
        this.active = false;
        this.modelMap = new WeakMap();
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
        while (model instanceof TransformedVoxellModel) {
            model = model.getSource();
        }
        if (model instanceof FieldModel) {
            const c = new TimeStepSelectorVoxelTransformControl();
            this.modelMap.set(model, c);
            return c;
        }
        return null;
    }

    init() {
        this.active = true;
        return true;
    }

    isActive() {
        return this.active;
    }

    getInterpolator(v) {
        return this.modelMap.get(v.getModel()) || null;
    }
}
