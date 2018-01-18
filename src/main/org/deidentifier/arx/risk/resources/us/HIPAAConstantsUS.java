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
package org.deidentifier.arx.risk.resources.us;

import java.io.InputStream;

import org.deidentifier.arx.risk.HIPAAConstants;

/**
 * Utility class providing access to important constants for finding HIPAA identifiers.
 * 
 * @author Fabian Prasser
 */
public class HIPAAConstantsUS extends HIPAAConstants{
    
    /**
     * Returns constants for the US population
     */
    protected InputStream getInputStream(String file) {
        return HIPAAConstantsUS.class.getResourceAsStream(file);
    }
}
