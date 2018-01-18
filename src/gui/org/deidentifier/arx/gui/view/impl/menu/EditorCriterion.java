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

package org.deidentifier.arx.gui.view.impl.menu;

import java.util.List;

import org.deidentifier.arx.gui.model.ModelCriterion;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import de.linearbits.swt.widgets.Knob;
import de.linearbits.swt.widgets.KnobColorProfile;
import de.linearbits.swt.widgets.KnobRange;

/**
 * Base class for editors
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
     * Parse method
     * @param model
     */
    @SuppressWarnings("unchecked")
    public void parseDefault(ModelCriterion model) {
        this.parse((T)model, true);
        this.model.parse(model, true);
    }

    /**
     * Build the composite
     * 
     * @param parent
     */
    protected abstract Composite build(Composite parent);

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
     * Creates a label
     * @return
     */
    protected Text createLabel(Composite parent) {

        final Text label = new Text(parent, SWT.BORDER | SWT.LEFT);
        GridData data = SWTUtil.createFillHorizontallyGridData(false);
        label.setLayoutData(data);
        label.setEditable(false);
        return label;
    }

    /**
     * Returns a set of typical parameters
     * @return
     */
    protected abstract List<ModelCriterion> getTypicalParameters();
    
    /**
     * Parse non-default parameters
     * @param model
     */
    protected void parse(T model) {
        this.parse(model, false);
    }

    /**
     * Parse
     * 
     * @param model
     * @param default
     */
    protected abstract void parse(T model, boolean defaultParameters);
    
    /**
     * Updates the label and tool tip text.
     *
     * @param label
     * @param value
     */
    protected void updateLabel(Text label, double value) {
        String text = SWTUtil.getPrettyString(value);
        label.setText(" " + text);
        label.setToolTipText(String.valueOf(value));
    }
    
    /**
     * Updates the label and tool tip text.
     *
     * @param label
     * @param value
     */
    protected void updateLabel(Text label, int value) {
        String text = SWTUtil.getPrettyString(value);
        label.setText(" " + text);
        label.setToolTipText(text);
    }
}
