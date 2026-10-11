import { Interface } from '../../../../../../compat/lang/Interface.js';
import { ViewControl } from '../ViewControl.js';

/**
 * Builds the geometry displaying a voxel model:
 *   getGeometry(model, progress): Promise<THREE.BufferGeometry | null>
 */
export const VoxellGeometryGenerator = new Interface('com.logicstyle.logicview.viewer.controls.geometry.VoxellGeometryGenerator', ViewControl);
