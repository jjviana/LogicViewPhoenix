import * as THREE from 'three';
import { Thread } from '../../../../../../compat/lang/Thread.js';
import { System } from '../../../../../../compat/util/Logger.js';
import { VoxellGeometryGenerator } from './VoxellGeometryGenerator.js';
import { VoxellColorGeneratorControlFactory } from '../color/VoxellColorGeneratorControlFactory.js';
import { ViewControlListenerSupport } from '../ViewControlSupport.js';

/**
 * Renders the enabled voxels as one "optimized polyhedron": for every enabled
 * voxel, each face that touches a disabled voxel (or the border of the grid)
 * becomes a quad, colored by the first color generator that accepts the model.
 *
 * Voxel (i, j, k) spans x = x0 + i*dx, y = y0 - j*dy, z = z0 - k*dz with the
 * grid centered on the origin, so j grows downwards and k away from the viewer.
 *
 * The original appended Point3f objects to Vectors and handed them to a
 * GeometryInfo as a QUAD_ARRAY. Here isEnabled() is evaluated once per voxel
 * into a mask, the exposed faces are counted, and the vertices, normals and
 * colors are written straight into typed arrays (two triangles per quad).
 */
export class PolyedronGeometryGenerator {
    static implements = [VoxellGeometryGenerator];

    constructor(rep) {
        this.rep = rep;
        this.listenerList = null;
    }

    controlComponentHidden() {
    }

    controlComponentShown() {
    }

    getControlComponent() {
        return null;
    }

