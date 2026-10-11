# LogicView Phoenix

LogicView is a viewer for voxel models: **material models** (a 3D grid of tissue
codes) and **field models** (a scalar field on the same grid, such as the SAR
computed by an FDTD simulation). It was written in Java with Swing and Java 3D
between 2001 and 2010.

LogicView Phoenix is a port of that application to the browser, rendering with
WebGL (through [three.js](https://threejs.org)). It is a *structural* port: the
Mosaic component kernel, `Repository.xml`, the loaders, the viewer, its view
controls, printing and animation keep the packages, class names, interfaces and
algorithms of the original. The user-visible strings still come from the
original `.properties` files. See [PORTING.md](PORTING.md) for the details.

It opens the SAM head SAR distribution at 1800 MHz on start-up.

## Running it

It is a static site with no build step. Serve the repository root with any web
server and open it in a browser with WebGL 2 (any current Chrome, Edge, Firefox
or Safari):

```sh
python3 -m http.server 8000
# then open http://localhost:8000/
```

Opening `index.html` straight from the file system does not work, because
browsers do not allow modules and data to be loaded from `file://` URLs.

## Using it

* **Model > Load Model...** opens other models. Pick the file type (Field Models,
  Log of Field Models or Material Models), then give the browser the model's
  files: several files, a whole folder, or a `.zip` archive (the original
  `SAMHead_1800_Het.zip` works as is). Files can also be dropped on the dialog.
  Data files may be gzip-compressed (`SAR0.gz` is found when `SAR0` is asked for).
* Each model opens in its own window, with the original control tabs:
  * **Zoom** - drag the mouse up and down over the view (or use the wheel), or type a value.
  * **Rotate** - drag the mouse over the view, or type the angles.
  * **Materials** - choose which tissues are shown.
  * **Plane Cut** - keep only a box of the grid.
  * **Time Step** - for fields with several time steps.
  * **Values** - keep only the voxels whose value is within a band.
  * **Colors** - the color scale (min/max colors, number of colors, range).
* **View > Save Snapshot...** lays the view, the material legend and the color
  scale out on an image and downloads it (PNG, JPEG or WebP).
* **View > Animate...** interpolates rotation, plane cut and/or time step over
  a number of frames and records a WebM video.
* **Help > Debug output** shows what the Java version wrote to `debug.txt`.

URL parameters: `?model=<url of a .3df or .3dm>` opens another model at
start-up, `?model=none` starts with an empty desktop,
`&loader=Log%20of%20Field%20Models` picks the loader, and `?config=<url>` uses
another component repository (the original command line argument).

## Repository layout

| Path | Contents |
| --- | --- |
| `index.html`, `css/metal.css` | The page, styled after Swing's Metal "Ocean" look and feel |
| `src/com/logicstyle/...` | The ported application, one ES module per original class |
| `src/com/logicstyle/mosaic/system/Repository.xml` | The component list the kernel loads |
| `src/compat/` | The small "Java runtime" the port stands on: interfaces, resource bundles, URLs, a mini Swing, Java 3D on three.js |
| `data/SAMHead_1800_Het/` | The SAM head model, in its original format (the two big data files gzip-compressed) |
| `legacy/` | The original source recovered from `Logicview.jar` with the CFR decompiler, and its resources |
| `vendor/three/` | three.js r160 (MIT) and the Helvetiker font used for the 3D axis labels |
