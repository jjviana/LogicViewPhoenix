import { ResourceBundle } from '../../../../compat/util/ResourceBundle.js';
import { JFrame, JMenuBar, JMenu } from '../../../../compat/swing/Swing.js';
import { DependencyException } from '../../mosaic/interfaces/DependencyException.js';
import { UIAction } from '../UIAction/UIAction.js';
import { MainWindow } from './MainWindow.js';
import { ViewPresenter } from './ViewPresenter.js';

const res = await ResourceBundle.getBundle(new URL('./MainWindowImpl.properties', import.meta.url));

/**
 * The main LogicView window: a frame whose menu bar holds every UIAction that
 * is in context for the main window (Model > Load Model...).
 */
export class MainWindowImpl extends JFrame {
    static implements = [MainWindow];

    constructor() {
        super();
        this.repository = null;
        this.actionMap = new Map();
        this.initComponents();
    }

    initComponents() {
        this.mb = new JMenuBar();
        this.setJMenuBar(this.mb);
    }

    deactivate() {
        this.hide();
    }

    getName() {
        return res.getString('name');
    }

    getDescription() {
        return res.getString('MainWindow.component_description');
    }

    init(c) {
        this.repository = c;
        const actions = this.repository.findAllByInterface(UIAction);
        if (actions.length === 0) {
            throw new DependencyException(UIAction);
        }
        const groupmap = new Map();
        for (const uia of actions) {
            if (!uia.isInContext(this)) {
                continue;
            }
            let m = groupmap.get(uia.getActionGroup().getName());
            if (m == null) {
                m = new JMenu(uia.getActionGroup().getName());
                groupmap.set(uia.getActionGroup().getName(), m);
                this.mb.add(m);
            }
            const item = m.add(uia.getName());
            item.addActionListener((evt) => {
                const action = this.actionMap.get(evt.source);
                action.execute(this);
            });
            this.actionMap.set(item, uia);
        }
        this.setTitle(res.getString('title'));
        this.show();
        return true;
    }

    /** Shows the views of a model; resolves to what the ViewPresenter returns. */
    addDocView(model, views) {
        const vp = this.repository.findComponentByInterface(ViewPresenter);
        if (vp == null) {
            throw new Error('No view presenter found...');
        }
        return vp.addViews(model, views, this);
    }

    getParentFrame() {
        return this;
    }
}
