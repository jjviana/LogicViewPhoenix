/** java.awt.Color (RGB only, components 0..255). */
export class Color {
    constructor(r, g, b) {
        if (g === undefined) {
            // new Color(int rgb)
            const rgb = r;
            this.r = (rgb >>> 16) & 0xff;
            this.g = (rgb >>> 8) & 0xff;
            this.b = rgb & 0xff;
        } else {
            this.r = r | 0;
            this.g = g | 0;
            this.b = b | 0;
        }
    }

    getRed() {
        return this.r;
    }

    getGreen() {
        return this.g;
    }

    getBlue() {
        return this.b;
    }

    /** ARGB packed integer, alpha fixed at 255 like java.awt.Color.getRGB(). */
    getRGB() {
        return (0xff000000 | (this.r << 16) | (this.g << 8) | this.b) | 0;
    }

    equals(o) {
        return o instanceof Color && o.r === this.r && o.g === this.g && o.b === this.b;
    }

    /** CSS representation, for drawing on canvases and DOM elements. */
    toCss() {
        return `rgb(${this.r},${this.g},${this.b})`;
    }

    toHex() {
        return '#' + [this.r, this.g, this.b].map((c) => c.toString(16).padStart(2, '0')).join('');
    }

    static fromHex(hex) {
        const v = parseInt(hex.replace('#', ''), 16);
        return new Color(v);
    }
}

Color.white = Color.WHITE = new Color(255, 255, 255);
Color.black = Color.BLACK = new Color(0, 0, 0);
Color.red = Color.RED = new Color(255, 0, 0);
Color.blue = Color.BLUE = new Color(0, 0, 255);
