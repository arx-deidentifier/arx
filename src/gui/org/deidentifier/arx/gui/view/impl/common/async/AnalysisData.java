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

package org.deidentifier.arx.gui.view.impl.common.async;

import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.gui.model.ModelConfiguration;

/**
 * This class implements a context for visualizing statistics.
 *
 * @author Fabian Prasser
 */
public class AnalysisData{

    /** The according config. */
    public final ModelConfiguration config;

    /** The according handle. */
    public final DataHandle         handle;

    /** The according definition. */
    public final DataDefinition     definition;

    /**
     * Initial constructor.
     *
     * @param config
     * @param handle
     */
    AnalysisData(ModelConfiguration config, DataHandle handle, DataDefinition definition) {
        this.config = config;
        this.handle = handle;
        this.definition = definition;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        AnalysisData other = (AnalysisData) obj;
        if (config == null) {
            if (other.config != null) return false;
        } else if (!config.equals(other.config)) return false;
        if (handle == null) {
            if (other.handle != null) return false;
        } else if (!handle.equals(other.handle)) return false;
        return true;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((config == null) ? 0 : config.hashCode());
        result = prime * result + ((handle == null) ? 0 : handle.hashCode());
        return result;
    }
}