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

package org.deidentifier.arx.gui.view.impl.wizard;

import java.text.ParseException;

import org.deidentifier.arx.DataType.DataTypeWithRatioScale;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.menu.EditorString;
import org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizard.HierarchyWizardView;
import org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizardModelGrouping.HierarchyWizardGroupingRange;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * Editor for ranges.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public class HierarchyWizardEditorRange<T> implements HierarchyWizardView {

    /** Var. */
    private final Group                           composite;
    
    /** Var. */
    private final DataTypeWithRatioScale<T>       type;
    
    /** Var. */
    private final HierarchyWizardModelGrouping<T> model;
    
    /** Var. */
    private final HierarchyWizardGroupingRange<T> range;
    
    /** Var. */
    private EditorString                          repeat;
    
    /** Var. */
    private EditorString                          snap;
    
    /** Var. */
    private EditorString                          label;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param model
     * @param lower
     */
    @SuppressWarnings("unchecked")
    public HierarchyWizardEditorRange(final Composite parent,
                                     final HierarchyWizardModelGrouping<T> model,
                                     final boolean lower) {

        this.composite = new Group(parent, SWT.SHADOW_ETCHED_IN);
        this.composite.setText(lower ? "Lower bound" : "Upper bound");
        this.composite.setLayout(SWTUtil.createGridLayout(2, false));
        this.composite.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        this.type = (DataTypeWithRatioScale<T>)model.getDataType();
        this.model = model;
        this.model.register(this);
        if (!lower) {
            this.range = model.getUpperRange();
            createRepeat(model, lower, range);
            createSnap(model, lower, range);
            createLabel(model, lower, range);
        }
        else {
            this.range = model.getLowerRange();
            createLabel(model, lower, range);
            createSnap(model, lower, range);
            createRepeat(model, lower, range);
        }
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizard.HierarchyWizardView#update()
     */
    @Override
    public void update() {
        repeat.update();
        snap.update();
        label.update();
    }

    /**
     * Creates a label.
     *
     * @param composite
     * @param string
     * @return
     */
    private Label createLabel(Composite composite, String string) {
        Label label = new Label(composite, SWT.NONE);
        label.setText(string);
        GridData data = SWTUtil.createFillVerticallyGridData();
        data.verticalAlignment = SWT.CENTER;
        label.setLayoutData(data);
        return label;
    }

    /**
     * Create the label editor.
     *
     * @param model
     * @param lower
     * @param adjustment
     */
    private void createLabel(final HierarchyWizardModelGrouping<T> model,
                             final boolean lower,
                             final HierarchyWizardGroupingRange<T> adjustment) {
        createLabel(composite, "Label:");
        label = new EditorString(composite) {
            
            @Override
            public boolean accepts(final String s) {
                return type.isValid(s);
            }

            @Override
            public String getValue() {
                T value = adjustment.label;
                if (value == null) return "";
                else return type.format(value);
            }

            @Override
            public void setValue(final String s) {
                T value = type.parse(s);
                try {
                    if (type.compare(type.format(value), 
                                     type.format(adjustment.label)) != 0){
                        adjustment.label = value;
                        if (lower){
                            if (type.compare(adjustment.snap, adjustment.label) < 0) {
                                adjustment.snap = adjustment.label;
                            }
                            if (type.compare(adjustment.repeat, adjustment.snap) < 0) {
                                adjustment.repeat = adjustment.snap;
                            }
                        } else {
                            if (type.compare(adjustment.snap, adjustment.label) > 0) {
                                adjustment.snap = adjustment.label;
                            }
                            if (type.compare(adjustment.repeat, adjustment.snap) > 0) {
                                adjustment.repeat = adjustment.snap;
                            }
                        }
                        
                        model.update();
                    }
                } catch (NumberFormatException | ParseException e) {
                    // Ignore
                }
            }
        };
    }

    /**
     * Create the repeat editor.
     *
     * @param model
     * @param lower
     * @param adjustment
     */
    private void createRepeat(final HierarchyWizardModelGrouping<T> model,
                              final boolean lower,
                              final HierarchyWizardGroupingRange<T> adjustment) {
        createLabel(composite, "Repeat:");
        repeat = new EditorString(composite) {
            
            @Override
            public boolean accepts(final String s) {
                return type.isValid(s);
            }

            @Override
            public String getValue() {
                T value = adjustment.repeat;
                if (value == null) return "";
                else return type.format(value);
            }

            @Override
            public void setValue(final String s) {
                T value = type.parse(s);
                try {
                    if (type.compare(type.format(value), 
                                     type.format(adjustment.repeat)) != 0){
                         
                        adjustment.repeat = value;
                        if (lower){
                            if (type.compare(adjustment.repeat, adjustment.snap) < 0) {
                                adjustment.snap = adjustment.repeat;
                            }
                            if (type.compare(adjustment.snap, adjustment.label) < 0) {
                                adjustment.label = adjustment.snap;
                            }
                        } else {
                            if (type.compare(adjustment.repeat, adjustment.snap) > 0) {
                                adjustment.snap = adjustment.repeat;
                            }
                            if (type.compare(adjustment.snap, adjustment.label) > 0) {
                                adjustment.label = adjustment.snap;
                            }
                        }
                        
                        model.update();
                    }
                } catch (NumberFormatException | ParseException e) {
                    // Ignore
                }
            }
        };
    }

    /**
     * Create the snap editor.
     *
     * @param model
     * @param lower
     * @param adjustment
     */
    private void createSnap(final HierarchyWizardModelGrouping<T> model,
                            final boolean lower,
                            final HierarchyWizardGroupingRange<T> adjustment) {
        createLabel(composite, "Snap:");
        snap = new EditorString(composite) {
            
            @Override
            public boolean accepts(final String s) {
                return type.isValid(s);
            }

            @Override
            public String getValue() {
                T value = adjustment.snap;
                if (value == null) return "";
                else return type.format(value);
            }

            @Override
            public void setValue(final String s) {
                T value = type.parse(s);
                try {
                    if (type.compare(type.format(value), 
                                     type.format(adjustment.snap)) != 0){
                        
                        adjustment.snap = value;
                        if (lower){
                            if (type.compare(adjustment.repeat, adjustment.snap) < 0) {
                                adjustment.repeat = adjustment.snap;
                            }
                            if (type.compare(adjustment.snap, adjustment.label) < 0) {
                                adjustment.label = adjustment.snap;
                            }
                        } else {
                            if (type.compare(adjustment.repeat, adjustment.snap) > 0) {
                                adjustment.repeat = adjustment.snap;
                            }
                            if (type.compare(adjustment.snap, adjustment.label) > 0) {
                                adjustment.label = adjustment.snap;
                            }
                        }
                        
                        model.update();
                    }
                } catch (NumberFormatException | ParseException e) {
                    // Ignore
                }
            }
        };
    }
}
