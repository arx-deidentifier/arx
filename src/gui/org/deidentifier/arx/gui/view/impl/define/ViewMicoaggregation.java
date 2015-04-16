package org.deidentifier.arx.gui.view.impl.define;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.aggregates.MicroaggregateFunction;
import org.deidentifier.arx.aggregates.MicroaggregateFunction.ArithmeticMean;
import org.deidentifier.arx.aggregates.MicroaggregateFunction.GeometricMean;
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
    
    /**
     * All available microaggregation functions
     * @author Florian Kohlmayer
     *
     */
    public enum Microaggregatefunctions {
        
        ARITHMETIC_MEAN("Arithmetic mean"),
        GEOMETRIC_MEAN("Gemoetric mean");
        
        /** Label */
        private final String label;
        
        /**
         * Construct
         * @param label
         */
        private Microaggregatefunctions(String label) {
            this.label = label;
        }
        
        @Override
        public String toString() {
            return label;
        }
    }
    
    /** The attribute for which the view was created */
    private String                    attribute;
    /** The controller */
    private Controller                controller;
    /** The base composite */
    private Composite                 base;
    /** The model */
    private Model                     model;
    /** The selected microaggregation function */
    private MicroaggregateFunction    selectedFunction;
    /** The list containing all valid functions for the selected datatype */
    private Microaggregatefunctions[] validFunctions;
    /** The Combobox */
    private Combo                     functionCombo;
    /** The button */
    private Button                    microaggregationButton;
    
    /**
     * Instantiates.
     * 
     * @param parent
     * @param attribute
     * @param controller
     * @param microaggregationButton
     */
    public ViewMicoaggregation(Composite parent, String attribute, Controller controller, Button microaggregationButton) {
        this.attribute = attribute;
        this.controller = controller;
        this.model = controller.getModel();
        this.microaggregationButton = microaggregationButton;
        
        // Listener
        controller.addListener(ModelPart.INPUT, this);
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        controller.addListener(ModelPart.DATA_TYPE, this);
        
        // Create base composite
        base = new Composite(parent, SWT.NONE | SWT.BORDER);
        GridData layoutData = SWTUtil.createFillHorizontallyGridData();
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        base.setLayout(layout);
        base.setLayoutData(layoutData);
        
        // add dropdown selection boxes
        final Label fLabel = new Label(base, SWT.PUSH);
        fLabel.setText("Function");
        functionCombo = new Combo(base, SWT.READ_ONLY);
        functionCombo.setLayoutData(SWTUtil.createFillGridData());
        updateValidFunctions();
        
        functionCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                int index = ((Combo) arg0.getSource()).getSelectionIndex();
                selectfunction(index);
            }
        });
        
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
            }
        } else if (event.part == ModelPart.INPUT) {
            updateValidFunctions();
            if (model != null && model.getInputConfig() != null) {
                 // TODO: select correct function!
                }
        } else if (event.part == ModelPart.DATA_TYPE) {
            updateValidFunctions();
        }
    }
    
    /**
     * Creates the selected function.
     * @param index
     */
    private void selectfunction(int index) {
        if (validFunctions.length > index) {
            Microaggregatefunctions selected = validFunctions[index];
            switch (selected) {
            case ARITHMETIC_MEAN:
                selectedFunction = new ArithmeticMean();
                break;
            case GEOMETRIC_MEAN:
                selectedFunction = new GeometricMean();
                break;
            default:
                throw new IllegalArgumentException("Microaggregatefuntion not supported!");
            }
            
            functionCombo.select(index);
            setCurrentFunction();
        }
    }
    
    /**
     * Sets the current function to the model.
     */
    private void setCurrentFunction() {
        if ((model == null) || (model.getInputConfig() == null)) {
            return;
        }
        model.getInputConfig().setMicroaggregationFunction(attribute, selectedFunction);
    }
    
    /**
     * Updates the valid functions based on the datatype.
     */
    private void updateValidFunctions() {
        Microaggregatefunctions[] functions = Microaggregatefunctions.values();
        List<String> items = new ArrayList<String>();
        List<Microaggregatefunctions> validFunctions = new ArrayList<Microaggregatefunctions>();
        for (int i = 0; i < functions.length; i++) {
            Microaggregatefunctions function = functions[i];
            switch (function) {
            case ARITHMETIC_MEAN:
                if (new ArithmeticMean().getMinimalRequiredScale().compareTo(model.getInputDefinition().getDataType(attribute).getScaleOfMeasure()) <= 0) {
                    items.add(function.toString());
                    validFunctions.add(function);
                }
                break;
            case GEOMETRIC_MEAN:
                if (new GeometricMean().getMinimalRequiredScale().compareTo(model.getInputDefinition().getDataType(attribute).getScaleOfMeasure()) <= 0) {
                    items.add(function.toString());
                    validFunctions.add(function);
                }
                break;
            default:
                throw new IllegalArgumentException("Microaggregation function not supported!");
            }
        }
        
        functionCombo.setItems(items.toArray(new String[items.size()]));
        this.validFunctions = validFunctions.toArray(new Microaggregatefunctions[validFunctions.size()]);
        
        if (items.size() > 0) {
            microaggregationButton.setEnabled(true);
            functionCombo.setEnabled(true);
            selectfunction(0);
        } else {
            functionCombo.setItems(new String[] { "No valid microaggregation function for datatype" });
            functionCombo.select(0);
            functionCombo.setEnabled(false);
            microaggregationButton.setEnabled(false);
        }
    }
    
}
