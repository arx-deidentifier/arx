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

import org.deidentifier.arx.DataType.DataTypeWithRatioScale;
import org.deidentifier.arx.aggregates.AggregateFunction;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.menu.EditorString;
import org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizard.HierarchyWizardView;
import org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizardEditorFunction.IHierarchyFunctionEditorParent;
import org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizardModelGrouping.HierarchyWizardGroupingInterval;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * An editor for intervals.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public class HierarchyWizardEditorInterval<T> implements HierarchyWizardView, IHierarchyFunctionEditorParent<T>{

    /** Var. */
    private HierarchyWizardGroupingInterval<T>     interval = null;
    
    /** Var. */
    private final HierarchyWizardModelGrouping<T>  model;
    
    /** Var. */
    private final EditorString                     editorMin;
    
    /** Var. */
    private final EditorString                     editorMax;
    
    /** Var. */
    private final DataTypeWithRatioScale<T>        type;
    
    /** Var. */
    private final HierarchyWizardEditorFunction<T> editorFunction;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param model
     */
    @SuppressWarnings("unchecked")
    public HierarchyWizardEditorInterval(final Composite parent,
                                   final HierarchyWizardModelGrouping<T> model) {
        this.model = model;
        this.model.register(this);
        this.type = (DataTypeWithRatioScale<T>)model.getDataType();

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        composite.setLayout(SWTUtil.createGridLayout(2, true));
        this.editorFunction = new HierarchyWizardEditorFunction<T>(this, model, composite, false);
        
        createLabel(composite, "Min:");
        editorMin = new EditorString(composite) {
            @Override
            public boolean accepts(final String s) {
                if (interval==null) return false;
                if (!type.isValid(s)) return false;
                T value = type.parse(s);
                if (type.compare(value, interval.max) > 0) return false;
                else return true;
            }

            @Override
            public String getValue() {
                if (interval==null) return "";
                else return type.format(interval.min);
            }

            @Override
            public void setValue(final String s) {
                if (interval!=null){
                    if (interval.min != type.parse(s)){
                        interval.min = type.parse(s);
                        model.update(HierarchyWizardEditorInterval.this);
                    }
                }
            }
        };
        
        createLabel(composite, "Max:");
        editorMax = new EditorString(composite) {
            
            @Override
            public boolean accepts(final String s) {
                if (interval==null) return false;
                if (!type.isValid(s)) return false;
                T value = type.parse(s);
                if (type.compare(value, interval.min) < 0) return false;
                else return true;
            }

            @Override
            public String getValue() {
                if (interval==null) return "";
                else return type.format(interval.max);
            }

            @Override
            public void setValue(final String s) {
                if (interval!=null){
                    if (interval.max != type.parse(s)){
                        interval.max = type.parse(s);
                        model.update(HierarchyWizardEditorInterval.this);
                    }
                }
            }
        };
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizardEditorFunction.IHierarchyFunctionEditorParent#setFunction(org.deidentifier.arx.aggregates.AggregateFunction)
     */
    @Override
    public void setFunction(AggregateFunction<T> function) {
        if (this.interval == null) return;
        if (editorFunction.isDefaultFunction(function)) {
            this.interval.function = model.getDefaultFunction();
        } else {
            this.interval.function = function;
        }
        model.update(this);
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizard.HierarchyWizardView#update()
     */
    @SuppressWarnings("unchecked")
    @Override
    public void update() {
        if (model.getSelectedElement() instanceof HierarchyWizardGroupingInterval){
            
            this.interval = (HierarchyWizardGroupingInterval<T>)model.getSelectedElement();
            this.editorFunction.setFunction(this.interval.function);
            this.editorMin.update();
            this.editorMax.update();
            
            if (model.isFirst(this.interval)){
                SWTUtil.enable(editorMin.getControl());
            } else {
                SWTUtil.disable(editorMin.getControl());
            }
            if (model.isLast(this.interval)){
                SWTUtil.enable(editorMax.getControl());
            } else {
                SWTUtil.disable(editorMax.getControl());
            }
            this.editorFunction.update();
        } else {
            this.interval = null;
            this.editorFunction.setFunction(null);
            SWTUtil.disable(editorMin.getControl());
            SWTUtil.disable(editorMax.getControl());
            SWTUtil.disable(this.editorFunction.getControl1());
            SWTUtil.disable(this.editorFunction.getControl2());
        }
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
}
