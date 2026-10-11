import { FieldModelImp } from './FieldModelImp.js';

const ln10 = Math.log(10.0);
const FLOAT_MIN_VALUE = 1.4e-45;

/** A field model showing log10(|value|) of the field. */
export class FieldModelLogImp extends FieldModelImp {
    getUnits() {
        return 'Log of ' + super.getUnits();
    }

    async setCurrentTimeStep(step) {
        await super.setCurrentTimeStep(step);
        const grid = this.grid;
        const floor = Math.log(FLOAT_MIN_VALUE) / ln10;
        for (let i = 0; i < grid.length; ++i) {
            grid[i] = grid[i] !== 0.0 ? Math.log(Math.abs(grid[i])) / ln10 : floor;
        }
    }
}
