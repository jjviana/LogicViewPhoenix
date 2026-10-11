import { ResourceBundle } from '../../../../../compat/util/ResourceBundle.js';
import { EventListenerList } from '../../../../../compat/util/EventListenerList.js';
import { Viewer } from '../Viewer.js';
import { ViewConstructionListener } from '../ViewConstructionListener.js';
import { PrintPageElementFactory } from '../printing/PrintPageElementFactory.js';
import { VoxellModel } from '../../model/VoxellModel.js';
import { VoxellModelView } from './VoxellModelView.js';

const res = await ResourceBundle.getBundle(new URL('./VoxellModelViewer.properties', import.meta.url));

/** Shows voxel models (material and field models) in 3D. */
export class VoxellModelViewer {
    static implements = [Viewer, PrintPageElementFactory];

    constructor() {
        this.rep = null;
        this.active = false;
        this.listenerList = null;
    }

    deactivate() {
        this.active = false;
    }

    getDescription() {
        return res.getString('description');
    }

    getName() {
        return res.getString('name');
    }

    getView(model) {
        return new VoxellModelView(model, this);
    }

    getViewName() {
        return res.getString('viewName');
    }

    init(c) {
        this.rep = c;
        this.active = true;
        return true;
    }

    isActive() {
        return this.active;
    }

    isModelSupported(m) {
        return m instanceof VoxellModel;
    }

    addViewConstructionListener(listener) {
        if (this.listenerList == null) {
            this.listenerList = new EventListenerList();
        }
        this.listenerList.add(ViewConstructionListener, listener);
    }

    removeViewConstructionListener(listener) {
        this.listenerList && this.listenerList.remove(ViewConstructionListener, listener);
    }

    listeners() {
        return this.listenerList ? this.listenerList.getListeners(ViewConstructionListener) : [];
    }

    fireViewConstructionListenerViewConstructionStarting(event) {
        for (const l of this.listeners()) {
            l.viewConstructionStarting(event);
        }
    }

    fireViewConstructionListenerViewConstructionFinished() {
        for (const l of this.listeners()) {
            l.viewConstructionFinished(this);
        }
    }

    fireViewConstructionListenerViewConstructionProgress(title, percent, show) {
        for (const l of this.listeners()) {
            l.viewConstructionProgress(title, percent, show);
        }
    }

    getRepository() {
        return this.rep;
    }

    getPageElements(v) {
        return [v];
    }
}
