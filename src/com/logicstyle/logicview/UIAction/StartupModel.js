import { System } from '../../../../compat/util/Logger.js';
import { ResourceLocation } from '../../../../compat/net/ResourceLocation.js';
import { DependencyException } from '../../mosaic/interfaces/DependencyException.js';
import { Component } from '../../mosaic/interfaces/Component.js';
import { MainWindow } from '../MainWindow/MainWindow.js';
import { ModelLoader } from '../loader/ModelLoader.js';
import { UIAction } from './UIAction.js';
import { LoadModel, SAMPLE_MODELS } from './LoadModel.js';

/**
 * Phoenix addition: opens a model as soon as the application has started,
 * by default the SAR of the SAM head at 1800 MHz.
 *
 *   index.html?model=<url of a .3df/.3dm>   open another model
 *   index.html?model=none                   start with an empty desktop
 *   index.html?...&loader=<type name>       e.g. "Log of Field Models"
 */
export class StartupModel {
    static implements = [Component];

    constructor() {
        this.active = false;
    }

    init(c) {
        const main = c.findComponentByInterface(MainWindow);
        if (main == null) {
            throw new DependencyException(MainWindow);
        }
        const loadModel = c.findAllByInterface(UIAction).find((a) => a instanceof LoadModel);
        if (loadModel == null) {
            throw new DependencyException('LoadModel is required', UIAction);
        }
        const params = new URLSearchParams(window.location.search);
        const model = params.get('model') ?? SAMPLE_MODELS[0].location;
        const loaderName = params.get('loader');
        this.active = true;
        if (model === 'none' || model === '') {
            return true;
        }
        // Run once every component is initialized (init() is still in progress).
        setTimeout(() => {
            const loaders = c.findByInterface(ModelLoader);
            const loader = loaderName
                ? loaders.find((l) => l.getTypeName() === loaderName)
                : loaders.find((l) => model.endsWith(l.getFilenameExtension()));
            if (!loader) {
                System.err.println('StartupModel: no loader for ' + model);
                return;
            }
            this.loading = loadModel.load(main, ResourceLocation.fromURL(model), loader);
        }, 0);
        return true;
    }

    isActive() {
        return this.active;
    }

    deactivate() {
        this.active = false;
    }

    getName() {
        return 'Startup model';
    }

    getDescription() {
        return 'Opens a model when LogicView starts';
    }
}
