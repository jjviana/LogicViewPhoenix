import { Interface } from '../../../../compat/lang/Interface.js';

/**
 *   findByInterface(Interface): Component[]       active components only
 *   findAllByInterface(Interface): Component[]    every registered component
 *   findComponentByInterface(Interface): Component  the first one registered
 */
export const ComponentRepository = new Interface('com.logicstyle.mosaic.interfaces.ComponentRepository');
