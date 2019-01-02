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

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContextVisualization;

/**
 * The current context.
 *
 * @author Fabian Prasser
 */
public class AnalysisContextQuality implements AnalysisContextVisualization{
    
    /** Context information. */
    public DataHandle    handle         = null;
    
    /** Context information. */
    public Model         model          = null;
    
    /**
     * Creates a new context from the given context.
     *
     * @param context
     */
    public AnalysisContextQuality(AnalysisContext context){
        if (context.getData()==null) return;
        this.handle = context.getData().handle;
        if (handle == null) return;
        this.model = context.getModel();
        if (model==null) return; 
    }
    
    @Override
    public boolean isAttributeSelected(String attribute) {
        if (attribute==null) return false;
        else return true;
    }

    /**
     * Is this a valid context.
     *
     * @return
     */
    public boolean isValid(){
        if (this.handle == null) return false;
        else if (this.model == null) return false;
        else return true;
    }
}
