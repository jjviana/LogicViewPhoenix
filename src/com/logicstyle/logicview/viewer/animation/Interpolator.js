import { Interface } from '../../../../../compat/lang/Interface.js';

/**
 * Something that can be animated:
 *   setInterpolationStep(step)   step goes from 0 to 1 along the animation (may return a promise)
 *   getInterpolatorName()
 *   getConfigurationComponent(): HTMLElement | null
 */
export const Interpolator = new Interface('com.logicstyle.logicview.viewer.animation.Interpolator');
