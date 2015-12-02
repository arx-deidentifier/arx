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

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.linearbits.swt.widgets.Knob;
import de.linearbits.swt.widgets.KnobColorProfile;
import de.linearbits.swt.widgets.KnobRange;

/**
 * A component for configuring risk thresholds
 * 
 * @author Fabian Prasser
 */
public class ComponentRiskThresholds {

    /** View */
    private static final String    CAPTION  = Resources.getMessage("ComponentRiskThresholds.4"); //$NON-NLS-1$
    /** View */
    private static final String    LABEL1   = Resources.getMessage("ComponentRiskThresholds.0"); //$NON-NLS-1$
    /** View */
    private static final String    LABEL2   = Resources.getMessage("ComponentRiskThresholds.1"); //$NON-NLS-1$
    /** View */
    private static final String    LABEL3   = Resources.getMessage("ComponentRiskThresholds.2"); //$NON-NLS-1$
    /** View */
    private static final String    LABEL4   = Resources.getMessage("ComponentRiskThresholds.3"); //$NON-NLS-1$

    /** Constant */
    private static final int       MIN_KNOB = 30;
    /** View */
    private final Knob<Double>     bar1;
    /** View */
    private final Knob<Double>     bar2;
    /** View */
    private final Knob<Double>     bar3;
    /** View */
    private final Knob<Double>     bar4;
    /** View */
    private final Composite        root;

    /** Color profile */
    private final KnobColorProfile defaultColorProfile;
    /** Color profile */
    private final KnobColorProfile focusedColorProfile;
    
    /** Model*/
    private final List<SelectionListener> listeners = new ArrayList<SelectionListener>();

    /**
     * Creates a new instance
     * @param parent
     * @param shortText 
     */
    public ComponentRiskThresholds(final Composite parent) {

        // Color profiles
        this.defaultColorProfile = KnobColorProfile.createDefaultSystemProfile(parent.getDisplay());
        this.focusedColorProfile = KnobColorProfile.createFocusedBlueRedProfile(parent.getDisplay());
        
        this.root = new Composite(parent, SWT.NONE);
        this.root.setLayout(SWTUtil.createGridLayout(1));
        this.root.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent arg0) {
                if (defaultColorProfile != null && !defaultColorProfile.isDisposed()) {
                    defaultColorProfile.dispose();
                }
                if (focusedColorProfile != null && !focusedColorProfile.isDisposed()) {
                    focusedColorProfile.dispose();
                }
            }
        });
        
        // Label
        Label label = new Label(root, SWT.CENTER);
        label.setText(CAPTION);
        GridData labeldata = SWTUtil.createFillHorizontallyGridData();
        labeldata.horizontalAlignment = SWT.CENTER;
        label.setLayoutData(labeldata);
        SWTUtil.changeFont(label, SWT.BOLD);
        
        // Base
        Composite base = new Composite(root, SWT.NONE);
        base.setLayoutData(GridDataFactory.swtDefaults().grab(true, true).align(SWT.CENTER, SWT.CENTER).create());

        // Layout
        GridLayout layout = SWTUtil.createGridLayout(4);
        layout.marginHeight = 0;
        layout.marginTop = 0;
        layout.marginBottom = 0;
        layout.verticalSpacing = 0;
        base.setLayout(layout);
        
        createLabel(base, LABEL1);
        createLabel(base, LABEL2);
        
        bar1 = createKnob(base, LABEL1);
        createLabel(base, bar1);
        bar2 = createKnob(base, LABEL2);
        createLabel(base, bar2);

        createLabel(base, LABEL3);
        createLabel(base, LABEL4);

        bar3 = createKnob(base, LABEL3);
        createLabel(base, bar3);
        bar4 = createKnob(base, LABEL4);
        createLabel(base, bar4);
    }
    
    /**
     * Adds a selection listener
     * @param listener
     */
    public void addSelectionListener(SelectionListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Gets a threshold
     * @return
     */
    public double getThresholdHighestRisk() {
        return bar3.getValue();
    }

    /**
     * Gets a threshold
     * @return
     */
    public double getThresholdIndividualRecords() {
        return bar1.getValue();
    }

    /**
     * Gets a threshold
     * @return
     */
    public double getThresholdRecordsAtRisk() {
        return bar2.getValue();
    }
    
    /**
     * Gets a threshold
     * @return
     */
    public double getThresholdSuccessRate() {
        return bar4.getValue();
    }

    /**
     * Sets layout data
     * @param data
     */
    public void setLayoutData(Object data) {
        this.root.setLayoutData(data);
    }

    /**
     * Sets a threshold
     * @param arg0
     */
    public void setThresholdHighestRisk(double arg0) {
        bar3.setValue(arg0);
    }

    /**
     * Sets a threshold
     * @param arg0
     */
    public void setThresholdIndividualRecords(double arg0) {
        bar1.setValue(arg0);
    }

    /**
     * Sets a threshold
     * @param arg0
     */
    public void setThresholdRecordsAtRisk(double arg0) {
        bar2.setValue(arg0);
    }

    /**
     * Sets a threshold
     * @param arg0
     */
    public void setThresholdSuccessRate(double arg0) {
        bar4.setValue(arg0);
    }

    /**
     * Creates a knob
     * @param root
     * @param text
     * @return
     */
    private Knob<Double> createKnob(Composite root, String text) {
        Knob<Double> knob = new Knob<Double>(root, SWT.NULL, new KnobRange.Double(0d, 100d));
        knob.setLayoutData(GridDataFactory.swtDefaults().grab(true, true).align(SWT.CENTER, SWT.CENTER).hint(MIN_KNOB, MIN_KNOB).create());
        knob.setDefaultColorProfile(defaultColorProfile);
        knob.setFocusedColorProfile(focusedColorProfile);
        knob.setToolTipText(text);
        return knob;
    }

    /**
     * Creates a label for a knob
     * @param root
     * @param knob
     */
    private void createLabel(final Composite root, 
                             final Knob<Double> knob) {

        // Label
        String text = "100%"; //$NON-NLS-1$
        final CLabel label = new CLabel(root, SWT.LEFT);
        label.setText(text);
        GridData data = SWTUtil.createFillHorizontallyGridData();
        data.horizontalAlignment = SWT.LEFT;
        label.setLayoutData(data);
        label.setToolTipText(text);
        
        // Listen
        knob.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                String text = SWTUtil.getPrettyString(knob.getValue())+"%"; //$NON-NLS-1$
                label.setText(text);
                label.setToolTipText(text);
                
                // Cascade
                for (SelectionListener listener : listeners) {
                    listener.widgetSelected(arg0);
                }
            }
        });
    }

    /**
     * Creates a label
     * @param root
     * @param text
     */
    private void createLabel(Composite root, String text) {

        // Label
        CLabel label = new CLabel(root, SWT.CENTER);
        label.setText(text);
        label.setLayoutData(SWTUtil.createFillHorizontallyGridData(true, 2));
        label.setToolTipText(text);
    }
}
