/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.mosaic.interfaces.config;

import java.util.List;

public interface Node {
    public String getName();

    public String[] getAttributeNames();

    public String getAttributeValue(String var1);

    public List getChilds();
}

