package org.deidentifier.arx.gui.view.impl.utility;

import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContextVisualization;

public class AnalysisContextR implements AnalysisContextVisualization{


    /** Context information. */
    public String        attribute     = null;
    
    /** Context information. */
    public DataType<?>   dataType      = null;
    
    /** Context information. */
    public AttributeType attributeType = null;
    
    /** Context information. */
    public DataHandle    handle         = null;
    
    /** Context information. */
    public Model         model          = null;
    
    /** Context information. */
    public AnalysisContext context       = null;
    
    /**
     * Creates a new context from the given context.
     *
     * @param context
     */
    public AnalysisContextR(AnalysisContext context){
        if (context.getData()==null) return;
        this.handle = context.getData().handle;
        this.context = context;
        if (handle == null) return;
        this.model = context.getModel();
        if (model==null) return;
        if (model.getAttributePair() == null) return;
        this.attribute = context.getModel().getSelectedAttribute();
        this.dataType = handle.getDefinition().getDataType(attribute);
        this.attributeType = handle.getDefinition().getAttributeType(attribute); 
    }
    
    @Override
    public boolean isAttributeSelected(String attribute) {
        if (attribute==null) return false;
        else return attribute.equals(this.attribute);
    }

    /**
     * Is this a valid context.
     *
     * @return
     */
    public boolean isValid(){
        if (this.handle == null) return false;
        else if (this.model == null) return false;
        else if (this.attribute == null) return false;
        else if (this.dataType == null) return false;
        else if (this.attributeType == null) return false;
        else return true;
    }
}

