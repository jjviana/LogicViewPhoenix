import { Interface } from '../../../../compat/lang/Interface.js';
import { VoxellModel } from './VoxellModel.js';

/** A voxel model derived from another one: getSource(). */
export const TransformedVoxellModel = new Interface('com.logicstyle.logicview.model.TransformedVoxellModel', VoxellModel);
