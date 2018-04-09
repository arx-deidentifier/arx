package org.deidentifier.arx.gui.view.impl.utility;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelConfiguration;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContextVisualization;

public class AnalysisContextR implements AnalysisContextVisualization {

    /** Context information. */
    public DataHandle      input   = null;

    /** Context information */
    public DataHandle      output  = null;

    /** Context information. */
    public Model           model   = null;

    /**
     * Creates a new context from the given context.
     *
     * @param context
     */
    public AnalysisContextR(AnalysisContext context){
        this.model = context.getModel();
        this.input = getInput();
        this.output = getOutput();
    }

    /**
     * Returns the input handle
     * 
     * @return
     */
    private DataHandle getInput() {

        // Prepare
        DataHandle handle = null;
        ModelConfiguration config = null;

        // Check
        if (model == null) return null;

        // If output available
        if (model.getOutputConfig() != null && model.getOutputConfig().getInput() != null &&
            model.getOutput() != null) {
            config = model.getOutputConfig();
        } else {
            config = model.getInputConfig();
        }
        if (config.getInput() == null) return null;
        handle = config.getInput().getHandle();

        // If subset view enabled
        if (model.getViewConfig().isSubset() && model.getOutputConfig() != null &&
            model.getOutputConfig().getConfig() != null && handle != null) {
            handle = handle.getView();
        }

        // Return
        return handle;
    }
    
    /**
     * Returns the output handle
     * @return
     */
    private DataHandle getOutput() {

        // Prepare
        DataHandle handle = null;

        // Check
        if (model == null) return null;

        // Get output
        handle = model.getOutput();

        // If subset view enabled
        if (model.getViewConfig().isSubset() && model.getOutputConfig() != null &&
            model.getOutputConfig().getConfig() != null && handle != null) {
            handle = handle.getView();
        }

        // Return handle
        return handle;
    }

    @Override
    public boolean isAttributeSelected(String attribute) {
        return false;
    }

    /**
     * Is this a valid context.
     *
     * @return
     */
    public boolean isValid(){
        if (this.model == null) return false;
        else if (this.input == null) return false;
        else return true;
    }
}