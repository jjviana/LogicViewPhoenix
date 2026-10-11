import { Interface } from '../../../../compat/lang/Interface.js';
import { Component } from '../../mosaic/interfaces/Component.js';
import { ActionGroup } from '../UIAction/ActionGroup.js';

/**
 * Decides how the views of a model are presented.
 *   addViews(model, viewers, mainWindow)
 *   addViewPresentationListener(l), removeViewPresentationListener(l)
 */
export const ViewPresenter = new Interface('com.logicstyle.logicview.MainWindow.ViewPresenter', Component);
ViewPresenter.VIEW_ACTION_GROUP = new ActionGroup('View');
