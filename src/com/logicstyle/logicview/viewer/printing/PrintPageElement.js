import { Interface } from '../../../../../compat/lang/Interface.js';

/**
 * Something that can be laid out on a printed page (a snapshot or an animation
 * frame): the 3D view itself, the color scale, the material list...
 *
 *   getName(), getMinimumSize(), getSize(), setSize(d)
 *   getSupportedOrientations(), getOrientation(), setOrientation(o)
 *   print(graphics)      (may return a promise)
 *
 * PORTRAIT elements are stacked to the right of the page, LANDSCAPE ones at the
 * bottom and CENTER ones share the remaining space.
 */
export const PrintPageElement = new Interface('com.logicstyle.logicview.viewer.printing.PrintPageElement');
PrintPageElement.PORTRAIT = 1;
PrintPageElement.LANDSCAPE = 2;
PrintPageElement.CENTER = 3;
