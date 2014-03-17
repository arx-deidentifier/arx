package org.deidentifier.arx.gui.view.impl.analyze;

import java.awt.Panel;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelConfiguration;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;

public abstract class ViewStatistics extends Panel{
    
    private static final long serialVersionUID = 5170104283288654748L;
    
    protected ModelPart target;
    protected Model model;

    /**
     * Returns the data handle
     * @return
     */
    protected DataHandle getHandle() {
        DataHandle handle = null;
        if (model != null){
            if (target == ModelPart.INPUT){
                handle = model.getInputConfig().getInput().getHandle();
                if (model.getViewConfig().isSubset() && 
                    model.getOutputConfig() != null &&
                    model.getOutputConfig().getConfig() != null &&
                    handle != null) {
                    handle = handle.getView();
                }
                return handle;
            } else {
                handle = model.getOutput();
                if (model.getViewConfig().isSubset() && 
                    model.getOutputConfig() != null &&
                    model.getOutputConfig().getConfig() != null &&
                    handle != null) {
                    handle = handle.getView();
                }
                return handle;
            }
        } else {
            return null;
        }
    }
    

    /**
     * Returns the config
     * @return
     */
    protected ModelConfiguration getConfig() {
        if (model != null){
            if (target == ModelPart.INPUT){
                return model.getInputConfig();
            } else {
                return model.getOutputConfig();
            }
        } else {
            return null;
        }
    }
}
