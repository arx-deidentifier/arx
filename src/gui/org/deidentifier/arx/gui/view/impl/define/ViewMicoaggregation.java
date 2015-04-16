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
    
    /** Determines if the view should be enabled */
    private boolean                   enabled = false;
    /** The Comobox */
    private Combo                     functionCombo;
    private Button microaggregationButton;
    
    /**
     * All availbale microaggregation fucntions
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
    
    /**
     * Upates the valid funcitons based on the datatype.
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
        
        this.functionCombo.setItems(items.toArray(new String[items.size()]));
        this.validFunctions = validFunctions.toArray(new Microaggregatefunctions[validFunctions.size()]);
        
        if (items.size() > 0) {
            this.enabled = true;
            this.microaggregationButton.setEnabled(true);
            this.functionCombo.setEnabled(true);
            selectfunction(0);
        } else {
            this.enabled = false;
            this.functionCombo.setItems(new String[] { "No valid microaggregation function for datatype" });
            this.functionCombo.select(0);
            this.functionCombo.setEnabled(false);
            this.microaggregationButton.setEnabled(false);
            removeCurrentFunction();
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
                this.selectedFunction = new ArithmeticMean();
                this.functionCombo.select(index);
                setCurrentFunction();
                break;
            case GEOMETRIC_MEAN:
                this.selectedFunction = new GeometricMean();
                this.functionCombo.select(index);
                setCurrentFunction();
                break;
            default:
                throw new IllegalArgumentException("Microaggregatefuntion not supported!");
            }
        }
    }
    
    @Override
    public void dispose() {
        controller.removeListener(this);
        if (!base.isDisposed()) {
            base.dispose();
        }
    }
    
    /**
     * Removes the function from the model.
     */
    public void removeCurrentFunction() {
        if ((model == null) || (model.getInputConfig() == null)) {
            return;
        }
        model.getInputConfig().removeMicroaggregationFunction(attribute);
    }
    
    @Override
    public void reset() {
        if (!base.isDisposed()) {
            base.redraw();
        }
    }
    
    /**
     * Sets the current function to the model.
     */
    public void setCurrentFunction() {
        if ((model == null) || (model.getInputConfig() == null)) {
            return;
        }
        model.getInputConfig().setMicroaggregationFunction(attribute, selectedFunction);
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
        } else if (event.part == ModelPart.DATA_TYPE) {
            updateValidFunctions();
        }
    }
    
    /**
     * Returns if functions are available for the currently selected datatype.
     * 
     * @return
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Deselects the button and removes the current function from the model.
     */
    public void deselect() {
        removeCurrentFunction();
    }
    
    /**
     * Selects the button and sets the current function.
     */
    public void select() {
        if (enabled) {
            setCurrentFunction();
        }
    }
    
}
