# Porting notes

The original application was distributed as `Logicview.jar` (built in March
2010). The jar holds compiled classes, and Java class files decompile well: the
whole source was recovered with the CFR 0.152 decompiler and is kept, together
with the original resources, in [`legacy/decompiled-src`](legacy/decompiled-src).
The port follows that source class by class.

## Architecture (unchanged)

* **Mosaic kernel** (`com.logicstyle.mosaic`). `Kernel` reads `Repository.xml`,
  `ApplicationRepository` instantiates each listed class, registers it under
  every interface it implements (and their super interfaces that extend
  `Component`), then calls `init()` on every component until all succeed,
  dropping those that throw `DependencyException` and detecting deadlocks.
  Components find each other only through the repository
  (`findByInterface`, `findAllByInterface`, `findComponentByInterface`).
* **Models** (`logicview.model`): `VoxellModel`, `MaterialModel`, `FieldModel`,
  `TransformedVoxellModel`...
* **Loaders** (`logicview.loader`): `MaterialModelLoader` (`.3dm`),
  `FieldModelLoader` and `FieldLogModelLoader` (`.3df`).
* **UI actions** (`logicview.UIAction`): every window builds its menu bar from
  the `UIAction` components that are in context for it.
* **Viewer** (`logicview.viewer`): `VoxellModelViewer` creates a
  `VoxellModelView`, which asks every `ViewControlFactory` for a control. The
  model goes through each active `VoxellTransformControl` (statistics,
  materials, plane cut, time step, values), the `VoxellGeometryGenerator` turns
  the result into a surface colored by the first `VoxellColorGeneratorControl`
  that accepts the model, and `SceneControl`s (zoom, rotate) act on the scene
  graph.
* **Printing** (`viewer.printing`) and **animation** (`viewer.animation`):
  page elements from `PrintPageElementFactory`s laid out by a
  `ViewPrintConstructor`; `Interpolator`s driven by an `Animator`.

## How Java concepts were carried over

| Java | Port |
| --- | --- |
| interfaces, `instanceof` | `Interface` objects (`src/compat/lang/Interface.js`); classes declare `static implements = [...]` and `obj instanceof SomeInterface` works through `Symbol.hasInstance` |
| `Class.forName(name).newInstance()` | dynamic `import()` of `src/<package path>/<Class>.js` (`compat/lang/Class.js`) |
| `ResourceBundle.getBundle(...)` | the original `.properties` files, loaded by `compat/util/ResourceBundle.js` |
| `java.net.URL` | `ResourceLocation`: files on the web server, picked by the user, or inside a zip; gzip handled transparently |
| JDOM | the browser's `DOMParser` |
| threads, `synchronized`, `ProgressMonitor` | `async` functions that yield regularly; rebuilds of a view are serialized on a promise chain (`viewUpdateLock`) |
| Swing (`JFrame`, `JDialog`, `JTabbedPane`, `JOptionPane`...) | a small DOM look-alike in `compat/swing/Swing.js`, styled after Metal "Ocean" |
| `java.awt.Graphics`, `Color`, `Font`, `Dimension` | `compat/awt`, on top of the 2D canvas |
| `System.err` redirected to `debug.txt` | an in-memory log, shown by Help > Debug output |
| Java 3D | three.js, see below |
| Java Media Framework (QuickTime `.mov`) | `MediaRecorder` (WebM), `viewer.animation.webm` |

### Java 3D on three.js

`compat/j3d/J3D.js` provides `BranchGroup`, `TransformGroup` (a group whose
matrix is set explicitly, like a `Transform3D`), `SpotLight` and `Behavior`.
`VoxellModelView` builds the same scene graph as the original (root, global
transform group holding the four spot lights, shape transform group holding the
surface and the coordinate axis), and the mouse behaviors still receive
AWT-like mouse events and edit the transform groups with the same matrix code.

