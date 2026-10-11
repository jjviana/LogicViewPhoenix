/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.UIAction;

import javax.swing.ImageIcon;

public class ActionGroup {
    private String name;
    private ImageIcon icon;

    public ActionGroup(String name) {
        this.name = name;
        this.icon = null;
    }

    public ActionGroup(String name, ImageIcon icon) {
        this.icon = icon;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ImageIcon getIcon() {
        return this.icon;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }
}

