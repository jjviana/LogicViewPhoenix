import { Thread } from '../lang/Thread.js';

/**
 * A very small Swing look-alike built from DOM elements.
 *
 * LogicView's user interface was written with Swing: a main JFrame with a menu
 * bar, non-modal JDialogs for every opened model and for the control dialogs,
 * JTabbedPanes, JOptionPane message boxes and ProgressMonitors. The port keeps
 * that structure, so the UI classes can stay close to the originals, and the
 * stylesheet (css/metal.css) dresses everything in the Metal "Ocean" theme.
 */

/** Creates an element: el('div', {className: 'x', onclick: f}, child, 'text', ...) */
export function el(tag, attributes = {}, ...children) {
    const e = document.createElement(tag);
    for (const [k, v] of Object.entries(attributes || {})) {
        if (v === undefined || v === null) {
            continue;
        }
        if (k === 'style' && typeof v === 'object') {
            Object.assign(e.style, v);
        } else if (k.startsWith('on') && typeof v === 'function') {
            e.addEventListener(k.substring(2).toLowerCase(), v);
        } else if (k in e && typeof v !== 'string') {
            e[k] = v;
        } else if (k === 'className' || k === 'textContent' || k === 'value' || k === 'checked' || k === 'disabled') {
            e[k] = v;
        } else {
            e.setAttribute(k, v);
        }
    }
    for (const c of children.flat()) {
        if (c === null || c === undefined || c === false) {
            continue;
        }
        e.append(c instanceof Node ? c : document.createTextNode(String(c)));
    }
    return e;
}

/* ------------------------------------------------------------------ */
/* Windows                                                              */
/* ------------------------------------------------------------------ */

let zCounter = 100;
let cascade = 0;

function desktop() {
    return document.getElementById('desktop') || document.body;
}

/**
 * javax.swing.JDialog: a floating, draggable and resizable window living in the
 * desktop area under the main frame.
 */
export class JDialog {
    constructor(parent = null, modal = false, { resizable = true, maximizable = false, className = '', centered = false } = {}) {
        this.parent = parent;
        this.modal = modal;
        this.centered = centered;
        this.windowListeners = [];
        this.visible = false;
        this.disposed = false;
        this.positioned = false;
        this.maximized = false;

        this.titleText = el('span', { className: 'lv-title-text' });
        const buttons = el('span', { className: 'lv-title-buttons' });
        if (maximizable) {
            buttons.append(el('button', {
                className: 'lv-title-button lv-maximize', title: 'Maximize', type: 'button',
                onclick: () => this.setMaximized(!this.maximized),
            }));
        }
        buttons.append(el('button', {
            className: 'lv-title-button lv-close', title: 'Close', type: 'button',
            onclick: () => this.processWindowClosing(),
        }));
        this.titleBar = el('div', { className: 'lv-titlebar' }, el('span', { className: 'lv-title-icon' }), this.titleText, buttons);
        this.menuBarSlot = el('div', { className: 'lv-menubar-slot' });
        this.contentPane = el('div', { className: 'lv-content' });
        this.root = el('div', {
            className: 'lv-window' + (resizable ? ' lv-resizable' : '') + (className ? ' ' + className : ''),
            role: 'dialog',
        }, this.titleBar, this.menuBarSlot, this.contentPane);
        this.root.addEventListener('pointerdown', () => this.toFront(), true);
        this.installDragging();
        this.titleBar.addEventListener('dblclick', (e) => {
            if (maximizable && !e.target.closest('button')) {
                this.setMaximized(!this.maximized);
            }
        });
    }

