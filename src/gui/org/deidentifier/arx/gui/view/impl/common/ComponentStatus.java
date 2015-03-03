/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.deidentifier.arx.gui.view.impl.common;

import org.deidentifier.arx.gui.Controller;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * This class implements a wrapper around a control that displays the current status:
 * (1) nothing to display, (2) working, (3) done (shows the control).
 *
 * @author Fabian Prasser
 */
public class ComponentStatus {

    /**  TODO */
    private final Controller controller;
    
    /**  TODO */
    private final StackLayout layout;
    
    /**  TODO */
    private final Composite working;
    
    /**  TODO */
    private final Composite empty;
    
    /**  TODO */
    private final Composite parent;
    
    /**  TODO */
    private final Control child;
    
    /**
     * Creates a new instance.
     *
     * @param controller
     * @param parent
     * @param child
     */
    public ComponentStatus(Controller controller, 
                           Composite parent, Control child){
        
        
        this.child = child;
        this.parent = parent;
        this.controller = controller;
        
        if (parent.getLayout() == null ||
            !(parent.getLayout() instanceof StackLayout)) {
            throw new RuntimeException("Parent must have a StackLayout");
        }
        
        this.layout = (StackLayout)parent.getLayout();
        
        this.working = getWorkingComposite(parent);
        this.empty = getEmptyComposite(parent);
        
        this.layout.topControl = child;
        this.parent.layout(true);
    }
    
    /**
     * Is the current status visible.
     *
     * @return
     */
    public boolean isVisible(){
        return this.parent.isVisible();
    }

    /**
     * Enables status 'done'. Shows the actual control.
     */
    public void setDone(){
        this.layout.topControl = child;
        this.parent.layout();
    }

    /**
     * Enables status 'empty'.
     */
    public void setEmpty(){
        this.layout.topControl = empty;
        this.parent.layout();
    }
    
    /**
     * Enables status 'working'.
     */
    public void setWorking(){
        this.layout.topControl = working;
        this.parent.layout();
    }
    
    /**
     * Creates a composite for the empty status.
     *
     * @param parent
     * @return
     */
    private Composite getEmptyComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new FillLayout());
        ComponentGIFLabel label = new ComponentGIFLabel(composite, SWT.CENTER);
        label.setText("No data available.");
        return composite;
    }
    
    /**
     * Creates a composite for the working status.
     *
     * @param parent
     * @return
     */
    private Composite getWorkingComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new FillLayout());
        ComponentGIFLabel label = new ComponentGIFLabel(composite, SWT.CENTER);
        label.setGIF(controller.getResources().getStream("working.gif"));
        label.setText("Analyzing...");
        return composite;
    }
}
