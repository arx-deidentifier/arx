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

package org.deidentifier.arx.gui.view.impl.wizard;

import org.deidentifier.arx.DataType.DataTypeWithRatioScale;
import org.deidentifier.arx.gui.resources.Resources;
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
    private EditorString                          editorSnap;
    
    /** Var. */
    private EditorString                          editorTopBottomCoding;
    
    /** Var. */
    private EditorString                          editorMinMax;

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
        this.composite.setText(lower ? Resources.getMessage("HierarchyWizardEditorRange.0") : Resources.getMessage("HierarchyWizardEditorRange.1")); //$NON-NLS-1$ //$NON-NLS-2$
        this.composite.setLayout(SWTUtil.createGridLayout(2, false));
        this.composite.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        this.type = (DataTypeWithRatioScale<T>)model.getDataType();
        this.model = model;
        this.model.register(this);
        if (!lower) {
            this.range = model.getUpperRange();
            createSnap(model, lower, range);
            createTopBottomCoding(model, lower, range);
            createMinMax(model, lower, range);
        }
        else {
            this.range = model.getLowerRange();
            createMinMax(model, lower, range);
            createTopBottomCoding(model, lower, range);
            createSnap(model, lower, range);
        }
    }

    @Override
    public void update() {
        editorSnap.update();
        editorTopBottomCoding.update();
        editorMinMax.update();
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
     * @param bottom
     */
    private void createMinMax(final HierarchyWizardModelGrouping<T> model,
                             final boolean lower,
                             final HierarchyWizardGroupingRange<T> adjustment) {
        createLabel(composite, lower ? Resources.getMessage("HierarchyWizardEditorRange.10") : //$NON-NLS-1$
                                       Resources.getMessage("HierarchyWizardEditorRange.11")); //$NON-NLS-1$
        editorMinMax = new EditorString(composite) {
            
            @Override
            public boolean accepts(final String s) {
                return type.isValid(s);
            }

            @Override
            public String getValue() {
                T value = adjustment.minMaxBound;
                if (value == null) return ""; //$NON-NLS-1$
                else return type.format(value);
            }

            @Override
            public void setValue(final String s) {
                T value = type.parse(s);
                adjustment.minMaxBound = value;
                model.update();
            }

            @Override
            public boolean isDifferent(String value1, String value2) {
                if (!accepts(value1) || !accepts(value2)) {
                    return true;
                }
                return type.compare(type.parse(value1), type.parse(value2)) != 0;
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
    private void createSnap(final HierarchyWizardModelGrouping<T> model,
                              final boolean lower,
                              final HierarchyWizardGroupingRange<T> adjustment) {
        createLabel(composite, Resources.getMessage("HierarchyWizardEditorRange.4")); //$NON-NLS-1$
        editorSnap = new EditorString(composite) {
            
            @Override
            public boolean accepts(final String s) {
                return type.isValid(s);
            }

            @Override
            public String getValue() {
                T value = adjustment.snapBound;
                if (value == null) return ""; //$NON-NLS-1$
                else return type.format(value);
            }

            @Override
            public void setValue(final String s) {
                T value = type.parse(s);
                adjustment.snapBound = value;
                model.update();
            }

            @Override
            public boolean isDifferent(String value1, String value2) {
                if (!accepts(value1) || !accepts(value2)) {
                    return true;
                }
                return type.compare(type.parse(value1), type.parse(value2)) != 0;
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
    private void createTopBottomCoding(final HierarchyWizardModelGrouping<T> model,
                            final boolean lower,
                            final HierarchyWizardGroupingRange<T> adjustment) {
        
        createLabel(composite, lower ? Resources.getMessage("HierarchyWizardEditorRange.7") //$NON-NLS-1$
                                     : Resources.getMessage("HierarchyWizardEditorRange.8")); //$NON-NLS-1$

        editorTopBottomCoding = new EditorString(composite) {
            
            @Override
            public boolean accepts(final String s) {
                return type.isValid(s);
            }

            @Override
            public String getValue() {
                T value = adjustment.bottomTopCodingBound;
                if (value == null) return ""; //$NON-NLS-1$
                else return type.format(value);
            }

            @Override
            public void setValue(final String s) {
                T value = type.parse(s);
                adjustment.bottomTopCodingBound = value;
                model.update();
            }

            @Override
            public boolean isDifferent(String value1, String value2) {
                if (!accepts(value1) || !accepts(value2)) {
                    return true;
                }
                return type.compare(type.parse(value1), type.parse(value2)) != 0;
            }
        };
    }
}
