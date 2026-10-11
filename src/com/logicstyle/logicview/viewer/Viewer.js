import { Interface } from '../../../../compat/lang/Interface.js';
import { Component } from '../../mosaic/interfaces/Component.js';

/**
 * Builds views for the models it supports.
 *
 *   getView(model): View
 *   isModelSupported(model): boolean
 *   addViewConstructionListener(l), removeViewConstructionListener(l)
 *   getViewName()
 */
export const Viewer = new Interface('com.logicstyle.logicview.viewer.Viewer', Component);
