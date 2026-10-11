/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.animation.jmf;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;
import javax.media.Buffer;
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
import javax.media.Time;
import javax.media.control.TrackControl;
import javax.media.datasink.DataSinkErrorEvent;
import javax.media.datasink.DataSinkEvent;
import javax.media.datasink.DataSinkListener;
import javax.media.datasink.EndOfStreamEvent;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;

public class JpegImagesToMovie
implements ControllerListener,
DataSinkListener {
    Object waitSync = new Object();
    boolean stateTransitionOK = true;
    Object waitFileSync = new Object();
    boolean fileDone = false;
    boolean fileSuccess = true;

    public boolean doIt(int width, int height, int frameRate, Vector inFiles, MediaLocator outML) {
        Processor p;
        ImageDataSource ids = new ImageDataSource(width, height, frameRate, inFiles);
        try {
            System.err.println("- create processor for the image datasource ...");
            p = Manager.createProcessor(ids);
        }
        catch (Exception e) {
            System.err.println("Yikes!  Cannot create a processor from the data source.");
            return false;
        }
        p.addControllerListener(this);
        p.configure();
        if (!this.waitForState(p, 180)) {
            System.err.println("Failed to configure the processor.");
            return false;
        }
        p.setContentDescriptor(new ContentDescriptor("video.quicktime"));
        TrackControl[] tcs = p.getTrackControls();
        Format[] f = tcs[0].getSupportedFormats();
        if (f == null || f.length <= 0) {
            System.err.println("The mux does not support the input format: " + tcs[0].getFormat());
            return false;
        }
        tcs[0].setFormat(f[0]);
        System.err.println("Setting the track format to: " + f[0]);
        p.realize();
        if (!this.waitForState(p, 300)) {
            System.err.println("Failed to realize the processor.");
            return false;
        }
        DataSink dsink = this.createDataSink(p, outML);
        if (dsink == null) {
            System.err.println("Failed to create a DataSink for the given output MediaLocator: " + outML);
            return false;
        }
        dsink.addDataSinkListener(this);
        this.fileDone = false;
        System.err.println("start processing...");
        try {
            p.start();
            dsink.start();
        }
        catch (IOException e) {
            System.err.println("IO error during processing");
            return false;
        }
        this.waitForFileDone();
        try {
            dsink.close();
        }
        catch (Exception e) {
            // empty catch block
        }
        p.removeControllerListener(this);
        System.err.println("...done processing.");
        return true;
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    boolean waitForFileDone() {
        Object object = this.waitFileSync;
        synchronized (object) {
            try {
                while (!this.fileDone) {
                    this.waitFileSync.wait();
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return this.fileSuccess;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void dataSinkUpdate(DataSinkEvent evt) {
        if (evt instanceof EndOfStreamEvent) {
            Object object = this.waitFileSync;
            synchronized (object) {
                this.fileDone = true;
                this.waitFileSync.notifyAll();
            }
        }
        if (evt instanceof DataSinkErrorEvent) {
            Object object = this.waitFileSync;
            synchronized (object) {
                this.fileDone = true;
                this.fileSuccess = false;
                this.waitFileSync.notifyAll();
            }
        }
    }

    public static void main(String[] args) {
        MediaLocator oml;
        if (args.length == 0) {
            JpegImagesToMovie.prUsage();
        }
        int width = -1;
        int height = -1;
        int frameRate = 1;
        Vector<String> inputFiles = new Vector<String>();
        String outputURL = null;
        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals("-w")) {
                if (++i >= args.length) {
                    JpegImagesToMovie.prUsage();
                }
                width = new Integer(args[i]);
                continue;
            }
            if (args[i].equals("-h")) {
                if (++i >= args.length) {
                    JpegImagesToMovie.prUsage();
                }
                height = new Integer(args[i]);
                continue;
            }
            if (args[i].equals("-f")) {
                if (++i >= args.length) {
                    JpegImagesToMovie.prUsage();
                }
                frameRate = new Integer(args[i]);
                continue;
            }
            if (args[i].equals("-o")) {
                if (++i >= args.length) {
                    JpegImagesToMovie.prUsage();
                }
                outputURL = args[i];
                continue;
            }
            inputFiles.addElement(args[i]);
        }
        if (outputURL == null || inputFiles.size() == 0) {
            JpegImagesToMovie.prUsage();
        }
        if (!outputURL.endsWith(".mov") && !outputURL.endsWith(".MOV")) {
            System.err.println("The output file extension should end with a .mov extension");
            JpegImagesToMovie.prUsage();
        }
        if (width < 0 || height < 0) {
            System.err.println("Please specify the correct image size.");
            JpegImagesToMovie.prUsage();
        }
        if (frameRate < 1) {
            frameRate = 1;
        }
        if ((oml = JpegImagesToMovie.createMediaLocator(outputURL)) == null) {
            System.err.println("Cannot build media locator from: " + outputURL);
            System.exit(0);
        }
        JpegImagesToMovie imageToMovie = new JpegImagesToMovie();
        imageToMovie.doIt(width, height, frameRate, inputFiles, oml);
        System.exit(0);
    }

    static void prUsage() {
        System.err.println("Usage: java JpegImagesToMovie -w <width> -h <height> -f <frame rate> -o <output URL> <input JPEG file 1> <input JPEG file 2> ...");
        System.exit(-1);
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

    class ImageSourceStream
    implements PullBufferStream {
        Vector images;
        int width;
        int height;
        VideoFormat format;
        int nextImage = 0;
        boolean ended = false;

        public ImageSourceStream(int width, int height, int frameRate, Vector images) {
            this.width = width;
            this.height = height;
            this.images = images;
            this.format = new VideoFormat("jpeg", new Dimension(width, height), -1, Format.byteArray, frameRate);
        }

        public boolean willReadBlock() {
            return false;
        }

        public void read(Buffer buf) throws IOException {
            if (this.nextImage >= this.images.size()) {
                System.err.println("Done reading all images.");
                buf.setEOM(true);
                buf.setOffset(0);
                buf.setLength(0);
                this.ended = true;
                return;
            }
            String imageFile = (String)this.images.elementAt(this.nextImage);
            ++this.nextImage;
            System.err.println("  - reading image file: " + imageFile);
            RandomAccessFile raFile = new RandomAccessFile(imageFile, "r");
            byte[] data = null;
            if (buf.getData() instanceof byte[]) {
                data = (byte[])buf.getData();
            }
            if (data == null || (long)data.length < raFile.length()) {
                data = new byte[(int)raFile.length()];
                buf.setData(data);
            }
            raFile.readFully(data, 0, (int)raFile.length());
            System.err.println("    read " + raFile.length() + " bytes.");
            buf.setOffset(0);
            buf.setLength((int)raFile.length());
            buf.setFormat(this.format);
            buf.setFlags(buf.getFlags() | 0x10);
            raFile.close();
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
    }

    class ImageDataSource
    extends PullBufferDataSource {
        ImageSourceStream[] streams = new ImageSourceStream[1];

        ImageDataSource(int width, int height, int frameRate, Vector images) {
            this.streams[0] = new ImageSourceStream(width, height, frameRate, images);
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
            return DURATION_UNKNOWN;
        }

        public Object[] getControls() {
            return new Object[0];
        }

        public Object getControl(String type) {
            return null;
        }
    }
}

