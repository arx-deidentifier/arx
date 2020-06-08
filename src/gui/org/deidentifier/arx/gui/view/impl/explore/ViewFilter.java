/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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

package org.deidentifier.arx.gui.view.impl.explore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.model.ModelNodeFilter;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentFilterTable;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolderButtonBar;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import de.linearbits.swt.rangeslider.RangeSlider;

/**
 * This class displays a filter for the lattice.
 *
 * @author Fabian Prasser
 */
public class ViewFilter implements IView {

    /** Scale size. */
    private static final int     SCALE_MAX_VALUE       = 1000;

    /** Scale update interval. */
    private static final int     SCALE_UPDATE_INTERVAL = 100;

    /** Widget. */
    private Composite            root;

    /** Widget. */
    private ComponentFilterTable generalization;

    /** Widget. */
    private Button               anonymous;

    /** Widget. */
    private Button               nonanonymous;

    /** Widget. */
    private Button               unknown;

    /** Widget */
    private RangeSlider          slider;

    /** Model. */
    private Controller           controller;

    /** Model. */
    private ModelNodeFilter      filter                = null;

    /** Model. */
    private Model                model                 = null;

    /** Model. */
    private ARXResult            result                = null;

    /** Model. */
    private boolean              mouseDown             = false;

    /** Fire the event */
    private volatile Boolean     fireEvent             = false;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     */
    public ViewFilter(final Composite parent, final Controller controller) {

        // Listen
        this.controller = controller;
        this.controller.addListener(ModelPart.RESULT, this);
        this.controller.addListener(ModelPart.INPUT, this);
        this.controller.addListener(ModelPart.MODEL, this);
        this.controller.addListener(ModelPart.FILTER, this);

        // Images
        Image imageReset = controller.getResources().getManagedImage("arrow_refresh.png"); //$NON-NLS-1$
        Image imageOptimum = controller.getResources().getManagedImage("bullet_yellow.png"); //$NON-NLS-1$

        // Bar
        ComponentTitledFolderButtonBar bar = new ComponentTitledFolderButtonBar("id-21"); //$NON-NLS-1$
        bar.add(Resources.getMessage("ViewFilter.0"), imageOptimum, new Runnable(){ //$NON-NLS-1$
            public void run() {
                actionShowOptimum();
            }
        });
        bar.add(Resources.getMessage("ViewFilter.1"), imageReset, new Runnable(){ //$NON-NLS-1$
            public void run() {
                actionReset();
            }
        });

        // Border
        ComponentTitledFolder border = new ComponentTitledFolder(parent, controller,  bar, null); //$NON-NLS-1$
        border.setLayoutData(SWTUtil.createFillGridData());
        
        // Create root
        root = border.createItem(Resources.getMessage("NodeFilterView.3"), //$NON-NLS-1$
                                 null);
        
        GridLayout groupLayout = new GridLayout();
        groupLayout.numColumns = 2;
        root.setLayout(groupLayout);

        create(root);
        
        border.setSelection(0);
        border.setEnabled(true);
        reset();
        
        parent.getDisplay().timerExec(SCALE_UPDATE_INTERVAL, new Runnable(){
            public void run() {
                synchronized(fireEvent) {
                    if (fireEvent) {
                        fireEvent = false;
                        actionInfoLossChanged();
                    }
                }
                if (!parent.isDisposed() && !parent.getDisplay().isDisposed()) {
                    parent.getDisplay().timerExec(SCALE_UPDATE_INTERVAL, this);
                }
            }
        });
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }
    
    @Override
    public void reset() {
        filter = null;
        result = null;
        anonymous.setSelection(false);
        nonanonymous.setSelection(false);
        unknown.setSelection(false);
        slider.setSelection(slider.getMinimum(), slider.getMaximum());
        slider.setEnabled(false);
        generalization.clear();
        SWTUtil.disable(root);
    }

    @Override
    public void update(final ModelEvent event) {
        if (event.part == ModelPart.INPUT) {
            reset();
        } else if (event.part == ModelPart.RESULT) {
            ARXResult result = (ARXResult)event.data;
            if (result == null || result.getLattice() == null){
                reset();
                SWTUtil.disable(root);
            } else {
                initialize(result, model.getNodeFilter());
                SWTUtil.enable(root);
            }
        } else if (event.part == ModelPart.MODEL) {
            model = (Model) event.data;
            reset();
        } else if (event.part == ModelPart.FILTER) {
            // Only update if we receive a new filter
            if ((filter == null) || (model.getNodeFilter() != filter)) {
                if (model.getResult() == null || model.getResult().getLattice() == null){
                    reset();
                    SWTUtil.disable(root);
                } else {
                    initialize(model.getResult(), model.getNodeFilter());
                    SWTUtil.enable(root);
                }
            }
        }
    }

    /**
     * Action.
     */
    private void actionAnonymousChanged() {
        if (filter != null) {
            if (!anonymous.getSelection()) filter.disallowAnonymous();
            else filter.allowAnonymous();
            fireModelEvent();
        }
    }

