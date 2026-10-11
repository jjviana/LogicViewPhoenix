import { Interface } from '../../../../compat/lang/Interface.js';
import { Component } from '../../mosaic/interfaces/Component.js';

/**
 * Loads a model from a location.
 *
 *   LoadModel(location): Promise<Model>   (asynchronous in the port)
 *   getTypeName(), getFilenameExtension()
 *   addModelLoadListener(l), removeModelLoadListener(l)
 */
export const ModelLoader = new Interface('com.logicstyle.logicview.loader.ModelLoader', Component);
