/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.loader.field;

import com.logicstyle.logicview.loader.field.FieldModelImp;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
import java.net.URL;

public class FieldModelLogImp
extends FieldModelImp {
    private static final double ln10 = Math.log(10.0);

    public FieldModelLogImp(URL url, ComponentRepository rep) {
        super(url, rep);
    }

    public String getUnits() {
        String retValue = super.getUnits();
        retValue = "Log of " + retValue;
        return retValue;
    }

    public void setCurrentTimeStep(int step) {
        super.setCurrentTimeStep(step);
        for (int i = 0; i < this.grid.length; ++i) {
            for (int j = 0; j < this.grid[i].length; ++j) {
                for (int k = 0; k < this.grid[i][j].length; ++k) {
                    this.grid[i][j][k] = this.grid[i][j][k] != 0.0f ? (float)(Math.log(Math.abs(this.grid[i][j][k])) / ln10) : (float)(Math.log(1.4E-45f) / ln10);
                }
            }
        }
    }
}