    installDragging() {
        let start = null;
        this.titleBar.addEventListener('pointerdown', (e) => {
            if (e.button !== 0 || e.target.closest('button') || this.maximized) {
                return;
            }
            start = { x: e.clientX, y: e.clientY, left: this.root.offsetLeft, top: this.root.offsetTop };
            this.titleBar.setPointerCapture(e.pointerId);
            e.preventDefault();
        });
        this.titleBar.addEventListener('pointermove', (e) => {
            if (!start) {
                return;
            }
            const d = desktop();
            const left = Math.min(Math.max(start.left + e.clientX - start.x, -this.root.offsetWidth + 80), d.clientWidth - 80);
            const top = Math.min(Math.max(start.top + e.clientY - start.y, 0), d.clientHeight - 24);
            this.root.style.left = left + 'px';
            this.root.style.top = top + 'px';
        });
        const end = () => {
            start = null;
        };
        this.titleBar.addEventListener('pointerup', end);
        this.titleBar.addEventListener('pointercancel', end);
    }

    setTitle(t) {
        this.title = t;
        this.titleText.textContent = t;
    }

    getTitle() {
        return this.title;
    }

    getContentPane() {
        return this.contentPane;
    }

    setJMenuBar(menuBar) {
        this.menuBarSlot.replaceChildren(menuBar.element);
    }

    setSize(width, height) {
        this.root.style.width = width + 'px';
        this.root.style.height = height + 'px';
    }

    /** Size to the preferred size of the content. */
    pack() {
        this.root.style.width = '';
        this.root.style.height = '';
    }

    setLocation(x, y) {
        this.root.style.left = x + 'px';
        this.root.style.top = y + 'px';
        this.positioned = true;
    }

    setMaximized(max) {
        this.maximized = max;
        this.root.classList.toggle('lv-maximized', max);
        this.root.dispatchEvent(new Event('lv-resized'));
    }

    addWindowListener(l) {
        this.windowListeners.push(l);
    }

    processWindowClosing() {
        for (const l of [...this.windowListeners]) {
            l.windowClosing && l.windowClosing(this);
        }
        if (!this.disposed) {
            this.setVisible(false);
        }
    }

    toFront() {
        if (this.modal) {
            return;
        }
        this.root.style.zIndex = ++zCounter;
        for (const w of document.querySelectorAll('.lv-window.lv-active')) {
            w.classList.remove('lv-active');
        }
        this.root.classList.add('lv-active');
    }

    isVisible() {
        return this.visible;
    }

    show() {
        this.setVisible(true);
    }

    hide() {
        this.setVisible(false);
    }

    setVisible(visible) {
        if (this.disposed) {
            return;
        }
        if (visible === this.visible) {
            if (visible) {
                this.toFront();
            }
            return;
        }
        this.visible = visible;
        const d = desktop();
        if (visible) {
            if (this.modal) {
                this.overlay = el('div', { className: 'lv-modal-overlay' });
                this.overlay.style.zIndex = ++zCounter + 10000;
                d.append(this.overlay);
                this.root.style.zIndex = ++zCounter + 10000;
            }
            d.append(this.root);
            if (!this.positioned) {
                this.placeInitially();
            }
            this.toFront();
            const focusable = this.contentPane.querySelector('input:not([type=hidden]), button, select');
            if (focusable && this.modal) {
                focusable.focus();
            }
        } else {
            this.root.remove();
            if (this.overlay) {
                this.overlay.remove();
                this.overlay = null;
            }
            for (const l of [...this.windowListeners]) {
                l.windowHidden && l.windowHidden(this);
            }
        }
    }

    placeInitially() {
        const d = desktop();
        const w = this.root.offsetWidth;
        const h = this.root.offsetHeight;
        let x;
        let y;
        if (this.parent && this.parent.root && this.parent.root.isConnected && !(this.parent instanceof JFrame)) {
            const p = this.parent.root;
            x = p.offsetLeft + (p.offsetWidth - w) / 2;
            y = p.offsetTop + Math.min(60, (p.offsetHeight - h) / 2);
        } else if (this.modal || this.centered) {
            x = (d.clientWidth - w) / 2;
            y = (d.clientHeight - h) / 3;
        } else {
            x = 24 + cascade * 28;
            y = 16 + cascade * 28;
            cascade = (cascade + 1) % 8;
        }
        x = Math.max(0, Math.min(x, d.clientWidth - w));
        y = Math.max(0, Math.min(y, d.clientHeight - h));
        this.setLocation(Math.round(x), Math.round(y));
    }

