/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.animation;

import com.logicstyle.logicview.MainWindow.ViewPresenter;
import com.logicstyle.logicview.UIAction.ActionGroup;
import com.logicstyle.logicview.UIAction.UIAction;
import com.logicstyle.logicview.model.View;
import com.logicstyle.logicview.viewer.animation.AnimationControlPanel;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
import com.logicstyle.mosaic.interfaces.DependencyException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;

public class AnimateUIAction
implements UIAction {
    private static final ResourceBundle res = ResourceBundle.getBundle(AnimateUIAction.class.getName());
    private boolean active = false;
    private ComponentRepository repository;
    private boolean dialogOpen = false;
    private AnimationControlPanel controlPanel;

    public void deactivate() {
        this.active = false;
    }

    public void execute(Object context) {
        if (!this.dialogOpen) {
            this.controlPanel = new AnimationControlPanel(this.repository, (View)context);
            this.controlPanel.addWindowListener(new CloseWindowAdapter());
            this.controlPanel.show();
            this.dialogOpen = true;
        }
    }

    public ActionGroup getActionGroup() {
        return ViewPresenter.VIEW_ACTION_GROUP;
    }

    public String getDescription() {
        return res.getString("menuLabel");
    }

    public ImageIcon getIcon() {
        return null;
    }

    public String getName() {
        return res.getString("name");
    }

    public boolean init(ComponentRepository c) throws DependencyException {
        this.repository = c;
        return true;
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean isInContext(Object context) {
        return context instanceof View;
    }

    class CloseWindowAdapter
    extends WindowAdapter {
        CloseWindowAdapter() {
        }

        public void windowClosed(WindowEvent windowEvent) {
            super.windowClosed(windowEvent);
            AnimateUIAction.this.dialogOpen = false;
        }
    }
}

