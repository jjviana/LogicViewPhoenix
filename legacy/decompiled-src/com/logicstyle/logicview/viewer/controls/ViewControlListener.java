/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.controls;

import com.logicstyle.logicview.viewer.controls.ViewControl;
import java.util.EventListener;

public interface ViewControlListener
extends EventListener {
    public void controlDeactivated(ViewControl var1);

    public void controlActivated(ViewControl var1);

    public void controlStateChanged(ViewControl var1);
}

