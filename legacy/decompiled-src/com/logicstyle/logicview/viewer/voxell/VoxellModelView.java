/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.j3d.utils.geometry.GeometryInfo
 *  com.sun.j3d.utils.universe.SimpleUniverse
 *  com.sun.j3d.utils.universe.ViewingPlatform
 *  javax.media.j3d.Appearance
 *  javax.media.j3d.Background
 *  javax.media.j3d.BoundingSphere
 *  javax.media.j3d.Bounds
 *  javax.media.j3d.BranchGroup
 *  javax.media.j3d.Canvas3D
 *  javax.media.j3d.Font3D
 *  javax.media.j3d.FontExtrusion
 *  javax.media.j3d.Geometry
 *  javax.media.j3d.GeometryArray
 *  javax.media.j3d.ImageComponent2D
 *  javax.media.j3d.LineArray
 *  javax.media.j3d.Material
 *  javax.media.j3d.Node
 *  javax.media.j3d.PolygonAttributes
 *  javax.media.j3d.Screen3D
 *  javax.media.j3d.Shape3D
 *  javax.media.j3d.SpotLight
 *  javax.media.j3d.Text3D
 *  javax.media.j3d.Transform3D
 *  javax.media.j3d.TransformGroup
 *  javax.vecmath.Color3b
 *  javax.vecmath.Color3f
 *  javax.vecmath.Point3d
 *  javax.vecmath.Point3f
 *  javax.vecmath.Vector3f
 */
package com.logicstyle.logicview.viewer.voxell;

import com.logicstyle.logicview.model.Model;
import com.logicstyle.logicview.model.View;
import com.logicstyle.logicview.model.VoxellModel;
import com.logicstyle.logicview.viewer.controls.SceneControl;
import com.logicstyle.logicview.viewer.controls.ViewControl;
import com.logicstyle.logicview.viewer.controls.ViewControlFactory;
import com.logicstyle.logicview.viewer.controls.ViewControlListener;
import com.logicstyle.logicview.viewer.controls.VoxellTransformControl;
import com.logicstyle.logicview.viewer.controls.color.VoxellColorGeneratorControl;
import com.logicstyle.logicview.viewer.controls.geometry.VoxellGeometryGenerator;
import com.logicstyle.logicview.viewer.printing.PrintPageElement;
import com.logicstyle.logicview.viewer.util.ViewControlPanel;
import com.logicstyle.logicview.viewer.voxell.VoxellModelViewer;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Panel;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.Font3D;
import javax.media.j3d.FontExtrusion;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.LineArray;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Screen3D;
import javax.media.j3d.Shape3D;
import javax.media.j3d.SpotLight;
import javax.media.j3d.Text3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.ProgressMonitor;
import javax.vecmath.Color3b;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

