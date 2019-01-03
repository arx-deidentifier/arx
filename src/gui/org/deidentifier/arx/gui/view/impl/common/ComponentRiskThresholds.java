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
package org.deidentifier.arx.gui.view.impl.common;

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

    /** Constant */
    private static final String    CAPTION  = Resources.getMessage("ComponentRiskThresholds.4"); //$NON-NLS-1$
    /** Constant */
    private static final String    LABEL1   = Resources.getMessage("ComponentRiskThresholds.2"); //$NON-NLS-1$
    /** Constant */
    private static final String    LABEL2   = Resources.getMessage("ComponentRiskThresholds.1"); //$NON-NLS-1$
    /** Constant */
    private static final String    LABEL3   = Resources.getMessage("ComponentRiskThresholds.3"); //$NON-NLS-1$
    /** Constant */
    private static final int       MIN_KNOB = 30;

    /** View */
    private final Knob<Double>     knob1;
    /** View */
    private final CLabel           label1;
    /** View */
    private final Knob<Double>     knob2;
    /** View */
    private final CLabel           label2;
    /** View */
    private final Knob<Double>     knob3;
    /** View */
    private final CLabel           label3;
    /** View */
    private final Composite        root;

    /** Color profile */
    private final KnobColorProfile defaultColorProfile;
    /** Color profile */
    private final KnobColorProfile focusedColorProfile;

    /**
     * Creates a new instance
     * @param parent
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
        GridLayout layout = SWTUtil.createGridLayout(6, true);
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        base.setLayout(layout);
        
        createSeparator(base, "Main", 2);
        createSeparator(base, "Derived", 4);
        
        createLabel(base, LABEL1);
        createLabel(base, LABEL2);
        createLabel(base, LABEL3);
        
        knob1 = createKnob(base);
        label1 = createLabel(base, knob1);
        knob2 = createKnob(base);
        label2 = createLabel(base, knob2);
        knob3 = createKnob(base);
        label3 = createLabel(base, knob3);
    }
    
    /**
     * Adds a selection listener
     * @param listener
     */
    public void addSelectionListenerThresholdHighestRisk(SelectionListener listener) {
        this.knob1.addSelectionListener(listener);
    }

    /**
     * Adds a selection listener
     * @param listener
     */
    public void addSelectionListenerThresholdRecordsAtRisk(SelectionListener listener) {
        this.knob2.addSelectionListener(listener);
    }

    /**
     * Adds a selection listener
     * @param listener
     */
    public void addSelectionListenerThresholdSuccessRate(SelectionListener listener) {
        this.knob3.addSelectionListener(listener);
    }

    /**
     * Gets a threshold
     * @return
     */
    public double getThresholdHighestRisk() {
        return knob1.getValue() / 100d;
    }

    /**
     * Gets a threshold
     * @return
     */
    public double getThresholdRecordsAtRisk() {
        return knob2.getValue() / 100d;
    }

    /**
     * Gets a threshold
     * @return
     */
    public double getThresholdSuccessRate() {
        return knob3.getValue() / 100d;
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
        knob1.setValue(arg0 * 100d, false);
        updateLabel(knob1, label1);
    }

    /**
     * Sets a threshold
     * @param arg0
     */
    public void setThresholdRecordsAtRisk(double arg0) {
        knob2.setValue(arg0 * 100d, false);
        updateLabel(knob2, label2);
    }

    /**
     * Sets a threshold
     * @param arg0
     */
    public void setThresholdSuccessRate(double arg0) {
        knob3.setValue(arg0 * 100d, false);
        updateLabel(knob3, label3);
    }

    /**
     * Creates a knob
     * @param root
     * @param text
     * @return
     */
    private Knob<Double> createKnob(Composite root) {
        Knob<Double> knob = new Knob<Double>(root, SWT.NULL, new KnobRange.Double(0d, 100d));
        knob.setLayoutData(GridDataFactory.swtDefaults().grab(true, true).align(SWT.CENTER, SWT.CENTER).hint(MIN_KNOB, MIN_KNOB).create());
        knob.setDefaultColorProfile(defaultColorProfile);
        knob.setFocusedColorProfile(focusedColorProfile);
        return knob;
    }

    /**
     * Creates a label for a knob
     * @param root
     * @param knob
     */
    private CLabel createLabel(final Composite root, 
                               final Knob<Double> knob) {

        // Label
        String text = "100%"; //$NON-NLS-1$
        final CLabel label = new CLabel(root, SWT.NONE);
        label.setText(text);
        label.setAlignment(SWT.LEFT);
        label.setLayoutData(SWTUtil.createFillGridData());
        label.setToolTipText(text);
        
        // Listen
        knob.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                updateLabel(knob, label);
            }
        });
        
        // Return
        return label;
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

    /**
     * Creates a separator
     * @param root
     * @param text
     * @param span
     */
    private void createSeparator(Composite root, String text, int span) {
        ComponentTitledSeparator separator = new ComponentTitledSeparator(root, SWT.NONE);
        GridData data = SWTUtil.createFillHorizontallyGridData(true,  span);
        data.horizontalIndent = 0;
        data.verticalIndent = 0;
        separator.setLayoutData(data);
        separator.setText(text);
    }

    /**
     * Updates the value on the label
     * @param knob
     * @param label
     */
    private void updateLabel(Knob<Double> knob, CLabel label) {
        String text = SWTUtil.getPrettyString(knob.getValue())+"%"; //$NON-NLS-1$
        label.setText(text);
        label.setToolTipText(text);
    }
}
