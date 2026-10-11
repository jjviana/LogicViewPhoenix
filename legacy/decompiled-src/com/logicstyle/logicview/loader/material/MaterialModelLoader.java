/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jdom.Document
 *  org.jdom.Element
 *  org.jdom.JDOMException
 *  org.jdom.input.SAXBuilder
 */
package com.logicstyle.logicview.loader.material;

import com.logicstyle.logicview.loader.ModelLoadListener;
import com.logicstyle.logicview.loader.ModelLoader;
import com.logicstyle.logicview.loader.material.MaterialModelImp;
import com.logicstyle.logicview.model.MaterialModel;
import com.logicstyle.logicview.model.Model;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
import com.logicstyle.mosaic.interfaces.DependencyException;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import javax.swing.event.EventListenerList;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class MaterialModelLoader
implements ModelLoader {
    ResourceBundle res = ResourceBundle.getBundle(MaterialModelLoader.class.getName());
    private ComponentRepository rep;
    private boolean active = false;
    private EventListenerList listenerList = null;
    private WeakHashMap urlmap = new WeakHashMap();

    public Model LoadModel(URL location) {
        LineNumberReader r;
        URL daturl;
        String dimentions;
        Document modeldoc;
        InputStreamReader reader;
        MaterialModel cached = (MaterialModel)this.urlmap.get(location);
        if (cached != null) {
            return cached;
        }
        this.fireModelLoadListenerLoadStarting(this);
        int cubesz = -1;
        int cubesy = -1;
        int slices = -1;
        float deltax = -1.0f;
        float deltay = -1.0f;
        float deltaz = -1.0f;
        SAXBuilder builder = new SAXBuilder();
        try {
            URLConnection con = location.openConnection();
            reader = new InputStreamReader(con.getInputStream());
        }
        catch (IOException e) {
            throw new RuntimeException(e.toString());
        }
        try {
            modeldoc = builder.build((Reader)reader);
        }
        catch (JDOMException e) {
            throw new RuntimeException(this.res.getString("ModelLoader.ParsingException") + e.toString());
        }
        catch (Throwable e) {
            throw new RuntimeException(this.res.getString("ModelLoader.UnexpectedException") + e.toString());
        }
        Element root = modeldoc.getRootElement();
        if (!root.getName().equals("MaterialModel")) {
            throw new RuntimeException(this.res.getString("ModelLoader.FileFormatException"));
        }
        String modelname = root.getAttributeValue("name");
        if (modelname == null) {
            modelname = this.res.getString("ModelLoader.UntitledModeltitle");
        }
        if ((dimentions = root.getAttributeValue("dimention")) == null) {
            throw new RuntimeException(this.res.getString("ModelLoader.NoDimentionsError"));
        }
        StringTokenizer ctok = new StringTokenizer(dimentions, ",");
        try {
            String dim;
            if (ctok.hasMoreTokens()) {
                dim = ctok.nextToken();
                slices = Integer.valueOf(dim);
            }
            if (ctok.hasMoreTokens()) {
                dim = ctok.nextToken();
                cubesy = Integer.valueOf(dim);
            }
            if (ctok.hasMoreTokens()) {
                dim = ctok.nextToken();
                cubesz = Integer.valueOf(dim);
            }
        }
        catch (NumberFormatException e) {
            // empty catch block
        }
        if (slices == -1 || cubesy == -1 || cubesz == -1) {
            throw new RuntimeException(this.res.getString("ModelLoader.DimentionsSintaxError"));
        }
        String deltas = root.getAttributeValue("deltas");
        if (deltas == null) {
            throw new RuntimeException(this.res.getString("ModelLoader.NoDeltasError"));
        }
        ctok = new StringTokenizer(deltas, ",");
        try {
            String delta;
            if (ctok.hasMoreTokens()) {
                delta = ctok.nextToken();
                deltax = Float.valueOf(delta).floatValue();
            }
            if (ctok.hasMoreTokens()) {
                delta = ctok.nextToken();
                deltay = Float.valueOf(delta).floatValue();
            }
            if (ctok.hasMoreTokens()) {
                delta = ctok.nextToken();
                deltaz = Float.valueOf(delta).floatValue();
            }
        }
        catch (NumberFormatException e) {
            // empty catch block
        }
        if (deltax == -1.0f || deltay == -1.0f || deltaz == -1.0f) {
            throw new RuntimeException(this.res.getString("ModelLoader.DeltaFormatError"));
        }
        Element materials = root.getChild("materials");
        if (materials == null) {
            throw new RuntimeException(this.res.getString("ModelLoader.NoMaterialsError"));
        }
        List mats = materials.getChildren("material");
        if (mats.size() == 0) {
            throw new RuntimeException(this.res.getString("ModelLoader.EmptyMaterialsException"));
        }
        MaterialModel.Material[] mat_tmp = new MaterialModel.Material[mats.size()];
        for (int i = 0; i < mats.size(); ++i) {
            String scode;
            MaterialModel.Material m = new MaterialModel.Material();
            Element material = (Element)mats.get(i);
            m.name = material.getAttributeValue("name");
            String scolor = material.getAttributeValue("color");
            ctok = new StringTokenizer(scolor, ",");
            int r2 = -1;
            int g = -1;
            int b = -1;
            try {
                if (ctok.hasMoreTokens()) {
                    r2 = Integer.valueOf(ctok.nextToken());
                }
                if (ctok.hasMoreTokens()) {
                    g = Integer.valueOf(ctok.nextToken());
                }
                if (ctok.hasMoreTokens()) {
                    b = Integer.valueOf(ctok.nextToken());
                }
            }
            catch (NumberFormatException e) {
                // empty catch block
            }
            if (r2 == -1 || g == -1 || b == -1) {
                throw new RuntimeException(this.res.getString("ModelLoader.MaterialColorFormatError") + m.name);
            }
            m.color = new Color(r2, g, b);
            String strans = material.getAttributeValue("transparency");
            if (strans != null) {
                try {
                    m.alpha = Float.valueOf(strans).floatValue();
                }
                catch (NumberFormatException e) {
                    throw new RuntimeException(this.res.getString("ModelLoader.TransparencyFormatError") + m.name);
                }
            }
            if ((scode = material.getAttributeValue("code")) == null) {
                throw new RuntimeException(this.res.getString("ModelLoader.NoMaterialCodeError") + m.name);
            }
            try {
                int code = Integer.valueOf(scode);
            }
            catch (NumberFormatException e) {
                throw new RuntimeException(this.res.getString("ModelLoader.MaterialCodeFormatError") + m.name);
            }
            String svisible = material.getAttributeValue("visible");
            if (svisible != null) {
                m.enabled = Boolean.valueOf(svisible);
            }
            mat_tmp[code - 1] = m;
        }
        Element datafile = root.getChild("datafile");
        if (datafile == null) {
            throw new RuntimeException(this.res.getString("ModelLoader.NoDatafileError"));
        }
        String filename = datafile.getAttributeValue("name");
        if (filename == null) {
            throw new RuntimeException(this.res.getString("ModelLoader.NoFileNameError"));
        }
        String basefile = location.getPath();
        if (basefile.lastIndexOf(47) != -1 && filename.indexOf(47) == -1) {
            try {
                daturl = new URL(location.getProtocol(), location.getHost(), location.getPort(), basefile.substring(0, basefile.lastIndexOf(47)) + "/" + filename);
            }
            catch (MalformedURLException e) {
                throw new RuntimeException(e.toString());
            }
        }
        try {
            daturl = new URL(filename);
        }
        catch (MalformedURLException e) {
            throw new RuntimeException(e.toString());
        }
        try {
            URLConnection datacon = daturl.openConnection();
            r = new LineNumberReader(new InputStreamReader(datacon.getInputStream()));
        }
        catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
        byte[][][] grid = new byte[slices][cubesy][cubesz];
        int[] mat_count = new int[mat_tmp.length];
        try {
            for (int i = 0; i < slices; ++i) {
                for (int j = 0; j < cubesy; ++j) {
                    for (int k = 0; k < cubesz; ++k) {
                        String token = this.getNextToken(r);
                        if (token == null) {
                            throw new RuntimeException(this.res.getString("ModelLoader.PrematureEOF") + r.getLineNumber());
                        }
                        try {
                            grid[i][j][k] = Byte.valueOf(token);
                            int n = grid[i][j][k] - 1;
                            mat_count[n] = mat_count[n] + 1;
                        }
                        catch (NumberFormatException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                        if (grid[i][j][k] >= 1) continue;
                        throw new RuntimeException(this.res.getString("ModelLoader.InvalidData") + r.getLineNumber() + ":" + token);
                    }
                }
                this.fireModelLoadListenerLoadingPercentComplete(100 * i / slices);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(this.res.getString("ModelLoader.IOException") + r.getLineNumber() + ":" + e.toString());
        }
        int real_materials_count = 0;
        for (int i = 0; i < mat_count.length; ++i) {
            if (mat_count[i] <= 0) continue;
            ++real_materials_count;
        }
        MaterialModelImp mod = new MaterialModelImp(slices, cubesy, cubesz, real_materials_count);
        mod.setdeltaX(deltax);
        mod.setdeltaY(deltay);
        mod.setdeltaZ(deltaz);
        if (real_materials_count < mat_tmp.length) {
            int i;
            byte[] translation_table = new byte[mat_count.length];
            int mat_cod = 1;
            for (i = 0; i < mat_count.length; ++i) {
                if (mat_count[i] > 0) {
                    mod.setMaterial(mat_cod, mat_tmp[i]);
                    translation_table[i] = mat_cod;
                    mat_cod = (byte)(mat_cod + 1);
                    continue;
                }
                translation_table[i] = -1;
            }
            for (i = 0; i < slices; ++i) {
                for (int j = 0; j < cubesy; ++j) {
                    for (int k = 0; k < cubesz; ++k) {
                        if (translation_table[grid[i][j][k] - 1] == -1) {
                            throw new RuntimeException("Bug found: material at the same time is and isn't at the grid!");
                        }
                        grid[i][j][k] = translation_table[grid[i][j][k] - 1];
                    }
                }
            }
        } else {
            for (int i = 0; i < mat_tmp.length; ++i) {
                mod.setMaterial(i, mat_tmp[i]);
            }
        }
        mod.setGrid(grid);
        this.fireModelLoadListenerLoadFinished(this);
        this.urlmap.put(location, mod);
        return mod;
    }

    public void deactivate() {
        this.active = false;
    }

    public String getDescription() {
        return this.res.getString("description");
    }

    public String getFilenameExtension() {
        return this.res.getString("filenameExtension");
    }

    public String getName() {
        return this.res.getString("name");
    }

    public String getTypeName() {
        return this.res.getString("typeName");
    }

    public boolean init(ComponentRepository c) throws DependencyException {
        this.rep = c;
        this.active = true;
        return true;
    }

    public boolean isActive() {
        return this.active;
    }

    public synchronized void addModelLoadListener(ModelLoadListener listener) {
        if (this.listenerList == null) {
            this.listenerList = new EventListenerList();
        }
        this.listenerList.add(ModelLoadListener.class, listener);
    }

    public synchronized void removeModelLoadListener(ModelLoadListener listener) {
        this.listenerList.remove(ModelLoadListener.class, listener);
    }

    private void fireModelLoadListenerLoadStarting(ModelLoader event) {
        if (this.listenerList == null) {
            return;
        }
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ModelLoadListener.class) continue;
            ((ModelLoadListener)listeners[i + 1]).loadStarting(event);
        }
    }

    private void fireModelLoadListenerLoadFinished(ModelLoader event) {
        if (this.listenerList == null) {
            return;
        }
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ModelLoadListener.class) continue;
            ((ModelLoadListener)listeners[i + 1]).loadFinished(event);
        }
    }

    private void fireModelLoadListenerLoadingPercentComplete(float event) {
        if (this.listenerList == null) {
            return;
        }
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ModelLoadListener.class) continue;
            ((ModelLoadListener)listeners[i + 1]).loadingPercentComplete(this, event);
        }
    }

    private String getNextToken(Reader r) throws IOException {
        char c;
        int val;
        StringBuffer token = new StringBuffer();
        do {
            val = r.read();
            c = (char)val;
        } while (val != -1 && Character.isWhitespace(c));
        if (val == -1) {
            return null;
        }
        while (val != -1 && !Character.isWhitespace(c)) {
            token.append(c);
            val = r.read();
            c = (char)val;
        }
        return token.toString();
    }
}

