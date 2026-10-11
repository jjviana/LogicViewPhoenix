/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.UIAction;

import com.logicstyle.logicview.MainWindow.ViewPresenter;
import com.logicstyle.logicview.UIAction.ActionGroup;
import com.logicstyle.logicview.UIAction.UIAction;
import com.logicstyle.logicview.model.View;
import com.logicstyle.logicview.viewer.printing.ViewPrinter;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
import com.logicstyle.mosaic.interfaces.DependencyException;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;

public class SaveViewSnapshot
implements UIAction {
    private ResourceBundle res = ResourceBundle.getBundle(SaveViewSnapshot.class.getName());
    private ComponentRepository rep;
    private boolean active = false;

    public void deactivate() {
        this.active = false;
    }

    public void execute(Object context) {
        ViewPrinter vp = (ViewPrinter)this.rep.findComponentByInterface(ViewPrinter.class);
        vp.print((View)context);
    }

    public ActionGroup getActionGroup() {
        return ViewPresenter.VIEW_ACTION_GROUP;
    }

    public String getDescription() {
        return this.res.getString("description");
    }

    public ImageIcon getIcon() {
        return null;
    }

    public String getName() {
        return this.res.getString("menuItem");
    }

    public boolean init(ComponentRepository c) throws DependencyException {
        this.rep = c;
        return true;
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean isInContext(Object context) {
        return context instanceof View;
    }
}

