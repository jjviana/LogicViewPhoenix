import { Interface } from '../../../../compat/lang/Interface.js';
import { forName } from '../../../../compat/lang/Class.js';
import { Logger, Level } from '../../../../compat/util/Logger.js';
import { Component } from '../interfaces/Component.js';
import { ComponentRepository } from '../interfaces/ComponentRepository.js';
import { DependencyException } from '../interfaces/DependencyException.js';

const log = Logger.getLogger('mosaic.system');

export class FatalError extends Error {
    constructor(message) {
        super(message);
        this.name = 'FatalError';
    }
}

/**
 * The Mosaic component repository.
 *
 * Reads the <component> entries of the configuration document, instantiates
 * each class, indexes the instance under every interface it implements (and
 * their super interfaces, as long as they extend Component) and then runs the
 * initialization loop: init() is called on every component until all of them
 * return true. A component whose dependencies are missing throws a
 * DependencyException and is removed; if a pass makes no progress the kernel
 * reports an initialization deadlock.
 *
 * Java instantiated the classes inside the constructor with Class.forName();
 * module loading is asynchronous in the browser, so the classes are loaded by
 * the static `load()` and the constructor receives the instances.
 */
export class ApplicationRepository {
    static implements = [ComponentRepository];

    static async load(config) {
        const root = config.documentElement;
        const instances = [];
        const failures = [];
        for (const component of root.children) {
            if (component.tagName !== 'component') {
                continue;
            }
            const classElement = [...component.children].find((c) => c.tagName === 'class');
            const clname = classElement ? classElement.textContent.trim() : '';
            try {
                const componentClass = await forName(clname);
                instances.push(new componentClass());
            } catch (e) {
                log.log(Level.WARNING, 'Error loading component ' + clname, e.cause || e);
                failures.push({ component: clname, reason: describe(e.cause || e) });
            }
        }
        return new ApplicationRepository(instances, failures);
    }

    constructor(instances, failures = []) {
        this.components = new Map();
        this.component_list = [];
        // Phoenix: the components that could not be loaded or initialized
        // (the original only logged them), so the application can tell the user.
        this.failures = failures;
        for (const instance of instances) {
            for (const i of Interface.getClassInterfaces(instance.constructor)) {
                this.addComponentClass(i, instance);
            }
            this.component_list.push(instance);
        }
        if (this.component_list.length === 0) {
            log.log(Level.WARNING, 'No component loaded, exiting...');
            throw new FatalError('No component loaded');
        }
        // current_state[i] == true means "component i is not initialized yet".
        let current_state = this.component_list.map(() => true);
        let previous_state = this.component_list.map(() => true);
        for (;;) {
            for (let i = 0; i < this.component_list.length; ++i) {
                const c = this.component_list[i];
                try {
                    current_state[i] = !c.init(this);
                } catch (e) {
                    if (e instanceof DependencyException) {
                        log.log(Level.WARNING, 'Initialization of component ' + c.getName() + ' failed due to failed dependency: ' + e.getRequired().getName());
                    } else {
                        log.log(Level.SEVERE, 'Initialization of component ' + safeName(c) + ' failed due to exception', e);
                        this.failures.push({ component: safeName(c), reason: describe(e) });
                    }
                    this.removeComponent(c);
                    current_state.splice(i, 1);
                    previous_state.splice(i, 1);
                    --i;
                }
            }
            if (!current_state.includes(true) || equals(current_state, previous_state)) {
                break;
            }
            previous_state = [...current_state];
        }
        if (current_state.includes(true)) {
            log.log(Level.SEVERE, 'Initialization deadlock detected with the following components');
            current_state.forEach((pending, i) => pending && log.log(Level.SEVERE, safeName(this.component_list[i])));
            log.log(Level.SEVERE, 'Exiting..');
            throw new FatalError('Initialization deadlock');
        }
        log.log(Level.INFO, 'Components initialization sucessfull.');
    }

    addComponentClass(iface, comp) {
        if (Component.isAssignableFrom(iface)) {
            if (!this.components.has(iface)) {
                this.components.set(iface, []);
            }
            const ll = this.components.get(iface);
            if (!ll.includes(comp)) {
                ll.push(comp);
            }
            for (const s of iface.getInterfaces()) {
                this.addComponentClass(s, comp);
            }
        }
    }

    removeComponent(c) {
        this.component_list.splice(this.component_list.indexOf(c), 1);
        for (const [iface, ll] of this.components) {
            const i = ll.indexOf(c);
            if (i >= 0) {
                ll.splice(i, 1);
                if (ll.length === 0) {
                    this.components.delete(iface);
                }
            }
        }
    }

    findAllByInterface(iface) {
        return [...(this.components.get(iface) || [])];
    }

    findByInterface(iface) {
        return (this.components.get(iface) || []).filter((c) => c.isActive());
    }

    findComponentByInterface(iface) {
        const l = this.components.get(iface);
        return l && l.length > 0 ? l[0] : null;
    }
}

function equals(a, b) {
    return a.length === b.length && a.every((v, i) => v === b[i]);
}

function describe(e) {
    return e && e.message ? e.message : String(e);
}

function safeName(c) {
    try {
        return c.getName();
    } catch (e) {
        return c.constructor.name;
    }
}
