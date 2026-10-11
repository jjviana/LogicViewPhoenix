/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.model;

import com.logicstyle.logicview.model.VoxellModel;
import java.awt.Color;

public interface MaterialModel
extends VoxellModel {
    public Material[] getMaterials();

    public static class Material {
        public String name = new String();
        public Color color = Color.white;
        public float alpha = 0.0f;
        public boolean enabled = true;
    }
}

