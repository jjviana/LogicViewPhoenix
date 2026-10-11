/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jdom.Document
 *  org.jdom.Element
 *  org.jdom.JDOMException
 *  org.jdom.input.SAXBuilder
 */
package com.logicstyle.logicview.loader.field;

import com.logicstyle.logicview.loader.ModelLoader;
import com.logicstyle.logicview.model.FieldModel;
import com.logicstyle.logicview.model.MaterialModel;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
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
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class FieldModelImp
implements FieldModel {
    private ResourceBundle res = ResourceBundle.getBundle(FieldModelImp.class.getName());
    protected float[][][] grid;
    private int timeStepCount;
    private float deltax;
    private float deltay;
    private float deltaz;
    private URL location;
    private int currentTimeStep;
    private int startDataSufixValue;
    private int incrementDataSufixValue;
    private String datPrefix;
    private String name;
    private MaterialModel matmod;
    private ComponentRepository rep;
    private String units;

    public FieldModelImp(URL location, ComponentRepository r) {
        Element data;
        String dimentions;
        Document modeldoc;
        InputStreamReader reader;
        this.rep = r;
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
        if (!root.getName().equals("FieldModel")) {
            throw new RuntimeException(this.res.getString("ModelLoader.FileFormatException"));
        }
        int slices = -1;
        int cubesy = -1;
        int cubesz = -1;
        this.name = root.getAttributeValue("name");
        if (this.name == null) {
            this.name = this.res.getString("ModelLoader.UntitledModeltitle");
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
                this.deltax = Float.valueOf(delta).floatValue();
            }
            if (ctok.hasMoreTokens()) {
                delta = ctok.nextToken();
                this.deltay = Float.valueOf(delta).floatValue();
            }
            if (ctok.hasMoreTokens()) {
                delta = ctok.nextToken();
                this.deltaz = Float.valueOf(delta).floatValue();
            }
        }
        catch (NumberFormatException e) {
            // empty catch block
        }
        if (this.deltax == -1.0f || this.deltay == -1.0f || this.deltaz == -1.0f) {
            throw new RuntimeException(this.res.getString("ModelLoader.DeltaFormatError"));
        }
        this.grid = new float[slices][cubesy][cubesz];
        String tsteps = root.getAttributeValue("timesteps");
        if (tsteps == null) {
            throw new RuntimeException(this.res.getString("ModelLoader.NoTimeStepCountError"));
        }
        try {
            this.timeStepCount = Integer.valueOf(tsteps);
        }
        catch (NumberFormatException e) {
            throw new RuntimeException(this.res.getString("ModelLoader.TimeStepCountSintaxError"));
        }
        if (this.timeStepCount < 1) {
            throw new RuntimeException(this.res.getString("ModelLoader.TimeStepCountSintaxError"));
        }
        this.units = root.getAttributeValue("units");
        if (this.units == null) {
            this.units = this.res.getString("defaultUnits");
        }
        if ((data = root.getChild("data")) == null) {
            throw new RuntimeException(this.res.getString("ModelLoader.NoDatafileError"));
        }
        this.datPrefix = data.getAttributeValue("prefix");
        if (this.datPrefix == null) {
            throw new RuntimeException(this.res.getString("ModelLoader.NoDatPrefixError"));
        }
        String temp = data.getAttributeValue("initialSufixCount");
        if (temp != null) {
            try {
                this.startDataSufixValue = Integer.parseInt(temp);
            }
            catch (NumberFormatException e) {
                throw new RuntimeException(this.res.getString("ModelLoader.InvalidStartStepPrefixError"));
            }
        } else {
            this.startDataSufixValue = 0;
        }
        temp = data.getAttributeValue("sufixIncrement");
        if (temp != null) {
            try {
                this.incrementDataSufixValue = Integer.parseInt(temp);
            }
            catch (NumberFormatException e) {
                throw new RuntimeException(this.res.getString("ModelLoader.InvaludSufixIncrement"));
            }
        } else {
            this.incrementDataSufixValue = 1;
        }
        this.location = location;
        this.setCurrentTimeStep(0);
        Element MaterialModel2 = root.getChild("MaterialModel");
        if (MaterialModel2 != null) {
            URL matmodurl;
            String matmodel_location = MaterialModel2.getAttributeValue("location");
            if (matmodel_location == null) {
                throw new RuntimeException(this.res.getString("missingMaterialModelLocation"));
            }
            try {
                matmodurl = new URL(location, matmodel_location);
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
                throw new RuntimeException(e.toString());
            }
            List loaders = this.rep.findByInterface(ModelLoader.class);
            MaterialModel matmod = null;
            for (int i = 0; i < loaders.size(); ++i) {
                ModelLoader ml = (ModelLoader)loaders.get(i);
                if (!matmodel_location.endsWith(ml.getFilenameExtension())) continue;
                try {
                    matmod = (MaterialModel)ml.LoadModel(matmodurl);
                    break;
                }
                catch (Exception ex) {
                    // empty catch block
                }
            }
            if (matmod == null) {
                throw new RuntimeException(this.res.getString("noMaterialModelLoader"));
            }
            this.setMaterialModel(matmod);
        }
    }

    public int getCurrentTimeStep() {
        return this.currentTimeStep;
    }

    public int getTimeStepCount() {
        return this.timeStepCount;
    }

    public void setCurrentTimeStep(int step) {
        LineNumberReader r;
        URL daturl;
        String basefile = this.location.getPath();
        String s_step = String.valueOf(this.startDataSufixValue + step * this.incrementDataSufixValue);
        String filename = this.datPrefix + s_step;
        try {
            daturl = new URL(this.location.getProtocol(), this.location.getHost(), this.location.getPort(), basefile.substring(0, basefile.lastIndexOf(47)) + "/" + filename);
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
        try {
            for (int i = 0; i < this.grid.length; ++i) {
                for (int j = 0; j < this.grid[i].length; ++j) {
                    for (int k = 0; k < this.grid[i][j].length; ++k) {
                        String token = this.getNextToken(r);
                        if (token == null) {
                            throw new RuntimeException(this.res.getString("ModelLoader.PrematureEOF") + r.getLineNumber());
                        }
                        try {
                            this.grid[i][j][k] = Float.valueOf(token).floatValue();
                            continue;
                        }
                        catch (NumberFormatException e) {
                            System.err.println(this.res.getString("ModelLoader.NumberFormatException") + r.getLineNumber() + ":" + token);
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException(this.res.getString("ModelLoader.IOException") + r.getLineNumber() + ":" + e.toString());
        }
        this.currentTimeStep = step;
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

    public double getDeltaX() {
        return this.deltax;
    }

    public double getDeltaY() {
        return this.deltay;
    }

    public double getDeltaZ() {
        return this.deltaz;
    }

    public int getDepth() {
        return this.grid[0][0].length;
    }

    public String getDescription() {
        return this.res.getString("description");
    }

    public double getDoubleValueAt(int x, int y, int z) {
        return this.grid[x][y][z];
    }

    public float getFloatValueAt(int x, int y, int z) {
        return this.grid[x][y][z];
    }

    public int getHeight() {
        return this.grid[0].length;
    }

    public int getIntValueAt(int x, int y, int z) {
        return (int)this.grid[x][y][z];
    }

    public String getName() {
        return this.name;
    }

    public Object getObjectValueAt(int x, int y, int z) {
        return new Float(this.grid[x][y][z]);
    }

    public int getWidth() {
        return this.grid.length;
    }

    public boolean isEnabled(int x, int y, int z) {
        return this.grid[x][y][z] != 0.0f;
    }

    public MaterialModel getMaterialModel() {
        return this.matmod;
    }

    public void setMaterialModel(MaterialModel m) {
        this.matmod = m;
    }

    public String getUnits() {
        return this.units;
    }

    public int getTimeStepSkip() {
        return this.incrementDataSufixValue;
    }
}

