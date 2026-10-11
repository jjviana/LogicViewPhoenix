/**
 * java.util.ResourceBundle backed by the original `.properties` files.
 *
 * Every class of the original application looked up its user visible strings
 * with `ResourceBundle.getBundle(Foo.class.getName())`. The port keeps those
 * very same .properties files next to the JavaScript modules, and each module
 * loads its bundle with a top level await:
 *
 *   const res = await ResourceBundle.getBundle(new URL('./LoadModel.properties', import.meta.url));
 *   res.getString('fileChooserTitle');
 */
const cache = new Map();

export class MissingResourceException extends Error {
    constructor(message, className, key) {
        super(message);
        this.name = 'MissingResourceException';
        this.className = className;
        this.key = key;
    }
}

export class ResourceBundle {
    constructor(name, entries) {
        this.name = name;
        this.entries = entries;
    }

    getString(key) {
        if (!this.entries.has(key)) {
            throw new MissingResourceException(`Can't find resource for bundle ${this.name}, key ${key}`, this.name, key);
        }
        return this.entries.get(key);
    }

    containsKey(key) {
        return this.entries.has(key);
    }

    keySet() {
        return new Set(this.entries.keys());
    }

    /** Loads (once) and returns the bundle stored at `url`. */
    static getBundle(url) {
        const key = String(url);
        if (!cache.has(key)) {
            cache.set(key, fetch(key).then((response) => {
                if (!response.ok) {
                    throw new MissingResourceException(`Can't find bundle ${key}`, key, '');
                }
                return response.text();
            }).then((text) => new ResourceBundle(key, ResourceBundle.parseProperties(text))));
        }
        return cache.get(key);
    }

    /** Parser for the java.util.Properties text format (comments, continuations and escapes). */
    static parseProperties(text) {
        const entries = new Map();
        const rawLines = text.split(/\r\n|\r|\n/);
        const lines = [];
        for (let i = 0; i < rawLines.length; i++) {
            let line = rawLines[i].replace(/^[ \t\f]+/, '');
            if (line.startsWith('#') || line.startsWith('!')) {
                continue;
            }
            // Logical lines continue while ending with an odd number of backslashes.
            while (/(^|[^\\])(\\\\)*\\$/.test(line) && i + 1 < rawLines.length) {
                line = line.slice(0, -1) + rawLines[++i].replace(/^[ \t\f]+/, '');
            }
            if (line.length > 0) {
                lines.push(line);
            }
        }
        for (const line of lines) {
            let keyEnd = 0;
            while (keyEnd < line.length) {
                const c = line[keyEnd];
                if (c === '\\') {
                    keyEnd += 2;
                    continue;
                }
                if (c === '=' || c === ':' || c === ' ' || c === '\t' || c === '\f') {
                    break;
                }
                keyEnd++;
            }
            const key = unescape(line.substring(0, keyEnd));
            let rest = line.substring(keyEnd).replace(/^[ \t\f]*/, '');
            if (rest.startsWith('=') || rest.startsWith(':')) {
                rest = rest.substring(1).replace(/^[ \t\f]*/, '');
            }
            entries.set(key, unescape(rest));
        }
        return entries;
    }
}

function unescape(s) {
    return s.replace(/\\(u[0-9a-fA-F]{4}|.)/g, (m, esc) => {
        switch (esc[0]) {
            case 'n': return '\n';
            case 't': return '\t';
            case 'r': return '\r';
            case 'f': return '\f';
            case 'u': return String.fromCharCode(parseInt(esc.substring(1), 16));
            default: return esc;
        }
    });
}