public class VoxellModelView
implements View,
PrintPageElement {
    private VoxellModel mod;
    private VoxellModelViewer viewer;
    private static ResourceBundle res = ResourceBundle.getBundle(VoxellModelView.class.getName());
    private Object viewUpdateLock = new Object();
    private Vector view_controls;
    private BranchGroup root;
    private BranchGroup rootHolder;
    private TransformGroup shapeTransformGroup;
    private BranchGroup shape;
    private SimpleUniverse u;
    private Canvas3D c3d;
    private Canvas3D printing_canvas;
    private SimpleUniverse printing_universe;
    private Dimension printing_size;
    private Dimension minimum_printing_size = new Dimension(100, 100);

    public VoxellModelView(VoxellModel m, VoxellModelViewer v) {
        this.mod = m;
        this.viewer = v;
        this.view_controls = new Vector();
    }

    public Component getViewComponent() {
        Panel p = new Panel(new BorderLayout());
        ComponentRepository rep = this.viewer.getRepository();
        List viewControls = rep.findByInterface(ViewControlFactory.class);
        if (viewControls.size() > 0) {
            for (int i = 0; i < viewControls.size(); ++i) {
                ViewControlFactory factory = (ViewControlFactory)viewControls.get(i);
                ViewControl v = factory.getViewControl(this.mod);
                if (v == null) continue;
                this.view_controls.addElement(v);
                v.addViewControlListener(new ViewControlListener(){

                    public void controlActivated(final ViewControl v) {
                        Thread t = new Thread(new Runnable(){

                            /*
                             * WARNING - Removed try catching itself - possible behaviour change.
                             */
                            public void run() {
                                Object object = VoxellModelView.this.viewUpdateLock;
                                synchronized (object) {
                                    ProgressMonitor monitor = new ProgressMonitor((Component)VoxellModelView.this.c3d, "Reconstructing view", null, 0, 100);
                                    monitor.setMillisToDecideToPopup(0);
                                    monitor.setMillisToPopup(0);
                                    monitor.setProgress(10);
                                    if (v instanceof VoxellTransformControl || v instanceof VoxellColorGeneratorControl) {
                                        VoxellModelView.this.generateShape();
                                        monitor.setProgress(50);
                                        VoxellModelView.this.shapeTransformGroup.removeAllChildren();
                                        VoxellModelView.this.shapeTransformGroup.addChild((Node)VoxellModelView.this.shape);
                                    }
                                    if (v instanceof SceneControl) {
                                        VoxellModelView.this.setupScene();
                                    }
                                    monitor.setProgress(100);
                                }
                            }
                        });
                        t.start();
                    }

                    public void controlDeactivated(ViewControl v) {
                        this.controlActivated(v);
                    }

                    public void controlStateChanged(ViewControl v) {
                        this.controlActivated(v);
                    }
                });
            }
            ViewControl[] controls_array = new ViewControl[this.view_controls.size()];
            System.arraycopy(this.view_controls.toArray(), 0, controls_array, 0, controls_array.length);
            ViewControlPanel vp = new ViewControlPanel(controls_array);
            p.add((Component)vp, "North");
        }
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        this.c3d = new Canvas3D(config);
        p.add((Component)this.c3d, "Center");
        this.u = new SimpleUniverse(this.c3d);
        this.rootHolder = new BranchGroup();
        this.rootHolder.setCapability(17);
        this.rootHolder.setCapability(12);
        this.rootHolder.setCapability(13);
        this.rootHolder.setCapability(14);
        this.u.addBranchGraph(this.rootHolder);
        this.u.getViewingPlatform().setNominalViewingTransform();
        ViewingPlatform pl = this.u.getViewingPlatform();
        Transform3D currentXform = new Transform3D();
        pl.getViewPlatformTransform().getTransform(currentXform);
        currentXform.setScale(0.1);
        pl.getViewPlatformTransform().setTransform(currentXform);
        this.u.getViewer().getView().setVisibilityPolicy(2);
        this.generateShape();
        this.setupScene();
        this.printing_canvas = new Canvas3D(config, true);
        this.u.getViewer().getView().addCanvas3D(this.printing_canvas);
        return p;
    }

    private void generateShape() {
        VoxellModel current = this.mod;
        for (int i = 0; i < this.view_controls.size(); ++i) {
            ViewControl v = (ViewControl)this.view_controls.elementAt(i);
            if (!(v instanceof VoxellTransformControl) || !v.isActive()) continue;
            current = ((VoxellTransformControl)v).processModel(current);
        }
        this.shape = this.getCurrentSurface(current);
    }

    private void setupScene() {
        this.rootHolder.removeAllChildren();
        this.root = new BranchGroup();
        this.root.setCapability(12);
        this.root.setCapability(13);
        this.root.setCapability(14);
        this.root.setCapability(17);
        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 2.0);
        SpotLight keyLight = new SpotLight(new Color3f(Color.white), new Point3f(-0.707f, 0.707f, 1.0f), new Point3f(0.0f, 0.75f, 0.0f), new Vector3f(0.707f, -0.707f, -1.0f), 1.0471976f, 100.0f);
        SpotLight keyLight2 = new SpotLight(new Color3f(Color.white), new Point3f(0.707f, -0.707f, 1.0f), new Point3f(0.0f, 0.75f, 0.0f), new Vector3f(-0.707f, 0.707f, -1.0f), 1.0471976f, 100.0f);
        SpotLight fillLight = new SpotLight(new Color3f(Color.white), new Point3f(1.06f, 1.06f, 1.0f), new Point3f(0.0f, 0.75f, 0.0f), new Vector3f(-1.06f, 1.06f, -1.0f), 1.0471976f, 100.0f);
        SpotLight backLight = new SpotLight(new Color3f(Color.white), new Point3f(-0.707f, 0.707f, -1.0f), new Point3f(0.0f, 1.0f, 0.0f), new Vector3f(0.707f, -0.707f, 1.0f), 1.0471976f, 100.0f);
        keyLight.setInfluencingBounds((Bounds)bounds);
        keyLight2.setInfluencingBounds((Bounds)bounds);
        fillLight.setInfluencingBounds((Bounds)bounds);
        backLight.setInfluencingBounds((Bounds)bounds);
        this.shapeTransformGroup = new TransformGroup();
        this.shapeTransformGroup.setCapability(18);
        this.shapeTransformGroup.setCapability(17);
        this.shapeTransformGroup.setCapability(13);
        this.shapeTransformGroup.setCapability(14);
        this.shapeTransformGroup.setCapability(17);
        TransformGroup globalTransformGroup = new TransformGroup();
        globalTransformGroup.setCapability(18);
        globalTransformGroup.setCapability(17);
        globalTransformGroup.setCapability(13);
        globalTransformGroup.setCapability(14);
        globalTransformGroup.setCapability(17);
        globalTransformGroup.addChild((Node)keyLight);
        globalTransformGroup.addChild((Node)keyLight2);
        globalTransformGroup.addChild((Node)fillLight);
        globalTransformGroup.addChild((Node)backLight);
        globalTransformGroup.addChild((Node)this.shapeTransformGroup);
        this.shapeTransformGroup.addChild((Node)this.shape);
        this.root.addChild((Node)globalTransformGroup);
        for (int i = 0; i < this.view_controls.size(); ++i) {
            ViewControl v = (ViewControl)this.view_controls.elementAt(i);
            if (!(v instanceof SceneControl)) continue;
            ((SceneControl)v).processScene(this.root, globalTransformGroup, this.shapeTransformGroup);
        }
        GeometryInfo gi = new GeometryInfo(1);
        Point3f[] points = new Point3f[]{new Point3f(-0.707f, 0.707f, 1.0f), new Point3f(1.06f, 1.06f, 1.0f), new Point3f(0.707f, 0.707f, -1.0f)};
        gi.setCoordinates(points);
        Shape3D s3d = new Shape3D((Geometry)gi.getGeometryArray());
        Appearance ap = new Appearance();
        PolygonAttributes pat = new PolygonAttributes();
        pat.setPolygonMode(0);
        pat.setCullFace(0);
        ap.setPolygonAttributes(pat);
        s3d.setAppearance(ap);
        globalTransformGroup.addChild((Node)s3d);
        Background bg = new Background(new Color3f(Color.white));
        bg.setApplicationBounds((Bounds)new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 20.0));
        globalTransformGroup.addChild((Node)bg);
        this.rootHolder.addChild((Node)this.root);
    }

    public BranchGroup getCurrentSurface(VoxellModel cur) {
        this.viewer.fireViewConstructionListenerViewConstructionProgress(res.getString("Model.BuildingSurfaceActionName"), 0.0f, true);
        GeometryArray g = null;
        for (int i = 0; i < this.view_controls.size(); ++i) {
            ViewControl v = (ViewControl)this.view_controls.elementAt(i);
            if (!(v instanceof VoxellGeometryGenerator) || !v.isActive()) continue;
            g = ((VoxellGeometryGenerator)v).getGeometry(cur);
            break;
        }
        BranchGroup bgr = new BranchGroup();
        Appearance ap = new Appearance();
        PolygonAttributes pat = new PolygonAttributes();
        pat.setPolygonMode(2);
        pat.setCullFace(0);
        ap.setPolygonAttributes(pat);
        Material mat = new Material();
        mat.setShininess(1.0f);
        mat.setSpecularColor(new Color3f(0.02f, 0.02f, 0.02f));
        mat.setColorTarget(4);
        ap.setMaterial(mat);
        if (g != null) {
            Shape3D s3d = new Shape3D((Geometry)g);
            s3d.setAppearance(ap);
            bgr.addChild((Node)s3d);
        }
        BranchGroup coordinateAxis = this.generateCoordinateAxis(cur);
        bgr.addChild((Node)coordinateAxis);
        bgr.setCapability(17);
        bgr.compile();
        return bgr;
    }

    public String getViewName() {
        return res.getString("viewName");
    }

    public Dimension getMinimumSize() {
        return this.minimum_printing_size;
    }

    public String getName() {
        return res.getString("printElementName");
    }

    public int getOrientation() {
        return 3;
    }

    public Dimension getSize() {
        return this.printing_size;
    }

    public int getSupportedOrientations() {
        return 3;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public synchronized void print(Graphics g) {
        Object object = this.viewUpdateLock;
        synchronized (object) {
            Screen3D sc3d = this.printing_canvas.getScreen3D();
            sc3d.setSize(this.printing_size);
            sc3d.setPhysicalScreenWidth(2.8222222222222223E-4 * (double)this.printing_size.width);
            sc3d.setPhysicalScreenHeight(2.8222222222222223E-4 * (double)this.printing_size.height);
            BufferedImage img = new BufferedImage(this.printing_size.width, this.printing_size.height, 1);
            ImageComponent2D comp = new ImageComponent2D(1, img, true, false);
            comp.setCapability(2);
            this.printing_canvas.setOffScreenBuffer(comp);
            this.printing_canvas.renderOffScreenBuffer();
            this.printing_canvas.waitForOffScreenRendering();
            if (System.getProperty("animation.pauseAfterFrameProduced") != null) {
                String pause = System.getProperty("animation.pauseAfterFrameProduced");
                System.err.println("Pausing for " + pause + " to wait for rendering");
                try {
                    Thread.sleep(Long.parseLong(pause));
                }
                catch (InterruptedException e) {
                    // empty catch block
                }
            }
            g.drawImage(img, 0, 0, null);
        }
    }

    public void setOrientation(int orientation) {
    }

    public void setSize(Dimension size) {
        this.printing_size = size;
    }

    public Model getModel() {
        return this.mod;
    }

    private BranchGroup generateCoordinateAxis(VoxellModel cur) {
        double axisSize;
        double smallerDumensionDelta;
        int smallerDimension;
        LineArray lineArray = new LineArray(6, 5);
        Point3d[] lineEnds = new Point3d[6];
        if (cur.getWidth() < cur.getHeight()) {
            smallerDimension = cur.getWidth();
            smallerDumensionDelta = cur.getDeltaX();
        } else {
            smallerDimension = cur.getHeight();
            smallerDumensionDelta = cur.getDeltaY();
        }
        if (cur.getDeltaZ() < (double)smallerDimension) {
            smallerDimension = cur.getDepth();
            smallerDumensionDelta = cur.getDeltaZ();
        }
        if ((axisSize = (double)(smallerDimension / 4) * smallerDumensionDelta) < smallerDumensionDelta) {
            axisSize = smallerDumensionDelta;
        }
        double xLeft = (double)(-cur.getWidth() / 2) * cur.getDeltaX() - cur.getDeltaX();
        double yBottom = (double)(-cur.getHeight() / 2) * cur.getDeltaY() - cur.getDeltaY();
        double zBack = (double)(-cur.getDepth() / 2) * cur.getDeltaZ() - cur.getDeltaZ();
        lineEnds[0] = new Point3d(xLeft, yBottom, zBack);
        lineEnds[1] = new Point3d(xLeft + axisSize, yBottom, zBack);
        lineEnds[2] = new Point3d(xLeft, yBottom, zBack);
        lineEnds[3] = new Point3d(xLeft, yBottom + axisSize, zBack);
        lineEnds[4] = new Point3d(xLeft, yBottom, zBack);
        lineEnds[5] = new Point3d(xLeft, yBottom, zBack + axisSize);
        lineArray.setCoordinates(0, lineEnds);
        Color3b black = new Color3b();
        Color3b red = new Color3b();
        red.set(Color.RED);
        Color3b blue = new Color3b();
        blue.set(Color.BLUE);
        lineArray.setColors(0, new Color3b[]{black, black, red, red, blue, blue});
        Shape3D lineShape = new Shape3D((Geometry)lineArray);
        Font textFont = new Font("Dialog", 1, 1);
        Font3D textFont3d = new Font3D(textFont, new FontExtrusion());
        BranchGroup gr = new BranchGroup();
        gr.addChild((Node)lineShape);
        Text3D xLabel = new Text3D(textFont3d, "x", new Point3f(0.0f, 0.0f, 0.0f));
        Text3D yLabel = new Text3D(textFont3d, "y", new Point3f(0.0f, 0.0f, 0.0f));
        Text3D zLabel = new Text3D(textFont3d, "z", new Point3f(0.0f, 0.0f, 0.0f));
        Color3f black3f = new Color3f(0.0f, 0.0f, 0.0f);
        Color3f red3f = new Color3f(Color.red);
        Color3f blue3f = new Color3f(Color.blue);
        TransformGroup XlabelTransformgroup = new TransformGroup();
        Transform3D xLabelTransform = new Transform3D();
        xLabelTransform.setScale(axisSize / 4.0);
        xLabelTransform.setTranslation(new Vector3f((float)(xLeft + axisSize), (float)yBottom, (float)zBack));
        XlabelTransformgroup.setTransform(xLabelTransform);
        Appearance ap = new Appearance();
        ap.setMaterial(new Material(black3f, black3f, black3f, black3f, 0.0f));
        XlabelTransformgroup.addChild((Node)new Shape3D((Geometry)xLabel, ap));
        gr.addChild((Node)XlabelTransformgroup);
        TransformGroup YlabelTransformgroup = new TransformGroup();
        Transform3D yLabelTransform = new Transform3D();
        yLabelTransform.setScale(axisSize / 4.0);
        yLabelTransform.setTranslation(new Vector3f((float)xLeft, (float)(yBottom + axisSize), (float)zBack));
        YlabelTransformgroup.setTransform(yLabelTransform);
        ap = new Appearance();
        ap.setMaterial(new Material(red3f, red3f, red3f, red3f, 0.0f));
        YlabelTransformgroup.addChild((Node)new Shape3D((Geometry)yLabel, ap));
        gr.addChild((Node)YlabelTransformgroup);
        TransformGroup ZlabelTransformgroup = new TransformGroup();
        Transform3D zLabelTransform = new Transform3D();
        zLabelTransform.setScale(axisSize / 4.0);
        zLabelTransform.setTranslation(new Vector3f((float)xLeft, (float)yBottom, (float)(zBack + axisSize)));
        ZlabelTransformgroup.setTransform(zLabelTransform);
        ap = new Appearance();
        ap.setMaterial(new Material(blue3f, blue3f, blue3f, blue3f, 0.0f));
        ZlabelTransformgroup.addChild((Node)new Shape3D((Geometry)zLabel, ap));
        gr.addChild((Node)ZlabelTransformgroup);
        gr.setCapability(17);
        gr.compile();
        return gr;
    }
}

