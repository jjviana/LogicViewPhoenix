import { ResourceBundle } from '../../../../../../compat/util/ResourceBundle.js';
import { ViewControlFactory } from '../ViewControlFactory.js';
import { ZoomSceneControl } from './ZoomSceneControl.js';

const res = await ResourceBundle.getBundle(new URL('./ZoomSceneControlFactory.properties', import.meta.url));

export class ZoomSceneControlFactory {
    static implements = [ViewControlFactory];

    constructor() {
        this.rep = null;
        this.active = false;
    }

    deactivate() {
        this.active = false;
    }

    getDescription() {
        return res.getString('name');
    }

    getName() {
        return res.getString('name');
    }

    getViewControl() {
        return new ZoomSceneControl();
    }

    init(c) {
        this.rep = c;
        this.active = true;
        return true;
    }

    isActive() {
        return this.active;
    }
}
