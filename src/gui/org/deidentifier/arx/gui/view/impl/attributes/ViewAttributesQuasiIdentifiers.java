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

package org.deidentifier.arx.gui.view.impl.attributes;


import java.util.HashSet;
import java.util.Set;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * This view allows to select a subset of the quasi-identifiers
 * 
 * @author Fabian Prasser
 */
public class ViewAttributesQuasiIdentifiers implements IView {

    /** Controller */
    private final Controller controller;

    /** View */
    private final Composite  root;
    /** View */
    private final Table      table;
    /** View */
    private final Label      label;

    /** Model */
    private Model            model;

    /**
     * Creates a new instance.
     * 
     * @param parent
     * @param controller
     */
    public ViewAttributesQuasiIdentifiers(final Composite parent,
                                    final Controller controller) {

        controller.addListener(ModelPart.INPUT, this);
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.SELECTED_QUASI_IDENTIFIERS, this);
        
        this.controller = controller;

        // Create group
        root = parent;
        root.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).create());

        // Create table
        table = SWTUtil.createTable(parent, SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        table.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(2, 1).create());
        table.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent arg0) {
                fireEvent();
            }   
        });
        
        // Create button
        Button button = new Button(parent, SWT.PUSH);
        button.setLayoutData(SWTUtil.createGridData());
        button.setText(Resources.getMessage("ViewRisksQuasiIdentifiers.0")); //$NON-NLS-1$
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                for (TableItem item : table.getItems()) {
                    item.setChecked(false);
                }
                fireEvent();
            }
        });
        
        // Create label
        label = new Label(parent, SWT.RIGHT);
        label.setText(""); //$NON-NLS-1$
        label.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        
        // Reset view
        reset();
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    @Override
    public void reset() {
        label.setText(""); //$NON-NLS-1$
        for (TableItem item : table.getItems()) {
            item.dispose();
        }
        SWTUtil.disable(root);
    }

    @Override
    public void update(final ModelEvent event) {
        if (event.part == ModelPart.MODEL) {
           this.model = (Model) event.data;
           update();
        } else if (event.part == ModelPart.INPUT || event.part == ModelPart.SELECTED_QUASI_IDENTIFIERS) {
           update();
        }
    }

    /**
     * Checks the selected items and fires an event on changes
     */
    private void fireEvent() {
        Set<String> selection = new HashSet<String>();
        for (TableItem item : table.getItems()) {
            if (item.getChecked()) {
                selection.add(item.getText());
            }
        }
        if (model != null) {
            
            if (selection.equals(model.getSelectedQuasiIdentifiers())) {
                return;
            }
            
            if (selection.size() <= model.getRiskModel().getMaxQiSize()) {
                model.setSelectedQuasiIdentifiers(selection);
                controller.update(new ModelEvent(ViewAttributesQuasiIdentifiers.this, ModelPart.SELECTED_QUASI_IDENTIFIERS, selection));
                label.setText(Resources.getMessage("ViewRisksQuasiIdentifiers.3") + (int)(Math.pow(2, selection.size())-1)); //$NON-NLS-1$
                label.setForeground(GUIHelper.COLOR_BLACK);
            } else {
                label.setText(Resources.getMessage("ViewRisksQuasiIdentifiers.4") + (int)(Math.pow(2, selection.size())-1)); //$NON-NLS-1$
                label.setForeground(GUIHelper.COLOR_RED);
            }
        }
    }

    /**
     * Updates the view.
     * 
     * @param node
     */
    private void update() {

        if (model == null || model.getInputConfig() == null ||
            model.getInputConfig().getInput() == null || model.getSelectedQuasiIdentifiers() == null) {
            return;
        }
        
        DataHandle handle = model.getInputConfig().getInput().getHandle();

        root.setRedraw(false);
        
        Set<String> selection = model.getSelectedQuasiIdentifiers();
        
        for (TableItem item : table.getItems()) {
            item.dispose();
        }
        
        for (int i = 0; i < handle.getNumColumns(); i++) {
            TableItem item = new TableItem(table, SWT.NONE);
            String value = handle.getAttributeName(i);
            item.setText(value);
            item.setChecked(selection.contains(value));
        }
        
        label.setText(Resources.getMessage("ViewRisksQuasiIdentifiers.5") + (int)(Math.pow(2, selection.size())-1)); //$NON-NLS-1$
        
        root.setRedraw(true);
        SWTUtil.enable(root);
    }
}
