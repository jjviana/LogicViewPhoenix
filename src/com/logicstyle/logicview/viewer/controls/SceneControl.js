import { Interface } from '../../../../../compat/lang/Interface.js';
import { ViewControl } from './ViewControl.js';

/**
 * A control acting on the scene graph rather than on the model:
 *   processScene(root, globalTransformGroup, shapeTransformGroup)
 */
export const SceneControl = new Interface('com.logicstyle.logicview.viewer.controls.SceneControl', ViewControl);
