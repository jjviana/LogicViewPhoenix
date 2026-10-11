import { EventListenerList } from '../../../../compat/util/EventListenerList.js';
import { ViewPresenter } from './ViewPresenter.js';
import { ViewPresentationListener } from './ViewPresentationListener.js';
import { MultiViewDialog } from './MultiViewDialog.js';

/** Presents every model in its own window. */
export class MultiViewPresenter {
    static implements = [ViewPresenter];

    constructor() {
        this.active = false;
        this.rep = null;
        this.listenerList = null;
    }

    addViews(m, views, w) {
        const d = new MultiViewDialog(w.getParentFrame(), m, views, this.rep);
        d.addWindowListener({
            windowClosing: () => this.fireViewPresentationListenerViewPresentationFinished(m, views),
        });
        const desktop = document.getElementById('desktop');
        const width = Math.max(480, Math.min(1100, desktop.clientWidth - 64));
        const height = Math.max(360, Math.min(860, desktop.clientHeight - 48));
        d.setSize(width, height);
        d.show();
        this.fireViewPresentationListenerViewPresentationStarted(m, views);
        return d;
    }

    deactivate() {
        this.active = false;
    }

    getDescription() {
        return 'Multiple dialogs view presenter';
    }

    getName() {
        return 'MultiViewPresenter';
    }

    init(c) {
        this.rep = c;
        return true;
    }

    isActive() {
        return this.active;
    }

    addViewPresentationListener(listener) {
        if (this.listenerList == null) {
            this.listenerList = new EventListenerList();
        }
        this.listenerList.add(ViewPresentationListener, listener);
    }

    removeViewPresentationListener(listener) {
        this.listenerList && this.listenerList.remove(ViewPresentationListener, listener);
    }

    fireViewPresentationListenerViewPresentationStarted(m, views) {
        for (const l of this.listenerList ? this.listenerList.getListeners(ViewPresentationListener) : []) {
            l.viewPresentationStarted(m, views);
        }
    }

    fireViewPresentationListenerViewPresentationFinished(m, viewers) {
        for (const l of this.listenerList ? this.listenerList.getListeners(ViewPresentationListener) : []) {
            l.viewPresentationFinished(m, viewers);
        }
    }
}
