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
    } catch (e) {
        console.error(e);
        fatal('LogicView could not start: ' + (e.message || e));
    }
}
