/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.printing.file;

import com.logicstyle.logicview.viewer.printing.ImagePrinter;
import com.logicstyle.logicview.viewer.printing.ImagePrinterFactory;
import com.logicstyle.logicview.viewer.printing.file.FileImagePrinter;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
import com.logicstyle.mosaic.interfaces.DependencyException;
import java.util.ResourceBundle;

public class FileImagePrinterFactory
implements ImagePrinterFactory {
    private ComponentRepository rep;
    private boolean active;
    private static ResourceBundle res = ResourceBundle.getBundle(FileImagePrinterFactory.class.getName());

    public ImagePrinter createImagePrinter() {
        return new FileImagePrinter();
    }

    public void deactivate() {
        this.active = false;
    }

    public String getDescription() {
        return res.getString("description");
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
}

