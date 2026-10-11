/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.loader.field;

import com.logicstyle.logicview.loader.ModelLoadListener;
import com.logicstyle.logicview.loader.ModelLoader;
import com.logicstyle.logicview.loader.field.FieldModelImp;
import com.logicstyle.logicview.model.Model;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
import com.logicstyle.mosaic.interfaces.DependencyException;
import java.net.URL;
import java.util.ResourceBundle;
import javax.swing.event.EventListenerList;

public class FieldModelLoader
implements ModelLoader {
    private ResourceBundle res = ResourceBundle.getBundle(FieldModelLoader.class.getName());
    private boolean active = false;
    protected ComponentRepository rep;
    private EventListenerList listenerList = null;

    public Model LoadModel(URL location) {
        return new FieldModelImp(location, this.rep);
    }

    public void deactivate() {
        this.active = false;
    }

    public String getDescription() {
        return this.res.getString("description");
    }

    public String getFilenameExtension() {
        return this.res.getString("fileExtension");
    }

    public String getName() {
        return this.res.getString("name");
    }

    public String getTypeName() {
        return this.res.getString("typeModel");
    }

    public boolean init(ComponentRepository c) throws DependencyException {
        this.active = true;
        this.rep = c;
        return true;
    }

    public boolean isActive() {
        return this.active;
    }

    public synchronized void addModelLoadListener(ModelLoadListener listener) {
        if (this.listenerList == null) {
            this.listenerList = new EventListenerList();
        }
        this.listenerList.add(ModelLoadListener.class, listener);
    }

    public synchronized void removeModelLoadListener(ModelLoadListener listener) {
        this.listenerList.remove(ModelLoadListener.class, listener);
    }

    private void fireModelLoadListenerLoadStarting(ModelLoader event) {
        if (this.listenerList == null) {
            return;
        }
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ModelLoadListener.class) continue;
            ((ModelLoadListener)listeners[i + 1]).loadStarting(event);
        }
    }

    private void fireModelLoadListenerLoadFinished(ModelLoader event) {
        if (this.listenerList == null) {
            return;
        }
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ModelLoadListener.class) continue;
            ((ModelLoadListener)listeners[i + 1]).loadFinished(event);
        }
    }

    private void fireModelLoadListenerLoadingPercentComplete(ModelLoader event) {
        if (this.listenerList == null) {
            return;
        }
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ModelLoadListener.class) continue;
            ((ModelLoadListener)listeners[i + 1]).loadingPercentComplete(event, 0.0f);
        }
    }
}

