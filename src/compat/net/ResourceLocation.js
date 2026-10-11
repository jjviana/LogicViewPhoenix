/**
 * Stand-in for java.net.URL as used by LogicView's loaders.
 *
 * The loaders open a model description (.3df / .3dm) and then resolve the
 * files it references relative to it, exactly like `new URL(location, name)`.
 * In a browser those files can come from three places, each modelled by a
 * small "file system":
 *
 *   HttpFileSystem   files served next to the application (the default model)
 *   LocalFileSystem  files picked by the user (file chooser, folder, drag & drop)
 *   ZipFileSystem    entries of a .zip archive picked by the user
 *
 * Opening a location yields a byte stream. Gzip is handled transparently:
 * if "SAR0" does not exist but "SAR0.gz" does, the compressed file is used, and
 * any gzip-compressed content is inflated on the fly.
 */
export class ResourceLocation {
    constructor(fileSystem, path) {
        this.fileSystem = fileSystem;
        this.path = path;
    }

    /** new URL(this, relative) */
    resolve(relative) {
        return new ResourceLocation(this.fileSystem, this.fileSystem.resolve(this.path, relative));
    }

    getPath() {
        return this.path;
    }

    /** File name part of the path. */
    getName() {
        return decodeURIComponent(this.path.substring(this.path.lastIndexOf('/') + 1));
    }

    toString() {
        return this.fileSystem.describe(this.path);
    }

    equals(other) {
        return other instanceof ResourceLocation && other.fileSystem === this.fileSystem && other.path === this.path;
    }

    /** Key usable in Maps (java.net.URL equality). */
    key() {
        return this.fileSystem.id + ':' + this.path;
    }

    /**
     * Opens the resource. Resolves to { stream, length } where `stream` yields
     * the (decompressed) bytes and `onProgress(read, length)` is called with the
     * number of raw bytes consumed so far. With `preferCompressed` the ".gz"
     * variant is looked for first (bulk data files are usually stored
     * compressed, as the bundled sample is), saving a failed request.
     */
    async openStream(onProgress = null, { preferCompressed = false } = {}) {
        const candidates = preferCompressed ? [this.path + '.gz', this.path] : [this.path, this.path + '.gz'];
        let raw = null;
        for (const candidate of candidates) {
            raw = await this.fileSystem.open(candidate);
            if (raw) {
                break;
            }
        }
        if (!raw) {
            throw new Error('File not found: ' + this.toString());
        }
        let stream = raw.stream;
        if (onProgress) {
            let read = 0;
            stream = stream.pipeThrough(new TransformStream({
                transform(chunk, controller) {
                    read += chunk.byteLength;
                    onProgress(read, raw.length);
                    controller.enqueue(chunk);
                },
            }));
        }
        return { stream: await inflateIfGzip(stream), length: raw.length };
    }

    async readText(encoding = 'iso-8859-1') {
        const { stream } = await this.openStream();
        const buffer = await new Response(stream).arrayBuffer();
        return new TextDecoder(encoding).decode(buffer);
    }

    /** Location of a file served relative to the page (or to an absolute URL). */
    static fromURL(url, base = document.baseURI) {
        const absolute = new URL(url, base);
        return new ResourceLocation(HttpFileSystem.instance, absolute.href);
    }
}

/** Peeks at the first two bytes and inflates the stream if it is gzip-compressed. */
async function inflateIfGzip(stream) {
    const reader = stream.getReader();
    const first = await reader.read();
    const head = first.done ? new Uint8Array(0) : first.value;
    const rest = new ReadableStream({
        start(controller) {
            if (head.byteLength > 0) {
                controller.enqueue(head);
            }
        },
        async pull(controller) {
            const { done, value } = await reader.read();
            if (done) {
                controller.close();
            } else {
                controller.enqueue(value);
            }
        },
        cancel(reason) {
            return reader.cancel(reason);
        },
    });
    if (head.byteLength >= 2 && head[0] === 0x1f && head[1] === 0x8b) {
        return rest.pipeThrough(new DecompressionStream('gzip'));
    }
    return rest;
}

function resolvePath(basePath, relative) {
    const url = new URL(relative, 'file://' + encodePath(basePath));
    return decodeURIComponent(url.pathname);
}

function encodePath(path) {
    return path.split('/').map(encodeURIComponent).join('/');
}

