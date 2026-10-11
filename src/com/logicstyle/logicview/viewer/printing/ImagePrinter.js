import { Interface } from '../../../../../compat/lang/Interface.js';

/**
 * A destination for snapshots:
 *   getDescription(), getPreferredSampleSize(), getConfigurationComponent()
 *   printImage(canvas): Promise<boolean>
 */
export const ImagePrinter = new Interface('com.logicstyle.logicview.viewer.printing.ImagePrinter');
