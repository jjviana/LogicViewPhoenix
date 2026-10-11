import { ResourceBundle } from '../../../../../compat/util/ResourceBundle.js';
import { ViewPrintConstructorFactory } from './ViewPrintConstructorFactory.js';
import { DefaultViewPrintConstructor } from './DefaultViewPrintConstructor.js';

const res = await ResourceBundle.getBundle(new URL('./DefaultViewPrintConstructorFactory.properties', import.meta.url));

export class DefaultViewPrintConstructorFactory {
    static implements = [ViewPrintConstructorFactory];

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

    getViewPrintConstructor(v) {
        return new DefaultViewPrintConstructor(this.rep, v);
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
