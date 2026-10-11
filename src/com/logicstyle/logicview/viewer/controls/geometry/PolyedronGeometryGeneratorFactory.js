import { ResourceBundle } from '../../../../../../compat/util/ResourceBundle.js';
import { ViewControlFactory } from '../ViewControlFactory.js';
import { PolyedronGeometryGenerator } from './PolyedronGeometryGenerator.js';

const res = await ResourceBundle.getBundle(new URL('./PolyedronGeometryGeneratorFactory.properties', import.meta.url));

export class PolyedronGeometryGeneratorFactory {
    static implements = [ViewControlFactory];

    constructor() {
        this.active = false;
        this.rep = null;
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

    init(c) {
        this.active = true;
        this.rep = c;
        return true;
    }

    isActive() {
        return this.active;
    }

    getViewControl() {
        return new PolyedronGeometryGenerator(this.rep);
    }
}
