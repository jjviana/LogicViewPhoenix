/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.loader.field;

import com.logicstyle.logicview.loader.ModelLoader;
import com.logicstyle.logicview.loader.field.FieldModelLoader;
import com.logicstyle.logicview.loader.field.FieldModelLogImp;
import com.logicstyle.logicview.model.Model;
import java.net.URL;

public class FieldLogModelLoader
extends FieldModelLoader
implements ModelLoader {
    public String getDescription() {
        return "Log of Field Models";
    }

    public Model LoadModel(URL location) {
        return new FieldModelLogImp(location, this.rep);
    }

    public String getTypeName() {
        return "Log of Field Models";
    }
}