    /**
     * Action.
     */
    private void actionInfoLossChanged() {
        if (filter != null) {
            double maxLoss = (double)slider.getUpperValue() / (double)SCALE_MAX_VALUE;
            double minLoss = (double)slider.getLowerValue() / (double)SCALE_MAX_VALUE;
            filter.allowInformationLoss(minLoss, maxLoss);
            fireModelEvent();
        }
    }

    /**
     * Action.
     */
    private void actionNonAnonymousChanged() {
        if (filter != null) {
            if (!nonanonymous.getSelection()) filter.disallowNonAnonymous();
            else filter.allowNonAnonymous();
            fireModelEvent();
        }
    }

    /**
     * Action.
     */
    private void actionReset() {
        if (filter != null) {

            // Create ordered list of qis
            DataDefinition definition = model.getOutputDefinition();
            if (definition == null) {
                reset();
                return;
            }
            filter.reset(model.getOutput(), definition);
            update();
            fireModelEvent();
        }
    }

    /**
     * Action.
     */
    private void actionShowOptimum() {
        if (filter != null) {
            filter.initialize(model.getResult(), false);
            update();
            fireModelEvent();
        }
    }
    
    /**
     * Action.
     */
    private void actionUnknownChanged() {
        if (filter != null) {
            if (!unknown.getSelection()) filter.disallowUnknown();
            else filter.allowUnknown();
            fireModelEvent();
        }
    }
    
    /**
     * Fires the according events
     */
    private void actionUpdateSlider(){
        synchronized(fireEvent){
            fireEvent = true;
        }
    }
    
