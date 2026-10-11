/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.UIAction;

import com.logicstyle.logicview.UIAction.ActionGroup;
import com.logicstyle.mosaic.interfaces.Component;
import javax.swing.ImageIcon;

public interface UIAction
extends Component {
    public String getName();

    public ActionGroup getActionGroup();

    public ImageIcon getIcon();

    public boolean isInContext(Object var1);

    public void execute(Object var1);
}

