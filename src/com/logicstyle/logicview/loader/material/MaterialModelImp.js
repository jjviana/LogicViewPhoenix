import { ResourceBundle } from '../../../../../compat/util/ResourceBundle.js';
import { MaterialModel } from '../../model/MaterialModel.js';

const res = await ResourceBundle.getBundle(new URL('./MaterialModelImp.properties', import.meta.url));

/**
 * A material model: a grid of material codes plus the list of materials.
 * The Java byte[x][y][z] grid is stored as one flat Int8Array, x major.
 */
export class MaterialModelImp {
    static implements = [MaterialModel];

    constructor(sx, sy, sz, matcount) {
        this.materials = new Array(matcount);
        this.sizeX = sx;
        this.sizeY = sy;
        this.sizeZ = sz;
        this.grid = null;
        this.deltaX = 0;
        this.deltaY = 0;
        this.deltaZ = 0;
        this.name = null;
    }

    getDescription() {
        return res.getString('description');
    }

    /** The name attribute of the description (the original read it but never kept it). */
    getName() {
        return this.name || res.getString('name');
    }

    getDeltaX() {
        return this.deltaX;
    }

    getDeltaY() {
        return this.deltaY;
    }

    getDeltaZ() {
        return this.deltaZ;
    }

    getMaterials() {
        return this.materials;
    }

    getWidth() {
        return this.sizeX;
    }

    getHeight() {
        return this.sizeY;
    }

    getDepth() {
        return this.sizeZ;
    }

    getMaterialCount() {
        return this.materials.length;
    }

    setMaterial(code, m) {
        this.materials[code - 1] = m;
    }

    getMaterial(index) {
        return this.materials[index];
    }

    setdeltaX(deltaX) {
        this.deltaX = deltaX;
    }

    setdeltaY(deltaY) {
        this.deltaY = deltaY;
    }

    setdeltaZ(deltaZ) {
        this.deltaZ = deltaZ;
    }

    getDoubleValueAt(x, y, z) {
        return this.grid[(x * this.sizeY + y) * this.sizeZ + z];
    }

    getFloatValueAt(x, y, z) {
        return this.grid[(x * this.sizeY + y) * this.sizeZ + z];
    }

    getIntValueAt(x, y, z) {
        return this.grid[(x * this.sizeY + y) * this.sizeZ + z];
    }

    getObjectValueAt(x, y, z) {
        return this.grid[(x * this.sizeY + y) * this.sizeZ + z];
    }

    /** The flat grid (byte[][][] getGrid() in Java). */
    getGrid() {
        return this.grid;
    }

    setGrid(b) {
        this.grid = b;
    }

    isEnabled(x, y, z) {
        return this.materials[this.grid[(x * this.sizeY + y) * this.sizeZ + z] - 1].enabled;
    }
}
