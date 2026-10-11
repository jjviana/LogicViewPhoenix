import { el, JTabbedPane } from '../../../../../compat/swing/Swing.js';

/**
 * The tabbed panel on top of every view, one tab per control that has a
 * control component. Showing a tab tells its control (so that, for instance,
 * dragging the mouse zooms while the Zoom tab is shown and rotates while the
 * Rotate tab is shown).
 */
export class ViewControlPanel {
    constructor(c) {
        this.controls = c.filter((control) => control.getControlComponent() != null);
        this.controlsPane = new JTabbedPane();
        this.element = el('div', { className: 'lv-viewcontrolpanel' }, this.controlsPane.element);
        this.controlsPane.addChangeListener(() => {
            const sel = this.controlsPane.getSelectedIndex();
            this.controls[sel].controlComponentShown(this.controls[sel].getControlComponent());
            for (let i = 0; i < this.controls.length; ++i) {
                if (i === sel) {
                    continue;
                }
                this.controls[i].controlComponentHidden(this.controls[i].getControlComponent());
            }
        });
        for (const control of this.controls) {
            this.controlsPane.addTab(control.getName(), control.getControlComponent());
        }
        if (this.controls.length > 0) {
            this.controls[0].controlComponentShown(this.controls[0].getControlComponent());
        }
        this.initialized = true;
    }
}
