import { ResourceBundle } from '../../../../../compat/util/ResourceBundle.js';
import { UIAction } from '../../UIAction/UIAction.js';
import { View } from '../../model/View.js';
import { ViewPresenter } from '../../MainWindow/ViewPresenter.js';
import { AnimationControlPanel } from './AnimationControlPanel.js';

const res = await ResourceBundle.getBundle(new URL('./AnimateUIAction.properties', import.meta.url));

/** View > Animate...: one animation dialog at a time. */
export class AnimateUIAction {
    static implements = [UIAction];

    constructor() {
        this.active = false;
        this.repository = null;
        this.dialogOpen = false;
        this.controlPanel = null;
    }

    deactivate() {
        this.active = false;
    }

    execute(context) {
        if (!this.dialogOpen) {
            this.controlPanel = new AnimationControlPanel(this.repository, context);
            this.controlPanel.addWindowListener({ windowClosed: () => {
                this.dialogOpen = false;
            } });
            this.controlPanel.show();
            this.dialogOpen = true;
        } else {
            this.controlPanel.toFront();
        }
    }

    getActionGroup() {
        return ViewPresenter.VIEW_ACTION_GROUP;
    }

    getDescription() {
        return res.getString('menuLabel');
    }

    getIcon() {
        return null;
    }

    getName() {
        return res.getString('name');
    }

    init(c) {
        this.repository = c;
        return true;
    }

    isActive() {
        return this.active;
    }

    isInContext(context) {
        return context instanceof View;
    }
}
