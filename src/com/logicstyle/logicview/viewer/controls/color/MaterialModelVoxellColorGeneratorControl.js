import { ResourceBundle } from '../../../../../../compat/util/ResourceBundle.js';
import { VoxellColorGeneratorControl } from './VoxellColorGeneratorControl.js';
import { TransformedVoxellModel } from '../../../model/TransformedVoxellModel.js';

const res = await ResourceBundle.getBundle(new URL('./MaterialModelVoxellColorGeneratorControl.properties', import.meta.url));

/** Colors each voxel with the color of its material. */
export class MaterialModelVoxellColorGeneratorControl {
    static implements = [VoxellColorGeneratorControl];

    addViewControlListener() {
    }

    removeViewControlListener() {
    }

    controlComponentHidden() {
    }

    controlComponentShown() {
    }

    getColorForPoint(x, y, z, model) {
        const orig = model;
        while (model instanceof TransformedVoxellModel) {
            model = model.getSource();
        }
        const mat = model.getMaterials();
        return mat[orig.getIntValueAt(x, y, z) - 1].color;
    }

    getControlComponent() {
        return null;
    }

    getName() {
        return res.getString('name');
    }

    isActive() {
        return true;
    }
}
