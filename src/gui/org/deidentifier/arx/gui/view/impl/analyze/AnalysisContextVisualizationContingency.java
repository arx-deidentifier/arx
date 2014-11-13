/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
