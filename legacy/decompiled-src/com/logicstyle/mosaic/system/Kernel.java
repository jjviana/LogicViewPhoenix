/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jdom.Document
 *  org.jdom.input.SAXBuilder
 */
package com.logicstyle.mosaic.system;

import com.logicstyle.mosaic.system.ApplicationRepository;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

public class Kernel {
    private static Logger log = Logger.getLogger("mosaic.system");

    public static void main(String[] args) {
        Document config;
        InputStreamReader confreader;
        SAXBuilder builder = new SAXBuilder(false);
        try {
            PrintStream err = new PrintStream(new FileOutputStream("debug.txt"));
            System.setErr(err);
            System.out.println("Check file \"debug.txt\" for debug output");
        }
        catch (Throwable t) {
            System.err.println("Cannot redirect output to file \"debug.txt\".");
            t.printStackTrace();
        }
        if (args.length > 0) {
            try {
                FileReader fr;
                confreader = fr = new FileReader(args[0]);
            }
            catch (Exception e) {
                log.log(Level.SEVERE, "Error reading config file:", e);
                return;
            }
        } else {
            InputStream is = Kernel.class.getResourceAsStream("Repository.xml");
            if (is == null) {
                log.log(Level.SEVERE, "No config file repository.xml found");
                log.log(Level.INFO, "Usage: Application [<repository config file>]");
                return;
            }
            InputStreamReader ir = new InputStreamReader(is);
            confreader = ir;
        }
        try {
            config = builder.build((Reader)confreader);
        }
        catch (Exception e) {
            log.log(Level.SEVERE, "Error parsing configuration file", e);
            return;
        }
        try {
            ApplicationRepository rep = new ApplicationRepository(config);
        }
        catch (Throwable t) {
            log.log(Level.SEVERE, "Error creating application repository", t);
            System.exit(-1);
            return;
        }
    }
}

