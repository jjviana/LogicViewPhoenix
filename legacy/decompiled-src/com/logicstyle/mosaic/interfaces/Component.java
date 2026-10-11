/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.mosaic.interfaces;

import com.logicstyle.mosaic.interfaces.ComponentRepository;
import com.logicstyle.mosaic.interfaces.DependencyException;

public interface Component {
    public boolean init(ComponentRepository var1) throws DependencyException;

    public boolean isActive();

    public void deactivate();

    public String getName();

    public String getDescription();
}

