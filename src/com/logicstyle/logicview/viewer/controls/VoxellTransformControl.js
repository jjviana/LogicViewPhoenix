import { Interface } from '../../../../../compat/lang/Interface.js';
import { ViewControl } from './ViewControl.js';

/**
 * A control that transforms the voxel model before the surface is built:
 *   processModel(source: VoxellModel): VoxellModel
 */
export const VoxellTransformControl = new Interface('com.logicstyle.logicview.viewer.controls.VoxellTransformControl', ViewControl);
