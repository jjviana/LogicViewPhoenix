import { JDialog, el, button } from '../../../../compat/swing/Swing.js';
import { UIAction } from './UIAction.js';
import { MainWindow } from '../MainWindow/MainWindow.js';
import { HELP_ACTION_GROUP } from './HelpActions.js';

/** Phoenix addition: Help > About LogicView. */
export class About {
    static implements = [UIAction];

    constructor() {
        this.active = false;
        this.rep = null;
    }

    execute(context) {
        const d = new JDialog(context, true, { resizable: false, className: 'lv-about' });
        d.setTitle('About LogicView');
        const components = this.rep.component_list || [];
        d.getContentPane().append(
            el('div', { className: 'lv-about-body' },
                el('div', { className: 'lv-about-logo' }, 'LogicView'),
                el('p', {}, 'A viewer for voxel material models and field models (such as SAR distributions computed with FDTD).'),
                el('p', {}, 'Originally written in Java with Swing and Java 3D (2001–2010). ',
                    'LogicView Phoenix is a port of the same design to JavaScript and WebGL (three.js): ',
                    'the Mosaic component kernel, the loaders, the viewer and its controls keep their original structure.'),
                el('p', { className: 'lv-small' }, components.length + ' components loaded from Repository.xml.')),
            el('div', { className: 'lv-button-row' }, button('OK', () => d.dispose())));
        d.addWindowListener({ windowClosing: () => d.dispose() });
        d.setVisible(true);
    }

    getActionGroup() {
        return HELP_ACTION_GROUP;
    }

    getDescription() {
        return 'About LogicView';
    }

    getIcon() {
        return null;
    }

    getName() {
        return 'About LogicView...';
    }

    init(c) {
        this.rep = c;
        this.active = true;
        return true;
    }

    isActive() {
        return this.active;
    }

    deactivate() {
        this.active = false;
    }

    isInContext(context) {
        return context instanceof MainWindow;
    }
}
