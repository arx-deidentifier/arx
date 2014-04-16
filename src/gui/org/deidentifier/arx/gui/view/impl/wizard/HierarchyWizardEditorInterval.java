/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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
 * An editor for intervals
 * @author Fabian Prasser
 *
 * @param <T>
 */
public class HierarchyWizardEditorInterval<T> implements HierarchyWizardView, IHierarchyFunctionEditorParent<T>{

    /** Var */
    private HierarchyWizardGroupingInterval<T>     interval = null;
    /** Var */
    private final HierarchyWizardModelGrouping<T>  model;
    /** Var */
    private final EditorString                     editorMin;
    /** Var */
    private final EditorString                     editorMax;
    /** Var */
    private final DataTypeWithRatioScale<T>        type;
    /** Var */
    private final HierarchyWizardEditorFunction<T> editorFunction;
    /** Var */
    private final Composite                        composite;

    /**
     * Creates a new instance
     * @param parent
     * @param model
     */
    @SuppressWarnings("unchecked")
    public HierarchyWizardEditorInterval(final Composite parent,
                                   final HierarchyWizardModelGrouping<T> model) {
        this.model = model;
        this.model.register(this);
        this.type = (DataTypeWithRatioScale<T>)model.getDataType();

        composite = new Composite(parent, SWT.NONE);
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
            SWTUtil.enable(composite);
        } else {
            this.interval = null;
            this.editorFunction.setFunction(null);
            SWTUtil.disable(editorMin.getControl());
            SWTUtil.disable(editorMax.getControl());
            SWTUtil.disable(composite);
        }
    }

    /**
     * Creates a label
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