    dispose() {
        this.setVisible(false);
        this.disposed = true;
        for (const l of [...this.windowListeners]) {
            l.windowClosed && l.windowClosed(this);
        }
    }
}

/**
 * javax.swing.JFrame. The application has a single frame, the main window,
 * which takes the top of the page (title and menu bar); the rest of the page is
 * the "screen" where dialogs float.
 */
export class JFrame {
    constructor(title = '') {
        this.root = document.getElementById('main-frame');
        this.titleText = this.root.querySelector('.lv-frame-title');
        this.menuBarSlot = this.root.querySelector('.lv-frame-menubar');
        this.setTitle(title);
    }

    setTitle(t) {
        this.title = t;
        this.titleText.textContent = t;
        document.title = t;
    }

    setJMenuBar(menuBar) {
        this.menuBarSlot.replaceChildren(menuBar.element);
    }

    setSize() {
    }

    show() {
        this.root.hidden = false;
    }

    hide() {
        this.root.hidden = true;
    }

    setCursor(busy) {
        document.body.classList.toggle('lv-busy', !!busy);
    }
}

/* ------------------------------------------------------------------ */
/* Menus                                                                */
/* ------------------------------------------------------------------ */

export class JMenuBar {
    constructor() {
        this.element = el('div', { className: 'lv-menubar', role: 'menubar' });
        this.menus = [];
        document.addEventListener('pointerdown', (e) => {
            if (!this.element.contains(e.target)) {
                this.closeAll();
            }
        });
    }

    add(menu) {
        this.menus.push(menu);
        menu.bar = this;
        this.element.append(menu.element);
        return menu;
    }

    closeAll() {
        for (const m of this.menus) {
            m.setOpen(false);
        }
    }
}

export class JMenu {
    constructor(name) {
        this.name = name;
        this.button = el('button', { className: 'lv-menu-button', type: 'button', role: 'menuitem' }, name);
        this.popup = el('div', { className: 'lv-menu-popup', role: 'menu' });
        this.element = el('div', { className: 'lv-menu' }, this.button, this.popup);
        this.button.addEventListener('click', () => {
            const open = !this.element.classList.contains('lv-open');
            this.bar && this.bar.closeAll();
            this.setOpen(open);
        });
        this.button.addEventListener('pointerenter', () => {
            if (this.bar && this.bar.menus.some((m) => m !== this && m.element.classList.contains('lv-open'))) {
                this.bar.closeAll();
                this.setOpen(true);
            }
        });
    }

    setOpen(open) {
        this.element.classList.toggle('lv-open', open);
    }

    /** Adds an item; returns it so callers can attach an action listener. */
    add(label) {
        const item = el('button', { className: 'lv-menu-item', type: 'button', role: 'menuitem' }, label);
        const listeners = [];
        item.addEventListener('click', () => {
            this.bar && this.bar.closeAll();
            for (const l of listeners) {
                l({ source: menuItem });
            }
        });
        const menuItem = { element: item, addActionListener: (l) => listeners.push(l) };
        this.popup.append(item);
        return menuItem;
    }
}

/* ------------------------------------------------------------------ */
/* Tabs                                                                 */
/* ------------------------------------------------------------------ */

export class JTabbedPane {
    constructor() {
        this.tabs = [];
        this.selected = -1;
        this.changeListeners = [];
        this.tabStrip = el('div', { className: 'lv-tabstrip', role: 'tablist' });
        this.body = el('div', { className: 'lv-tabbody' });
        this.element = el('div', { className: 'lv-tabbedpane' }, this.tabStrip, this.body);
    }

