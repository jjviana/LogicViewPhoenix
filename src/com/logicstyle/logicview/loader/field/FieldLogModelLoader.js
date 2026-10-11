import { ModelLoader } from '../ModelLoader.js';
import { FieldModelLoader } from './FieldModelLoader.js';
import { FieldModelLogImp } from './FieldModelLogImp.js';

/** Loads field models (.3df) showing the base 10 logarithm of the field. */
export class FieldLogModelLoader extends FieldModelLoader {
    static implements = [ModelLoader];

    getDescription() {
        return 'Log of Field Models';
    }

    getModelClass() {
        return FieldModelLogImp;
    }

    getTypeName() {
        return 'Log of Field Models';
    }
}
