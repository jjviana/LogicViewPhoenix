import { ResourceBundle } from '../../../../../compat/util/ResourceBundle.js';
import { Graphics } from '../../../../../compat/awt/Graphics.js';
import { System } from '../../../../../compat/util/Logger.js';
import { JDialog, JOptionPane, el, button, comboBox, titledPanel } from '../../../../../compat/swing/Swing.js';
import { ViewPrinter } from './ViewPrinter.js';
import { ViewPrintConstructorFactory } from './ViewPrintConstructorFactory.js';
import { ImagePrinterFactory } from './ImagePrinterFactory.js';
import { MainWindow } from '../../MainWindow/MainWindow.js';

const res = await ResourceBundle.getBundle(new URL('./DefaultViewPrinter.properties', import.meta.url));

/** Opens the "Printing options" window for a view: destination, elements, preview, Print. */
export class DefaultViewPrinter {
    static implements = [ViewPrinter];

    constructor() {
        this.active = false;
        this.rep = null;
    }

    canPrint() {
        return true;
    }

    deactivate() {
        this.active = false;
    }

    getDescription() {
        return res.getString('description');
    }

    getName() {
        return res.getString('name');
    }

    init(c) {
        this.rep = c;
        return true;
    }

    isActive() {
        return this.active;
    }

    print(v) {
        const pf = new PrintOptionsFrame(this.rep, v);
        pf.setVisible(true);
    }
}

class PrintOptionsFrame extends JDialog {
    constructor(rep, v) {
        super(rep.findComponentByInterface(MainWindow), false, { maximizable: true });
        this.setTitle(res.getString('PrintOptionsTitle'));
        const factory = rep.findComponentByInterface(ViewPrintConstructorFactory);
        this.printConstructor = factory.getViewPrintConstructor(v);
        this.destp = new DestinationPanel(rep);
        const printButton = button(res.getString('printbuttonlabel'), () => this.printSnapshot());
        this.getContentPane().classList.add('lv-column');
        this.getContentPane().append(this.destp.element, this.printConstructor.getPrintConstructorComponent(), el('div', { className: 'lv-button-row' }, printButton));
        this.addWindowListener({ windowClosing: () => this.dispose() });
        this.setSize(640, 480);
    }

    async printSnapshot() {
        const p = this.destp.getSelectedPrinter();
        const size = p.getPreferredSampleSize();
        const out = document.createElement('canvas');
        out.width = size.width;
        out.height = size.height;
        const ctx = out.getContext('2d');
        ctx.fillStyle = '#000';
        ctx.fillRect(0, 0, size.width, size.height);
        try {
            await this.printConstructor.printView(new Graphics(ctx), size);
            if (await p.printImage(out)) {
                await JOptionPane.showMessageDialog(this, res.getString('printOkmessage'), res.getString('printOkmessagetitle'), JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (e) {
            System.err.printStackTrace(e);
            await JOptionPane.showMessageDialog(this, String(e.message || e), 'Error', JOptionPane.ERROR_MESSAGE);
        }
    }
}

class DestinationPanel {
    constructor(rep) {
        this.destinations = [];
        for (const f of rep.findByInterface(ImagePrinterFactory)) {
            this.destinations.push(f.createImagePrinter());
        }
        this.destinationsComboBox = comboBox(this.destinations.map((d) => d.getDescription()));
        this.configHolder = el('span', {}, this.destinations.length > 0 ? this.destinations[0].getConfigurationComponent() : '');
        this.destinationsComboBox.addEventListener('change', () => {
            this.configHolder.replaceChildren(this.getSelectedPrinter().getConfigurationComponent());
        });
        this.element = titledPanel(res.getString('destinationPanelTitle'),
            el('div', { className: 'lv-flow' }, el('span', {}, res.getString('destinationlabel')), this.destinationsComboBox, this.configHolder));
    }

    getSelectedPrinter() {
        return this.destinations[this.destinationsComboBox.selectedIndex];
    }
}
