import { ResourceBundle } from '../../../../../../compat/util/ResourceBundle.js';
import { Color } from '../../../../../../compat/awt/Color.js';
import { button } from '../../../../../../compat/swing/Swing.js';
import { VoxellColorGeneratorControl } from './VoxellColorGeneratorControl.js';
import { ViewControlListenerSupport } from '../ViewControlSupport.js';
import { ColorControlDialog } from './ColorControlDialog.js';

const res = await ResourceBundle.getBundle(new URL('./FieldModelVoxellColorGeneratorControl.properties', import.meta.url));

/** Colors voxels by field value, from blue (min) to red (max) by default. */
export class FieldModelVoxellColorGeneratorControl {
    static implements = [VoxellColorGeneratorControl];

    constructor(currentModel, originalModel, main) {
        this.listenerList = null;
        this.control = button(res.getString('controlButtonLabel'), () => {
            this.cdialog.setVisible(true);
        });
        this.main = main;
        this.createColorDialog(originalModel, currentModel);
        this.setModel(currentModel);
    }

    controlComponentHidden() {
    }

    controlComponentShown() {
    }

    getColorForPoint(x, y, z, model) {
        return this.cdialog.getColorforValue(model.getFloatValueAt(x, y, z));
    }

    getControlComponent() {
        return this.control;
    }

    getName() {
        return res.getString('name');
    }

    isActive() {
        return true;
    }

    createColorDialog(originalModel, currentModel) {
        const color1 = Color.blue;
        const color2 = Color.red;
        this.cdialog = new ColorControlDialog(this, this.main.getParentFrame(), color1, color2, currentModel, 256, originalModel.getUnits());
    }

    setModel(m) {
        this.cdialog.setCurrentModel(m);
    }

    applyColorScale() {
        this.fireViewControlListenerControlStateChanged(this);
    }

    getPrintPageElement() {
        return this.cdialog.getPrintPageElement();
    }
}

Object.assign(FieldModelVoxellColorGeneratorControl.prototype, ViewControlListenerSupport);
