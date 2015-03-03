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

package org.deidentifier.arx.gui.view.impl.analyze;

import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.gui.model.Model;


/**
 * The current context.
 *
 * @author Fabian Prasser
 */
public class AnalysisContextVisualizationContingency implements AnalysisContextVisualization {

    /** Context information. */
    public String        attribute1     = null;
    
    /** Context information. */
    public String        attribute2     = null;
    
    /** Context information. */
    public DataType<?>   dataType1      = null;
    
    /** Context information. */
    public DataType<?>   dataType2      = null;
    
    /** Context information. */
    public AttributeType attributeType1 = null;
    
    /** Context information. */
    public AttributeType attributeType2 = null;
    
    /** Context information. */
    public DataHandle    handle         = null;
    
    /** Context information. */
    public Model         model          = null;

    /**
     * Creates a new context from the given context.
     *
     * @param context
     */
    public AnalysisContextVisualizationContingency(AnalysisContext context){
        if (context.getContext()==null) return;
        this.handle = context.getContext().handle;
        if (handle == null) return;
        this.model = context.getModel();
        if (model==null) return;
        if (model.getAttributePair() == null) return;
        this.attribute1 = context.getModel().getAttributePair()[0];
        this.attribute2 = context.getModel().getAttributePair()[1];
        this.dataType1 = handle.getDefinition().getDataType(attribute1);
        this.dataType2 = handle.getDefinition().getDataType(attribute2);
        this.attributeType1 = handle.getDefinition().getAttributeType(attribute1);
        this.attributeType2 = handle.getDefinition().getAttributeType(attribute2); 
    }
    
    /**
     * Is this a valid context.
     *
     * @return
     */
    public boolean isValid(){
        if (this.handle == null) return false;
        else if (this.model == null) return false;
        else if (this.attribute1 == null) return false;
        else if (this.attribute2 == null) return false;
        else if (this.dataType1 == null) return false;
        else if (this.dataType2 == null) return false;
        else if (this.attributeType1 == null) return false;
        else if (this.attributeType2 == null) return false;
        else return true;
    }
    
    /**
     * Is the provided attribute selected according to the config?.
     *
     * @param attribute
     * @return
     */
    public boolean isAttributeSelected(String attribute){
        if (attribute == null) return false;
        if (attribute.equals(attribute1)) return true;
        if (attribute.equals(attribute2)) return true;
        return false;
    }
}
