import { Interface } from '../../../../compat/lang/Interface.js';

/**
 * The unit of the Mosaic component system. Every entry of Repository.xml names
 * a class implementing this interface; the kernel instantiates it, registers it
 * under all the interfaces it implements and then calls init() until every
 * component reports it is initialized.
 *
 *   init(ComponentRepository): boolean   (may throw DependencyException)
 *   isActive(): boolean
 *   deactivate(): void
 *   getName(): string
 *   getDescription(): string
 */
export const Component = new Interface('com.logicstyle.mosaic.interfaces.Component');