Lighting is done by a shader that reproduces the fixed-function OpenGL lighting
Java 3D used: per-vertex (Gouraud) shading, spot exponent ("concentration")
100, spread angle 60 degrees, linear attenuation, no ambient light, vertex
colors used as ambient and diffuse color, no two-sided lighting. The camera
uses Java 3D's defaults: 45 degree *horizontal* field of view and the nominal
viewing transform (eye at z = 2.414). Colors are not color-managed, as in 2002.

The surface generator (`PolyedronGeometryGenerator`) produces the same quads
as before (one per face between an enabled and a disabled voxel), but
evaluates `isEnabled()` once per voxel into a mask and writes typed arrays
directly instead of building `Vector`s of `Point3f`.

## Additions

* `UIAction.StartupModel`: opens the SAM head SAR model on start-up (`?model=` to change).
* `UIAction.ShowDebugOutput` and `UIAction.About`: a Help menu.
* The model chooser also lists the bundled sample models.
* The mouse wheel zooms whichever tab is shown; view windows can be maximized.
* The load progress monitor follows the loaders' and viewer's progress events
  (the original declared them, but nothing listened).

All additions are ordinary components listed in `Repository.xml`.

## Behavior changes (bugs of the original fixed on the way)

* `MaterialModelLoader`: the data file name is resolved relative to the model
  description (the decompiled code always fell through to `new URL(filename)`);
  when every listed material occurs in the data, the materials are stored at
  the right index (the original was off by one).
* Material models are titled with their `name` attribute (read, but unused).
* `ColorControlDialog`: color tables with other than 256 colors now span the
  whole min-to-max color range (the interpolation coefficient was the color
  index itself); the maximum search starts from minus the largest float rather
  than `Float.MIN_VALUE`.
* Applying a typed color range also checks "Hold color scale"; otherwise the
  next rebuild would replace it with the computed range.
* Rotation interpolator: Z interpolated from the X start angle, and the
  fallback for unset axes mixed radians and degrees.
* Snapshots of field models include the material legend (the factory looked
  the control up with the field model instead of its material model).
* "Record animation results" is honored (frames were recorded regardless).
* The Values control is one per model instead of one shared by every view (a
  DOM element cannot be shown in two windows).
* Errors while loading a field's material model are logged instead of silently
  swallowed.
* Animations wait for the view to finish rebuilding before grabbing a frame
  (the Java version raced, hence its `animation.pauseAfterFrameProduced` property).

## Kept on purpose

* The initial view: Java 3D's nominal viewing distance shows the head small in
  the middle of the window; zoom in with the mouse.
* Mouse rotation speed (0.03 rad per pixel) and zoom speed (0.004 per pixel).
* Degrees converted with 6.283 instead of 2 pi.
* The "fill" light pointing away from the model, so only three lights matter.
* The coordinate axis length always derived from the depth.
* The color range following the visible voxels on every rebuild unless held.
* `ViewStatistics` ("An uuuuugly hack pretending to be a statistics viewer")
  printing per-material min/avg/max on every rebuild.
* The viewer's view name, "Material Model", in every view window title (now
  followed by the model name).

## Not ported

* `viewer.controls.animation.*` (`AnimationViewControl`, a first
  `AnimationControlPanel` and a duplicate `Interpolator`): unused, not listed
  in `Repository.xml`.
* The JMF plumbing (`JpegImagesToMovie`, `ImageDataSource`, `ImageSourceStream`).

## Data

`data/SAMHead_1800_Het` holds the SAM head model from `SAMHead_1800_Het.zip`:
`SAR.3df` and `material_3d_space.3dm` verbatim, and the two large text files
(`SAR0`, 98 MB, and `material_3d_space.dat`, 33 MB) gzip-compressed to keep the
repository small. They are still the original text format; the loaders inflate
them on the fly. `caso10.txt` (the FDTD run log) and `Sar1g10g.txt` (1 g / 10 g
averaged SAR) are included for reference. The other two text files of the
archive (`PE_caso10.txt`, `fonte_caso10.txt`) are not used by the viewer and
are left out.
