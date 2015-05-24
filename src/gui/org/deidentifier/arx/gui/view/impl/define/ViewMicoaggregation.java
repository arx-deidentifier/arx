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

package org.deidentifier.arx.gui.view.impl.define;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.MicroAggregationFunctionDescription;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * This class implements a view for configuring microaggregation.
 * 
 * @author Florian Kohlmayer
 * @author Fabian Prasser
 *
 */
public class ViewMicoaggregation implements IView {
    
    /** Model */
    private String                                attribute;
    /** Model */
    private Model                                 model;
    /** Model */
    private MicroAggregationFunctionDescription[] functions;
    
    /** Controller */
    private Controller                            controller;
    
    /** Widget */
    private Composite                             cmpBase;
    /** Widget */
    private Combo                                 cmbFunction;
    /** Widget */
    private Button                                btnMissingData;
    /** Widget */
    private Button                                bntEnable;
    
    /**
     * Instantiates.
     * 
     * @param parent
     * @param attribute
     * @param controller
     * @param microaggregationButton
     */
    public ViewMicoaggregation(Composite parent, final String attribute, Controller controller, Button microaggregationButton) {
        this.attribute = attribute;
        this.controller = controller;
        this.bntEnable = microaggregationButton;
        this.model = controller.getModel();
        
        // Listener
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        controller.addListener(ModelPart.DATA_TYPE, this);
        
        // Create base composite
        cmpBase = new Composite(parent, SWT.NONE | SWT.BORDER);
        GridData layoutData = SWTUtil.createFillHorizontallyGridData();
        GridLayout layout = new GridLayout();
        layout.numColumns = 4;
        cmpBase.setLayout(layout);
        cmpBase.setLayoutData(layoutData);
        
        // Add dropdown selection boxes
        final Label fLabel = new Label(cmpBase, SWT.PUSH);
        fLabel.setText(Resources.getMessage("ViewMicoaggregation.0")); //$NON-NLS-1$
        cmbFunction = new Combo(cmpBase, SWT.READ_ONLY);
        cmbFunction.setLayoutData(SWTUtil.createFillGridData());
        cmbFunction.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                int index = ((Combo) arg0.getSource()).getSelectionIndex();
                selectFunction(index);
            }
        });
        
        // Add button for missing data
        final Label hLabel = new Label(cmpBase, SWT.PUSH);
        hLabel.setText(Resources.getMessage("ViewMicoaggregation.1")); //$NON-NLS-1$
        btnMissingData = new Button(cmpBase, SWT.CHECK);
        btnMissingData.setLayoutData(SWTUtil.createFillGridData());
        btnMissingData.setText(Resources.getMessage("ViewMicoaggregation.2")); //$NON-NLS-1$
        btnMissingData.setSelection(true);
        btnMissingData.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if ((model != null) && (model.getInputConfig() != null)) {
                    model.getInputConfig().setMicroAggregationIgnoreMissingData(attribute, btnMissingData.getSelection());
                }
            }
        });
        
        updateFunctions();
    }
    
    @Override
    public void dispose() {
        controller.removeListener(this);
        if (!cmpBase.isDisposed()) {
            cmpBase.dispose();
        }
    }
    
    @Override
    public void reset() {
        if (!cmpBase.isDisposed()) {
            cmpBase.redraw();
        }
    }
    
    @Override
    public void update(ModelEvent event) {
        if (event.part == ModelPart.ATTRIBUTE_TYPE) {
            final String attr = (String) event.data;
            if (attr.equals(attribute)) {
                updateFunctions();
                restoreFunction();
                restoreHandlingOfMissingData();
            }
        } else if (event.part == ModelPart.DATA_TYPE) {
            updateFunctions();
        }
    }
    
    /**
     * Restores the function stored in the view model.
     */
    private void restoreFunction() {
        if ((model != null) && (model.getInputConfig() != null)) {
            MicroAggregationFunctionDescription restoredFunction = model.getInputConfig().getMicroAggregationFunction(attribute);
            for (int i = 0; i < functions.length; i++) {
                MicroAggregationFunctionDescription function = functions[i];
                if (function.equals(restoredFunction)) {
                    selectFunction(i);
                    return;
                }
            }
            if (functions.length > 0) {
                selectFunction(0);
            }
        }
    }
    
    /**
     * Restores the stored handling of null values.
     */
    private void restoreHandlingOfMissingData() {
        if ((model != null) && (model.getInputConfig() != null)) {
            btnMissingData.setSelection(model.getInputConfig().getMicroAggregationIgnoreMissingData(attribute));
        }
    }
    
    /**
     * Creates the selected function.
     * @param index
     */
    private void selectFunction(int index) {
        if (functions.length > index) {
            MicroAggregationFunctionDescription selected = functions[index];
            if ((model == null) || (model.getInputConfig() == null)) {
                cmbFunction.select(0);
                return;
            }
            cmbFunction.select(index);
            model.getInputConfig().setMicroAggregationFunction(attribute, selected);
        }
    }
    
    /**
     * Updates the valid functions based on the data type.
     */
    private void updateFunctions() {
        List<MicroAggregationFunctionDescription> functions = AttributeType.listMicroAggregationFunctions();
        List<String> items = new ArrayList<String>();
        List<MicroAggregationFunctionDescription> validFunctions = new ArrayList<MicroAggregationFunctionDescription>();
        for (int i = 0; i < functions.size(); i++) {
            MicroAggregationFunctionDescription function = functions.get(i);
            if (model.getInputDefinition().getDataType(attribute).getDescription().getScale().provides(function.getRequiredScale())) {
                items.add(function.getLabel());
                validFunctions.add(function);
            }
        }
        
        this.functions = validFunctions.toArray(new MicroAggregationFunctionDescription[validFunctions.size()]);
        
        if (items.size() > 0) {
            cmbFunction.setItems(items.toArray(new String[items.size()]));
            cmbFunction.setEnabled(true);
            restoreFunction();
            bntEnable.setEnabled(true);
        } else {
            cmbFunction.setItems(new String[] {Resources.getMessage("ViewMicoaggregation.3")}); //$NON-NLS-1$
            cmbFunction.setEnabled(false);
            cmbFunction.select(0);
            bntEnable.setEnabled(false);
        }
    }
}
