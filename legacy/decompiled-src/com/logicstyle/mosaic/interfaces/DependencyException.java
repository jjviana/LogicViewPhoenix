/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.mosaic.interfaces;

public class DependencyException
extends Exception {
    private Class required;

    public DependencyException(Class r) {
        this.required = r;
    }

    public DependencyException(String msg, Class req) {
        super(msg);
        this.required = req;
    }

    public Class getRequired() {
        return this.required;
    }
}

