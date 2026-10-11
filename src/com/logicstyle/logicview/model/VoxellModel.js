import { Interface } from '../../../../compat/lang/Interface.js';
import { Model } from './Model.js';

/**
 * A model made of a 3D grid of voxels.
 *
 *   getWidth(), getHeight(), getDepth()        grid size along x, y and z
 *   getDeltaX(), getDeltaY(), getDeltaZ()      voxel size (meters)
 *   getIntValueAt(x, y, z), getFloatValueAt(x, y, z),
 *   getDoubleValueAt(x, y, z), getObjectValueAt(x, y, z)
 *   isEnabled(x, y, z)                         is the voxel to be displayed?
 */
export const VoxellModel = new Interface('com.logicstyle.logicview.model.VoxellModel', Model);
