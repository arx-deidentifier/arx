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

package org.deidentifier.arx.gui.view.impl.common.table;

import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;

/**
 * 
 * @author Fabian Prasser
 *
 */
public abstract class CTLayer extends AbstractLayerTransform implements CTComponent {

    /**  TODO */
    private final CTConfiguration config;
    
    /**  TODO */
    private final CTContext context;
    
    /**
     * 
     *
     * @param config
     * @param context
     */
    public CTLayer(CTConfiguration config, CTContext context){
        this.config = config;
        this.context = context;
    }
    
    /**
     * 
     *
     * @param underlyingDataLayer
     * @param config
     * @param context
     */
    public CTLayer(IUniqueIndexLayer underlyingDataLayer, CTConfiguration config, CTContext context) {
        super(underlyingDataLayer);
        this.config = config;
        this.context = context;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.common.table.CTComponent#getConfig()
     */
    public CTConfiguration getConfig(){
        return config;
    }

    /**
     * 
     *
     * @return
     */
    public CTContext getContext() {
        return context;
    }
}
