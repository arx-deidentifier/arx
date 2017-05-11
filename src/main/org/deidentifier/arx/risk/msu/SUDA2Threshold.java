/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
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
package org.deidentifier.arx.risk.msu;

import java.util.Set;

/**
 * The result of executing SUDA2
 * 
 * @author Fabian Prasser
 */
public class SUDA2Threshold extends SUDA2Result {
    
    /**
     * Exception thrown in thresholds are not met
     * @author Fabian Prasser
     *
     */
    public static final class SUDA2ThresholdException extends RuntimeException {
        private static final long serialVersionUID = 2705587022766447851L;        
    }

    @Override
    void registerMSU(Set<SUDA2Item> set) {
        throw new SUDA2ThresholdException();
    }

    @Override
    void registerMSU(SUDA2Item item, SUDA2ItemSet set) {
        throw new SUDA2ThresholdException();
    }

    @Override
    void registerMSU(SUDA2ItemSet set) {
        throw new SUDA2ThresholdException();
    }
}
