/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.animation;

import com.logicstyle.logicview.viewer.animation.Animation;
import com.logicstyle.logicview.viewer.animation.Animator;
import com.logicstyle.logicview.viewer.animation.AnimatorListener;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

public class AnimationDialog
extends JDialog
implements AnimatorListener {
    private static final ResourceBundle res = ResourceBundle.getBundle(AnimationDialog.class.getName());
    private Animation animation;
    private int totalFrames;
    private boolean animating = false;
    private FrameDisplay frameDisplay;
    private JProgressBar animationProgressBar;
    private JButton cancelButton;
    private JLabel currentFrameLabel;
    private JScrollPane framePane;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JPanel progressPanel;
    private JLabel totalFramesLabel;

    public AnimationDialog(Dialog parent, Animation animation) {
        super(parent, true);
        this.animation = animation;
        this.initComponents();
        this.frameDisplay = new FrameDisplay(animation.getFrameWidth(), animation.getFrameHeight());
        this.framePane.setViewportView(this.frameDisplay);
        this.totalFramesLabel.setText("" + animation.getNumberOfFrames());
        this.currentFrameLabel.setText("1");
        this.animationProgressBar.setMinimum(1);
        this.animationProgressBar.setMaximum(animation.getNumberOfFrames());
        this.setSize(640, 480);
        this.validate();
    }

    private void initComponents() {
        this.progressPanel = new JPanel();
        this.jLabel1 = new JLabel();
        this.currentFrameLabel = new JLabel();
        this.jLabel2 = new JLabel();
        this.totalFramesLabel = new JLabel();
        this.animationProgressBar = new JProgressBar();
        this.cancelButton = new JButton();
        this.framePane = new JScrollPane();
        this.setDefaultCloseOperation(2);
        this.progressPanel.setLayout(new GridBagLayout());
        this.jLabel1.setText(res.getString("labelPart1"));
        this.progressPanel.add((Component)this.jLabel1, new GridBagConstraints());
        this.currentFrameLabel.setText("1");
        this.progressPanel.add((Component)this.currentFrameLabel, new GridBagConstraints());
        this.jLabel2.setText(res.getString("labelPart2"));
        this.progressPanel.add((Component)this.jLabel2, new GridBagConstraints());
        this.totalFramesLabel.setText("?");
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 0;
        this.progressPanel.add((Component)this.totalFramesLabel, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 0;
        gridBagConstraints.fill = 2;
        this.progressPanel.add((Component)this.animationProgressBar, gridBagConstraints);
        this.cancelButton.setText(res.getString("stopButtonLabel"));
        this.cancelButton.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent evt) {
                AnimationDialog.this.cancelButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 0;
        this.progressPanel.add((Component)this.cancelButton, gridBagConstraints);
        this.getContentPane().add((Component)this.progressPanel, "North");
        this.getContentPane().add((Component)this.framePane, "Center");
        this.pack();
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        if (this.animating) {
            this.animation.getAnimator().setKeepRunning(false);
            this.cancelButton.setText(res.getString("cancelingAnimationLabel"));
        } else {
            this.setVisible(false);
        }
    }

    public void animationFinishing(Animator animator) {
        this.cancelButton.setText(res.getString("closeWindowButtonLabel"));
        this.animating = false;
    }

    public void animationStarting(Animator animator) {
        this.animating = true;
    }

    public void frameProduced(BufferedImage frame, int timeStep) {
        this.currentFrameLabel.setText("" + timeStep);
        this.animationProgressBar.setValue(timeStep);
        this.frameDisplay.setFrame(frame);
    }

    class FrameDisplay
    extends JComponent {
        private Dimension size;
        private BufferedImage frame;

        FrameDisplay(int width, int height) {
            this.size = new Dimension(width, height);
        }

        protected void paintComponent(Graphics graphics) {
            if (this.frame != null) {
                graphics.drawImage(this.frame, 0, 0, null);
            }
        }

        public Dimension getMinimumSize() {
            return this.size;
        }

        public void setFrame(BufferedImage b) {
            this.frame = b;
            this.repaint();
        }
    }
}

