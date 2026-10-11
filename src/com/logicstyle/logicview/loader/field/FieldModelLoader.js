import { ResourceBundle } from '../../../../../compat/util/ResourceBundle.js';
import { ModelLoader } from '../ModelLoader.js';
import { ModelLoadListenerSupport } from '../LoaderSupport.js';
import { FieldModelImp } from './FieldModelImp.js';

const res = await ResourceBundle.getBundle(new URL('./FieldModelLoader.properties', import.meta.url));

/** Loads field models (.3df). */
export class FieldModelLoader {
    static implements = [ModelLoader];

    constructor() {
        this.active = false;
        this.rep = null;
        this.listenerList = null;
    }

    /** The model class this loader instantiates. */
    getModelClass() {
        return FieldModelImp;
    }

    async LoadModel(location) {
        this.fireModelLoadListenerLoadStarting(this);
        const model = await this.getModelClass().create(location, this.rep, (percent) => this.fireModelLoadListenerLoadingPercentComplete(percent));
        this.fireModelLoadListenerLoadFinished(this);
        return model;
    }

    deactivate() {
        this.active = false;
    }

    getDescription() {
        return res.getString('description');
    }

    getFilenameExtension() {
        return res.getString('fileExtension');
    }

    getName() {
        return res.getString('name');
    }

    getTypeName() {
        return res.getString('typeModel');
    }

    init(c) {
        this.active = true;
        this.rep = c;
        return true;
    }

    isActive() {
        return this.active;
    }
}

Object.assign(FieldModelLoader.prototype, ModelLoadListenerSupport);
