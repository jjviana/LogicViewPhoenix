/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.animation.jmf;

import com.logicstyle.logicview.viewer.animation.jmf.ImageSourceStream;
import java.awt.image.BufferedImage;
import javax.media.MediaLocator;
import javax.media.Time;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;

class ImageDataSource
extends PullBufferDataSource {
    ImageSourceStream[] streams = new ImageSourceStream[1];
    private Time duration;

    ImageDataSource(int width, int height, int frameRate, double duration) {
        this.streams[0] = new ImageSourceStream(width, height, frameRate);
        this.duration = new Time(duration);
    }

    public void setLocator(MediaLocator source) {
    }

    public MediaLocator getLocator() {
        return null;
    }

    public String getContentType() {
        return "raw";
    }

    public void connect() {
    }

    public void disconnect() {
    }

    public void start() {
    }

    public void stop() {
    }

    public PullBufferStream[] getStreams() {
        return this.streams;
    }

    public Time getDuration() {
        System.err.println("Returning duration = " + this.duration.getSeconds());
        return this.duration;
    }

    public Object[] getControls() {
        return new Object[0];
    }

    public Object getControl(String type) {
        return null;
    }

    public void addFrame(BufferedImage image) {
        this.streams[0].addImage(image);
    }

    public void endRecording() {
        this.streams[0].setEnded(true);
    }
}