    addTab(title, component) {
        const index = this.tabs.length;
        const tab = el('button', { className: 'lv-tab', type: 'button', role: 'tab', onclick: () => this.setSelectedIndex(index) }, title);
        const page = el('div', { className: 'lv-tabpage', role: 'tabpanel' }, component);
        page.hidden = true;
        this.tabs.push({ title, component, tab, page });
        this.tabStrip.append(tab);
        this.body.append(page);
        if (this.selected < 0) {
            this.setSelectedIndex(0, false);
        }
    }

    /** JTabbedPane.add(title, component) */
    add(title, component) {
        this.addTab(title, component);
    }

    getTabCount() {
        return this.tabs.length;
    }

    getSelectedIndex() {
        return this.selected;
    }

    setSelectedIndex(i, notify = true) {
        if (i === this.selected) {
            return;
        }
        this.selected = i;
        this.tabs.forEach((t, j) => {
            t.tab.classList.toggle('lv-selected', i === j);
            t.tab.setAttribute('aria-selected', String(i === j));
            t.page.hidden = i !== j;
        });
        if (notify) {
            for (const l of this.changeListeners) {
                l({ source: this });
            }
        }
    }

    addChangeListener(l) {
        this.changeListeners.push(l);
    }
}

/* ------------------------------------------------------------------ */
/* Small widgets                                                        */
/* ------------------------------------------------------------------ */

export function button(label, onclick, attrs = {}) {
    return el('button', { className: 'lv-button', type: 'button', onclick, ...attrs }, label);
}

export function textField(columns, value = '', attrs = {}) {
    return el('input', { className: 'lv-textfield', type: 'text', size: columns, value, ...attrs });
}

export function checkBox(label, selected = false, attrs = {}) {
    const input = el('input', { type: 'checkbox', checked: selected });
    const wrapper = el('label', { className: 'lv-checkbox', ...attrs }, input, el('span', {}, label));
    wrapper.input = input;
    return wrapper;
}

export function comboBox(items) {
    return el('select', { className: 'lv-combobox' }, items.map((i) => el('option', { value: i }, i)));
}

/** A panel with a javax.swing.border.TitledBorder. */
export function titledPanel(title, ...children) {
    const legend = el('legend', {}, title);
    const f = el('fieldset', { className: 'lv-titled' }, legend, ...children);
    f.setTitle = (t) => {
        legend.textContent = t;
    };
    return f;
}

/** A JProgressBar. */
export function progressBar(min = 0, max = 100) {
    const fill = el('div', { className: 'lv-progress-fill' });
    const bar = el('div', { className: 'lv-progress', role: 'progressbar' }, fill);
    bar.min = min;
    bar.max = max;
    bar.setMinimum = (v) => {
        bar.min = v;
    };
    bar.setMaximum = (v) => {
        bar.max = v;
    };
    bar.setValue = (v) => {
        const f = bar.max > bar.min ? (v - bar.min) / (bar.max - bar.min) : 0;
        fill.style.width = Math.max(0, Math.min(100, f * 100)) + '%';
        bar.setAttribute('aria-valuenow', String(v));
    };
    bar.setIndeterminate = (b) => bar.classList.toggle('lv-indeterminate', b);
    return bar;
}

/* ------------------------------------------------------------------ */
/* JOptionPane                                                          */
/* ------------------------------------------------------------------ */

export const JOptionPane = {
    ERROR_MESSAGE: 0,
    INFORMATION_MESSAGE: 1,
    WARNING_MESSAGE: 2,
    QUESTION_MESSAGE: 3,
    YES_OPTION: 0,
    NO_OPTION: 1,
    YES_NO_OPTION: 0,

    showMessageDialog(parent, message, title = 'Message', type = 1) {
        return optionDialog(parent, message, title, type, ['OK']).then(() => undefined);
    },

    /** Resolves to YES_OPTION (0) or NO_OPTION (1). */
    showConfirmDialog(parent, message, title = 'Select an Option', optionType = 0, type = 3) {
        return optionDialog(parent, message, title, type, ['Yes', 'No']).then((i) => (i === 0 ? 0 : 1));
    },
};

