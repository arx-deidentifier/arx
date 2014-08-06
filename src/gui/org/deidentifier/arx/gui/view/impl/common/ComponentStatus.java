/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
 * (1) nothing to display, (2) working, (3) done (shows the control)
 * 
 * @author Fabian Prasser
 */
public class ComponentStatus {

    private final Controller controller;
    private final StackLayout layout;
    private final Composite working;
    private final Composite empty;
    private final Composite parent;
    private final Control child;
    
    /**
     * Creates a new instance
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
     * Creates a composite for the empty status
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
     * Creates a composite for the working status
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

    /**
     * Enables status 'working'
     */
    public void setWorking(){
        this.layout.topControl = working;
        this.parent.layout();
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
}
