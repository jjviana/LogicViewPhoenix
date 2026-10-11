import { Interface } from '../../../../compat/lang/Interface.js';
import { Component } from '../../mosaic/interfaces/Component.js';

/**
 * A menu action. Every window builds its menu bar from the UIAction
 * components that are "in context" for it (the main window, or a view).
 *
 *   getName(), getActionGroup(), getIcon()
 *   isInContext(context): boolean
 *   execute(context)
 */
export const UIAction = new Interface('com.logicstyle.logicview.UIAction.UIAction', Component);
