/*
 * ARX Data Anonymization Tool
 * Copyright 2012 - 2024 Fabian Prasser and contributors
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

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;

/**
 * Table style wrapper
 * @author Fabian Prasser
 *
 */
public abstract class CTStyleConfiguration extends AbstractRegistryConfiguration implements CTComponent {

    /**  Configuration */
    private final CTConfiguration config;
    
    /**
     * Creates a new instance
     * @param config
     */
    public CTStyleConfiguration(CTConfiguration config){
        this.config = config;
    }
    
    @Override
    public CTConfiguration getConfig() {
        return config;
    }
}
