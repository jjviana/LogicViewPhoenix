/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.loader.material;

import com.logicstyle.logicview.model.MaterialModel;
import java.util.ResourceBundle;

public class MaterialModelImp
implements MaterialModel {
    private ResourceBundle res = ResourceBundle.getBundle(MaterialModelImp.class.getName());
    private byte[][][] grid;
    private int sizeX;
    private int sizeY;
    private int sizeZ;
    private MaterialModel.Material[] materials;
    private float deltaX;
    private float deltaY;
    private float deltaZ;

    public MaterialModelImp(int sx, int sy, int sz, int matcount) {
        this.materials = new MaterialModel.Material[matcount];
        this.sizeX = sx;
        this.sizeY = sy;
        this.sizeZ = sz;
    }

    public String getDescription() {
        return this.res.getString("description");
    }

    public String getName() {
        return this.res.getString("name");
    }

    public double getDeltaX() {
        return this.deltaX;
    }

    public double getDeltaY() {
        return this.deltaY;
    }

    public double getDeltaZ() {
        return this.deltaZ;
    }

    public MaterialModel.Material[] getMaterials() {
        return this.materials;
    }

    public int getWidth() {
        return this.sizeX;
    }

    public int getHeight() {
        return this.sizeY;
    }

    public int getDepth() {
        return this.sizeZ;
    }

    public int getMaterialCount() {
        return this.materials.length;
    }

    public void setMaterial(int code, MaterialModel.Material m) {
        this.materials[code - 1] = m;
    }

    public MaterialModel.Material getMaterial(int index) {
        return this.materials[index];
    }

    public void setdeltaX(float deltaX) {
        this.deltaX = deltaX;
    }

    public void setdeltaY(float deltaY) {
        this.deltaY = deltaY;
    }

    public void setdeltaZ(float deltaZ) {
        this.deltaZ = deltaZ;
    }

    public double getDoubleValueAt(int x, int y, int z) {
        return this.grid[x][y][z];
    }

    public float getFloatValueAt(int x, int y, int z) {
        return this.grid[x][y][z];
    }

    public int getIntValueAt(int x, int y, int z) {
        return this.grid[x][y][z];
    }

    public Object getObjectValueAt(int x, int y, int z) {
        return new Integer(this.grid[x][y][z]);
    }

    public byte[][][] getGrid() {
        return this.grid;
    }

    void setGrid(byte[][][] b) {
        this.grid = b;
    }

    public boolean isEnabled(int x, int y, int z) {
        return this.materials[this.grid[x][y][z] - 1].enabled;
    }
}

