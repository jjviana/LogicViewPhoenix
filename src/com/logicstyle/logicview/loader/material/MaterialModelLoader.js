import { ResourceBundle } from '../../../../../compat/util/ResourceBundle.js';
import { StringTokenizer, parseInt32, parseFloat32 } from '../../../../../compat/util/StringTokenizer.js';
import { TokenReader, PrematureEOFException, NumberFormatException } from '../../../../../compat/io/TokenReader.js';
import { Thread } from '../../../../../compat/lang/Thread.js';
import { Color } from '../../../../../compat/awt/Color.js';
import { ModelLoader } from '../ModelLoader.js';
import { ModelLoadListenerSupport, parseXml, getChild, getChildren, getAttributeValue } from '../LoaderSupport.js';
import { Material } from '../../model/MaterialModel.js';
import { MaterialModelImp } from './MaterialModelImp.js';

const res = await ResourceBundle.getBundle(new URL('./MaterialModelLoader.properties', import.meta.url));

/**
 * Loads material models (.3dm): an XML description listing the materials and
 * naming a data file with one material code per voxel, whitespace separated,
 * x major (for x, for y, for z).
 *
 * Materials that do not occur in the data are dropped and the codes are
 * renumbered, so the material list only shows what is really in the model.
 * Loaded models are cached by location, as the field models of a simulation
 * usually share the same material model.
 */
export class MaterialModelLoader {
    static implements = [ModelLoader];

    constructor() {
        this.rep = null;
        this.active = false;
        this.listenerList = null;
        this.urlmap = new Map();
    }

