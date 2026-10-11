/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.voxell;

import com.logicstyle.logicview.model.Model;
import com.logicstyle.logicview.model.View;
import com.logicstyle.logicview.model.VoxellModel;
import com.logicstyle.logicview.viewer.ViewConstructionListener;
import com.logicstyle.logicview.viewer.Viewer;
import com.logicstyle.logicview.viewer.printing.PrintPageElementFactory;
import com.logicstyle.logicview.viewer.voxell.VoxellModelView;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
import com.logicstyle.mosaic.interfaces.DependencyException;
import java.util.Collection;
import java.util.Collections;
import java.util.ResourceBundle;
import javax.swing.event.EventListenerList;

public class VoxellModelViewer
implements Viewer,
PrintPageElementFactory {
    private static ResourceBundle res = ResourceBundle.getBundle(VoxellModelViewer.class.getName());
    private ComponentRepository rep;
    private boolean active = false;
    private EventListenerList listenerList = null;

    public void deactivate() {
        this.active = false;
    }

    public String getDescription() {
        return res.getString("description");
    }

    public String getName() {
        return res.getString("name");
    }

    public View getView(Model model) {
        return new VoxellModelView((VoxellModel)model, this);
    }

    public String getViewName() {
        return res.getString("viewName");
    }

    public boolean init(ComponentRepository c) throws DependencyException {
        this.rep = c;
        this.active = true;
        return true;
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean isModelSupported(Model m) {
        return m instanceof VoxellModel;
    }

    public synchronized void addViewConstructionListener(ViewConstructionListener listener) {
        if (this.listenerList == null) {
            this.listenerList = new EventListenerList();
        }
        this.listenerList.add(ViewConstructionListener.class, listener);
    }

    public synchronized void removeViewConstructionListener(ViewConstructionListener listener) {
        this.listenerList.remove(ViewConstructionListener.class, listener);
    }

    void fireViewConstructionListenerViewConstructionStarting(Viewer event) {
        if (this.listenerList == null) {
            return;
        }
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ViewConstructionListener.class) continue;
            ((ViewConstructionListener)listeners[i + 1]).viewConstructionStarting(event);
        }
    }

    void fireViewConstructionListenerViewConstructionFinished() {
        if (this.listenerList == null) {
            return;
        }
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ViewConstructionListener.class) continue;
            ((ViewConstructionListener)listeners[i + 1]).viewConstructionFinished(this);
        }
    }

    void fireViewConstructionListenerViewConstructionProgress(String title, float percent, boolean show) {
        if (this.listenerList == null) {
            return;
        }
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ViewConstructionListener.class) continue;
            ((ViewConstructionListener)listeners[i + 1]).viewConstructionProgress(title, percent, show);
        }
    }

    ComponentRepository getRepository() {
        return this.rep;
    }

    public Collection getPageElements(View v) {
        return Collections.singleton(v);
    }
}

