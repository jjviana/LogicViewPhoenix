import { ResourceBundle } from '../../../../../../compat/util/ResourceBundle.js';
import { JDialog, el, button } from '../../../../../../compat/swing/Swing.js';
import { VoxellTransformControl } from '../VoxellTransformControl.js';
import { ViewControlListenerSupport } from '../ViewControlSupport.js';
import { WrapperVoxellModel } from '../../util/WrapperVoxellModel.js';
import { MainWindow } from '../../../MainWindow/MainWindow.js';
import { MaterialsPanel } from './MaterialsPanel.js';

const res = await ResourceBundle.getBundle(new URL('./MaterialSelectionVoxellTransformControl.properties', import.meta.url));

/**
 * Shows only the voxels made of the selected materials. Works for material
 * models and for any model associated with one (field models).
 */
export class MaterialSelectionVoxellTransformControl {
    static implements = [VoxellTransformControl];

    constructor(m, c) {
        this.rep = c;
        this.listenerList = null;
        const mats = m.getMaterials();
        this.matState = mats.map((mat) => mat.enabled);
        this.model_wrapper = new MaterialSelectionWrapper(m, this);
        this.mp = new MaterialsPanel(m, this);
        const mw = c.findComponentByInterface(MainWindow);
        this.matdialog = new JDialog(mw.getParentFrame(), false);
        const scroll = el('div', { className: 'lv-scroll' }, this.mp.element);
        const updateButton = button(res.getString('updateButtonTitle'), () => this.controlUpdated());
        this.matdialog.getContentPane().classList.add('lv-column');
        this.matdialog.getContentPane().append(scroll, el('div', { className: 'lv-button-row' }, updateButton));
        this.matdialog.setTitle(res.getString('dialogTitle'));
        this.matdialog.setSize(220, 420);
        this.label_component = el('div', { className: 'lv-flow' }, button(res.getString('buttonTitle'), () => this.matdialog.setVisible(true)));
    }

    controlComponentHidden() {
    }

    controlComponentShown() {
    }

    getControlComponent() {
        return this.label_component;
    }

    getName() {
        return res.getString('name');
    }

    isActive() {
        return true;
    }

    processModel(source) {
        this.model_wrapper.setSource(source);
        return this.model_wrapper;
    }

    controlUpdated() {
        this.fireViewControlListenerControlStateChanged(this);
    }

    setMaterialState(index, state) {
        this.matState[index] = state;
    }

    getMaterialState(index) {
        return this.matState[index];
    }

    getPrintPageElement() {
        return this.mp.getPageElement();
    }
}

Object.assign(MaterialSelectionVoxellTransformControl.prototype, ViewControlListenerSupport);

class MaterialSelectionWrapper extends WrapperVoxellModel {
    constructor(m, control) {
        super(m);
        this.matmod = m;
        this.control = control;
    }

    isEnabled(x, y, z) {
        return this.control.matState[this.matmod.getIntValueAt(x, y, z) - 1];
    }
}
