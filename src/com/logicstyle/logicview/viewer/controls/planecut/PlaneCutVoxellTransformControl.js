import { ResourceBundle } from '../../../../../../compat/util/ResourceBundle.js';
import { el, button } from '../../../../../../compat/swing/Swing.js';
import { System } from '../../../../../../compat/util/Logger.js';
import { VoxellTransformControl } from '../VoxellTransformControl.js';
import { ViewControlListenerSupport } from '../ViewControlSupport.js';
import { Interpolator } from '../../animation/Interpolator.js';
import { WrapperVoxellModel } from '../../util/WrapperVoxellModel.js';
import { InterpolatorControlPanel } from '../rotate/RotateSceneControl.js';
import { PlaneCutVoxellTransformControlPanel } from './PlaneCutVoxellTransformControlPanel.js';

const res = await ResourceBundle.getBundle(new URL('./PlaneCutVoxellTransformControl.properties', import.meta.url));

/** Keeps only the voxels inside a box [startX, endX] x [startY, endY] x [startZ, endZ]. */
export class PlaneCutVoxellTransformControl {
    static implements = [VoxellTransformControl, Interpolator];

    constructor(main, mod) {
        this.listenerList = null;
        this.interpolatorControlPanel = null;
        this.cmodel = new PlaneCutVoxellModel(mod);
        this.control_panel = new PlaneCutVoxellTransformControlPanel(main.getParentFrame(), this);
        this.control_component = el('div', { className: 'lv-flow' }, button(res.getString('controlButtonLabel'), () => this.control_panel.show()));
    }

    controlComponentHidden() {
    }

    controlComponentShown() {
    }

    getControlComponent() {
        return this.control_component;
    }

    getName() {
        return res.getString('name');
    }

    isActive() {
        return true;
    }

    processModel(source) {
        this.cmodel.setSource(source);
        return this.cmodel;
    }

    getVoxellModel() {
        return this.cmodel;
    }

    modelUpdated() {
        this.fireViewControlListenerControlStateChanged(this);
    }

    getConfigurationComponent() {
        if (this.interpolatorControlPanel == null) {
            this.interpolatorControlPanel = new InterpolatorControlPanel('Start and end plane cuts for each axis:');
        }
        return this.interpolatorControlPanel.element;
    }

    getInterpolatorName() {
        return 'Plane Cut';
    }

    setInterpolationStep(step) {
        const p = this.interpolatorControlPanel;
        const startX = parseInt(p.startX.value, 10);
        const startY = parseInt(p.startY.value, 10);
        const startZ = parseInt(p.startZ.value, 10);
        const endX = parseInt(p.endX.value, 10);
        const endY = parseInt(p.endY.value, 10);
        const endZ = parseInt(p.endZ.value, 10);
        const sx = startX !== -1 ? Math.trunc(startX + (endX - startX) * step) : this.cmodel.getStartX();
        const sy = startY !== -1 ? Math.trunc(startY + (endY - startY) * step) : this.cmodel.getStartY();
        const sz = startZ !== -1 ? Math.trunc(startZ + (endZ - startZ) * step) : this.cmodel.getStartZ();
        this.cmodel.setStartX(sx);
        this.cmodel.setStartY(sy);
        this.cmodel.setStartZ(sz);
        System.err.println('sx=' + sx + ',sy=' + sy + ',sz=' + sz);
        this.modelUpdated();
    }
}

Object.assign(PlaneCutVoxellTransformControl.prototype, ViewControlListenerSupport);

export class PlaneCutVoxellModel extends WrapperVoxellModel {
    constructor(start) {
        super(start);
        this.setStartX(0);
        this.setStartY(0);
        this.setStartZ(0);
        this.setEndX(start.getWidth());
        this.setEndY(start.getHeight());
        this.setEndZ(start.getDepth());
    }

    isEnabled(x, y, z) {
        if (x >= this.startX && x <= this.endX && y >= this.startY && y <= this.endY && z >= this.startZ && z <= this.endZ) {
            return super.isEnabled(x, y, z);
        }
        return false;
    }

    getStartX() {
        return this.startX;
    }

    setStartX(startX) {
        this.startX = startX;
    }

    getStartY() {
        return this.startY;
    }

    setStartY(startY) {
        this.startY = startY;
    }

    getStartZ() {
        return this.startZ;
    }

    setStartZ(startZ) {
        this.startZ = startZ;
    }

    getEndX() {
        return this.endX;
    }

    setEndX(endX) {
        this.endX = endX;
    }

    getEndY() {
        return this.endY;
    }

    setEndY(endY) {
        this.endY = endY;
    }

    getEndZ() {
        return this.endZ;
    }

    setEndZ(endZ) {
        this.endZ = endZ;
    }
}
