import { ResourceBundle } from '../../../../../../compat/util/ResourceBundle.js';
import { el, textField, button } from '../../../../../../compat/swing/Swing.js';
import { SceneControl } from '../SceneControl.js';
import { formatDecimal2 } from '../common/Format.js';
import { ViewerMouseZoom } from './ViewerMouseZoom.js';

const res = await ResourceBundle.getBundle(new URL('./ZoomSceneControl.properties', import.meta.url));

/** Zooms (moves the whole scene along Z) with the mouse or from a typed value. */
export class ZoomSceneControl {
    static implements = [SceneControl];

    constructor() {
        this.mzoom = new ViewerMouseZoom();
        this.updating_from_fields = false;
        this.zp = new ZoomControlPanel(this);
    }

    addViewControlListener() {
    }

    removeViewControlListener() {
    }

    getControlComponent() {
        return this.zp.element;
    }

    getName() {
        return res.getString('name');
    }

    isActive() {
        return true;
    }

    controlComponentShown() {
        this.mzoom.setActive(true);
    }

    controlComponentHidden() {
        this.mzoom.setActive(false);
    }

    processScene(root, global) {
        this.mzoom.setTransformGroup(global);
        const oldRoot = this.mzoom.getParent();
        if (oldRoot != null) {
            oldRoot.remove(this.mzoom);
        }
        root.add(this.mzoom);
        this.zp.updateFieldsFromTransform();
    }
}

class ZoomControlPanel {
    constructor(owner) {
        this.owner = owner;
        this.zoom = textField(5);
        const update = () => {
            owner.updating_from_fields = true;
            this.updateTransformFromFields();
            owner.updating_from_fields = false;
        };
        this.element = el('div', { className: 'lv-flow' },
            el('span', {}, res.getString('zoomPanelTitle')), this.zoom, button('Ok', update),
            el('span', { className: 'lv-hint' }, res.getString('description_text')));
        owner.mzoom.setupCallback({
            transformChanged: () => {
                if (!owner.updating_from_fields) {
                    this.updateFieldsFromTransform();
                }
            },
        });
        this.zoom.addEventListener('change', update);
    }

    updateFieldsFromTransform() {
        if (this.owner.mzoom.getTransformGroup()) {
            this.zoom.value = formatDecimal2(this.owner.mzoom.getZoom());
        }
    }

    updateTransformFromFields() {
        const z = Number(this.zoom.value);
        if (!Number.isNaN(z) && this.owner.mzoom.getTransformGroup()) {
            this.owner.mzoom.setZoom(z);
        }
    }
}
