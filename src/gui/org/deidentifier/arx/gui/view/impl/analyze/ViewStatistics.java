package org.deidentifier.arx.gui.view.impl.analyze;

import java.awt.Panel;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.gui.model.Model;
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
        if (model != null){
            if (target == ModelPart.INPUT){
                DataHandle handle = model.getInputConfig().getInput().getHandle();
                if (model.getViewConfig().isSubset() && 
                    model.getOutputConfig() != null &&
                    model.getOutputConfig().getConfig() != null &&
                    handle != null) {
                    handle = handle.getView();
                }
                return handle;
            } else {
                DataHandle handle = model.getOutput();
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
}
