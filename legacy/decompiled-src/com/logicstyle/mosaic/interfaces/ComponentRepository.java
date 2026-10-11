/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.mosaic.interfaces;

import com.logicstyle.mosaic.interfaces.Component;
import java.util.List;

public interface ComponentRepository {
    public List findByInterface(Class var1);

    public List findAllByInterface(Class var1);

    public Component findComponentByInterface(Class var1);
}

