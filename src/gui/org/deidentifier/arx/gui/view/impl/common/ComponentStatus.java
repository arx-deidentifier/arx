/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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

import java.io.IOException;
import java.io.InputStream;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.risk.ViewRisks;
import org.deidentifier.arx.gui.view.impl.utility.ViewStatistics;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * This class implements a wrapper around a control that displays the current status:
 * (1) nothing to display, (2) working, (3) done (shows the control).
 *
 * @author Fabian Prasser
 */
public class ComponentStatus {

    /** View */
    private final Controller        controller;

    /** View */
    private final StackLayout       layout;

    /** View */
    private final Composite         working;

    /** View */
    private final Composite         empty;

    /** View */
    private final Composite         parent;

    /** View */
    private final Control           child;

    /** View */
    private final ViewStatistics<?> view1;

    /** View */
    private final ViewRisks<?>      view2;

    /**
     * Creates a new instance.
     *
     * @param controller
     * @param parent
     * @param child
     */
    public ComponentStatus(Controller controller,
                           Composite parent,
                           Control child,
                           ViewStatistics<?> view) {
        this(controller, parent, child, view, null);
    }

    /**
     * Creates a new instance.
     *
     * @param controller
     * @param parent
     * @param child
     * @param view
     * @param provider
     */
    public ComponentStatus(Controller controller, 
                           Composite parent, 
                           Control child,
                           ViewStatistics<?> view,
                           ComponentStatusLabelProgressProvider progressProvider){
        
        this.child = child;
        this.parent = parent;
        this.controller = controller;
        this.view1 = view;
        this.view2 = null;
        
        if (parent.getLayout() == null ||
            !(parent.getLayout() instanceof StackLayout)) {
            throw new RuntimeException("Parent must have a StackLayout"); //$NON-NLS-1$
        }
        
        this.layout = (StackLayout)parent.getLayout();
        this.working = getWorkingComposite(parent, progressProvider);
        this.empty = getEmptyComposite(parent);
        this.layout.topControl = child;
        this.parent.layout(true);
    }
    
    /**
     * Creates a new instace
     * @param controller
     * @param parent
     * @param control
     * @param viewRisks
     * @param progressProvider
     */
    public ComponentStatus(Controller controller,
                           Composite parent,
                           Control child,
                           ViewRisks<?> view,
                           ComponentStatusLabelProgressProvider progressProvider) {

        this.child = child;
        this.parent = parent;
        this.controller = controller;
        this.view1 = null;
        this.view2 = view;
        
        if (parent.getLayout() == null ||
            !(parent.getLayout() instanceof StackLayout)) {
            throw new RuntimeException("Parent must have a StackLayout"); //$NON-NLS-1$
        }
        
        this.layout = (StackLayout)parent.getLayout();
        this.working = getWorkingComposite(parent, progressProvider);
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
        composite.setLayout(SWTUtil.createGridLayout(1));
        ComponentStatusLabel label = new ComponentStatusLabel(composite, SWT.CENTER);
        label.setText(Resources.getMessage("ComponentStatus.1"));
        label.setLayoutData(GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.BOTTOM).grab(true, true).create());
        Button update = new Button(composite, SWT.PUSH);
        update.setText(Resources.getMessage("ComponentStatus.2")); //$NON-NLS-1$
        update.setLayoutData(GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).grab(false, true).create());
        update.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (view1 != null) {
                    view1.triggerUpdate();
                }
                if (view2 != null) {
                    view2.triggerUpdate();
                }
            }
        });
        return composite;
    }
    
    /**
     * Creates a composite for the working status.
     *
     * @param parent
     * @return
     */
    private Composite getWorkingComposite(Composite parent, ComponentStatusLabelProgressProvider provider) {
        
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(SWTUtil.createGridLayout(1));
        
        ComponentStatusLabel label = new ComponentStatusLabel(composite, SWT.CENTER);
        label.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BOTTOM).grab(true, true).create());
        InputStream stream = controller.getResources().getStream("working.gif"); //$NON-NLS-1$
        try {
            label.setGIF(stream);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // Ignore silently
                }
            }
        }
        label.setText(Resources.getMessage("ComponentStatus.3")); //$NON-NLS-1$
        if (provider != null) {
            label.setProgressProvider(provider);
        }

        Button stop = new Button(composite, SWT.PUSH);
        stop.setText(Resources.getMessage("ComponentStatus.4")); //$NON-NLS-1$
        stop.setLayoutData(GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).grab(false, true).create());
        stop.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (view1 != null) {
                    view1.triggerStop();
                }
                if (view2 != null) {
                    view2.triggerStop();
                }
            }
        });
        return composite;
    }
}
