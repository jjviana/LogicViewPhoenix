import { DebugOutput } from '../../../../compat/util/Logger.js';
import { JDialog, el, button } from '../../../../compat/swing/Swing.js';
import { UIAction } from './UIAction.js';
import { MainWindow } from '../MainWindow/MainWindow.js';
import { HELP_ACTION_GROUP } from './HelpActions.js';

/**
 * Phoenix addition: Help > Debug output. The original Kernel redirected
 * System.err to a file named debug.txt; this shows that log.
 */
export class ShowDebugOutput {
    static implements = [UIAction];

    constructor() {
        this.active = false;
        this.dialog = null;
    }

    execute(context) {
        if (this.dialog == null) {
            this.dialog = new JDialog(context, false);
            this.dialog.setTitle('debug.txt');
            const text = el('pre', { className: 'lv-debug-output' });
            const refresh = () => {
                const atEnd = text.scrollTop + text.clientHeight >= text.scrollHeight - 4;
                text.textContent = DebugOutput.getText();
                if (atEnd) {
                    text.scrollTop = text.scrollHeight;
                }
            };
            DebugOutput.addChangeListener(() => {
                if (this.dialog.isVisible()) {
                    refresh();
                }
            });
            const save = button('Save debug.txt', () => {
                const a = el('a', { href: URL.createObjectURL(new Blob([DebugOutput.getText()], { type: 'text/plain' })), download: 'debug.txt' });
                a.click();
                setTimeout(() => URL.revokeObjectURL(a.href), 10000);
            });
            this.dialog.getContentPane().classList.add('lv-column');
            this.dialog.getContentPane().append(text, el('div', { className: 'lv-button-row' }, save, button('Close', () => this.dialog.setVisible(false))));
            this.dialog.setSize(640, 420);
            this.dialog.refresh = refresh;
        }
        this.dialog.setVisible(true);
        this.dialog.refresh();
    }

    getActionGroup() {
        return HELP_ACTION_GROUP;
    }

    getDescription() {
        return 'Shows the debug output';
    }

    getIcon() {
        return null;
    }

    getName() {
        return 'Debug output (debug.txt)';
    }

    init() {
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
