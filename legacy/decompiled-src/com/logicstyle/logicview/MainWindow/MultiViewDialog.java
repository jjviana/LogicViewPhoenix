/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.MainWindow;

import com.logicstyle.logicview.UIAction.UIAction;
import com.logicstyle.logicview.model.Model;
import com.logicstyle.logicview.model.View;
import com.logicstyle.logicview.viewer.Viewer;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class MultiViewDialog
extends JDialog {
    private Viewer[] views;
    private Model model;
    private HashMap actionMap;
    private JMenuBar mb;

    public MultiViewDialog(Frame parent, Model mod, Viewer[] v, ComponentRepository repository) {
        super(parent, false);
        this.initComponents();
        this.views = v;
        this.model = mod;
        final View view = v[0].getView(mod);
        this.setTitle(v[0].getViewName());
        this.getContentPane().add(view.getViewComponent(), "Center");
        this.pack();
        List actions = repository.findAllByInterface(UIAction.class);
        if (actions.size() == 0) {
            return;
        }
        this.actionMap = new HashMap();
        HashMap<String, JMenu> groupmap = new HashMap<String, JMenu>();
        for (int i = 0; i < actions.size(); ++i) {
            UIAction uia = (UIAction)actions.get(i);
            if (!uia.isInContext(view)) continue;
            JMenu m = (JMenu)groupmap.get(uia.getActionGroup().getName());
            if (m == null) {
                m = new JMenu(uia.getActionGroup().getName());
                groupmap.put(uia.getActionGroup().getName(), m);
                this.mb.add(m);
            }
            JMenuItem item = m.add(uia.getName());
            item.addActionListener(new ActionListener(){

                public void actionPerformed(ActionEvent evt) {
                    UIAction action = (UIAction)MultiViewDialog.this.actionMap.get(evt.getSource());
                    action.execute(view);
                }
            });
            this.actionMap.put(item, uia);
        }
    }

    private void initComponents() {
        this.mb = new JMenuBar();
        this.addWindowListener(new WindowAdapter(){

            public void windowClosing(WindowEvent evt) {
                MultiViewDialog.this.closeDialog(evt);
            }
        });
        this.setJMenuBar(this.mb);
        this.pack();
    }

    private void closeDialog(WindowEvent evt) {
        this.setVisible(false);
        this.dispose();
    }
}

