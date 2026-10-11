import { ResourceBundle } from '../../../../compat/util/ResourceBundle.js';
import { System } from '../../../../compat/util/Logger.js';
import { JDialog, JOptionPane, ProgressMonitor, el, button, comboBox, nextFrame } from '../../../../compat/swing/Swing.js';
import { ResourceLocation, LocalFileSystem, ZipFileSystem } from '../../../../compat/net/ResourceLocation.js';
import { UIAction } from './UIAction.js';
import { MainWindow } from '../MainWindow/MainWindow.js';
import { ModelLoader } from '../loader/ModelLoader.js';
import { Viewer } from '../viewer/Viewer.js';

const res = await ResourceBundle.getBundle(new URL('./LoadModel.properties', import.meta.url));

/** Models shipped with the application, offered in the open dialog. */
export const SAMPLE_MODELS = [
    {
        title: 'SAM head, 1800 MHz, heterogeneous — SAR',
        location: 'data/SAMHead_1800_Het/SAR.3df',
    },
    {
        title: 'SAM head, 1800 MHz, heterogeneous — materials',
        location: 'data/SAMHead_1800_Het/material_3d_space.3dm',
    },
];

/**
 * Model > Load Model...: asks for a file with a file chooser whose filters are
 * the active ModelLoaders, loads it with the loader of the chosen filter and
 * hands it to the main window together with the viewers that support it.
 *
 * Browsers cannot read the files next to the one the user picked, and a model
 * is several files (description, data, material model...), so the chooser
 * accepts a set of files, a folder or a .zip archive and lists the files that
 * match the selected file type.
 */
export class LoadModel {
    static implements = [UIAction];

    constructor() {
        this.rep = null;
        this.active = false;
    }

    deactivate() {
        this.active = false;
    }

    execute(context) {
        const main = context;
        const loaders = this.rep.findByInterface(ModelLoader);
        if (loaders.length === 0) {
            JOptionPane.showMessageDialog(main, 'No active loaders found', res.getString('errorMessageTitle'), JOptionPane.ERROR_MESSAGE);
            return;
        }
        const chooser = new ModelChooser(main, loaders);
        chooser.onApprove = (location, loader) => this.load(main, location, loader);
        chooser.setVisible(true);
    }

    /** The body of the original loading thread. Resolves to the new view window, or null. */
    async load(main, location, l) {
        main.getParentFrame().setCursor(true);
        const monitor = new ProgressMonitor(null, 'Loading ' + l.getTypeName(), null, 0, 100);
        monitor.setMillisToPopup(0);
        monitor.setMillisToDecideToPopup(0);
        monitor.setProgress(10);
        const loaders = this.rep.findByInterface(ModelLoader);
        const loadListener = {
            loadStarting: (loader) => monitor.setNote(loader.getTypeName() + ': loading...'),
            loadFinished: () => {},
            loadingPercentComplete: (loader, percent) => {
                monitor.setNote(loader.getTypeName() + ': ' + Math.round(percent) + '%');
                monitor.setProgress(10 + 0.4 * percent);
            },
        };
        let viewerListener = null;
        const viewers = this.rep.findByInterface(Viewer);
        try {
            System.err.println('location=' + location);
            for (const loader of loaders) {
                loader.addModelLoadListener(loadListener);
            }
            let mod;
            try {
                mod = await l.LoadModel(location);
            } finally {
                for (const loader of loaders) {
                    loader.removeModelLoadListener(loadListener);
                }
            }
            if (viewers.length === 0) {
                throw new Error('No active viewers found');
            }
            const supported = viewers.filter((v) => v.isModelSupported(mod));
            if (supported.length === 0) {
                throw new Error('No viewer found for model ' + l.getTypeName());
            }
            if (monitor.isCanceled()) {
                return null;
            }
            monitor.setNote('Constructing view...');
            monitor.setProgress(50);
            viewerListener = {
                viewConstructionStarting: () => {},
                viewConstructionFinished: () => {},
                viewConstructionProgress: (title, percent) => {
                    monitor.setNote(title + '...');
                    monitor.setProgress(50 + 0.49 * percent);
                },
            };
            for (const v of supported) {
                v.addViewConstructionListener(viewerListener);
            }
            await nextFrame();
            const window = await main.addDocView(mod, supported);
            if (window && window.view && window.view.whenReady) {
                await window.view.whenReady();
            }
            monitor.close();
            return window;
        } catch (t) {
            System.err.printStackTrace(t);
            monitor.close();
            await JOptionPane.showMessageDialog(main, res.getString('errorMessage') + '\n\n' + (t.message || t), res.getString('errorMessageTitle'), JOptionPane.ERROR_MESSAGE);
            return null;
        } finally {
            if (viewerListener) {
                for (const v of viewers) {
                    v.removeViewConstructionListener(viewerListener);
                }
            }
            main.getParentFrame().setCursor(false);
        }
    }

