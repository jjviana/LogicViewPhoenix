import { System } from '../../../../../../compat/util/Logger.js';
import { ViewControlFactory } from '../ViewControlFactory.js';
import { VoxellTransformControl } from '../VoxellTransformControl.js';
import { FieldModel } from '../../../model/FieldModel.js';

/**
 * "An uuuuugly hack pretending to be a statistics viewer": every time the
 * surface of a field model is rebuilt, prints the min / average / max of the
 * field per material to the debug output.
 */
export class ViewStatistics {
    static implements = [ViewControlFactory];

    constructor() {
        this.vh = new ViewStatsHack();
    }

    deactivate() {
    }

    getDescription() {
        return 'Statistics viewer ugly hack';
    }

    getName() {
        return 'An uuuuugly hack pretending to be a statistics viewer.';
    }

    getViewControl(model) {
        if (model instanceof FieldModel && model.getMaterialModel() != null) {
            return this.vh;
        }
        return null;
    }

    init() {
        return true;
    }

    isActive() {
        return true;
    }
}

export class ViewStatsHack {
    static implements = [VoxellTransformControl];

    addViewControlListener() {
    }

    removeViewControlListener() {
    }

    controlComponentHidden() {
    }

    controlComponentShown() {
    }

    getControlComponent() {
        return null;
    }

    getName() {
        return 'View Statistics';
    }

    isActive() {
        return true;
    }

    processModel(source) {
        if (!(source instanceof FieldModel)) {
            System.err.println("Warn: can't compute stats for model " + source.constructor.name);
            return source;
        }
        const fm = source;
        const mm = fm.getMaterialModel();
        if (mm == null) {
            System.err.println('Warn: material model not found for model ' + source.constructor.name);
            return source;
        }
        const mats = mm.getMaterials();
        const max = new Float64Array(mats.length).fill(-Number.MAX_VALUE);
        const min = new Float64Array(mats.length).fill(Number.MAX_VALUE);
        const avg = new Float64Array(mats.length);
        const nv = new Int32Array(mats.length);
        // Both grids are flat and x major, so they can be walked together.
        const field = fm.grid;
        const codes = mm.getGrid();
        for (let p = 0; p < field.length; ++p) {
            const val = field[p];
            const mat = codes[p] - 1;
            if (val < min[mat]) {
                min[mat] = val;
            }
            if (val > max[mat]) {
                max[mat] = val;
            }
            avg[mat] += val;
            nv[mat]++;
        }
        System.err.println('Model Statistics');
        System.err.println('Material\tMin\tAvg\tMax');
        for (let i = 0; i < mats.length; ++i) {
            if (nv[i] === 0) {
                continue;
            }
            avg[i] /= nv[i];
            System.err.println(mats[i].name + '\t' + min[i] + '\t' + avg[i] + '\t' + max[i]);
        }
        this.lastStatistics = mats.map((m, i) => ({ name: m.name, min: min[i], avg: avg[i], max: max[i], count: nv[i] }));
        return source;
    }
}
