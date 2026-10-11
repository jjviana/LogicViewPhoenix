/**
 * A tiny stand-in for Java interfaces.
 *
 * The original LogicView relied heavily on interface types: the Mosaic kernel
 * registers every component under each interface it implements, and the viewer
 * code is full of checks such as `model instanceof FieldModel`. JavaScript has
 * no interfaces, so this class recreates just enough of them:
 *
 *   export const ModelLoader = new Interface('com.logicstyle.logicview.loader.ModelLoader', Component);
 *
 *   class FieldModelLoader {
 *       static implements = [ModelLoader];
 *   }
 *
 *   loader instanceof ModelLoader      // true, thanks to Symbol.hasInstance
 *
 * As in Java, `Interface.getClassInterfaces(cls)` only returns the interfaces
 * declared directly by a class (Class.getInterfaces()), while `instanceof`
 * considers the whole class hierarchy.
 */
export class Interface {
    constructor(name, ...superInterfaces) {
        this.name = name;
        this.superInterfaces = superInterfaces;
    }

    getName() {
        return this.name;
    }

    getSimpleName() {
        return this.name.substring(this.name.lastIndexOf('.') + 1);
    }

    /** Equivalent of Class.getInterfaces() for an interface: its direct super interfaces. */
    getInterfaces() {
        return this.superInterfaces;
    }

    /** Equivalent of Class.isAssignableFrom(): is `other` this interface or a sub-interface of it? */
    isAssignableFrom(other) {
        if (other === this) {
            return true;
        }
        if (!(other instanceof Interface)) {
            return false;
        }
        return other.superInterfaces.some((s) => this.isAssignableFrom(s));
    }

    [Symbol.hasInstance](obj) {
        if (obj === null || obj === undefined || typeof obj !== 'object') {
            return false;
        }
        for (let proto = Object.getPrototypeOf(obj); proto && proto !== Object.prototype; proto = Object.getPrototypeOf(proto)) {
            const declared = Interface.getClassInterfaces(proto.constructor);
            if (declared.some((i) => this.isAssignableFrom(i))) {
                return true;
            }
        }
        return false;
    }

    toString() {
        return 'interface ' + this.name;
    }

    /** Equivalent of Class.getInterfaces(): only the interfaces declared directly by `cls`. */
    static getClassInterfaces(cls) {
        if (cls && Object.prototype.hasOwnProperty.call(cls, 'implements')) {
            return cls.implements;
        }
        return [];
    }
}

