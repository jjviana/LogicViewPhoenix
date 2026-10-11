/**
 * javax.media.j3d.Text3D with a Font3D built from new Font("Dialog", Font.BOLD, 1)
 * and the default FontExtrusion (0.2 deep): extruded 3D letters one unit high.
 */
import { FontLoader } from 'three/addons/loaders/FontLoader.js';
import { TextGeometry } from 'three/addons/geometries/TextGeometry.js';

const fontUrl = new URL('../../../vendor/three/fonts/helvetiker_bold.typeface.json', import.meta.url);
const font = new FontLoader().parse(await (await fetch(fontUrl)).json());

export function createText3DGeometry(text) {
    const geometry = new TextGeometry(text, {
        font,
        size: 1,
        height: 0.2, // FontExtrusion default depth
        curveSegments: 6,
        bevelEnabled: false,
    });
    // Java 3D extruded the glyphs towards -z.
    geometry.translate(0, 0, -0.2);
    return geometry;
}
