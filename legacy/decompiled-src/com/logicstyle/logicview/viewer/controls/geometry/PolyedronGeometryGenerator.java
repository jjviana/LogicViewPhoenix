/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.j3d.utils.geometry.GeometryInfo
 *  javax.media.j3d.GeometryArray
 *  javax.vecmath.Color3f
 *  javax.vecmath.Point3f
 *  javax.vecmath.Vector3f
 */
package com.logicstyle.logicview.viewer.controls.geometry;

import com.logicstyle.logicview.model.VoxellModel;
import com.logicstyle.logicview.viewer.controls.ViewControl;
import com.logicstyle.logicview.viewer.controls.ViewControlListener;
import com.logicstyle.logicview.viewer.controls.color.VoxellColorGeneratorControl;
import com.logicstyle.logicview.viewer.controls.color.VoxellColorGeneratorControlFactory;
import com.logicstyle.logicview.viewer.controls.geometry.VoxellGeometryGenerator;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
import com.sun.j3d.utils.geometry.GeometryInfo;
import java.awt.Color;
import java.awt.Component;
import java.util.List;
import java.util.Vector;
import javax.media.j3d.GeometryArray;
import javax.swing.event.EventListenerList;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

public class PolyedronGeometryGenerator
implements VoxellGeometryGenerator {
    private EventListenerList listenerList = null;
    private ComponentRepository rep;

    public PolyedronGeometryGenerator(ComponentRepository rep) {
        this.rep = rep;
    }

    public void controlComponentHidden(Component c) {
    }

    public void controlComponentShown(Component c) {
    }

    public Component getControlComponent() {
        return null;
    }

    public GeometryArray getGeometry(VoxellModel cur) {
        float x0;
        VoxellColorGeneratorControlFactory vcgf;
        VoxellColorGeneratorControl vcg = null;
        List colgen = this.rep.findByInterface(VoxellColorGeneratorControlFactory.class);
        for (int i = 0; i < colgen.size() && ((vcgf = (VoxellColorGeneratorControlFactory)colgen.get(i)) == null || (vcg = (VoxellColorGeneratorControl)vcgf.getViewControl(cur)) == null); ++i) {
        }
        float deltaX = (float)cur.getDeltaX();
        float deltaY = (float)cur.getDeltaY();
        float deltaZ = (float)cur.getDeltaZ();
        int sizeX = cur.getWidth();
        int sizeY = cur.getHeight();
        int sizeZ = cur.getDepth();
        float y0 = (float)(sizeY / 2) * deltaY;
        float z0 = (float)(sizeZ / 2) * deltaZ;
        float x = x0 = (float)(-sizeX / 2) * deltaX;
        float y = y0;
        float z = z0;
        Vector<Point3f> data = new Vector<Point3f>();
        Vector<Color> datacolor = new Vector<Color>();
        Vector<Vector3f> normals = new Vector<Vector3f>();
        Vector3f top_normal = new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f bottom_normal = new Vector3f(0.0f, -1.0f, 0.0f);
        Vector3f left_normal = new Vector3f(-1.0f, 0.0f, 0.0f);
        Vector3f right_normal = new Vector3f(1.0f, 0.0f, 0.0f);
        Vector3f front_normal = new Vector3f(0.0f, 0.0f, 1.0f);
        Vector3f back_normal = new Vector3f(0.0f, 0.0f, -1.0f);
        for (int i = 0; i < sizeX; ++i) {
            for (int j = 0; j < sizeY; ++j) {
                for (int k = 0; k < sizeZ; ++k) {
                    if (cur.isEnabled(i, j, k)) {
                        int numfaces = 0;
                        if (i == 0 || !cur.isEnabled(i - 1, j, k)) {
                            data.add(new Point3f(x, y, z));
                            data.add(new Point3f(x, y - deltaY, z));
                            data.add(new Point3f(x, y - deltaY, z - deltaZ));
                            data.add(new Point3f(x, y, z - deltaZ));
                            normals.add(left_normal);
                            normals.add(left_normal);
                            normals.add(left_normal);
                            normals.add(left_normal);
                            ++numfaces;
                        }
                        if (i == sizeX - 1 || !cur.isEnabled(i + 1, j, k)) {
                            data.add(new Point3f(x + deltaX, y, z));
                            data.add(new Point3f(x + deltaX, y - deltaY, z));
                            data.add(new Point3f(x + deltaX, y - deltaY, z - deltaZ));
                            data.add(new Point3f(x + deltaX, y, z - deltaZ));
                            normals.add(right_normal);
                            normals.add(right_normal);
                            normals.add(right_normal);
                            normals.add(right_normal);
                            ++numfaces;
                        }
                        if (k == 0 || !cur.isEnabled(i, j, k - 1)) {
                            data.add(new Point3f(x, y, z));
                            data.add(new Point3f(x + deltaX, y, z));
                            data.add(new Point3f(x + deltaX, y - deltaY, z));
                            data.add(new Point3f(x, y - deltaY, z));
                            normals.add(front_normal);
                            normals.add(front_normal);
                            normals.add(front_normal);
                            normals.add(front_normal);
                            ++numfaces;
                        }
                        if (k == sizeZ - 1 || !cur.isEnabled(i, j, k + 1)) {
                            data.add(new Point3f(x, y, z - deltaZ));
                            data.add(new Point3f(x + deltaX, y, z - deltaZ));
                            data.add(new Point3f(x + deltaX, y - deltaY, z - deltaZ));
                            data.add(new Point3f(x, y - deltaY, z - deltaZ));
                            normals.add(back_normal);
                            normals.add(back_normal);
                            normals.add(back_normal);
                            normals.add(back_normal);
                            ++numfaces;
                        }
                        if (j == 0 || !cur.isEnabled(i, j - 1, k)) {
                            data.add(new Point3f(x, y, z));
                            data.add(new Point3f(x + deltaX, y, z));
                            data.add(new Point3f(x + deltaX, y, z - deltaZ));
                            data.add(new Point3f(x, y, z - deltaZ));
                            normals.add(top_normal);
                            normals.add(top_normal);
                            normals.add(top_normal);
                            normals.add(top_normal);
                            ++numfaces;
                        }
                        if (j == sizeY - 1 || !cur.isEnabled(i, j + 1, k)) {
                            data.add(new Point3f(x, y - deltaY, z));
                            data.add(new Point3f(x + deltaX, y - deltaY, z));
                            data.add(new Point3f(x + deltaX, y - deltaY, z - deltaZ));
                            data.add(new Point3f(x, y - deltaY, z - deltaZ));
                            normals.add(bottom_normal);
                            normals.add(bottom_normal);
                            normals.add(bottom_normal);
                            normals.add(bottom_normal);
                            ++numfaces;
                        }
                        if (vcg != null) {
                            for (int ind = 0; ind < 4 * numfaces; ++ind) {
                                datacolor.add(vcg.getColorForPoint(i, j, k, cur));
                            }
                        }
                    }
                    z -= deltaZ;
                }
                y -= deltaY;
                z = z0;
            }
            x += deltaX;
            y = y0;
            z = z0;
        }
        if (data.size() == 0) {
            System.err.println("PolyedronGemoetryGenerator Warning: empty geometry");
            return null;
        }
        GeometryInfo geo = new GeometryInfo(2);
        Point3f[] points = new Point3f[data.size()];
        System.err.println("Number of vertices =" + data.size());
        for (int i = 0; i < points.length; ++i) {
            points[i] = (Point3f)data.elementAt(i);
        }
        Color3f[] pointcolor = new Color3f[datacolor.size()];
        for (int i = 0; i < pointcolor.length; ++i) {
            Color c = (Color)datacolor.elementAt(i);
            pointcolor[i] = new Color3f(c);
        }
        Vector3f[] norm = new Vector3f[normals.size()];
        for (int i = 0; i < normals.size(); ++i) {
            norm[i] = (Vector3f)normals.elementAt(i);
        }
        geo.setCoordinates(points);
        geo.setColors(pointcolor);
        geo.setNormals(norm);
        return geo.getGeometryArray();
    }

    public String getName() {
        return null;
    }

    public boolean isActive() {
        return true;
    }

    public synchronized void addViewControlListener(ViewControlListener listener) {
        if (this.listenerList == null) {
            this.listenerList = new EventListenerList();
        }
        this.listenerList.add(ViewControlListener.class, listener);
    }

    public synchronized void removeViewControlListener(ViewControlListener listener) {
        this.listenerList.remove(ViewControlListener.class, listener);
    }

    private void fireViewControlListenerControlDeactivated(ViewControl event) {
        if (this.listenerList == null) {
            return;
        }
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ViewControlListener.class) continue;
            ((ViewControlListener)listeners[i + 1]).controlDeactivated(event);
        }
    }

    private void fireViewControlListenerControlActivated(ViewControl event) {
        if (this.listenerList == null) {
            return;
        }
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ViewControlListener.class) continue;
            ((ViewControlListener)listeners[i + 1]).controlActivated(event);
        }
    }

    private void fireViewControlListenerControlStateChanged(ViewControl event) {
        if (this.listenerList == null) {
            return;
        }
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ViewControlListener.class) continue;
            ((ViewControlListener)listeners[i + 1]).controlStateChanged(event);
        }
    }
}

