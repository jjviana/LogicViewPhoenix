/**
 * java.awt.Font with the few metric queries the printing code needs
 * (getStringBounds / getLineMetrics). "Dialog" is mapped to the platform
 * sans-serif font, as the JRE did.
 */
const measureCanvas = typeof OffscreenCanvas !== 'undefined' ? new OffscreenCanvas(1, 1) : null;

export class Font {
    static PLAIN = 0;
    static BOLD = 1;
    static ITALIC = 2;

    constructor(name, style, size) {
        this.name = name;
        this.style = style;
        this.size = size;
    }

    toCss() {
        const family = this.name === 'Dialog' ? '"Lucida Grande", "Segoe UI", Tahoma, Arial, sans-serif' : this.name;
        return `${this.style & Font.ITALIC ? 'italic ' : ''}${this.style & Font.BOLD ? 'bold ' : ''}${this.size}px ${family}`;
    }

    getLineMetrics() {
        const ascent = this.size * 0.93;
        const descent = this.size * 0.24;
        const leading = this.size * 0.03;
        return { ascent, descent, leading, height: ascent + descent + leading, getAscent: () => ascent, getHeight: () => ascent + descent + leading };
    }

    getStringBounds(text) {
        let width = text.length * this.size * 0.6;
        const ctx = measureCanvas && measureCanvas.getContext('2d');
        if (ctx) {
            ctx.font = this.toCss();
            width = ctx.measureText(text).width;
        }
        const m = this.getLineMetrics();
        return { width, height: m.height, getWidth: () => width, getHeight: () => m.height };
    }
}