export function normalizePath(path) {
    return resolvePath('/', path.replace(/\\/g, '/').replace(/^\/*/, '/'));
}

let nextId = 1;

export class HttpFileSystem {
    constructor() {
        this.id = 'http' + nextId++;
    }

    resolve(base, relative) {
        return new URL(relative, base).href;
    }

    describe(path) {
        return path;
    }

    async open(path) {
        const response = await fetch(path);
        if (!response.ok) {
            return null;
        }
        const length = Number(response.headers.get('Content-Length')) || 0;
        return { stream: response.body, length };
    }
}

HttpFileSystem.instance = new HttpFileSystem();

/** Files picked by the user, keyed by their (relative) path. */
export class LocalFileSystem {
    /** @param entries iterable of [path, Blob] */
    constructor(entries, label = 'local files') {
        this.id = 'local' + nextId++;
        this.label = label;
        this.files = new Map();
        for (const [path, blob] of entries) {
            this.files.set(normalizePath(path), blob);
        }
    }

    resolve(base, relative) {
        return resolvePath(base, relative);
    }

    describe(path) {
        return `${this.label}:${path}`;
    }

    list() {
        return [...this.files.keys()];
    }

    async open(path) {
        const blob = this.files.get(path);
        if (!blob) {
            return null;
        }
        return { stream: blob.stream(), length: blob.size };
    }
}

/** The entries of a .zip archive (stored and deflated entries, no zip64). */
export class ZipFileSystem extends LocalFileSystem {
    static async open(file) {
        const size = file.size;
        const tailLength = Math.min(size, 65536 + 22);
        const tail = new DataView(await file.slice(size - tailLength).arrayBuffer());
        let eocd = -1;
        for (let i = tail.byteLength - 22; i >= 0; i--) {
            if (tail.getUint32(i, true) === 0x06054b50) {
                eocd = i;
                break;
            }
        }
        if (eocd < 0) {
            throw new Error(file.name + ' is not a zip archive');
        }
        const count = tail.getUint16(eocd + 10, true);
        const dirSize = tail.getUint32(eocd + 12, true);
        const dirOffset = tail.getUint32(eocd + 16, true);
        const dir = new DataView(await file.slice(dirOffset, dirOffset + dirSize).arrayBuffer());
        const decoder = new TextDecoder();
        const entries = [];
        let p = 0;
        for (let n = 0; n < count && p + 46 <= dir.byteLength; n++) {
            if (dir.getUint32(p, true) !== 0x02014b50) {
                break;
            }
            const method = dir.getUint16(p + 10, true);
            const compressedSize = dir.getUint32(p + 20, true);
            const uncompressedSize = dir.getUint32(p + 24, true);
            const nameLength = dir.getUint16(p + 28, true);
            const extraLength = dir.getUint16(p + 30, true);
            const commentLength = dir.getUint16(p + 32, true);
            const localOffset = dir.getUint32(p + 42, true);
            const name = decoder.decode(new Uint8Array(dir.buffer, dir.byteOffset + p + 46, nameLength));
            p += 46 + nameLength + extraLength + commentLength;
            const baseName = name.substring(name.lastIndexOf('/') + 1);
            if (name.endsWith('/') || name.startsWith('__MACOSX/') || baseName.startsWith('._') || baseName === '.DS_Store') {
                continue;
            }
            entries.push([name, new ZipEntryBlob(file, method, localOffset, compressedSize, uncompressedSize)]);
        }
        return new ZipFileSystem(entries, file.name);
    }
}

class ZipEntryBlob {
    constructor(file, method, localOffset, compressedSize, uncompressedSize) {
        this.file = file;
        this.method = method;
        this.localOffset = localOffset;
        this.compressedSize = compressedSize;
        // Progress is counted on the inflated bytes, so report the inflated size.
        this.size = uncompressedSize;
    }

    stream() {
        const self = this;
        let inner = null;
        return new ReadableStream({
            async start() {
                const header = new DataView(await self.file.slice(self.localOffset, self.localOffset + 30).arrayBuffer());
                const dataStart = self.localOffset + 30 + header.getUint16(26, true) + header.getUint16(28, true);
                let s = self.file.slice(dataStart, dataStart + self.compressedSize).stream();
                if (self.method === 8) {
                    s = s.pipeThrough(new DecompressionStream('deflate-raw'));
                } else if (self.method !== 0) {
                    throw new Error('Unsupported zip compression method ' + self.method);
                }
                inner = s.getReader();
            },
            async pull(controller) {
                const { done, value } = await inner.read();
                if (done) {
                    controller.close();
                } else {
                    controller.enqueue(value);
                }
            },
            cancel(reason) {
                return inner && inner.cancel(reason);
            },
        });
    }
}
