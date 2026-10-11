/**
 * Class.forName() for the port: a fully qualified class name such as
 * "com.logicstyle.logicview.UIAction.LoadModel" maps to the ES module
 * src/com/logicstyle/logicview/UIAction/LoadModel.js and its export named
 * "LoadModel".
 */
const SOURCE_ROOT = new URL('../../', import.meta.url);

export class ClassNotFoundException extends Error {
    constructor(name, cause) {
        super(name);
        this.name = 'ClassNotFoundException';
        this.cause = cause;
    }
}

export async function forName(className) {
    const simpleName = className.substring(className.lastIndexOf('.') + 1);
    const url = new URL(className.replace(/\./g, '/') + '.js', SOURCE_ROOT);
    let module;
    try {
        module = await import(url.href);
    } catch (e) {
        throw new ClassNotFoundException(className, e);
    }
    const cls = module[simpleName];
    if (typeof cls !== 'function') {
        throw new ClassNotFoundException(className);
    }
    return cls;
}
