import { Interface } from '../../../../../compat/lang/Interface.js';

/**
 * Configuration node (unused by LogicView itself, kept from the original
 * Mosaic interfaces):
 *   getName(), getAttributeNames(), getAttributeValue(name), getChilds()
 */
export const Node = new Interface('com.logicstyle.mosaic.interfaces.config.Node');