    async getGeometry(cur, progress = () => {}) {
        let vcg = null;
        const colgen = this.rep.findByInterface(VoxellColorGeneratorControlFactory);
        for (let i = 0; i < colgen.length; ++i) {
            const vcgf = colgen[i];
            if (vcgf != null && (vcg = vcgf.getViewControl(cur)) != null) {
                break;
            }
        }
        const deltaX = Math.fround(cur.getDeltaX());
        const deltaY = Math.fround(cur.getDeltaY());
        const deltaZ = Math.fround(cur.getDeltaZ());
        const sizeX = cur.getWidth();
        const sizeY = cur.getHeight();
        const sizeZ = cur.getDepth();
        const y0 = Math.trunc(sizeY / 2) * deltaY;
        const z0 = Math.trunc(sizeZ / 2) * deltaZ;
        const x0 = Math.trunc(-sizeX / 2) * deltaX;

        // Pass 1: which voxels are enabled.
        const strideX = sizeY * sizeZ;
        const mask = new Uint8Array(sizeX * strideX);
        let p = 0;
        for (let i = 0; i < sizeX; ++i) {
            for (let j = 0; j < sizeY; ++j) {
                for (let k = 0; k < sizeZ; ++k) {
                    mask[p++] = cur.isEnabled(i, j, k) ? 1 : 0;
                }
            }
            if (i % 4 === 3) {
                progress(0.6 * i / sizeX);
                await Thread.yield();
            }
        }

        // Pass 2: count the exposed faces.
        let faces = 0;
        p = 0;
        for (let i = 0; i < sizeX; ++i) {
            for (let j = 0; j < sizeY; ++j) {
                for (let k = 0; k < sizeZ; ++k, ++p) {
                    if (mask[p] === 0) {
                        continue;
                    }
                    if (i === 0 || mask[p - strideX] === 0) faces++;
                    if (i === sizeX - 1 || mask[p + strideX] === 0) faces++;
                    if (k === 0 || mask[p - 1] === 0) faces++;
                    if (k === sizeZ - 1 || mask[p + 1] === 0) faces++;
                    if (j === 0 || mask[p - sizeZ] === 0) faces++;
                    if (j === sizeY - 1 || mask[p + sizeZ] === 0) faces++;
                }
            }
        }
        if (faces === 0) {
            System.err.println('PolyedronGemoetryGenerator Warning: empty geometry');
            return null;
        }
        System.err.println('Number of vertices =' + faces * 4);
        progress(0.7);
        await Thread.yield();

        // Pass 3: emit the quads.
        const positions = new Float32Array(faces * 12);
        const normals = new Float32Array(faces * 12);
        const colors = new Uint8Array(faces * 12);
        const indices = new Uint32Array(faces * 6);
        let v = 0; // vertex counter
        let r = 0;
        let g = 0;
        let b = 0;
        const quad = (ax, ay, az, bx, by, bz, cx, cy, cz, dx, dy, dz, nx, ny, nz) => {
            const o = v * 3;
            positions[o] = ax; positions[o + 1] = ay; positions[o + 2] = az;
            positions[o + 3] = bx; positions[o + 4] = by; positions[o + 5] = bz;
            positions[o + 6] = cx; positions[o + 7] = cy; positions[o + 8] = cz;
            positions[o + 9] = dx; positions[o + 10] = dy; positions[o + 11] = dz;
            for (let q = 0; q < 12; q += 3) {
                normals[o + q] = nx; normals[o + q + 1] = ny; normals[o + q + 2] = nz;
                colors[o + q] = r; colors[o + q + 1] = g; colors[o + q + 2] = b;
            }
            const t = (v >> 2) * 6;
            indices[t] = v; indices[t + 1] = v + 1; indices[t + 2] = v + 2;
            indices[t + 3] = v; indices[t + 4] = v + 2; indices[t + 5] = v + 3;
            v += 4;
        };
        p = 0;
        for (let i = 0; i < sizeX; ++i) {
            const x = x0 + i * deltaX;
            const x1 = x + deltaX;
            for (let j = 0; j < sizeY; ++j) {
                const y = y0 - j * deltaY;
                const y1 = y - deltaY;
                for (let k = 0; k < sizeZ; ++k, ++p) {
                    if (mask[p] === 0) {
                        continue;
                    }
                    const left = i === 0 || mask[p - strideX] === 0;
                    const right = i === sizeX - 1 || mask[p + strideX] === 0;
                    const front = k === 0 || mask[p - 1] === 0;
                    const back = k === sizeZ - 1 || mask[p + 1] === 0;
                    const top = j === 0 || mask[p - sizeZ] === 0;
                    const bottom = j === sizeY - 1 || mask[p + sizeZ] === 0;
                    if (!(left || right || front || back || top || bottom)) {
                        continue;
                    }
                    if (vcg != null) {
                        const c = vcg.getColorForPoint(i, j, k, cur);
                        r = c.r;
                        g = c.g;
                        b = c.b;
                    } else {
                        r = g = b = 255;
                    }
                    const z = z0 - k * deltaZ;
                    const z1 = z - deltaZ;
                    if (left) quad(x, y, z, x, y1, z, x, y1, z1, x, y, z1, -1, 0, 0);
                    if (right) quad(x1, y, z, x1, y1, z, x1, y1, z1, x1, y, z1, 1, 0, 0);
                    if (front) quad(x, y, z, x1, y, z, x1, y1, z, x, y1, z, 0, 0, 1);
                    if (back) quad(x, y, z1, x1, y, z1, x1, y1, z1, x, y1, z1, 0, 0, -1);
                    if (top) quad(x, y, z, x1, y, z, x1, y, z1, x, y, z1, 0, 1, 0);
                    if (bottom) quad(x, y1, z, x1, y1, z, x1, y1, z1, x, y1, z1, 0, -1, 0);
                }
            }
            if (i % 16 === 15) {
                progress(0.7 + 0.3 * i / sizeX);
                await Thread.yield();
            }
        }
        const geometry = new THREE.BufferGeometry();
        geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
        geometry.setAttribute('normal', new THREE.BufferAttribute(normals, 3));
        geometry.setAttribute('color', new THREE.BufferAttribute(colors, 3, true));
        geometry.setIndex(new THREE.BufferAttribute(indices, 1));
        geometry.computeBoundingSphere();
        progress(1);
        return geometry;
    }

    getName() {
        return null;
    }

    isActive() {
        return true;
    }
}

Object.assign(PolyedronGeometryGenerator.prototype, ViewControlListenerSupport);
