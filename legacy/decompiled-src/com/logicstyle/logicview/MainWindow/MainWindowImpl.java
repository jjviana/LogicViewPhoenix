/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.MainWindow;

import com.logicstyle.logicview.MainWindow.MainWindow;
import com.logicstyle.logicview.MainWindow.ViewPresenter;
import com.logicstyle.logicview.UIAction.UIAction;
import com.logicstyle.logicview.model.Model;
import com.logicstyle.logicview.viewer.Viewer;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
import com.logicstyle.mosaic.interfaces.DependencyException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class MainWindowImpl
extends JFrame
implements MainWindow {
    private ComponentRepository repository;
    private static ResourceBundle res = ResourceBundle.getBundle(MainWindowImpl.class.getName());
    private HashMap actionMap = new HashMap();
    private JMenuBar mb;

    public MainWindowImpl() {
        this.initComponents();
    }

    private void initComponents() {
        this.mb = new JMenuBar();
        this.addWindowListener(new WindowAdapter(){

            public void windowClosing(WindowEvent evt) {
                MainWindowImpl.this.exitForm(evt);
            }
        });
        this.setJMenuBar(this.mb);
        this.pack();
    }

    private void exitForm(WindowEvent evt) {
        System.exit(0);
    }

    public void deactivate() {
        this.hide();
    }

    public String getName() {
        return res.getString("name");
    }

    public String getDescription() {
        return res.getString("MainWindow.component_description");
    }

    public boolean init(ComponentRepository c) throws DependencyException {
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        this.repository = c;
        List actions = this.repository.findAllByInterface(UIAction.class);
        if (actions.size() == 0) {
            throw new DependencyException(UIAction.class);
        }
        HashMap<String, JMenu> groupmap = new HashMap<String, JMenu>();
        for (int i = 0; i < actions.size(); ++i) {
            UIAction uia = (UIAction)actions.get(i);
            if (!uia.isInContext(this)) continue;
            JMenu m = (JMenu)groupmap.get(uia.getActionGroup().getName());
            if (m == null) {
                m = new JMenu(uia.getActionGroup().getName());
                groupmap.put(uia.getActionGroup().getName(), m);
                this.mb.add(m);
            }
            JMenuItem item = m.add(uia.getName());
            item.addActionListener(new ActionListener(){

                public void actionPerformed(ActionEvent evt) {
                    UIAction action = (UIAction)MainWindowImpl.this.actionMap.get(evt.getSource());
                    action.execute(MainWindowImpl.this);
                }
            });
            this.actionMap.put(item, uia);
        }
        this.setTitle(res.getString("title"));
        this.setSize(640, 480);
        this.show();
        return true;
    }

    public void addDocView(Model model, Viewer[] views) {
        ViewPresenter vp = (ViewPresenter)this.repository.findComponentByInterface(ViewPresenter.class);
        if (vp == null) {
            throw new RuntimeException("No view presenter found...");
        }
        vp.addViews(model, views, this);
    }

    public JFrame getParentFrame() {
        return this;
    }
}

