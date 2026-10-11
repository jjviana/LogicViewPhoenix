/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.printing;

import com.logicstyle.logicview.viewer.printing.ImagePrinter;
import com.logicstyle.mosaic.interfaces.Component;

public interface ImagePrinterFactory
extends Component {
    public ImagePrinter createImagePrinter();
}

