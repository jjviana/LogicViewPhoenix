import { Interface } from '../../../../compat/lang/Interface.js';
import { Color } from '../../../../compat/awt/Color.js';
import { VoxellModel } from './VoxellModel.js';

/**
 * A voxel model whose values are material codes (1..n): getMaterials().
 * Material code c is described by getMaterials()[c - 1].
 */
export const MaterialModel = new Interface('com.logicstyle.logicview.model.MaterialModel', VoxellModel);

/** MaterialModel.Material */
export class Material {
    constructor() {
        this.name = '';
        this.color = Color.white;
        this.alpha = 0.0;
        this.enabled = true;
    }
}

MaterialModel.Material = Material;
