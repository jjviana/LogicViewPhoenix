/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.MainWindow;

import com.logicstyle.logicview.UIAction.ActionGroup;
import com.logicstyle.logicview.model.Model;
import com.logicstyle.logicview.viewer.Viewer;
import com.logicstyle.mosaic.interfaces.Component;
import java.util.ResourceBundle;
import javax.swing.JFrame;

public interface MainWindow
extends Component {
    public static final ResourceBundle res = ResourceBundle.getBundle(MainWindow.class.getName());
    public static final ActionGroup MODEL_ACTION_GROUP = new ActionGroup(res.getString("model_group_name"));

    public void addDocView(Model var1, Viewer[] var2);

    public JFrame getParentFrame();
}