    async LoadModel(location) {
        const cached = this.urlmap.get(location.key());
        if (cached != null) {
            return cached;
        }
        this.fireModelLoadListenerLoadStarting(this);
        let cubesz = -1;
        let cubesy = -1;
        let slices = -1;
        let deltax = -1.0;
        let deltay = -1.0;
        let deltaz = -1.0;
        const modeldoc = parseXml(await location.readText(), res.getString('ModelLoader.ParsingException'));
        const root = modeldoc.documentElement;
        if (root.tagName !== 'MaterialModel') {
            throw new Error(res.getString('ModelLoader.FileFormatException'));
        }
        let modelname = getAttributeValue(root, 'name');
        if (modelname == null) {
            modelname = res.getString('ModelLoader.UntitledModeltitle');
        }
        const dimentions = getAttributeValue(root, 'dimention');
        if (dimentions == null) {
            throw new Error(res.getString('ModelLoader.NoDimentionsError'));
        }
        let ctok = new StringTokenizer(dimentions, ',');
        try {
            if (ctok.hasMoreTokens()) {
                slices = parseInt32(ctok.nextToken());
            }
            if (ctok.hasMoreTokens()) {
                cubesy = parseInt32(ctok.nextToken());
            }
            if (ctok.hasMoreTokens()) {
                cubesz = parseInt32(ctok.nextToken());
            }
        } catch (e) {
            // empty catch block
        }
        if (slices === -1 || cubesy === -1 || cubesz === -1) {
            throw new Error(res.getString('ModelLoader.DimentionsSintaxError'));
        }
        const deltas = getAttributeValue(root, 'deltas');
        if (deltas == null) {
            throw new Error(res.getString('ModelLoader.NoDeltasError'));
        }
        ctok = new StringTokenizer(deltas, ',');
        try {
            if (ctok.hasMoreTokens()) {
                deltax = parseFloat32(ctok.nextToken());
            }
            if (ctok.hasMoreTokens()) {
                deltay = parseFloat32(ctok.nextToken());
            }
            if (ctok.hasMoreTokens()) {
                deltaz = parseFloat32(ctok.nextToken());
            }
        } catch (e) {
            // empty catch block
        }
        if (deltax === -1.0 || deltay === -1.0 || deltaz === -1.0) {
            throw new Error(res.getString('ModelLoader.DeltaFormatError'));
        }
        const materials = getChild(root, 'materials');
        if (materials == null) {
            throw new Error(res.getString('ModelLoader.NoMaterialsError'));
        }
        const mats = getChildren(materials, 'material');
        if (mats.length === 0) {
            throw new Error(res.getString('ModelLoader.EmptyMaterialsException'));
        }
        const mat_tmp = new Array(mats.length);
        for (let i = 0; i < mats.length; ++i) {
            const m = new Material();
            const material = mats[i];
            m.name = getAttributeValue(material, 'name');
            const scolor = getAttributeValue(material, 'color') || '';
            ctok = new StringTokenizer(scolor, ',');
            let r2 = -1;
            let g = -1;
            let b = -1;
            try {
                if (ctok.hasMoreTokens()) {
                    r2 = parseInt32(ctok.nextToken());
                }
                if (ctok.hasMoreTokens()) {
                    g = parseInt32(ctok.nextToken());
                }
                if (ctok.hasMoreTokens()) {
                    b = parseInt32(ctok.nextToken());
                }
            } catch (e) {
                // empty catch block
            }
            if (r2 === -1 || g === -1 || b === -1) {
                throw new Error(res.getString('ModelLoader.MaterialColorFormatError') + m.name);
            }
            m.color = new Color(r2, g, b);
            const strans = getAttributeValue(material, 'transparency');
            if (strans != null) {
                try {
                    m.alpha = parseFloat32(strans);
                } catch (e) {
                    throw new Error(res.getString('ModelLoader.TransparencyFormatError') + m.name);
                }
            }
            const scode = getAttributeValue(material, 'code');
            if (scode == null) {
                throw new Error(res.getString('ModelLoader.NoMaterialCodeError') + m.name);
            }
            let code;
            try {
                code = parseInt32(scode);
            } catch (e) {
                throw new Error(res.getString('ModelLoader.MaterialCodeFormatError') + m.name);
            }
            if (code < 1 || code > mat_tmp.length) {
                throw new Error(res.getString('ModelLoader.MaterialCodeFormatError') + m.name);
            }
            const svisible = getAttributeValue(material, 'visible');
            if (svisible != null) {
                m.enabled = svisible.toLowerCase() === 'true';
            }
            mat_tmp[code - 1] = m;
        }
        const datafile = getChild(root, 'datafile');
        if (datafile == null) {
            throw new Error(res.getString('ModelLoader.NoDatafileError'));
        }
        const filename = getAttributeValue(datafile, 'name');
        if (filename == null) {
            throw new Error(res.getString('ModelLoader.NoFileNameError'));
        }
        // A plain file name is looked up next to the model description,
        // anything else is resolved as a URL.
        const daturl = location.resolve(filename);
        let r;
        try {
            r = new TokenReader((await daturl.openStream(null, { preferCompressed: true })).stream);
        } catch (e) {
            throw new Error(res.getString('ModelLoader.DataFileNotFoundError') + filename);
        }
        const sliceSize = cubesy * cubesz;
        const grid = new Int8Array(slices * sliceSize);
        const mat_count = new Array(mat_tmp.length).fill(0);
        try {
            for (let i = 0; i < slices; ++i) {
                const slice = grid.subarray(i * sliceSize, (i + 1) * sliceSize);
                await r.readNumbers(slice, sliceSize, { integerOnly: true });
                for (let p = 0; p < sliceSize; p++) {
                    const v = slice[p];
                    if (v < 1 || v > mat_count.length) {
                        throw new Error(res.getString('ModelLoader.InvalidData') + r.getLineNumber() + ':' + v);
                    }
                    mat_count[v - 1]++;
                }
                this.fireModelLoadListenerLoadingPercentComplete(100 * i / slices);
                if (i % 8 === 7) {
                    await Thread.yield();
                }
            }
        } catch (e) {
            if (e instanceof PrematureEOFException) {
                throw new Error(res.getString('ModelLoader.PrematureEOF') + e.lineNumber);
            }
            if (e instanceof NumberFormatException) {
                throw new Error(res.getString('ModelLoader.InvalidData') + e.lineNumber + ':' + e.token);
            }
            throw e;
        } finally {
            r.close();
        }
        let real_materials_count = 0;
        for (let i = 0; i < mat_count.length; ++i) {
            if (mat_count[i] > 0) {
                ++real_materials_count;
            }
        }
        const mod = new MaterialModelImp(slices, cubesy, cubesz, real_materials_count);
        mod.setdeltaX(deltax);
        mod.setdeltaY(deltay);
        mod.setdeltaZ(deltaz);
        if (real_materials_count < mat_tmp.length) {
            const translation_table = new Int8Array(mat_count.length);
            let mat_cod = 1;
            for (let i = 0; i < mat_count.length; ++i) {
                if (mat_count[i] > 0) {
                    if (mat_tmp[i] == null) {
                        throw new Error(res.getString('ModelLoader.InvalidData') + ' material code ' + (i + 1) + ' is not described');
                    }
                    mod.setMaterial(mat_cod, mat_tmp[i]);
                    translation_table[i] = mat_cod;
                    mat_cod++;
                    continue;
                }
                translation_table[i] = -1;
            }
            for (let p = 0; p < grid.length; p++) {
                grid[p] = translation_table[grid[p] - 1];
            }
        } else {
            for (let i = 0; i < mat_tmp.length; ++i) {
                mod.setMaterial(i + 1, mat_tmp[i]);
            }
        }
        mod.setGrid(grid);
        mod.name = modelname;
        this.fireModelLoadListenerLoadFinished(this);
        this.urlmap.set(location.key(), mod);
        return mod;
    }

    deactivate() {
        this.active = false;
    }

    getDescription() {
        return res.getString('description');
    }

    getFilenameExtension() {
        return res.getString('filenameExtension');
    }

    getName() {
        return res.getString('name');
    }

    getTypeName() {
        return res.getString('typeName');
    }

    init(c) {
        this.rep = c;
        this.active = true;
        return true;
    }

    isActive() {
        return this.active;
    }
}

Object.assign(MaterialModelLoader.prototype, ModelLoadListenerSupport);
