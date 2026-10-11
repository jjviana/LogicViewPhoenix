/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.j3d.utils.behaviors.mouse.MouseBehaviorCallback
 *  javax.media.j3d.BoundingSphere
 *  javax.media.j3d.Bounds
 *  javax.media.j3d.BranchGroup
 *  javax.media.j3d.Node
 *  javax.media.j3d.Transform3D
 *  javax.media.j3d.TransformGroup
 *  javax.vecmath.Point3d
 */
package com.logicstyle.logicview.viewer.controls.zoom;

import com.logicstyle.logicview.viewer.controls.SceneControl;
import com.logicstyle.logicview.viewer.controls.ViewControlListener;
import com.logicstyle.logicview.viewer.controls.zoom.ViewerMouseZoom;
import com.sun.j3d.utils.behaviors.mouse.MouseBehaviorCallback;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.vecmath.Point3d;

public class ZoomSceneControl
implements SceneControl {
    private ResourceBundle res = ResourceBundle.getBundle(ZoomSceneControl.class.getName());
    private JTextPane zlabel;
    private ViewerMouseZoom mzoom = new ViewerMouseZoom();
    private boolean updating_from_fields = false;
    private ZoomControlPanel zp = new ZoomControlPanel();

    public void addViewControlListener(ViewControlListener listener) {
    }

    public Component getControlComponent() {
        return this.zp;
    }

    public String getName() {
        return this.res.getString("name");
    }

    public boolean isActive() {
        return true;
    }

    public void removeViewControlListener(ViewControlListener listener) {
    }

    public void controlComponentShown(Component c) {
        this.mzoom.setActive(true);
    }

    public void controlComponentHidden(Component c) {
        this.mzoom.setActive(false);
    }

    public void processScene(BranchGroup root, TransformGroup global, TransformGroup shape) {
        this.mzoom.setTransformGroup(global);
        this.mzoom.setSchedulingBounds((Bounds)new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 1000.0));
        BranchGroup oldRoot = (BranchGroup)this.mzoom.getParent();
        if (oldRoot != null) {
            oldRoot.removeChild((Node)this.mzoom);
        }
        root.addChild((Node)this.mzoom);
        this.zp.updateFieldsFromTransform();
    }

    class ZoomControlPanel
    extends JPanel {
        private JTextField zoom;
        private DecimalFormat df;

        ZoomControlPanel() {
            this.add(new JLabel(ZoomSceneControl.this.res.getString("zoomPanelTitle")));
            this.zoom = new JTextField(5);
            this.add(this.zoom);
            this.add(new JButton("Ok"));
            ZoomSceneControl.this.mzoom.setupCallback(new MouseBehaviorCallback(){

                public void transformChanged(int type, Transform3D transform) {
                    if (!ZoomSceneControl.this.updating_from_fields) {
                        ZoomControlPanel.this.updateFieldsFromTransform();
                    }
                }
            });
            FocusListener fl = new FocusListener(){

                public void focusGained(FocusEvent evt) {
                }

                public void focusLost(FocusEvent evt) {
                    ZoomSceneControl.this.updating_from_fields = true;
                    ZoomControlPanel.this.updateTransformFromFields();
                    ZoomSceneControl.this.updating_from_fields = false;
                }
            };
            this.df = new DecimalFormat("##.##");
            this.zoom.addFocusListener(fl);
        }

        private void updateFieldsFromTransform() {
            this.zoom.setText(this.df.format(ZoomSceneControl.this.mzoom.getZoom()));
        }

        private void updateTransformFromFields() {
            try {
                double z = Double.valueOf(this.zoom.getText());
                ZoomSceneControl.this.mzoom.setZoom(z);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
    }
}

