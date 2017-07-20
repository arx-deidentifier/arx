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
 * Abstract base class for results of the SUDA2 algorithm
 * 
 * @author Fabian Prasser
 */
public abstract class SUDA2Result {

    /**
     * Registers an MSU
     * @param set
     */
    abstract void registerMSU(Set<SUDA2Item> set);

    /**
     * Registers an MSU
     * @param item
     * @param set
     */
    abstract void registerMSU(SUDA2Item item, SUDA2ItemSet set);

    /**
     * Registers an MSU
     * @param set
     */
    abstract void registerMSU(SUDA2ItemSet set);

}