    getActionGroup() {
        return MainWindow.MODEL_ACTION_GROUP;
    }

    getDescription() {
        return res.getString('description');
    }

    getIcon() {
        return null;
    }

    getName() {
        return res.getString('name') + '...';
    }

    init(c) {
        this.rep = c;
        this.active = true;
        return true;
    }

    isActive() {
        return this.active;
    }

    isInContext(context) {
        return context instanceof MainWindow;
    }
}

/** LoadModel.NameFilter: accepts the files with the extension of its loader. */
export class NameFilter {
    constructor(m) {
        this.loader = m;
    }

    accept(name) {
        return name.endsWith(this.loader.getFilenameExtension());
    }

    getDescription() {
        return this.loader.getTypeName() + ' (*' + this.loader.getFilenameExtension() + ')';
    }
}

/** The JFileChooser of the original, adapted to what a browser may do. */
class ModelChooser extends JDialog {
    constructor(main, loaders) {
        super(main, true, { className: 'lv-chooser' });
        this.setTitle(res.getString('fileChooserTitle'));
        this.filters = loaders.map((l) => new NameFilter(l));
        this.fileSystems = [];
        this.onApprove = null;

        this.typeCombo = comboBox(this.filters.map((f) => f.getDescription()));
        this.typeCombo.addEventListener('change', () => this.refreshList());
        this.list = el('select', { className: 'lv-filelist', size: 8 });
        this.list.addEventListener('dblclick', () => this.approve());
        this.list.addEventListener('change', () => {
            this.openButton.disabled = this.list.selectedIndex < 0;
        });

        const filesInput = el('input', { type: 'file', multiple: true, hidden: true });
        filesInput.addEventListener('change', () => this.addFiles([...filesInput.files], 'files'));
        const folderInput = el('input', { type: 'file', webkitdirectory: '', hidden: true });
        folderInput.addEventListener('change', () => this.addFiles([...folderInput.files], 'folder'));

        this.dropZone = el('div', { className: 'lv-dropzone' },
            el('div', { className: 'lv-dropzone-title' }, 'Drop model files, a folder or a .zip archive here'),
            el('div', { className: 'lv-button-row lv-left' },
                button('Files…', () => filesInput.click()),
                button('Folder…', () => folderInput.click())),
            filesInput, folderInput);
        this.installDrop();

        this.samples = el('select', { className: 'lv-filelist', size: SAMPLE_MODELS.length });
        for (const s of SAMPLE_MODELS) {
            this.samples.append(el('option', { value: s.location }, s.title));
        }
        this.samples.addEventListener('dblclick', () => this.approveSample());
        this.samples.addEventListener('change', () => {
            this.list.selectedIndex = -1;
            this.openButton.disabled = false;
        });
        this.list.addEventListener('focus', () => {
            this.samples.selectedIndex = -1;
        });

        this.openButton = button('Open', () => this.approve());
        this.openButton.disabled = true;
        this.getContentPane().classList.add('lv-padded');
        this.getContentPane().append(
            el('div', { className: 'lv-form-row' }, el('label', {}, 'Files of type:'), this.typeCombo),
            this.dropZone,
            el('div', { className: 'lv-caption' }, 'Matching files:'),
            this.list,
            el('div', { className: 'lv-caption' }, 'Sample models:'),
            this.samples,
            el('div', { className: 'lv-button-row' }, this.openButton, button('Cancel', () => this.dispose())));
        this.addWindowListener({ windowClosing: () => this.dispose() });
        this.root.style.width = '460px';
        this.refreshList();
    }

