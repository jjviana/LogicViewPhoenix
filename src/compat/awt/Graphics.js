import { Color } from './Color.js';

/**
 * java.awt.Graphics2D on top of a CanvasRenderingContext2D.
 * Only the operations used by LogicView's printing code are provided.
 * `create(x, y, w, h)` returns a translated and clipped child; callers must
 * `dispose()` it before using the parent again (the canvas state stack is shared).
 */
export class Graphics {
    constructor(ctx) {
        this.ctx = ctx;
        this.color = Color.black;
        this.font = null;
    }

    create(x, y, width, height) {
        this.ctx.save();
        this.ctx.translate(x, y);
        this.ctx.beginPath();
        this.ctx.rect(0, 0, width, height);
        this.ctx.clip();
        const g = new Graphics(this.ctx);
        g.color = this.color;
        g.font = this.font;
        g.disposed = false;
        g.dispose = () => {
            if (!g.disposed) {
                g.disposed = true;
                this.ctx.restore();
            }
        };
        return g;
    }

    dispose() {
    }

    setColor(c) {
        this.color = c;
    }

    setBackground() {
    }

    setFont(f) {
        this.font = f;
    }

    setStroke(width) {
        this.ctx.lineWidth = width;
    }

    fillRect(x, y, w, h) {
        this.ctx.fillStyle = this.color.toCss();
        this.ctx.fillRect(x, y, w, h);
    }

    drawRect(x, y, w, h) {
        this.ctx.strokeStyle = this.color.toCss();
        this.ctx.strokeRect(x + 0.5, y + 0.5, w, h);
    }

    drawLine(x1, y1, x2, y2) {
        this.ctx.strokeStyle = this.color.toCss();
        this.ctx.beginPath();
        this.ctx.moveTo(x1, y1);
        this.ctx.lineTo(x2, y2);
        this.ctx.stroke();
    }

    drawString(text, x, y) {
        this.ctx.fillStyle = this.color.toCss();
        if (this.font) {
            this.ctx.font = this.font.toCss();
        }
        this.ctx.textBaseline = 'alphabetic';
        this.ctx.fillText(text, x, y);
    }

    drawImage(image, x, y) {
        this.ctx.drawImage(image, x, y);
    }
}
