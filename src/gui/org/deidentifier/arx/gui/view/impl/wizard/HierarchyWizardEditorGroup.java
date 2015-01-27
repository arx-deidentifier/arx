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

import org.deidentifier.arx.aggregates.AggregateFunction;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.menu.EditorString;
import org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizard.HierarchyWizardView;
import org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizardEditorFunction.IHierarchyFunctionEditorParent;
import org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizardModelGrouping.HierarchyWizardGroupingGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Editor for groups.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public class HierarchyWizardEditorGroup<T> implements HierarchyWizardView, IHierarchyFunctionEditorParent<T>{

    /** Var. */
    private HierarchyWizardGroupingGroup<T>        group = null;
    
    /** Var. */
    private final EditorString                     editorSize;
    
    /** Var. */
    private final HierarchyWizardModelGrouping<T>  model;
    
    /** Var. */
    private final HierarchyWizardEditorFunction<T> editorFunction;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param model
     */
    public HierarchyWizardEditorGroup(final Composite parent,
                                final HierarchyWizardModelGrouping<T> model) {
        this.model = model;
        this.model.register(this);
        
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(SWTUtil.createGridLayout(2, true));    
        composite.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        this.editorFunction = new HierarchyWizardEditorFunction<T>(this, model, composite, false);

        createLabel(composite, "Size:");
        this.editorSize = new EditorString(composite) {
            @Override
            public boolean accepts(final String s) {
                if (group==null) return false;
                try {
                    int i = Integer.parseInt(s);
                    return i>0;
                } catch (NumberFormatException e) {
                    return false;
                }
            }

            @Override
            public String getValue() {
                if (group==null) return "";
                else return String.valueOf(group.size);
            }

            @Override
            public void setValue(final String s) {
                if (group!=null){
                    if (group.size != Integer.valueOf(s)){
                        group.size = Integer.valueOf(s);
                        model.update(HierarchyWizardEditorGroup.this);
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
        if (this.group == null) return;
        if (editorFunction.isDefaultFunction(function)) {
            this.group.function = model.getDefaultFunction();
        } else {
            this.group.function = function;
        }
        model.update(this);
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizard.HierarchyWizardView#update()
     */
    @SuppressWarnings("unchecked")
    @Override
    public void update() {
        if (model.getSelectedElement() instanceof HierarchyWizardGroupingGroup){
            this.group = (HierarchyWizardGroupingGroup<T>)model.getSelectedElement();
            this.editorFunction.setFunction(group.function);
            this.editorSize.update();
            SWTUtil.enable(editorSize.getControl());
            this.editorFunction.update();
        } else {
            this.group = null;
            this.editorFunction.setFunction(null);
            this.editorSize.update();
            SWTUtil.disable(editorSize.getControl());
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
