import { ResourceBundle } from '../../../../compat/util/ResourceBundle.js';
import { UIAction } from './UIAction.js';
import { View } from '../model/View.js';
import { ViewPresenter } from '../MainWindow/ViewPresenter.js';
import { ViewPrinter } from '../viewer/printing/ViewPrinter.js';

const res = await ResourceBundle.getBundle(new URL('./SaveViewSnapshot.properties', import.meta.url));

/** View > Save Snapshot...: hands the view to the ViewPrinter. */
export class SaveViewSnapshot {
    static implements = [UIAction];

    constructor() {
        this.rep = null;
        this.active = false;
    }

    deactivate() {
        this.active = false;
    }

    execute(context) {
        const vp = this.rep.findComponentByInterface(ViewPrinter);
        vp.print(context);
    }

    getActionGroup() {
        return ViewPresenter.VIEW_ACTION_GROUP;
    }

    getDescription() {
        return res.getString('description');
    }

    getIcon() {
        return null;
    }

    getName() {
        return res.getString('menuItem');
    }

    init(c) {
        this.rep = c;
        return true;
    }

    isActive() {
        return this.active;
    }

    isInContext(context) {
        return context instanceof View;
    }
}
