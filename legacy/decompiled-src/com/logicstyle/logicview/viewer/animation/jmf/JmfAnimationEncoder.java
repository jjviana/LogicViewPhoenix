/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.animation.jmf;

import com.logicstyle.logicview.viewer.animation.Animation;
import com.logicstyle.logicview.viewer.animation.AnimationEncoder;
import com.logicstyle.logicview.viewer.animation.Animator;
import com.logicstyle.logicview.viewer.animation.jmf.ImageDataSource;
import com.logicstyle.logicview.viewer.animation.jmf.JmfAnimationEncoderControlPanel;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.media.ConfigureCompleteEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.DataSink;
import javax.media.EndOfMediaEvent;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.PrefetchCompleteEvent;
import javax.media.Processor;
import javax.media.RealizeCompleteEvent;
import javax.media.ResourceUnavailableEvent;
import javax.media.control.TrackControl;
import javax.media.datasink.DataSinkErrorEvent;
import javax.media.datasink.DataSinkEvent;
import javax.media.datasink.DataSinkListener;
import javax.media.datasink.EndOfStreamEvent;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;

public class JmfAnimationEncoder
implements AnimationEncoder,
ControllerListener,
DataSinkListener {
    private ImageDataSource ids;
    private Animation animation;
    private String outputURL;
    private DataSink dsink;
    private Processor processor;
    private JmfAnimationEncoderControlPanel controlPanel = new JmfAnimationEncoderControlPanel();
    Object waitSync = new Object();
    boolean stateTransitionOK = true;

    public void setAnimation(Animation anim) {
        this.animation = anim;
    }

    public void animationFinishing(Animator animator) {
        this.endAnimationRecording();
    }

    public void animationStarting(Animator animator) {
        this.startAnimationRecording();
    }

    public void frameProduced(BufferedImage frame, int timeStep) {
        this.addAnimationFrame(frame);
    }

    public Component getAnimationEncodingControlCoponent() {
        return this.controlPanel;
    }

    private void startAnimationRecording() {
        Processor p;
        this.ids = new ImageDataSource(this.animation.getFrameWidth(), this.animation.getFrameHeight(), this.animation.getFramesPerSecond(), this.animation.getDuration());
        try {
            System.err.println("- create processor for the image datasource ...");
            p = Manager.createProcessor(this.ids);
        }
        catch (Exception e) {
            System.err.println("Yikes!  Cannot create a processor from the data source.");
            e.printStackTrace();
            return;
        }
        p.addControllerListener(this);
        p.configure();
        if (!this.waitForState(p, 180)) {
            System.err.println("Failed to configure the processor.");
            return;
        }
        p.setContentDescriptor(new ContentDescriptor("video.quicktime"));
        TrackControl[] tcs = p.getTrackControls();
        Format[] f = tcs[0].getSupportedFormats();
        if (f == null || f.length <= 0) {
            System.err.println("The mux does not support the input format: " + tcs[0].getFormat());
            return;
        }
        tcs[0].setFormat(f[0]);
        System.err.println("Setting the track format to: " + f[0]);
        p.realize();
        if (!this.waitForState(p, 300)) {
            System.err.println("Failed to realize the processor.");
            return;
        }
        this.outputURL = this.controlPanel.getOutputFilename();
        MediaLocator outML = JmfAnimationEncoder.createMediaLocator(this.outputURL);
        this.dsink = this.createDataSink(p, outML);
        if (this.dsink == null) {
            System.err.println("Failed to create a DataSink for the given output MediaLocator: " + outML);
            return;
        }
        this.dsink.addDataSinkListener(this);
        System.err.println("start processing...");
        try {
            p.start();
            this.dsink.start();
        }
        catch (IOException e) {
            System.err.println("IO error during processing");
            e.printStackTrace();
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void controllerUpdate(ControllerEvent evt) {
        if (evt instanceof ConfigureCompleteEvent || evt instanceof RealizeCompleteEvent || evt instanceof PrefetchCompleteEvent) {
            Object object = this.waitSync;
            synchronized (object) {
                this.stateTransitionOK = true;
                this.waitSync.notifyAll();
            }
        } else if (evt instanceof ResourceUnavailableEvent) {
            Object object = this.waitSync;
            synchronized (object) {
                this.stateTransitionOK = false;
                this.waitSync.notifyAll();
            }
        } else if (evt instanceof EndOfMediaEvent) {
            evt.getSourceController().stop();
            evt.getSourceController().close();
        }
    }

    public void dataSinkUpdate(DataSinkEvent evt) {
        if (evt instanceof EndOfStreamEvent) {
            evt.getSourceDataSink().close();
        } else if (evt instanceof DataSinkErrorEvent) {
            System.err.println("Data sink error: " + evt);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    boolean waitForState(Processor p, int state) {
        Object object = this.waitSync;
        synchronized (object) {
            try {
                while (p.getState() < state && this.stateTransitionOK) {
                    this.waitSync.wait();
                }
            }
            catch (Exception e) {
                // empty catch block
            }
        }
        return this.stateTransitionOK;
    }

    static MediaLocator createMediaLocator(String url) {
        String file;
        MediaLocator ml;
        if (url.indexOf(":") > 0 && (ml = new MediaLocator(url)) != null) {
            return ml;
        }
        if (url.startsWith(File.separator) ? (ml = new MediaLocator("file:" + url)) != null : (ml = new MediaLocator(file = "file:" + System.getProperty("user.dir") + File.separator + url)) != null) {
            return ml;
        }
        return null;
    }

    DataSink createDataSink(Processor p, MediaLocator outML) {
        DataSink dsink;
        DataSource ds = p.getDataOutput();
        if (ds == null) {
            System.err.println("Something is really wrong: the processor does not have an output DataSource");
            return null;
        }
        try {
            System.err.println("- create DataSink for: " + outML);
            dsink = Manager.createDataSink(ds, outML);
            dsink.open();
        }
        catch (Exception e) {
            System.err.println("Cannot create the DataSink: " + e);
            return null;
        }
        return dsink;
    }

    private void addAnimationFrame(BufferedImage frame) {
        this.ids.addFrame(frame);
    }

    private void endAnimationRecording() {
        this.ids.endRecording();
    }
}

