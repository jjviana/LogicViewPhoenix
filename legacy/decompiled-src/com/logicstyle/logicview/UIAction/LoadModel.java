/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.UIAction;

import com.logicstyle.logicview.MainWindow.MainWindow;
import com.logicstyle.logicview.UIAction.ActionGroup;
import com.logicstyle.logicview.UIAction.UIAction;
import com.logicstyle.logicview.loader.ModelLoader;
import com.logicstyle.logicview.model.Model;
import com.logicstyle.logicview.viewer.Viewer;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
import com.logicstyle.mosaic.interfaces.DependencyException;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.filechooser.FileFilter;

public class LoadModel
implements UIAction {
    private static ResourceBundle res = ResourceBundle.getBundle(LoadModel.class.getName());
    private ComponentRepository rep;
    private boolean active = false;

    public void deactivate() {
        this.active = false;
    }

    public void execute(final Object context) {
        Thread t = new Thread(new Runnable(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            public void run() {
                block15: {
                    MainWindow main = (MainWindow)context;
                    main.getParentFrame().setCursor(3);
                    try {
                        URL location;
                        List loaders = LoadModel.this.rep.findByInterface(ModelLoader.class);
                        JFileChooser chooser = new JFileChooser();
                        if (loaders.size() == 0) {
                            throw new RuntimeException("No active loaders found");
                        }
                        NameFilter[] nf = new NameFilter[loaders.size()];
                        for (int i = 0; i < loaders.size(); ++i) {
                            nf[i] = new NameFilter((ModelLoader)loaders.get(i));
                            chooser.addChoosableFileFilter(nf[i]);
                        }
                        int choice = chooser.showDialog(main.getParentFrame(), res.getString("fileChooserTitle"));
                        if (choice != 0) break block15;
                        File f = chooser.getSelectedFile();
                        NameFilter filter = (NameFilter)chooser.getFileFilter();
                        ModelLoader l = filter.loader;
                        String fname = f.getAbsolutePath();
                        if (!(fname = fname.replace('\\', '/')).startsWith("/")) {
                            fname = "/" + fname;
                        }
                        try {
                            location = new URL("file://" + fname);
                            System.err.println("location=" + location);
                        }
                        catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                        ProgressMonitor monitor = new ProgressMonitor(main.getParentFrame(), "Loading " + l.getTypeName(), null, 0, 100);
                        monitor.setMillisToPopup(0);
                        monitor.setMillisToDecideToPopup(0);
                        monitor.setProgress(10);
                        Model mod = l.LoadModel(location);
                        List viewers = LoadModel.this.rep.findByInterface(Viewer.class);
                        if (viewers.size() == 0) {
                            throw new RuntimeException("No active viewers found");
                        }
                        Vector<Viewer> supported = new Vector<Viewer>();
                        for (int j = 0; j < viewers.size(); ++j) {
                            Viewer v = (Viewer)viewers.get(j);
                            if (!v.isModelSupported(mod)) continue;
                            supported.addElement(v);
                        }
                        if (supported.size() == 0) {
                            throw new RuntimeException("No viewer found for model " + l.getTypeName());
                        }
                        Object[] sup = new Viewer[supported.size()];
                        supported.copyInto(sup);
                        if (monitor.isCanceled()) {
                            return;
                        }
                        monitor.setNote("Constructing view...");
                        monitor.setProgress(50);
                        main.addDocView(mod, (Viewer[])sup);
                        monitor.close();
                        return;
                    }
                    catch (Throwable t) {
                        t.printStackTrace();
                        main.getParentFrame().setCursor(0);
                        JOptionPane.showMessageDialog(main.getParentFrame(), res.getString("errorMessage"), res.getString("errorMessageTitle"), 0);
                    }
                    finally {
                        main.getParentFrame().setCursor(0);
                    }
                }
            }
        });
        t.start();
    }

    public ActionGroup getActionGroup() {
        return MainWindow.MODEL_ACTION_GROUP;
    }

    public String getDescription() {
        return res.getString("description");
    }

    public ImageIcon getIcon() {
        return null;
    }

    public String getName() {
        return res.getString("name");
    }

    public boolean init(ComponentRepository c) throws DependencyException {
        this.rep = c;
        this.active = true;
        return true;
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean isInContext(Object context) {
        return context instanceof MainWindow;
    }

    public static class NameFilter
    extends FileFilter {
        private ModelLoader loader;

        public NameFilter(ModelLoader m) {
            this.loader = m;
        }

        public boolean accept(File f) {
            return f.isDirectory() || f.getName().endsWith(this.loader.getFilenameExtension());
        }

        public String getDescription() {
            return this.loader.getTypeName();
        }
    }
}

