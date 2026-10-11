/**
 * The pieces of Java 3D that LogicView used, rebuilt on top of three.js.
 *
 * Java 3D was a scene graph API, so is three.js; the mapping is direct:
 *
 *   BranchGroup     -> BranchGroup (a THREE.Group)
 *   TransformGroup  -> TransformGroup (a THREE.Group whose matrix is set explicitly,
 *                      like a Transform3D)
 *   Shape3D         -> THREE.Mesh / THREE.LineSegments / THREE.Points
 *   SpotLight       -> SpotLight (a scene graph node read by the lighting shader)
 *   Behavior        -> Behavior (a scene graph node receiving the canvas' mouse events)
 *   Appearance + Material -> createLitMaterial(): a shader reproducing the
 *                      fixed-function OpenGL lighting Java 3D used (per vertex
 *                      Gouraud shading, spot exponent and attenuation, no
 *                      two-sided lighting), so the scene is lit like the original.
 */
import * as THREE from 'three';

export const MAX_LIGHTS = 8;

/** javax.media.j3d.BranchGroup */
export class BranchGroup extends THREE.Group {
    constructor() {
        super();
        this.name = 'BranchGroup';
    }

    addChild(node) {
        this.add(node);
    }

    removeAllChildren() {
        this.clear();
    }

    removeChild(node) {
        this.remove(node);
    }
}

/** javax.media.j3d.TransformGroup: a group with an explicit Transform3D (a Matrix4). */
export class TransformGroup extends BranchGroup {
    constructor() {
        super();
        this.name = 'TransformGroup';
        this.matrixAutoUpdate = false;
    }

    getTransform(target) {
        return target.copy(this.matrix);
    }

    setTransform(m) {
        this.matrix.copy(m);
        this.matrixWorldNeedsUpdate = true;
        this.dispatchEvent({ type: 'transformChanged' });
    }
}

/**
 * javax.media.j3d.SpotLight(color, position, attenuation, direction, spreadAngle, concentration)
 * Attenuation is (constant, linear, quadratic); spreadAngle is the half angle of
 * the cone; concentration is the spot exponent.
 */
export class SpotLight extends THREE.Object3D {
    constructor(color, position, attenuation, direction, spreadAngle, concentration) {
        super();
        this.name = 'SpotLight';
        this.isJ3DSpotLight = true;
        this.lightColor = new THREE.Vector3(color.r / 255, color.g / 255, color.b / 255);
        this.lightPosition = new THREE.Vector3(...position);
        this.attenuation = new THREE.Vector3(...attenuation);
        this.direction = new THREE.Vector3(...direction).normalize();
        this.spreadAngle = spreadAngle;
        this.concentration = concentration;
    }

    /** Java 3D used influencing bounds; every light here influences the whole scene. */
    setInfluencingBounds() {
    }
}

/**
 * javax.media.j3d.Behavior: a node that reacts to stimuli. The view delivers
 * the canvas mouse events to every Behavior in its scene graph.
 */
export class Behavior extends THREE.Object3D {
    constructor() {
        super();
        this.name = 'Behavior';
        this.isBehavior = true;
    }

    setSchedulingBounds() {
    }

    getParent() {
        return this.parent;
    }

    /** Receives the list of pending stimuli (mouse events). */
    processStimulus() {
    }
}

/**
 * The lights of one view, shared by all its lit materials. update() collects
 * the SpotLights of the scene and transforms them into eye coordinates.
 */
export class LightingEnvironment {
    constructor() {
        this.uniforms = {
            uNumLights: { value: 0 },
            uLightPos: { value: Array.from({ length: MAX_LIGHTS }, () => new THREE.Vector3()) },
            uLightDir: { value: Array.from({ length: MAX_LIGHTS }, () => new THREE.Vector3()) },
            uLightColor: { value: Array.from({ length: MAX_LIGHTS }, () => new THREE.Vector3()) },
            uLightAtten: { value: Array.from({ length: MAX_LIGHTS }, () => new THREE.Vector3(1, 0, 0)) },
            uLightCosCutoff: { value: new Array(MAX_LIGHTS).fill(-1) },
            uLightExponent: { value: new Array(MAX_LIGHTS).fill(0) },
            uAmbientLight: { value: new THREE.Vector3(0, 0, 0) },
        };
        this.normalMatrix = new THREE.Matrix3();
        this.modelView = new THREE.Matrix4();
    }

    update(scene, camera) {
        const u = this.uniforms;
        let n = 0;
        scene.traverseVisible((o) => {
            if (!o.isJ3DSpotLight || n >= MAX_LIGHTS) {
                return;
            }
            this.modelView.multiplyMatrices(camera.matrixWorldInverse, o.matrixWorld);
            this.normalMatrix.getNormalMatrix(this.modelView);
            u.uLightPos.value[n].copy(o.lightPosition).applyMatrix4(this.modelView);
            u.uLightDir.value[n].copy(o.direction).applyMatrix3(this.normalMatrix).normalize();
            u.uLightColor.value[n].copy(o.lightColor);
            u.uLightAtten.value[n].copy(o.attenuation);
            u.uLightCosCutoff.value[n] = Math.cos(o.spreadAngle);
            u.uLightExponent.value[n] = o.concentration;
            n++;
        });
        u.uNumLights.value = n;
    }
}

