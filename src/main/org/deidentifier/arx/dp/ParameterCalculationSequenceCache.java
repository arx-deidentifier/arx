/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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
package org.deidentifier.arx.dp;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implements a cache for enumerated sequences of type T
 * 
 * @author Raffael Bild
 * @author Fabian Prasser
 */
public class ParameterCalculationSequenceCache<T> extends LinkedHashMap<Integer, T> {

    /** SVUID */
    private static final long serialVersionUID = -6330943089566281435L;
    
    /** Default capacity */
    private static final int defaultCapacity = 10000;
    
    /** The capacity */
    private final int capacity;
    
    /** Constructor */
    public ParameterCalculationSequenceCache() {
        this(defaultCapacity);
    }
    
    /** Constructor */
    public ParameterCalculationSequenceCache(int capacity) {
        super(capacity);
        this.capacity = capacity;
    }
    
    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer,T> eldest) {
        return size() > capacity;
    }
}
