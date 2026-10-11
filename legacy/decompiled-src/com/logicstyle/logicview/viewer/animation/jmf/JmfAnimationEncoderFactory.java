/*
 * Decompiled with CFR 0.152.
 */
package com.logicstyle.logicview.viewer.animation.jmf;

import com.logicstyle.logicview.viewer.animation.AnimationEncoder;
import com.logicstyle.logicview.viewer.animation.AnimationEncoderFactory;
import com.logicstyle.mosaic.interfaces.ComponentRepository;
import com.logicstyle.mosaic.interfaces.DependencyException;
import java.util.ResourceBundle;

public class JmfAnimationEncoderFactory
implements AnimationEncoderFactory {
    private boolean active = false;
    private static final ResourceBundle res = ResourceBundle.getBundle(JmfAnimationEncoderFactory.class.getName());
    private Class jmfEncoderClass;

    public void deactivate() {
        this.active = false;
    }

    public AnimationEncoder getAnimationEncoder() {
        try {
            return (AnimationEncoder)this.jmfEncoderClass.newInstance();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getDescription() {
        return res.getString("name");
    }

    public String getName() {
        return res.getString("description");
    }

    public boolean init(ComponentRepository c) throws DependencyException {
        try {
            this.jmfEncoderClass = Class.forName(this.getClass().getPackage().getName() + ".JmfAnimationEncoder");
        }
        catch (ClassNotFoundException e) {
            System.err.println("Unable to load Jmf Encoder class - is Java Media Framework installed? Component not enabled");
            return false;
        }
        this.active = true;
        return true;
    }

    public boolean isActive() {
        return this.active;
    }
}

