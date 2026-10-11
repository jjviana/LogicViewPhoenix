/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jdom.Document
 *  org.jdom.Element
 */
package com.logicstyle.mosaic.system;

import com.logicstyle.mosaic.interfaces.Component;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
import com.logicstyle.mosaic.interfaces.DependencyException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Document;
import org.jdom.Element;

public class ApplicationRepository
implements ComponentRepository {
    public static Logger log = Logger.getLogger("mosaic.system");
    private HashMap components = new HashMap();
    private Vector component_list;
    private static final LinkedList empty_list = new LinkedList();

    public ApplicationRepository(Document config) {
        Component c;
        int i;
        Element root = config.getRootElement();
        List components = root.getChildren("component");
        Iterator it = components.iterator();
        this.component_list = new Vector();
        while (it.hasNext()) {
            try {
                Element component = (Element)it.next();
                String clname = component.getChildTextTrim("class");
                Class<?> component_class = Class.forName(clname);
                Component component_instance = (Component)component_class.newInstance();
                Class<?>[] component_interfaces = component_class.getInterfaces();
                for (int i2 = 0; i2 < component_interfaces.length; ++i2) {
                    this.addComponentClass(component_interfaces[i2], component_instance);
                }
                this.component_list.addElement(component_instance);
            }
            catch (Exception e) {
                log.log(Level.WARNING, "Error loading component ", e);
            }
        }
        if (this.component_list.size() == 0) {
            log.log(Level.WARNING, "No component loaded, exiting...");
            System.exit(-1);
        }
        BitSet current_state = new BitSet(this.component_list.size());
        BitSet previous_state = new BitSet(this.component_list.size());
        current_state.set(0, current_state.size());
        previous_state.set(0, previous_state.size());
        while (true) {
            for (i = 0; i < this.component_list.size(); ++i) {
                c = (Component)this.component_list.elementAt(i);
                try {
                    current_state.set(i, !c.init(this));
                    continue;
                }
                catch (DependencyException e) {
                    log.log(Level.WARNING, "Initialization of component " + c.getName() + " failed due to failed dependency: " + e.getRequired().getName());
                    this.removeComponent(c);
                    current_state = this.shrinkState(current_state, i);
                    previous_state = this.shrinkState(previous_state, i);
                    --i;
                    continue;
                }
                catch (Throwable t) {
                    log.log(Level.SEVERE, "Initialization of component " + c.getName() + " failed due to exception", t);
                    this.removeComponent(c);
                    current_state = this.shrinkState(current_state, i);
                    previous_state = this.shrinkState(previous_state, i);
                    --i;
                }
            }
            if (current_state.nextSetBit(0) >= this.component_list.size() || current_state.equals(previous_state)) break;
            previous_state = (BitSet)current_state.clone();
        }
        if (previous_state.equals(current_state)) {
            log.log(Level.SEVERE, "Initialization deadlock detected with the following components");
            i = current_state.nextSetBit(0);
            while (i > 0 && i < this.component_list.size()) {
                c = (Component)this.component_list.elementAt(i);
                log.log(Level.SEVERE, c.getName());
                i = current_state.nextSetBit(i + 1);
            }
            log.log(Level.SEVERE, "Exiting..");
            System.exit(-1);
        }
        log.log(Level.INFO, "Components initialization sucessfull.");
    }

    private void addComponentClass(Class c, Object comp) {
        if (Component.class.isAssignableFrom(c)) {
            LinkedList<Object> ll = (LinkedList<Object>)this.components.get(c);
            if (ll == null) {
                ll = new LinkedList<Object>();
                ll.add(comp);
                this.components.put(c, ll);
            } else {
                ll.add(comp);
            }
            Class<?>[] interfaces = c.getInterfaces();
            for (int i = 0; i < interfaces.length; ++i) {
                this.addComponentClass(interfaces[i], comp);
            }
        }
    }

    private void removeComponent(Component c) {
        this.component_list.removeElement(c);
        Class[] interfaces = this.findComponentInterfaces(c);
        for (int i = 0; i < interfaces.length; ++i) {
            LinkedList ll = (LinkedList)this.components.get(interfaces[i]);
            if (ll == null) {
                log.log(Level.WARNING, "Component not found on removeComponent for interface " + interfaces[i].getName());
                continue;
            }
            if (ll.size() == 1) {
                this.components.remove(interfaces[i]);
                continue;
            }
            ll.remove(c);
        }
    }

    private Class[] findComponentInterfaces(Component c) {
        Vector inter = new Vector();
        Class<?>[] interfaces = c.getClass().getInterfaces();
        for (int i = 0; i < interfaces.length; ++i) {
            if (!Component.class.isAssignableFrom(interfaces[i])) continue;
            inter.addElement(interfaces[i]);
        }
        Object[] cl = new Class[inter.size()];
        inter.copyInto(cl);
        return cl;
    }

    private BitSet shrinkState(BitSet b, int i) {
        int j;
        if (b.size() == 1) {
            return b;
        }
        BitSet n = new BitSet(b.size() - 1);
        for (j = 0; j < i; ++j) {
            n.set(j, b.get(j));
        }
        for (j = i + 1; j < b.length(); ++j) {
            n.set(j - 1, b.get(j));
        }
        return n;
    }

    public List findAllByInterface(Class inter) {
        List l = (List)this.components.get(inter);
        if (l == null) {
            return empty_list;
        }
        return l;
    }

    public List findByInterface(Class inter) {
        List l = (List)this.components.get(inter);
        if (l == null) {
            return empty_list;
        }
        LinkedList<Component> ll = new LinkedList<Component>();
        for (Component c : l) {
            if (!c.isActive()) continue;
            ll.add(c);
        }
        return ll;
    }

    public Component findComponentByInterface(Class inter) {
        List l = (List)this.components.get(inter);
        if (l == null) {
            return null;
        }
        Component c = (Component)l.get(0);
        return c;
    }
}