    /**
     * Creates the view.
     *
     * @param parent
     */
    private void create(final Composite parent) {

        // Add table
        generalization = new ComponentFilterTable(parent, controller);
        GridData data = SWTUtil.createFillGridData();
        data.heightHint = 70;
        data.horizontalSpan = 2;
        generalization.setLayoutData(data);
        generalization.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                    if (filter != null && 
                        generalization.getSelectedEntry() != null &&
                        generalization.getSelectedProperty() != null) {
                        
                        String entry = generalization.getSelectedEntry();
                        String property = generalization.getSelectedProperty();
                        int dimension = generalization.getIndexOfEntry(entry);
                        int level = Integer.valueOf(property);
                        boolean enabled = generalization.isSelected(entry, property);
                        
                        if (enabled) {
                            filter.allowGeneralization(dimension, level);
                        } else {
                            filter.disallowGeneralization(dimension, level);
                        }
                    }
                    fireModelEvent();
                }
            });

        Composite composite = new Composite(parent, SWT.NONE);
        GridData gdata = SWTUtil.createFillHorizontallyGridData();
        gdata.horizontalSpan = 2;
        composite.setLayoutData(gdata);
        composite.setLayout(GridLayoutFactory.swtDefaults().numColumns(6)
                                             .spacing(0, 0).margins(0, 0).create());
        
        anonymous = new Button(composite, SWT.CHECK | SWT.NO_FOCUS);
        anonymous.setLayoutData(GridDataFactory.swtDefaults().grab(false, false).create());
        anonymous.setToolTipText(Resources.getMessage("ViewFilter.2")); //$NON-NLS-1$
        anonymous.setText(""); //$NON-NLS-1$
        anonymous.setSelection(false);
        anonymous.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
               actionAnonymousChanged();
            }
        });
        
        final Label anonymousLabel = new Label(composite, SWT.NONE);
        anonymousLabel.setText(Resources.getMessage("ViewFilter.4")); //$NON-NLS-1$
        anonymousLabel.setLayoutData(GridDataFactory.swtDefaults().grab(true, false).create());
        anonymousLabel.addMouseListener(new MouseAdapter(){
            public void mouseDown(MouseEvent arg0) {
                anonymous.setSelection(!anonymous.getSelection());
                actionAnonymousChanged();
            }
        });
        
        
        nonanonymous = new Button(composite, SWT.CHECK | SWT.NO_FOCUS);
        nonanonymous.setToolTipText(Resources.getMessage("ViewFilter.5")); //$NON-NLS-1$
        nonanonymous.setText(""); //$NON-NLS-1$
        nonanonymous.setSelection(false);
        nonanonymous.setLayoutData(GridDataFactory.swtDefaults().grab(false, false).create());
        nonanonymous.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                actionNonAnonymousChanged();
            }
        });
        
        final Label nonanonymousLabel = new Label(composite, SWT.NONE);
        nonanonymousLabel.setText(Resources.getMessage("ViewFilter.7")); //$NON-NLS-1$
        nonanonymousLabel.setLayoutData(GridDataFactory.swtDefaults().grab(true, false).create());
        nonanonymousLabel.addMouseListener(new MouseAdapter(){
            public void mouseDown(MouseEvent arg0) {
                nonanonymous.setSelection(!nonanonymous.getSelection());
                actionNonAnonymousChanged();
            }
        });
        
        unknown = new Button(composite, SWT.CHECK | SWT.NO_FOCUS);
        unknown.setToolTipText(Resources.getMessage("ViewFilter.8")); //$NON-NLS-1$
        unknown.setText(""); //$NON-NLS-1$
        unknown.setLayoutData(GridDataFactory.swtDefaults().grab(false, false).create());
        unknown.setSelection(false);
        unknown.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                actionUnknownChanged();
            }
        });
        
        final Label unknownLabel = new Label(composite, SWT.NONE);
        unknownLabel.setText(Resources.getMessage("ViewFilter.10")); //$NON-NLS-1$
        unknownLabel.setLayoutData(GridDataFactory.swtDefaults().grab(true, false).create());
        unknownLabel.addMouseListener(new MouseAdapter(){
            public void mouseDown(MouseEvent arg0) {
                unknown.setSelection(!unknown.getSelection());
                actionUnknownChanged();
            }
        });
        
        final Label tableItem5 = new Label(parent, SWT.NONE);
        tableItem5.setText(Resources.getMessage("NodeFilterView.22")); //$NON-NLS-1$
        
        this.slider = new RangeSlider(parent, SWT.HORIZONTAL);
        this.slider.setMaximum(SCALE_MAX_VALUE);
        this.slider.setMinimum(0);
        this.slider.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        this.slider.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent arg0) {
                actionUpdateSlider();
            }
        });
        this.slider.addListener(SWT.MouseMove, new Listener() {
            public void handleEvent(Event e) {
                if (mouseDown == true) {
                    actionUpdateSlider();
                }
            }
        });

        this.slider.addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event e) {
                mouseDown = true;
            }
        });

        this.slider.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event e) {
                mouseDown = false;
            }
        });
        this.slider.addListener(SWT.KeyDown, new Listener() {
            public void handleEvent(Event e) {
                actionUpdateSlider();
            }
        });
        this.slider.addListener(SWT.KeyUp, new Listener() {
            public void handleEvent(Event e) {
                actionUpdateSlider();
            }
        });
    }

    /**
     * Fires a model event when the filter changes.
     */
    private void fireModelEvent(){
        controller.update(new ModelEvent(this,
                                         ModelPart.FILTER,
                                         filter));
    }

    /**
     * Initializes the view.
     *
     * @param result
     * @param nodeFilter
     */
    private void initialize(final ARXResult result,
                            final ModelNodeFilter nodeFilter) {

        // Reset
        reset();
        
        // Return if there is no lattice
        if (result==null || result.getLattice() == null) {
            return;
        }
        
        // Store data definition
        this.result = result;

        // Reset filter
        if (nodeFilter == null) {
            throw new IllegalStateException(Resources.getMessage("ViewFilter.11")); //$NON-NLS-1$
        }
        
        filter = nodeFilter;
        
        // Update
        this.update();
    }

    /**
     * Initializes the everything.
     */
    private void update() {
        
        // Check
        if (result == null) {
            reset();
            return;
        }

        // Disable drawing
        this.root.setRedraw(false);
        
        // Clear
        this.generalization.clear();

        // Create ordered list of qis
        String[] attributes = result.getLattice().getBottom().getQuasiIdentifyingAttributes();
        
        // Set generalization levels
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (String attribute : attributes) {
            min = Math.min(min, result.getLattice().getBottom().getGeneralization(attribute));
            max = Math.max(max, result.getLattice().getTop().getGeneralization(attribute));
        }
        List<String> properties = new ArrayList<String>();
        for (int i=min; i<=max; i++){
            properties.add(String.valueOf(i));
        }
        generalization.setProperties(properties);

        // Set levels for attributes
        List<String> entries = new ArrayList<>();
        Map<String, Set<String>> entryProperties = new HashMap<>();
        for (String attribute : attributes) {
            Set<String> attributeProperties = new HashSet<>();
            int attributeMin = result.getLattice().getBottom().getGeneralization(attribute);
            int attributeMax = result.getLattice().getTop().getGeneralization(attribute);
            for (int i=attributeMin; i<=attributeMax; i++){
                attributeProperties.add(String.valueOf(i));
            }
            entries.add(attribute);
            entryProperties.put(attribute, attributeProperties);
        }
        generalization.setPermitted(entries, entryProperties);

        int i=0;
        for (String attribute : attributes) {;
            Set<Integer> selected = filter.getAllowedGeneralizations(i); 
            Set<Integer> unselected = new HashSet<Integer>();
            for (String property : properties){
                unselected.add(Integer.valueOf(property));
            }
            unselected.removeAll(selected);
            for (int level : selected) {
                generalization.setSelected(attribute, String.valueOf(level), true);
            }
            for (int level : unselected) {
                generalization.setSelected(attribute, String.valueOf(level), false);
            }
            i++;
        }
        
        // Initialize classification
        anonymous.setSelection(filter.isAllowedAnonymous());
        nonanonymous.setSelection(filter.isAllowedNonAnonymous());
        unknown.setSelection(filter.isAllowedUnknown());
        
        // Min and max
        this.slider.setSelection((int)Math.round(filter.getAllowedMinInformationLoss() * (double)SCALE_MAX_VALUE), 
                                 (int)Math.round(filter.getAllowedMaxInformationLoss() * (double)SCALE_MAX_VALUE));
        
        // Draw
        this.root.setRedraw(true);
        this.root.redraw();

        // Enable
        SWTUtil.enable(root);
    }
}

