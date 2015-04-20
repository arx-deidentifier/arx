package org.deidentifier.arx.gui.view.impl.define;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.aggregates.MicroaggregateFunction;
import org.deidentifier.arx.aggregates.MicroaggregateFunction.HandlingOfNullValues;
import org.deidentifier.arx.aggregates.MicroaggregationFunctionDescription;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
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
 * This class represents the microaggregation view
 * @author Florian Kohlmayer
 *
 */
public class ViewMicoaggregation implements IView {
    
    /** The attribute for which the view was created */
    private String                                attribute;
    /** The controller */
    private Controller                            controller;
    /** The base composite */
    private Composite                             base;
    /** The model */
    private Model                                 model;
    /** The list containing all valid functions for the selected datatype */
    private MicroaggregationFunctionDescription[] validFunctions;
    /** The Combobox */
    private Combo                                 functionCombo;
    /** The Combobox */
    private Combo                                 nullValueCombo;
    /** The button */
    private Button                                microaggregationButton;
    
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
        model = controller.getModel();
        this.microaggregationButton = microaggregationButton;
        
        // Listener
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        controller.addListener(ModelPart.DATA_TYPE, this);
        
        // Create base composite
        base = new Composite(parent, SWT.NONE | SWT.BORDER);
        GridData layoutData = SWTUtil.createFillHorizontallyGridData();
        GridLayout layout = new GridLayout();
        layout.numColumns = 4;
        base.setLayout(layout);
        base.setLayoutData(layoutData);
        
        // add dropdown selection boxes
        final Label fLabel = new Label(base, SWT.PUSH);
        fLabel.setText("Function:");
        functionCombo = new Combo(base, SWT.READ_ONLY);
        functionCombo.setLayoutData(SWTUtil.createFillGridData());
        functionCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                int index = ((Combo) arg0.getSource()).getSelectionIndex();
                selectfunction(index);
            }
        });
        
        // add dropdown selection boxes
        final Label hLabel = new Label(base, SWT.PUSH);
        hLabel.setText("Null values:");
        nullValueCombo = new Combo(base, SWT.READ_ONLY);
        nullValueCombo.setLayoutData(SWTUtil.createFillGridData());
        nullValueCombo.setItems(getHandlingOfNullValuesLabels());
        nullValueCombo.select(0);
        
        nullValueCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                int index = ((Combo) arg0.getSource()).getSelectionIndex();
                if ((model != null) && (model.getInputConfig() != null)) {
                    model.getInputConfig().setMicroaggregationHandlingOfNullValues(attribute, HandlingOfNullValues.values()[index]);
                }
            }
        });
        
        updateValidFunctions();
        
    }
    
    @Override
    public void dispose() {
        controller.removeListener(this);
        if (!base.isDisposed()) {
            base.dispose();
        }
    }
    
    @Override
    public void reset() {
        if (!base.isDisposed()) {
            base.redraw();
        }
    }
    
    @Override
    public void update(ModelEvent event) {
        if (event.part == ModelPart.ATTRIBUTE_TYPE) {
            final String attr = (String) event.data;
            if (attr.equals(attribute)) {
                updateValidFunctions();
                restoreStoredFunction();
                restoreStoredHandlingOfNullValues();
            }
        } else if (event.part == ModelPart.DATA_TYPE) {
            updateValidFunctions();
        }
    }
    
    /**
     * Helper function to convert enums to label array.
     * @return
     */
    private String[] getHandlingOfNullValuesLabels() {
        HandlingOfNullValues[] values = HandlingOfNullValues.values();
        String[] result = new String[values.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = values[i].getLabel();
        }
        return result;
    }
    
    /**
     * Restores the function stored in the view model.
     */
    private void restoreStoredFunction() {
        if ((model != null) && (model.getInputConfig() != null)) {
            MicroaggregationFunctionDescription restoredFunction = model.getInputConfig().getMicroaggregationFunctionDescription(attribute);
            for (int i = 0; i < validFunctions.length; i++) {
                MicroaggregationFunctionDescription function = validFunctions[i];
                if (function.equals(restoredFunction)) {
                    selectfunction(i);
                    return;
                }
            }
            if (validFunctions.length > 0) {
                selectfunction(0);
            }
        }
    }
    
    /**
     * Restores the stored handling of null values.
     */
    private void restoreStoredHandlingOfNullValues() {
        if ((model != null) && (model.getInputConfig() != null)) {
            HandlingOfNullValues restored = model.getInputConfig().getMicroaggregationHandlingOfNullValues(attribute);
            HandlingOfNullValues[] values = HandlingOfNullValues.values();
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(restored)) {
                    nullValueCombo.select(i);
                }
            }
        }
    }
    
    /**
     * Creates the selected function.
     * @param index
     */
    private void selectfunction(int index) {
        if (validFunctions.length > index) {
            MicroaggregationFunctionDescription selected = validFunctions[index];
            if ((model == null) || (model.getInputConfig() == null)) {
                functionCombo.select(0);
                return;
            }
            functionCombo.select(index);
            model.getInputConfig().setMicroaggregationFunctionDescription(attribute, selected);
        }
    }
    
    /**
     * Updates the valid functions based on the datatype.
     */
    private void updateValidFunctions() {
        List<MicroaggregationFunctionDescription> functions = MicroaggregateFunction.list();
        List<String> items = new ArrayList<String>();
        List<MicroaggregationFunctionDescription> validFunctions = new ArrayList<MicroaggregationFunctionDescription>();
        for (int i = 0; i < functions.size(); i++) {
            MicroaggregationFunctionDescription function = functions.get(i);
            if (function.getRequiredScaleOfMeasure().compareTo(model.getInputDefinition().getDataType(attribute).getScaleOfMeasure()) <= 0) {
                items.add(function.getLabel());
                validFunctions.add(function);
            }
        }
        
        this.validFunctions = validFunctions.toArray(new MicroaggregationFunctionDescription[validFunctions.size()]);
        
        if (items.size() > 0) {
            functionCombo.setItems(items.toArray(new String[items.size()]));
            functionCombo.setEnabled(true);
            restoreStoredFunction();
            microaggregationButton.setEnabled(true);
        } else {
            functionCombo.setItems(new String[] { "No valid microaggregation function for datatype" });
            functionCombo.setEnabled(false);
            functionCombo.select(0);
            microaggregationButton.setEnabled(false);
        }
    }
    
}
