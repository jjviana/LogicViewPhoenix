import { JDialog, JMenuBar, JMenu } from '../../../../compat/swing/Swing.js';
import { UIAction } from '../UIAction/UIAction.js';

/**
 * A window showing the view of one model, with a menu bar holding the
 * UIActions that are in context for a view (View > Save Snapshot..., Animate...).
 */
export class MultiViewDialog extends JDialog {
    constructor(parent, mod, v, repository) {
        super(parent, false, { maximizable: true, className: 'lv-viewdialog' });
        this.initComponents();
        this.views = v;
        this.model = mod;
        const view = v[0].getView(mod);
        this.view = view;
        this.setTitle(v[0].getViewName() + ' — ' + mod.getName());
        this.getContentPane().append(view.getViewComponent());
        this.getContentPane().classList.add('lv-column');
        const actions = repository.findAllByInterface(UIAction);
        this.actionMap = new Map();
        const groupmap = new Map();
        for (const uia of actions) {
            if (!uia.isInContext(view)) {
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
                this.actionMap.get(evt.source).execute(view);
            });
            this.actionMap.set(item, uia);
        }
    }

    initComponents() {
        this.mb = new JMenuBar();
        this.addWindowListener({
            windowClosing: () => this.closeDialog(),
        });
        this.setJMenuBar(this.mb);
    }

    closeDialog() {
        this.setVisible(false);
        this.dispose();
        if (this.view.dispose) {
            this.view.dispose();
        }
    }
}
