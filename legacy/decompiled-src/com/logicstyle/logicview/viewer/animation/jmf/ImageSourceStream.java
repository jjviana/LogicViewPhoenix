/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.animation.jmf;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullBufferStream;
import javax.media.util.ImageToBuffer;

class ImageSourceStream
implements PullBufferStream {
    int width;
    int height;
    VideoFormat format;
    int nextImage = 0;
    boolean ended = false;
    private ArrayList images;
    private int frameRate;
    private double duration;

    public ImageSourceStream(int width, int height, int frameRate) {
        this.width = width;
        this.height = height;
        this.frameRate = frameRate;
        this.images = new ArrayList();
        this.format = new RGBFormat(new Dimension(width, height), width * height * 4, Format.byteArray, frameRate, 32, 3, 2, 1, 4, width * 4, 0, 1);
        System.err.println("ImageSourceStream created with format " + this.format);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean willReadBlock() {
        ArrayList arrayList = this.images;
        synchronized (arrayList) {
            return this.images.size() == 0;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void read(Buffer buf) throws IOException {
        ArrayList arrayList = this.images;
        synchronized (arrayList) {
            System.err.println("Read started. Image queue size = " + this.images.size());
            if (this.images.size() == 0 && this.ended) {
                System.err.println("Done reading all images.");
                buf.setEOM(true);
                buf.setOffset(0);
                buf.setLength(0);
                return;
            }
            if (this.images.size() == 0) {
                try {
                    System.err.println("Images queue size is empty. Sleeping...");
                    this.images.wait();
                }
                catch (InterruptedException e) {
                    // empty catch block
                }
                if (this.images.size() == 0) {
                    System.err.println("Images queue size still empty after sleep. Assuming movie terminated");
                    buf.setEOM(true);
                    buf.setOffset(0);
                    buf.setLength(0);
                    return;
                }
            }
            BufferedImage image = (BufferedImage)this.images.get(0);
            Buffer b = ImageToBuffer.createBuffer(image, this.frameRate);
            int[] processed = (int[])b.getData();
            byte[] output = new byte[processed.length * 4];
            buf.setData(output);
            for (int i = 0; i < processed.length; ++i) {
                int base = i * 4;
                output[base + 0] = (byte)(processed[i] >>> 0 & 0xFF);
                output[base + 1] = (byte)(processed[i] >>> 8 & 0xFF);
                output[base + 2] = (byte)(processed[i] >>> 16 & 0xFF);
                output[base + 3] = (byte)(processed[i] >>> 24 & 0xFF);
            }
            buf.setFormat(this.format);
            buf.setLength(processed.length * 4);
            buf.setOffset(0);
            buf.setFlags(buf.getFlags() | 0x10 | 0x20);
            System.err.println("Frame for image" + image + " generated");
            this.images.remove(image);
            System.err.println("Image queue size now " + this.images.size());
        }
    }

    public Format getFormat() {
        return this.format;
    }

    public ContentDescriptor getContentDescriptor() {
        return new ContentDescriptor("raw");
    }

    public long getContentLength() {
        return 0L;
    }

    public boolean endOfStream() {
        return this.ended;
    }

    public Object[] getControls() {
        return new Object[0];
    }

    public Object getControl(String type) {
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setEnded(boolean ended) {
        this.ended = ended;
        if (ended) {
            ArrayList arrayList = this.images;
            synchronized (arrayList) {
                this.images.notifyAll();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addImage(BufferedImage image) {
        ArrayList arrayList = this.images;
        synchronized (arrayList) {
            this.images.add(image);
            this.images.notifyAll();
        }
    }
}

