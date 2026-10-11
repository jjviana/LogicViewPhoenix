/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.j3d.utils.behaviors.mouse.MouseBehaviorCallback
 *  javax.media.j3d.BoundingSphere
 *  javax.media.j3d.Bounds
 *  javax.media.j3d.BranchGroup
 *  javax.media.j3d.Group
 *  javax.media.j3d.Node
 *  javax.media.j3d.Transform3D
 *  javax.media.j3d.TransformGroup
 *  javax.vecmath.Point3d
 *  javax.vecmath.Vector3f
 */
package com.logicstyle.logicview.viewer.controls.rotate;

import com.logicstyle.logicview.viewer.animation.Interpolator;
import com.logicstyle.logicview.viewer.controls.SceneControl;
import com.logicstyle.logicview.viewer.controls.ViewControlListener;
import com.logicstyle.logicview.viewer.controls.rotate.ViewerMouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseBehaviorCallback;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

public class RotateSceneControl
implements SceneControl,
Interpolator {
    private boolean active = false;
    private ResourceBundle res = ResourceBundle.getBundle(RotateSceneControl.class.getName());
    private JLabel rlabel;
    private ViewerMouseRotate mrot = new ViewerMouseRotate();
    private InterpolatorControlPanel interpolatorControlPanel;
    private RotationControlPanel rot_panel = new RotationControlPanel(this.mrot);

    public void addViewControlListener(ViewControlListener listener) {
    }

    public void controlComponentHidden(Component c) {
        this.mrot.setActive(false);
    }

    public void controlComponentShown(Component c) {
        this.mrot.setActive(true);
    }

    public Component getControlComponent() {
        return this.rot_panel;
    }

    public String getName() {
        return this.res.getString("name");
    }

    public boolean isActive() {
        return true;
    }

    public void removeViewControlListener(ViewControlListener listener) {
    }

    public void processScene(BranchGroup root, TransformGroup global, TransformGroup shape) {
        this.mrot.setTransformGroup(shape);
        this.mrot.setSchedulingBounds((Bounds)new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 1000.0));
        Group oldRoot = (Group)this.mrot.getParent();
        if (oldRoot != null) {
            oldRoot.removeChild((Node)this.mrot);
        }
        global.addChild((Node)this.mrot);
        this.rot_panel.updateFieldsFromTransform();
    }

    public Component getConfigurationComponent() {
        if (this.interpolatorControlPanel == null) {
            this.interpolatorControlPanel = new InterpolatorControlPanel();
        }
        return this.interpolatorControlPanel;
    }

    public String getInterpolatorName() {
        return "Rotation";
    }

    public void setInterpolationStep(float step) {
        double startx = Float.parseFloat(this.interpolatorControlPanel.startX.getText());
        double starty = Float.parseFloat(this.interpolatorControlPanel.startY.getText());
        double startz = Float.parseFloat(this.interpolatorControlPanel.startZ.getText());
        double endx = Float.parseFloat(this.interpolatorControlPanel.endX.getText());
        double endy = Float.parseFloat(this.interpolatorControlPanel.endY.getText());
        double endz = Float.parseFloat(this.interpolatorControlPanel.endZ.getText());
        Vector3f currentAngles = this.rot_panel.rot.getRotationAngles();
        double rx = startx != -1.0 ? startx + (endx - startx) * (double)step : (double)currentAngles.x;
        double ry = starty != -1.0 ? starty + (endy - starty) * (double)step : (double)currentAngles.y;
        double rz = startz != -1.0 ? startx + (endz - startz) * (double)step : (double)currentAngles.z;
        rx = rx / 360.0 * 6.283;
        ry = ry / 360.0 * 6.283;
        rz = rz / 360.0 * 6.283;
        this.rot_panel.rot.setRotationAngles(rx, ry, rz);
    }

    class RotationControlPanel
    extends JPanel {
        private JTextField rotX;
        private JTextField rotY;
        private JTextField rotZ;
        private ViewerMouseRotate rot;
        private DecimalFormat df;
        private boolean updating_from_fields = false;

        public RotationControlPanel(ViewerMouseRotate r) {
            this.setLayout(new FlowLayout());
            this.rotX = new JTextField(5);
            this.rotY = new JTextField(5);
            this.rotZ = new JTextField(5);
            this.add(new JLabel(RotateSceneControl.this.res.getString("rotationLabelBegin") + " X="));
            this.add(this.rotX);
            this.add(new JLabel(", Y="));
            this.add(this.rotY);
            this.add(new JLabel(", Z="));
            this.add(this.rotZ);
            this.rot = r;
            this.df = new DecimalFormat("##.##");
            this.rot.setupCallback(new MouseBehaviorCallback(){

                public void transformChanged(int type, Transform3D transform) {
                    if (!RotationControlPanel.this.updating_from_fields) {
                        RotationControlPanel.this.updateFieldsFromTransform();
                    }
                }
            });
            UtilDocumentListener ul = new UtilDocumentListener();
            FocusListener fl = new FocusListener(){

                public void focusGained(FocusEvent evt) {
                }

                public void focusLost(FocusEvent evt) {
                    RotationControlPanel.this.updating_from_fields = true;
                    RotationControlPanel.this.updateTransformFromFields();
                    RotationControlPanel.this.updating_from_fields = false;
                }
            };
            this.rotX.addFocusListener(fl);
            this.rotY.addFocusListener(fl);
            this.rotZ.addFocusListener(fl);
        }

        private void updateFieldsFromTransform() {
            Vector3f angles = this.rot.getRotationAngles();
            this.rotX.setText(this.df.format(360.0 * ((double)angles.x / 6.283)));
            this.rotY.setText(this.df.format(360.0 * ((double)angles.y / 6.283)));
            this.rotZ.setText(this.df.format(360.0 * ((double)angles.z / 6.283)));
        }

        private void updateTransformFromFields() {
            try {
                double rX = Double.valueOf(this.rotX.getText());
                double rY = Double.valueOf(this.rotY.getText());
                double rZ = Double.valueOf(this.rotZ.getText());
                rX = rX / 360.0 * 6.283;
                rY = rY / 360.0 * 6.283;
                rZ = rZ / 360.0 * 6.283;
                this.rot.setRotationAngles(rX, rY, rZ);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }

        class UtilDocumentListener
        implements DocumentListener {
            UtilDocumentListener() {
            }

            public void changedUpdate(DocumentEvent e) {
                RotationControlPanel.this.updating_from_fields = true;
                RotationControlPanel.this.updateTransformFromFields();
                RotationControlPanel.this.updating_from_fields = false;
            }

            public void insertUpdate(DocumentEvent e) {
            }

            public void removeUpdate(DocumentEvent e) {
            }
        }
    }

    class InterpolatorControlPanel
    extends JPanel {
        JTextField startX;
        JTextField endX;
        JTextField startY;
        JTextField endY;
        JTextField startZ;
        JTextField endZ;

        public InterpolatorControlPanel() {
            this.add(new JLabel("Start and end angles for each axis:"));
            this.startX = new JTextField("-1");
            this.add(new JLabel("X from "));
            this.add(this.startX);
            this.endX = new JTextField("-1");
            this.add(new JLabel(" to "));
            this.add(this.endX);
            this.startY = new JTextField("-1");
            this.add(new JLabel("Y from "));
            this.add(this.startY);
            this.endY = new JTextField("-1");
            this.add(new JLabel(" to "));
            this.add(this.endY);
            this.startZ = new JTextField("-1");
            this.add(new JLabel("Z from "));
            this.add(this.startZ);
            this.endZ = new JTextField("-1");
            this.add(new JLabel(" to "));
            this.add(this.endZ);
        }
    }
}

