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
package org.deidentifier.arx.aggregates.classification;

import java.util.HashMap;
import java.util.Map;

import org.deidentifier.arx.DataHandleInternal;
import org.deidentifier.arx.common.WrappedBoolean;

/**
 * Implements a classifier
 * @author Fabian Prasser
 */
public class MultiClassZeroR extends ClassificationMethod {

    /** Counts */
    private final Map<Integer, Integer>           counts = new HashMap<>();
    /** Result */
    private MultiClassZeroRClassificationResult   result = null;
    /** Index */
    private final ClassificationDataSpecification specification;
    
    /**
     * Creates a new instance
     * @param interrupt
     * @param specification
     */
    public MultiClassZeroR(WrappedBoolean interrupt, ClassificationDataSpecification specification) {
        super(interrupt);
        this.specification = specification;
    }

    @Override
    public ClassificationResult classify(DataHandleInternal handle, int row) {
        return result;
    }

    @Override
    public void close() {
        result = new MultiClassZeroRClassificationResult(counts, specification.classMap);
        counts.clear();
    }

    @Override
    public void train(DataHandleInternal features, DataHandleInternal clazz, int row) {
        Integer key = specification.classMap.get(clazz.getValue(row, specification.classIndex, true));
        Integer count = counts.get(key);
        count = count == null ? 1 : count + 1;
        counts.put(key, count);
        result = null;
    }
}
