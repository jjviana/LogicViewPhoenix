import { Interface } from '../../../../../compat/lang/Interface.js';

/**
 * Something that changes how a view looks. Each control may contribute a tab
 * to the view's control panel.
 *
 *   getControlComponent(): HTMLElement | null
 *   addViewControlListener(l), removeViewControlListener(l)
 *   isActive(), getName()
 *   controlComponentShown(component), controlComponentHidden(component)
 */
export const ViewControl = new Interface('com.logicstyle.logicview.viewer.controls.ViewControl');