const ICONS = ['lv-icon-error', 'lv-icon-information', 'lv-icon-warning', 'lv-icon-question'];

function optionDialog(parent, message, title, type, options) {
    return new Promise((resolve) => {
        const d = new JDialog(parent, true, { resizable: false, className: 'lv-optionpane' });
        d.setTitle(title);
        let answered = false;
        const answer = (i) => {
            if (!answered) {
                answered = true;
                d.dispose();
                resolve(i);
            }
        };
        const text = el('div', { className: 'lv-option-message' });
        for (const line of String(message).split('\n')) {
            text.append(el('div', {}, line));
        }
        d.getContentPane().append(
            el('div', { className: 'lv-option-body' }, el('div', { className: 'lv-option-icon ' + (ICONS[type] || '') }), text),
            el('div', { className: 'lv-button-row' }, options.map((o, i) => button(o, () => answer(i)))),
        );
        d.addWindowListener({ windowClosing: () => answer(-1) });
        d.root.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                answer(-1);
            }
        });
        d.setVisible(true);
    });
}

/* ------------------------------------------------------------------ */
/* ProgressMonitor                                                      */
/* ------------------------------------------------------------------ */

/**
 * javax.swing.ProgressMonitor: a small dialog showing a message, a note and a
 * progress bar, with a Cancel button.
 */
export class ProgressMonitor {
    constructor(parent, message, note, min, max) {
        this.min = min;
        this.max = max;
        this.canceled = false;
        this.millisToDecideToPopup = 500;
        this.millisToPopup = 0;
        this.created = performance.now();
        this.dialog = new JDialog(parent, false, { resizable: false, className: 'lv-progressmonitor', centered: true });
        this.dialog.setTitle('Progress...');
        this.noteLabel = el('div', { className: 'lv-progress-note' }, note || ' ');
        this.bar = progressBar(min, max);
        this.dialog.getContentPane().append(
            el('div', { className: 'lv-progress-body' },
                el('div', { className: 'lv-option-icon lv-icon-information' }),
                el('div', { className: 'lv-progress-text' }, el('div', { className: 'lv-progress-message' }, message), this.noteLabel, this.bar)),
            el('div', { className: 'lv-button-row' }, button('Cancel', () => {
                this.canceled = true;
                this.close();
            })),
        );
        this.dialog.addWindowListener({ windowClosing: () => {
            this.canceled = true;
        } });
    }

    setMillisToDecideToPopup(ms) {
        this.millisToDecideToPopup = ms;
    }

    setMillisToPopup(ms) {
        this.millisToPopup = ms;
    }

    setProgress(v) {
        if (this.closed) {
            return;
        }
        this.bar.setValue(v);
        if (!this.dialog.isVisible() && performance.now() - this.created >= Math.min(this.millisToDecideToPopup, this.millisToPopup) && v < this.max) {
            this.dialog.setVisible(true);
        }
        if (v >= this.max) {
            this.close();
        }
    }

    setNote(note) {
        this.noteLabel.textContent = note;
    }

    isCanceled() {
        return this.canceled;
    }

    close() {
        this.closed = true;
        this.dialog.dispose();
    }
}

/** Lets the browser paint: the port of "do this in another Thread". */
export const yieldToUI = () => Thread.yield();

/** Waits for the next animation frame (or a timeout when the page is hidden). */
export function nextFrame() {
    return new Promise((resolve) => {
        let done = false;
        const finish = () => {
            if (!done) {
                done = true;
                resolve();
            }
        };
        requestAnimationFrame(finish);
        setTimeout(finish, 100);
    });
}
