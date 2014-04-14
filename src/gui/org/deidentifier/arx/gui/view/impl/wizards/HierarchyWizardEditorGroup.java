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

package org.deidentifier.arx.gui.view.impl.wizards;

import org.deidentifier.arx.aggregates.AggregateFunction;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.menu.EditorString;
import org.deidentifier.arx.gui.view.impl.wizards.HierarchyWizard.HierarchyWizardView;
import org.deidentifier.arx.gui.view.impl.wizards.HierarchyWizardEditorFunction.IHierarchyFunctionEditorParent;
import org.deidentifier.arx.gui.view.impl.wizards.HierarchyWizardModelGrouping.HierarchyWizardGroupingGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Editor for groups
 * @author Fabian Prasser
 *
 * @param <T>
 */
public class HierarchyWizardEditorGroup<T> implements HierarchyWizardView, IHierarchyFunctionEditorParent<T>{

    /** Var */
    private HierarchyWizardGroupingGroup<T>        group = null;
    /** Var */
    private final EditorString                     editorSize;
    /** Var */
    private final HierarchyWizardModelGrouping<T>  model;
    /** Var */
    private final HierarchyWizardEditorFunction<T> editorFunction;

    /**
     * Creates a new instance
     * @param parent
     * @param model
     */
    public HierarchyWizardEditorGroup(final Composite parent,
                                final HierarchyWizardModelGrouping<T> model) {
        this.model = model;
        this.model.register(this);
        
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(SWTUtil.createGridLayout(2, false));    
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
    
    @SuppressWarnings("unchecked")
    @Override
    public void update() {
        if (model.getSelectedElement() instanceof HierarchyWizardGroupingGroup){
            this.group = (HierarchyWizardGroupingGroup<T>)model.getSelectedElement();
            this.editorFunction.setFunction(group.function);
            this.editorSize.update();
            SWTUtil.enable(editorSize.getControl());
        } else {
            this.group = null;
            this.editorFunction.setFunction(null);
            this.editorSize.update();
            SWTUtil.disable(editorSize.getControl());
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
