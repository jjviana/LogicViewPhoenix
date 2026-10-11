import { ResourceBundle } from '../../../../../compat/util/ResourceBundle.js';
import { StringTokenizer, parseInt32, parseFloat32 } from '../../../../../compat/util/StringTokenizer.js';
import { TokenReader, PrematureEOFException, NumberFormatException } from '../../../../../compat/io/TokenReader.js';
import { Thread } from '../../../../../compat/lang/Thread.js';
import { System } from '../../../../../compat/util/Logger.js';
import { FieldModel } from '../../model/FieldModel.js';
import { ModelLoader } from '../ModelLoader.js';
import { parseXml, getChild, getAttributeValue } from '../LoaderSupport.js';

const res = await ResourceBundle.getBundle(new URL('./FieldModelImp.properties', import.meta.url));

/**
 * A field model (.3df): an XML description such as
 *
 *   <FieldModel name="SAR" dimention="296,188,296" deltas="0.0011472,0.001523,0.0013407"
 *               timesteps="1" units="SAR [W/kg]">
 *     <data prefix="SAR" initialSufixCount="0" sufixIncrement="1"></data>
 *     <MaterialModel location="material_3d_space.3dm"/>
 *   </FieldModel>
 *
 * Time step n is read from the file <prefix><initialSufixCount + n * sufixIncrement>
 * (SAR0, SAR1, ...) next to the description: one float per voxel, whitespace
 * separated, x major. The optional material model is loaded with whichever
 * ModelLoader handles its file extension.
 *
 * Java did all of this in the constructor; here the asynchronous part lives in
 * `FieldModelImp.create(location, repository)`.
 */
export class FieldModelImp {
    static implements = [FieldModel];

    static async create(location, r, progressListener = null) {
        const model = new this(r);
        model.progressListener = progressListener;
        await model.load(location);
        return model;
    }

    constructor(r) {
        this.rep = r;
        this.grid = null;
        this.sizeX = 0;
        this.sizeY = 0;
        this.sizeZ = 0;
        this.timeStepCount = 0;
        this.deltax = -1.0;
        this.deltay = -1.0;
        this.deltaz = -1.0;
        this.location = null;
        this.currentTimeStep = 0;
        this.startDataSufixValue = 0;
        this.incrementDataSufixValue = 1;
        this.datPrefix = null;
        this.name = null;
        this.matmod = null;
        this.units = null;
        this.progressListener = null;
    }

