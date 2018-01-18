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

package org.deidentifier.arx.gui.view.impl.utility;

import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContextVisualization;

/**
 * The current context.
 *
 * @author Fabian Prasser
 */
public class AnalysisContextDistribution implements AnalysisContextVisualization{

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
    public AnalysisContextDistribution(AnalysisContext context){
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