const vertexShader = /* glsl */`
#define MAX_LIGHTS ${MAX_LIGHTS}
uniform int uNumLights;
uniform vec3 uLightPos[MAX_LIGHTS];
uniform vec3 uLightDir[MAX_LIGHTS];
uniform vec3 uLightColor[MAX_LIGHTS];
uniform vec3 uLightAtten[MAX_LIGHTS];
uniform float uLightCosCutoff[MAX_LIGHTS];
uniform float uLightExponent[MAX_LIGHTS];
uniform vec3 uAmbientLight;
uniform vec3 uDiffuse;
uniform vec3 uAmbient;
uniform vec3 uEmissive;
uniform vec3 uSpecular;
uniform float uShininess;
varying vec3 vColor;

void main() {
    vec4 mvPosition = modelViewMatrix * vec4(position, 1.0);
    vec3 N = normalize(normalMatrix * normal);
    vec3 diffuse = uDiffuse;
    vec3 ambient = uAmbient;
#ifdef USE_COLOR
    // Material.setColorTarget(AMBIENT_AND_DIFFUSE): the vertex color replaces both.
    diffuse = color.rgb;
    ambient = color.rgb;
#endif
    vec3 c = uEmissive + uAmbientLight * ambient;
    vec3 V = vec3(0.0, 0.0, 1.0); // non local viewer
    for (int i = 0; i < MAX_LIGHTS; i++) {
        if (i >= uNumLights) {
            break;
        }
        vec3 Lv = uLightPos[i] - mvPosition.xyz;
        float d = length(Lv);
        vec3 L = Lv / d;
        float attenuation = 1.0 / (uLightAtten[i].x + uLightAtten[i].y * d + uLightAtten[i].z * d * d);
        float sd = dot(-L, uLightDir[i]);
        float spot = sd < uLightCosCutoff[i] ? 0.0 : pow(max(sd, 1e-6), uLightExponent[i]);
        float ndl = dot(N, L);
        if (ndl > 0.0) {
            float ndh = max(dot(N, normalize(L + V)), 0.0);
            float specular = uShininess > 0.0 ? pow(max(ndh, 1e-6), uShininess) : 1.0;
            c += attenuation * spot * uLightColor[i] * (ndl * diffuse + specular * uSpecular);
        }
    }
    vColor = clamp(c, 0.0, 1.0);
    gl_Position = projectionMatrix * mvPosition;
}
`;

const fragmentShader = /* glsl */`
varying vec3 vColor;
void main() {
    gl_FragColor = vec4(vColor, 1.0);
}
`;

function toVec3(c) {
    if (!c) {
        return new THREE.Vector3(0, 0, 0);
    }
    if (c.isVector3) {
        return c.clone();
    }
    return new THREE.Vector3(c.r / 255, c.g / 255, c.b / 255);
}

/**
 * An Appearance with a Material (javax.media.j3d.Material defaults: ambient 0.2,
 * diffuse 1, specular 1, emissive 0, shininess 64) and CULL_NONE polygons.
 */
export function createLitMaterial(lighting, {
    vertexColors = false,
    ambient = new THREE.Vector3(0.2, 0.2, 0.2),
    diffuse = new THREE.Vector3(1, 1, 1),
    emissive = new THREE.Vector3(0, 0, 0),
    specular = new THREE.Vector3(1, 1, 1),
    shininess = 64,
} = {}) {
    return new THREE.ShaderMaterial({
        uniforms: {
            ...lighting.uniforms,
            uDiffuse: { value: toVec3(diffuse) },
            uAmbient: { value: toVec3(ambient) },
            uEmissive: { value: toVec3(emissive) },
            uSpecular: { value: toVec3(specular) },
            uShininess: { value: shininess },
        },
        vertexShader,
        fragmentShader,
        vertexColors,
        side: THREE.DoubleSide,
    });
}

/** Java AWT mouse event ids, as delivered to Behaviors. */
export const MouseEventId = {
    MOUSE_PRESSED: 501,
    MOUSE_RELEASED: 502,
    MOUSE_DRAGGED: 506,
    MOUSE_WHEEL: 507,
};

/** Converts a DOM pointer/wheel event into the AWT-like event a Behavior expects. */
export function toAwtMouseEvent(e, id, element) {
    const rect = element.getBoundingClientRect();
    return {
        id,
        x: Math.round(e.clientX - rect.left),
        y: Math.round(e.clientY - rect.top),
        // In AWT the right button reports isMetaDown() and the middle one isAltDown().
        metaDown: e.button === 2 || (e.buttons & 2) === 2 || e.metaKey,
        altDown: e.button === 1 || (e.buttons & 4) === 4 || e.altKey,
        wheelRotation: e.deltaY || 0,
        getID() {
            return this.id;
        },
        getX() {
            return this.x;
        },
        getY() {
            return this.y;
        },
        isMetaDown() {
            return this.metaDown;
        },
        isAltDown() {
            return this.altDown;
        },
    };
}
