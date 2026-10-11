/**
 * Browser entry point: the equivalent of `java -jar Logicview.jar`.
 */
import * as THREE from 'three';
import { Kernel } from './com/logicstyle/mosaic/system/Kernel.js';

// Java 3D (and the OpenGL of its time) knew nothing about color management:
// colors go to the screen exactly as specified.
THREE.ColorManagement.enabled = false;

function fatal(message) {
    const boot = document.getElementById('boot');
    boot.hidden = false;
    boot.classList.add('lv-fatal');
    boot.textContent = message;
}

/** Some components could not be loaded: say which, instead of failing later. */
async function reportComponentFailures(failures) {
    if (!failures || failures.length === 0) {
        return;
    }
    const { JOptionPane } = await import('./compat/swing/Swing.js');
    const lines = failures.map((f) => '\u2022 ' + f.component.substring(f.component.lastIndexOf('.') + 1) + ': ' + f.reason);
    JOptionPane.showMessageDialog(null,
        'Some LogicView components could not be started, so parts of the application will not work:\n\n'
        + lines.join('\n')
        + '\n\nIf the site was just updated, reload the page (Shift+Reload) in a few minutes.',
        'Components not started', JOptionPane.WARNING_MESSAGE);
}

function hasWebGL2() {
    try {
        return !!document.createElement('canvas').getContext('webgl2');
    } catch (e) {
        return false;
    }
}

if (!hasWebGL2()) {
    fatal('LogicView needs a browser with WebGL 2 enabled.');
} else {
    const config = new URLSearchParams(window.location.search).get('config');
    try {
        window.logicview = await Kernel.main(config ? [config] : []);
        document.getElementById('boot').hidden = true;
        reportComponentFailures(window.logicview.failures);
    } catch (e) {
        console.error(e);
        fatal('LogicView could not start: ' + (e.message || e));
    }
}
