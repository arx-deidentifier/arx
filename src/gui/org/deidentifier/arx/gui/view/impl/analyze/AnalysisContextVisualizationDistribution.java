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
public class AnalysisContextVisualizationDistribution implements AnalysisContextVisualization{

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
    public AnalysisContextVisualizationDistribution(AnalysisContext context){
        if (context.getContext()==null) return;
        this.handle = context.getContext().handle;
        this.context = context;
        if (handle == null) return;
        this.model = context.getModel();
        if (model==null) return;
        if (model.getAttributePair() == null) return;
        this.attribute = context.getModel().getSelectedAttribute();
        this.dataType = handle.getDefinition().getDataType(attribute);
        this.attributeType = handle.getDefinition().getAttributeType(attribute); 
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

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.analyze.AnalysisContextVisualization#isAttributeSelected(java.lang.String)
     */
    @Override
    public boolean isAttributeSelected(String attribute) {
        if (attribute==null) return false;
        else return attribute.equals(this.attribute);
    }
}
