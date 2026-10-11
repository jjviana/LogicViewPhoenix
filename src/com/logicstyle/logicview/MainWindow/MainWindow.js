import { Interface } from '../../../../compat/lang/Interface.js';
import { ResourceBundle } from '../../../../compat/util/ResourceBundle.js';
import { Component } from '../../mosaic/interfaces/Component.js';
import { ActionGroup } from '../UIAction/ActionGroup.js';

const res = await ResourceBundle.getBundle(new URL('./MainWindow.properties', import.meta.url));

/**
 * The application window.
 *   addDocView(model, viewers)   shows the views of a freshly loaded model
 *   getParentFrame()
 */
export const MainWindow = new Interface('com.logicstyle.logicview.MainWindow.MainWindow', Component);
MainWindow.res = res;
MainWindow.MODEL_ACTION_GROUP = new ActionGroup(res.getString('model_group_name'));
