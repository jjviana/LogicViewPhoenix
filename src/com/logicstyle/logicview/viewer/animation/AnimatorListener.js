import { Interface } from '../../../../../compat/lang/Interface.js';

/**
 *   animationStarting(animator)
 *   animationFinishing(animator)
 *   frameProduced(frame: HTMLCanvasElement, timeStep)   (may return a promise)
 */
export const AnimatorListener = new Interface('com.logicstyle.logicview.viewer.animation.AnimatorListener');