    async load(location) {
        let text;
        try {
            text = await location.readText();
        } catch (e) {
            throw new Error(String(e));
        }
        const modeldoc = parseXml(text, res.getString('ModelLoader.ParsingException'));
        const root = modeldoc.documentElement;
        if (root.tagName !== 'FieldModel') {
            throw new Error(res.getString('ModelLoader.FileFormatException'));
        }
        let slices = -1;
        let cubesy = -1;
        let cubesz = -1;
        this.name = getAttributeValue(root, 'name');
        if (this.name == null) {
            this.name = res.getString('ModelLoader.UntitledModeltitle');
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
                this.deltax = parseFloat32(ctok.nextToken());
            }
            if (ctok.hasMoreTokens()) {
                this.deltay = parseFloat32(ctok.nextToken());
            }
            if (ctok.hasMoreTokens()) {
                this.deltaz = parseFloat32(ctok.nextToken());
            }
        } catch (e) {
            // empty catch block
        }
        if (this.deltax === -1.0 || this.deltay === -1.0 || this.deltaz === -1.0) {
            throw new Error(res.getString('ModelLoader.DeltaFormatError'));
        }
        this.sizeX = slices;
        this.sizeY = cubesy;
        this.sizeZ = cubesz;
        this.grid = new Float32Array(slices * cubesy * cubesz);
        const tsteps = getAttributeValue(root, 'timesteps');
        if (tsteps == null) {
            throw new Error(res.getString('ModelLoader.NoTimeStepCountError'));
        }
        try {
            this.timeStepCount = parseInt32(tsteps);
        } catch (e) {
            throw new Error(res.getString('ModelLoader.TimeStepCountSintaxError'));
        }
        if (this.timeStepCount < 1) {
            throw new Error(res.getString('ModelLoader.TimeStepCountSintaxError'));
        }
        this.units = getAttributeValue(root, 'units');
        if (this.units == null) {
            this.units = res.getString('defaultUnits');
        }
        const data = getChild(root, 'data');
        if (data == null) {
            throw new Error(res.getString('ModelLoader.NoDatafileError'));
        }
        this.datPrefix = getAttributeValue(data, 'prefix');
        if (this.datPrefix == null) {
            throw new Error(res.getString('ModelLoader.NoDatPrefixError'));
        }
        let temp = getAttributeValue(data, 'initialSufixCount');
        if (temp != null) {
            try {
                this.startDataSufixValue = parseInt32(temp);
            } catch (e) {
                throw new Error(res.getString('ModelLoader.InvalidStartStepPrefixError'));
            }
        } else {
            this.startDataSufixValue = 0;
        }
        temp = getAttributeValue(data, 'sufixIncrement');
        if (temp != null) {
            try {
                this.incrementDataSufixValue = parseInt32(temp);
            } catch (e) {
                throw new Error(res.getString('ModelLoader.InvaludSufixIncrement'));
            }
        } else {
            this.incrementDataSufixValue = 1;
        }
        this.location = location;
        await this.setCurrentTimeStep(0);
        const materialModelElement = getChild(root, 'MaterialModel');
        if (materialModelElement != null) {
            const matmodel_location = getAttributeValue(materialModelElement, 'location');
            if (matmodel_location == null) {
                throw new Error(res.getString('missingMaterialModelLocation'));
            }
            const matmodurl = location.resolve(matmodel_location);
            const loaders = this.rep.findByInterface(ModelLoader);
            let matmod = null;
            for (let i = 0; i < loaders.length; ++i) {
                const ml = loaders[i];
                if (!matmodel_location.endsWith(ml.getFilenameExtension())) {
                    continue;
                }
                try {
                    matmod = await ml.LoadModel(matmodurl);
                    break;
                } catch (ex) {
                    // The original swallowed this silently; keep a trace of it.
                    System.err.println('Error loading material model ' + matmodurl + ': ' + ex.message);
                }
            }
            if (matmod == null) {
                throw new Error(res.getString('noMaterialModelLoader'));
            }
            this.setMaterialModel(matmod);
        }
    }

    getCurrentTimeStep() {
        return this.currentTimeStep;
    }

    getTimeStepCount() {
        return this.timeStepCount;
    }

    /** Reads the data file of time `step` into the grid. */
    async setCurrentTimeStep(step) {
        const filename = this.datPrefix + String(this.startDataSufixValue + step * this.incrementDataSufixValue);
        const daturl = this.location.resolve(filename);
        let r;
        try {
            r = new TokenReader((await daturl.openStream(null, { preferCompressed: true })).stream);
        } catch (e) {
            throw new Error(String(e.message || e));
        }
        const sliceSize = this.sizeY * this.sizeZ;
        try {
            for (let i = 0; i < this.sizeX; ++i) {
                await r.readNumbers(this.grid.subarray(i * sliceSize, (i + 1) * sliceSize), sliceSize);
                if (this.progressListener) {
                    this.progressListener(100 * i / this.sizeX);
                }
                if (i % 8 === 7) {
                    await Thread.yield();
                }
            }
        } catch (e) {
            if (e instanceof PrematureEOFException) {
                throw new Error(res.getString('ModelLoader.PrematureEOF') + e.lineNumber);
            }
            if (e instanceof NumberFormatException) {
                System.err.println(res.getString('ModelLoader.NumberFormatException') + e.lineNumber + ':' + e.token);
                throw new Error(res.getString('ModelLoader.NumberFormatException') + e.lineNumber + ':' + e.token);
            }
            throw new Error(res.getString('ModelLoader.IOException') + r.getLineNumber() + ':' + e);
        } finally {
            r.close();
        }
        this.currentTimeStep = step;
    }

    getDeltaX() {
        return this.deltax;
    }

    getDeltaY() {
        return this.deltay;
    }

    getDeltaZ() {
        return this.deltaz;
    }

    getDepth() {
        return this.sizeZ;
    }

    getDescription() {
        return res.getString('description');
    }

    getDoubleValueAt(x, y, z) {
        return this.grid[(x * this.sizeY + y) * this.sizeZ + z];
    }

    getFloatValueAt(x, y, z) {
        return this.grid[(x * this.sizeY + y) * this.sizeZ + z];
    }

    getHeight() {
        return this.sizeY;
    }

    getIntValueAt(x, y, z) {
        return Math.trunc(this.grid[(x * this.sizeY + y) * this.sizeZ + z]);
    }

    getName() {
        return this.name;
    }

    getObjectValueAt(x, y, z) {
        return this.grid[(x * this.sizeY + y) * this.sizeZ + z];
    }

    getWidth() {
        return this.sizeX;
    }

    isEnabled(x, y, z) {
        return this.grid[(x * this.sizeY + y) * this.sizeZ + z] !== 0.0;
    }

    getMaterialModel() {
        return this.matmod;
    }

    setMaterialModel(m) {
        this.matmod = m;
    }

    getUnits() {
        return this.units;
    }

    getTimeStepSkip() {
        return this.incrementDataSufixValue;
    }
}
