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

package org.deidentifier.arx.gui.view.impl.menu;

import java.text.DecimalFormat;

import org.deidentifier.arx.gui.model.ModelCriterion;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.linearbits.swt.widgets.Knob;
import de.linearbits.swt.widgets.KnobColorProfile;
import de.linearbits.swt.widgets.KnobRange;

/**
 * Base class
 * 
 * @author Fabian Prasser
 */
public abstract class EditorCriterion<T extends ModelCriterion> {

    /** Model */
    protected final T              model;
    /** View */
    private final Composite        root;
    /** Color profile */
    private final KnobColorProfile defaultColorProfile;
    /** Color profile */
    private final KnobColorProfile focusedColorProfile;
    /** Format*/
    private final DecimalFormat format = new DecimalFormat("0.00000E0###");

    /**
     * Creates a new instance.
     * 
     * @param parent
     * @param model
     */
    public EditorCriterion(final Composite parent, final T model) {
        
        // Init
        this.defaultColorProfile = KnobColorProfile.createDefaultSystemProfile(parent.getDisplay());
        this.focusedColorProfile = KnobColorProfile.createFocusedBlueRedProfile(parent.getDisplay());
        
        // Prepare
        this.model = (T) model;
        this.root = this.build(parent);
        
        // Define color profiles
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
        
        // Parse
        this.parse(this.model);
    }
    
    /**
     * Creates a double knob
     * @param parent
     * @param min
     * @param max
     * @return
     */
    protected Knob<Double> createKnobDouble(Composite parent, double min, double max) {
        Knob<Double> knob = new Knob<Double>(parent, SWT.NULL, new KnobRange.Double(min, max));
        knob.setLayoutData(GridDataFactory.swtDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(30, 30).create());
        knob.setDefaultColorProfile(defaultColorProfile);
        knob.setFocusedColorProfile(focusedColorProfile);
        return knob;
    }

    /**
     * Creates a double knob
     * @param parent
     * @param min
     * @param max
     * @return
     */
    protected Knob<Integer> createKnobInteger(Composite parent, int min, int max) {
        Knob<Integer> knob = new Knob<Integer>(parent, SWT.NULL, new KnobRange.Integer(min, max));
        knob.setLayoutData(GridDataFactory.swtDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(30, 30).create());
        knob.setDefaultColorProfile(defaultColorProfile);
        knob.setFocusedColorProfile(focusedColorProfile);
        return knob;
    }

    /**
     * Updates the label and tool tip text.
     *
     * @param label
     * @param value
     */
    protected void updateLabel(Label label, int value) {
        String text = String.valueOf(value);
        label.setText(" " + text);
        label.setToolTipText(text);
    }

    /**
     * Updates the label and tool tip text.
     *
     * @param label
     * @param value
     */
    protected void updateLabel(Label label, double value) {
        String text = format.format(value).replace(",", ".").replace("E", "e");
        label.setText(" " + text);
        label.setToolTipText(text);
    }

    /**
     * Creates a label
     * @return
     */
    protected Label createLabel(Composite parent) {
        Label label = new Label(parent, SWT.BORDER | SWT.LEFT);
        GridData data = SWTUtil.createFillHorizontallyGridData(false);
        label.setLayoutData(data);
        return label;
    }
    
    /**
     * Disposes the editor
     */
    public void dispose() {
        this.root.dispose();
    }

    /**
     * Returns the altered model
     * 
     * @return
     */
    public T getModel() {
        return this.model;
    }

    /**
     * Build the composite
     * 
     * @param parent
     */
    protected abstract Composite build(Composite parent);

    /**
     * Parse
     * 
     * @param model
     */
    protected abstract void parse(T model);
}
