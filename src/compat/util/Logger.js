/**
 * java.util.logging.Logger and System.err, redirected to an in-memory
 * "debug.txt" (the original Kernel redirected System.err to a file called
 * debug.txt in the working directory). The log is also echoed to the browser
 * console and can be read from the Help menu.
 */
const lines = [];
const listeners = new Set();

function append(text) {
    for (const line of String(text).split('\n')) {
        lines.push(line);
    }
    if (lines.length > 20000) {
        lines.splice(0, lines.length - 20000);
    }
    for (const l of listeners) {
        l();
    }
}

export const DebugOutput = {
    getText() {
        return lines.join('\n');
    },
    addChangeListener(l) {
        listeners.add(l);
    },
    removeChangeListener(l) {
        listeners.delete(l);
    },
};

export const System = {
    err: {
        println(text = '') {
            console.log(String(text));
            append(text);
        },
        printStackTrace(e) {
            console.error(e);
            append(e && e.stack ? e.stack : String(e));
        },
    },
    out: {
        println(text = '') {
            console.log(String(text));
        },
    },
};

export const Level = {
    SEVERE: { name: 'SEVERE', value: 1000 },
    WARNING: { name: 'WARNING', value: 900 },
    INFO: { name: 'INFO', value: 800 },
};

const loggers = new Map();

export class Logger {
    constructor(name) {
        this.name = name;
    }

    static getLogger(name) {
        if (!loggers.has(name)) {
            loggers.set(name, new Logger(name));
        }
        return loggers.get(name);
    }

    log(level, message, error) {
        const text = `${new Date().toISOString()} ${this.name}\n${level.name}: ${message}`;
        if (level.value >= Level.SEVERE.value) {
            console.error(text, error ?? '');
        } else if (level.value >= Level.WARNING.value) {
            console.warn(text, error ?? '');
        } else {
            console.info(text);
        }
        append(text);
        if (error) {
            append(error.stack ? error.stack : String(error));
        }
    }
}
