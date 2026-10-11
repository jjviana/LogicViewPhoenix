/**
 * Whitespace separated number reader.
 *
 * Both original loaders read their data files through a LineNumberReader and a
 * private `getNextToken()` that skipped `Character.isWhitespace()` characters
 * and returned the next run of non-whitespace characters, which was then parsed
 * with `Float.valueOf()` / `Byte.valueOf()`.
 *
 * The data files are big (the SAM head SAR file is ~100 MB of text), so instead
 * of building a string per token this reader parses the numbers straight from
 * the byte stream while keeping the same semantics: any whitespace separates
 * tokens, a short file raises a "premature EOF" error carrying the line number
 * and a malformed token raises a NumberFormatException with the token text.
 */
export class NumberFormatException extends Error {
    constructor(token, lineNumber) {
        super(`For input string: "${token}"`);
        this.name = 'NumberFormatException';
        this.token = token;
        this.lineNumber = lineNumber;
    }
}

export class PrematureEOFException extends Error {
    constructor(lineNumber) {
        super('premature EOF at line ' + lineNumber);
        this.name = 'PrematureEOFException';
        this.lineNumber = lineNumber;
    }
}

// Float.valueOf() spellings not handled by the fast path.
const JAVA_FLOAT = /^[+-]?(NaN|Infinity|(\d+\.?\d*|\.\d+)([eE][+-]?\d+)?[fFdD]?)$/;

const POW10 = [];
for (let i = 0; i <= 22; i++) {
    POW10.push(Math.pow(10, i));
}

// Character.isWhitespace() for the single byte range.
const WHITESPACE = new Uint8Array(256);
for (const c of [0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x1c, 0x1d, 0x1e, 0x1f, 0x20]) {
    WHITESPACE[c] = 1;
}

export class TokenReader {
    constructor(stream) {
        this.reader = stream.getReader();
        this.chunk = new Uint8Array(0);
        this.pos = 0;
        this.lineNumber = 0;
        this.eof = false;
    }

    getLineNumber() {
        return this.lineNumber;
    }

    async fill() {
        while (this.pos >= this.chunk.length && !this.eof) {
            const { done, value } = await this.reader.read();
            if (done) {
                this.eof = true;
            } else {
                this.chunk = value;
                this.pos = 0;
            }
        }
        return this.pos < this.chunk.length;
    }

    /**
     * Reads `count` numbers into `target` (any typed array) starting at index 0.
     * With `integerOnly` tokens must be plain integers (Byte.valueOf semantics).
     * `onValue(value, index, lineNumber)` may validate each value; it returns
     * nothing or throws.
     */
    async readNumbers(target, count, { integerOnly = false, onValue = null } = {}) {
        let index = 0;
        // Token state survives chunk boundaries.
        let inToken = false;
        let mantissa = 0;
        let fracDigits = 0;
        let sawDot = false;
        let sawDigit = false;
        let negative = false;
        let inExp = false;
        let expNegative = false;
        let expValue = 0;
        let expDigits = 0;
        let expSignAllowed = false;
        let valid = true;
        const text = new Uint8Array(64);
        let textLength = 0;

        const tokenText = () => String.fromCharCode(...text.subarray(0, textLength)) + (textLength === text.length ? '...' : '');

        const finishToken = () => {
            let value;
            if (valid && sawDigit && (!inExp || expDigits > 0) && !(integerOnly && (sawDot || inExp))) {
                const e = (expNegative ? -expValue : expValue) - fracDigits;
                value = mantissa;
                if (e < 0) {
                    value = -e <= 22 ? value / POW10[-e] : value * Math.pow(10, e);
                } else if (e > 0) {
                    value = e <= 22 ? value * POW10[e] : value * Math.pow(10, e);
                }
                if (negative) {
                    value = -value;
                }
                if (integerOnly && (value < -128 || value > 127)) {
                    throw new NumberFormatException(tokenText(), this.lineNumber);
                }
            } else {
                // Rare spellings accepted by Float.valueOf ("NaN", "Infinity", "1.5f"...).
                const t = tokenText();
                if (integerOnly || !JAVA_FLOAT.test(t)) {
                    throw new NumberFormatException(t, this.lineNumber);
                }
                value = t.endsWith('NaN') ? NaN : Number.parseFloat(t.replace(/[fFdD]$/, ''));
            }
            if (onValue) {
                onValue(value, index, this.lineNumber);
            }
            target[index++] = value;
            inToken = false;
        };

        while (index < count) {
            if (!(await this.fill())) {
                if (inToken) {
                    finishToken();
                    if (index >= count) {
                        break;
                    }
                }
                throw new PrematureEOFException(this.lineNumber);
            }
            const chunk = this.chunk;
            const end = chunk.length;
            let p = this.pos;
            while (p < end) {
                const c = chunk[p++];
                if (WHITESPACE[c]) {
                    if (c === 0x0a) {
                        this.lineNumber++;
                    }
                    if (inToken) {
                        finishToken();
                        if (index >= count) {
                            break;
                        }
                    }
                    continue;
                }
                if (!inToken) {
                    inToken = true;
                    mantissa = 0;
                    fracDigits = 0;
                    sawDot = false;
                    sawDigit = false;
                    negative = false;
                    inExp = false;
                    expNegative = false;
                    expValue = 0;
                    expDigits = 0;
                    valid = true;
                    textLength = 0;
                    if (c === 0x2d || c === 0x2b) { // - +
                        negative = c === 0x2d;
                        text[textLength++] = c;
                        continue;
                    }
                }
                if (textLength < text.length) {
                    text[textLength++] = c;
                }
                const signAllowed = expSignAllowed;
                expSignAllowed = false;
                if (c >= 0x30 && c <= 0x39) {
                    if (inExp) {
                        expValue = expValue * 10 + (c - 0x30);
                        expDigits++;
                    } else {
                        mantissa = mantissa * 10 + (c - 0x30);
                        sawDigit = true;
                        if (sawDot) {
                            fracDigits++;
                        }
                    }
                } else if (c === 0x2e && !sawDot && !inExp) { // .
                    sawDot = true;
                } else if ((c === 0x65 || c === 0x45) && !inExp && sawDigit) { // e E
                    inExp = true;
                    expSignAllowed = true;
                    continue;
                } else if ((c === 0x2d || c === 0x2b) && signAllowed) {
                    expNegative = c === 0x2d;
                } else {
                    valid = false;
                }
            }
            this.pos = p;
        }
        return target;
    }

    /** Reads the next number (slow path, for small files). */
    async nextNumber(options) {
        const one = new Float64Array(1);
        await this.readNumbers(one, 1, options);
        return one[0];
    }

    close() {
        this.reader.cancel().catch(() => {});
    }
}
