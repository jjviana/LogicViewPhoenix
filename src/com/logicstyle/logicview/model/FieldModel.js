import { Interface } from '../../../../compat/lang/Interface.js';
import { VoxellModel } from './VoxellModel.js';
import { Materialized } from './Materialized.js';

/**
 * A scalar field over a voxel grid (e.g. SAR), possibly with several time steps.
 *
 *   getTimeStepSkip(), getTimeStepCount()
 *   setCurrentTimeStep(step)   (asynchronous in the port: it reads a data file)
 *   getCurrentTimeStep(), getUnits()
 */
export const FieldModel = new Interface('com.logicstyle.logicview.model.FieldModel', VoxellModel, Materialized);
