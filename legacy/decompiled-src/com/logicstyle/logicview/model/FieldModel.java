/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.model;

import com.logicstyle.logicview.model.Materialized;
import com.logicstyle.logicview.model.VoxellModel;

public interface FieldModel
extends VoxellModel,
Materialized {
    public int getTimeStepSkip();

    public int getTimeStepCount();

    public void setCurrentTimeStep(int var1);

    public int getCurrentTimeStep();

    public String getUnits();
}

