/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.MainWindow;

import com.logicstyle.logicview.MainWindow.MainWindow;
import com.logicstyle.logicview.MainWindow.MultiViewDialog;
import com.logicstyle.logicview.MainWindow.ViewPresentationListener;
import com.logicstyle.logicview.MainWindow.ViewPresenter;
import com.logicstyle.logicview.model.Model;
import com.logicstyle.logicview.viewer.Viewer;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
import com.logicstyle.mosaic.interfaces.DependencyException;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.WeakHashMap;
import javax.swing.event.EventListenerList;

public class MultiViewPresenter
implements ViewPresenter {
    private boolean active = false;
    private ComponentRepository rep;
    private EventListenerList listenerList = null;
    private WeakHashMap view_map = new WeakHashMap();

    public void addViews(final Model m, final Viewer[] views, MainWindow w) {
        MultiViewDialog d = new MultiViewDialog((Frame)w.getParentFrame(), m, views, this.rep);
        d.addWindowListener(new WindowAdapter(){

            public void windowClosing(WindowEvent evt) {
                MultiViewPresenter.this.fireViewPresentationListenerViewPresentationFinished(m, views);
            }
        });
        d.setSize(640, 480);
        d.show();
        this.fireViewPresentationListenerViewPresentationStarted(m, views);
    }

    public void deactivate() {
        this.active = false;
    }

    public String getDescription() {
        return "Multiple dialogs view presenter";
    }

    public String getName() {
        return "MultiViewPresenter";
    }

    public boolean init(ComponentRepository c) throws DependencyException {
        this.rep = c;
        return true;
    }

    public boolean isActive() {
        return this.active;
    }

    public synchronized void addViewPresentationListener(ViewPresentationListener listener) {
        if (this.listenerList == null) {
            this.listenerList = new EventListenerList();
        }
        this.listenerList.add(ViewPresentationListener.class, listener);
    }

    public synchronized void removeViewPresentationListener(ViewPresentationListener listener) {
        this.listenerList.remove(ViewPresentationListener.class, listener);
    }

    private void fireViewPresentationListenerViewPresentationStarted(Model m, Viewer[] views) {
        if (this.listenerList == null) {
            return;
        }
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ViewPresentationListener.class) continue;
            ((ViewPresentationListener)listeners[i + 1]).viewPresentationStarted(m, views);
        }
    }

    private void fireViewPresentationListenerViewPresentationFinished(Model m, Viewer[] viewers) {
        if (this.listenerList == null) {
            return;
        }
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ViewPresentationListener.class) continue;
            ((ViewPresentationListener)listeners[i + 1]).viewPresentationFinished(m, viewers);
        }
    }
}

