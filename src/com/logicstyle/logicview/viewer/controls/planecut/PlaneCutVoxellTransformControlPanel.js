import { ResourceBundle } from '../../../../../../compat/util/ResourceBundle.js';
import { JDialog, JOptionPane, el, button, titledPanel } from '../../../../../../compat/swing/Swing.js';

const res = await ResourceBundle.getBundle(new URL('./PlaneCutVoxellTransformControlPanel.properties', import.meta.url));

/** The plane cut dialog: start and end sliders for each axis. */
export class PlaneCutVoxellTransformControlPanel extends JDialog {
    constructor(parent, client) {
        super(parent, false, { resizable: false });
        this.client = client;
        this.initComponents();
    }

    initComponents() {
        this.setTitle(res.getString('title'));
        const slider = (onchange) => el('input', { type: 'range', className: 'lv-slider', min: 0, max: 100, step: 1, oninput: onchange });
        const axisPanel = (axis) => {
            const start = slider(() => this.updateTitleBorder(axis));
            const end = slider(() => this.updateTitleBorder(axis));
            const panel = titledPanel('',
                el('div', { className: 'lv-slider-row' }, el('label', {}, res.getString('minText')), start),
                el('div', { className: 'lv-slider-row' }, el('label', {}, res.getString('maxText')), end));
            this['start' + axis + 'slider'] = start;
            this['end' + axis + 'slider'] = end;
            this['cut' + axis + 'panel'] = panel;
            return panel;
        };
        this.applyButton = button(res.getString('applyButtonLabel'), () => this.applyButtonActionPerformed());
        this.getContentPane().append(axisPanel('X'), axisPanel('Y'), axisPanel('Z'), el('div', { className: 'lv-button-row' }, this.applyButton));
        this.getContentPane().classList.add('lv-padded');
    }

    async applyButtonActionPerformed() {
        const cmod = this.client.getVoxellModel();
        const v = (s) => Number(s.value);
        for (const axis of ['X', 'Y', 'Z']) {
            if (v(this['start' + axis + 'slider']) >= v(this['end' + axis + 'slider'])) {
                await JOptionPane.showMessageDialog(this, res.getString('invalid' + axis + 'Cut'), res.getString('invalidCutTitle'), JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        cmod.setStartX(v(this.startXslider));
        cmod.setEndX(v(this.endXslider));
        cmod.setStartY(v(this.startYslider));
        cmod.setEndY(v(this.endYslider));
        cmod.setStartZ(v(this.startZslider));
        cmod.setEndZ(v(this.endZslider));
        this.client.modelUpdated();
    }

    updateTitleBorder(axis) {
        this['cut' + axis + 'panel'].setTitle(res.getString('cutPlaneTitle') + axis + ':' + this['start' + axis + 'slider'].value + ':' + this['end' + axis + 'slider'].value);
    }

    show() {
        const cmod = this.client.getVoxellModel();
        const src = cmod.getSource();
        const sizes = { X: src.getWidth(), Y: src.getHeight(), Z: src.getDepth() };
        const starts = { X: cmod.getStartX(), Y: cmod.getStartY(), Z: cmod.getStartZ() };
        const ends = { X: cmod.getEndX(), Y: cmod.getEndY(), Z: cmod.getEndZ() };
        for (const axis of ['X', 'Y', 'Z']) {
            for (const [s, value] of [[this['start' + axis + 'slider'], starts[axis]], [this['end' + axis + 'slider'], ends[axis]]]) {
                s.min = 0;
                s.max = sizes[axis];
                s.value = value;
            }
            this.updateTitleBorder(axis);
        }
        super.show();
    }
}
