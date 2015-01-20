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

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.AggregateFunction;
import org.deidentifier.arx.aggregates.AggregateFunction.AggregateFunctionWithParameter;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.menu.EditorSelection;
import org.deidentifier.arx.gui.view.impl.menu.EditorString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Editor for functions.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public class HierarchyWizardEditorFunction<T> {
    
    /**
     * Tiny callback for parents.
     *
     * @author Fabian Prasser
     * @param <T>
     */
    public static interface IHierarchyFunctionEditorParent<T> {
        
        /**
         * 
         *
         * @param function
         */
        public void setFunction(AggregateFunction<T> function);
    }

    /** Var. */
    private final AggregateFunction<T>            defaultFunction;
    
    /** Var. */
    private final List<AggregateFunction<T>>      functions;
    
    /** Var. */
    private final List<String>                    labels;
    
    /** Var. */
    private final EditorSelection                 editor1;
    
    /** Var. */
    private final EditorString                    editor2;
    
    /** Var. */
    private AggregateFunction<T>                  function = null;
    
    /** Var. */
    private final HierarchyWizardModelGrouping<T> model;
    
    /** Var. */
    private final boolean                         general;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param model
     * @param composite
     * @param general
     */
    public HierarchyWizardEditorFunction(final IHierarchyFunctionEditorParent<T> parent,
                                   final HierarchyWizardModelGrouping<T> model,
                                   final Composite composite,
                                   final boolean general) {

        DataType<T> type = model.getDataType();
        this.general = general;
        this.functions = new ArrayList<AggregateFunction<T>>();
        this.labels = new ArrayList<String>();
        this.model = model;
        this.defaultFunction = new AggregateFunction<T>(type){
            private static final long serialVersionUID = -6444219024682845316L;
            @Override public String aggregate(String[] values) {return null;}
            @Override public String toLabel() {return "Default";}
            @Override public String toString() {return "Default";}
        };
        
        if (!general) {
            this.createEntry(this.defaultFunction);
            this.createEntry(AggregateFunction.forType(type).createConstantFunction(""));
        } 
        
        this.createEntry(AggregateFunction.forType(type).createBoundsFunction());
        this.createEntry(AggregateFunction.forType(type).createPrefixFunction());
        this.createEntry(AggregateFunction.forType(type).createIntervalFunction(true, false));
        this.createEntry(AggregateFunction.forType(type).createSetFunction());
        this.createEntry(AggregateFunction.forType(type).createSetOfPrefixesFunction(1));
        

        createLabel(composite, "Aggregate function:");
        this.editor1 = new EditorSelection(composite, labels.toArray(new String[labels.size()])) {
            @Override
            public boolean accepts(final String s) {
                return labels.contains(s);
            }

            @Override
            public String getValue() {
                if (function == null){
                    return null;
                }
                else return function.toLabel();
            }

            @Override
            public void setValue(final String s) {
                function = functions.get(labels.indexOf(s));
                HierarchyWizardEditorFunction.this.update();
                parent.setFunction(function);
            }
        };

        createLabel(composite, "Function Parameter:");
        this.editor2 = new EditorString(composite) {
            @Override
            public boolean accepts(String s) {
                if (s=="") s = null;
                if (function == null) return false;
                if (!function.hasParameter()) return false;
                else return ((AggregateFunctionWithParameter<T>)function).acceptsParameter(s);
            }

            @Override
            public String getValue() {
                if (function == null) return "";
                if (!function.hasParameter()) return "";
                else {
                    String param = ((AggregateFunctionWithParameter<T>)function).getParameter();
                    if (param == null) return "";
                    else return param;
                }
            }

            @Override
            public void setValue(String s) {
                if (s=="") s = null;
                if (function == null) return;
                if (!function.hasParameter()) return;
                else {
                    function = ((AggregateFunctionWithParameter<T>)function).newInstance(s);
                    parent.setFunction(function);
                }
            }
        };
    }
    
    /**
     * Return control1.
     *
     * @return
     */
    public Control getControl1() {
        return editor1.getControl();
    }
    
    /**
     * Return control2.
     *
     * @return
     */
    public Control getControl2() {
        return editor2.getControl();
    }

    /**
     * Returns the default function.
     *
     * @return
     */
    public AggregateFunction<T> getDefaultFunction(){
        return this.defaultFunction;
    }

    /**
     * Returns whether this is the default function.
     *
     * @param function
     * @return
     */
    public boolean isDefaultFunction(AggregateFunction<T> function){
        return this.defaultFunction == function;
    }
    
    /**
     * Sets the function to display.
     *
     * @param function
     */
    public void setFunction(AggregateFunction<T> function){
        if (!general && function == model.getDefaultFunction()){
            function = defaultFunction;
        }
        this.function = function;
        this.update();
    }
    
    /**
     * Updates all editors.
     */
    public void update() {

        // Update editors
        editor1.update();
        editor2.update();
        if (this.function != null){
            SWTUtil.enable(editor1.getControl());
            if (this.function.hasParameter()){
                SWTUtil.enable(editor2.getControl());
            } else {
                SWTUtil.disable(editor2.getControl());
            }
        } else {
            SWTUtil.disable(editor1.getControl());
            SWTUtil.disable(editor2.getControl());
        }
    }

    /**
     * Creates a function entry.
     *
     * @param function
     */
    private void createEntry(AggregateFunction<T> function) {
        this.functions.add(function);
        this.labels.add(function.toLabel());
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
        GridData data = SWTUtil.createFillHorizontallyGridData();
        data.grabExcessHorizontalSpace = false;
        data.verticalAlignment = SWT.CENTER;
        label.setLayoutData(data);
        return label;
    }
}
