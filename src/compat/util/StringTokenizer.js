/** java.util.StringTokenizer (delimiter characters, no returned delimiters). */
export class StringTokenizer {
    constructor(str, delimiters = ' \t\n\r\f') {
        this.tokens = [];
        let current = '';
        for (const c of str) {
            if (delimiters.includes(c)) {
                if (current.length > 0) {
                    this.tokens.push(current);
                }
                current = '';
            } else {
                current += c;
            }
        }
        if (current.length > 0) {
            this.tokens.push(current);
        }
        this.index = 0;
    }

    hasMoreTokens() {
        return this.index < this.tokens.length;
    }

    nextToken() {
        if (!this.hasMoreTokens()) {
            throw new Error('NoSuchElementException');
        }
        return this.tokens[this.index++];
    }

    countTokens() {
        return this.tokens.length - this.index;
    }
}

/** Integer.valueOf(): throws on anything that is not a plain decimal integer. */
export function parseInt32(s) {
    if (!/^[+-]?\d+$/.test(s)) {
        throw new Error('NumberFormatException: For input string: "' + s + '"');
    }
    return Number.parseInt(s, 10);
}

/** Float.valueOf(): accepts Java float literals (with optional surrounding blanks). */
export function parseFloat32(s) {
    const t = s.trim();
    if (!/^[+-]?(NaN|Infinity|(\d+\.?\d*|\.\d+)([eE][+-]?\d+)?[fFdD]?)$/.test(t)) {
        throw new Error('NumberFormatException: For input string: "' + s + '"');
    }
    return Math.fround(Number.parseFloat(t.replace(/[fFdD]$/, '')));
}