    installDrop() {
        const zone = this.root;
        zone.addEventListener('dragover', (e) => {
            e.preventDefault();
            this.dropZone.classList.add('lv-dragging');
        });
        zone.addEventListener('dragleave', () => this.dropZone.classList.remove('lv-dragging'));
        zone.addEventListener('drop', async (e) => {
            e.preventDefault();
            this.dropZone.classList.remove('lv-dragging');
            const files = [];
            const entries = [...e.dataTransfer.items].map((i) => i.webkitGetAsEntry && i.webkitGetAsEntry()).filter(Boolean);
            if (entries.length > 0) {
                for (const entry of entries) {
                    await collectEntry(entry, files);
                }
            } else {
                for (const f of e.dataTransfer.files) {
                    files.push([f.name, f]);
                }
            }
            this.addEntries(files, 'dropped files');
        });
    }

    addFiles(files, label) {
        this.addEntries(files.map((f) => [f.webkitRelativePath || f.name, f]), label);
    }

    async addEntries(entries, label) {
        const plain = [];
        for (const [path, file] of entries) {
            if (path.toLowerCase().endsWith('.zip')) {
                try {
                    this.fileSystems.push(await ZipFileSystem.open(file));
                } catch (e) {
                    JOptionPane.showMessageDialog(this, e.message, res.getString('errorMessageTitle'), JOptionPane.ERROR_MESSAGE);
                }
            } else {
                plain.push([path, file]);
            }
        }
        if (plain.length > 0) {
            this.fileSystems.push(new LocalFileSystem(plain, label));
        }
        this.refreshList();
    }

    refreshList() {
        const filter = this.filters[this.typeCombo.selectedIndex];
        this.list.replaceChildren();
        this.candidates = [];
        for (const fs of this.fileSystems) {
            for (const path of fs.list()) {
                if (filter.accept(path)) {
                    this.candidates.push(new ResourceLocation(fs, path));
                    this.list.append(el('option', {}, (fs instanceof ZipFileSystem ? fs.label + ' › ' : '') + path.replace(/^\//, '')));
                }
            }
        }
        if (this.candidates.length > 0) {
            this.list.selectedIndex = 0;
            this.samples.selectedIndex = -1;
        }
        this.openButton.disabled = this.list.selectedIndex < 0 && this.samples.selectedIndex < 0;
    }

    loaderForSample(location) {
        const selected = this.filters[this.typeCombo.selectedIndex];
        if (selected.accept(location)) {
            return selected.loader;
        }
        const filter = this.filters.find((f) => f.accept(location));
        return filter ? filter.loader : null;
    }

    approveSample() {
        const location = this.samples.value;
        const loader = this.loaderForSample(location);
        if (loader) {
            this.dispose();
            this.onApprove(ResourceLocation.fromURL(location), loader);
        }
    }

    approve() {
        if (this.samples.selectedIndex >= 0) {
            this.approveSample();
            return;
        }
        const i = this.list.selectedIndex;
        if (i < 0) {
            return;
        }
        const loader = this.filters[this.typeCombo.selectedIndex].loader;
        this.dispose();
        this.onApprove(this.candidates[i], loader);
    }
}

/** Walks a dropped FileSystemEntry, collecting [path, File] pairs. */
async function collectEntry(entry, out) {
    if (entry.isFile) {
        const file = await new Promise((resolve, reject) => entry.file(resolve, reject));
        out.push([entry.fullPath, file]);
    } else if (entry.isDirectory) {
        const reader = entry.createReader();
        for (;;) {
            const batch = await new Promise((resolve, reject) => reader.readEntries(resolve, reject));
            if (batch.length === 0) {
                break;
            }
            for (const e of batch) {
                await collectEntry(e, out);
            }
        }
    }
}
