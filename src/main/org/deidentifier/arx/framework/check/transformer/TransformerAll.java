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

package org.deidentifier.arx.framework.check.transformer;

import org.deidentifier.arx.ARXConfiguration.ARXConfigurationInternal;
import org.deidentifier.arx.framework.check.distribution.IntArrayDictionary;
import org.deidentifier.arx.framework.data.DataMatrix;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;

/**
 * The class TransformerAll.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class TransformerAll extends AbstractTransformer {

    /**
     * Instantiates a new transformer.
     *
     * @param data the data
     * @param hierarchies the hierarchies
     * @param dataAnalyzed
     * @param dataAnalyzedNumberOfColumns
     * @param dictionarySensValue
     * @param dictionarySensFreq
     * @param config
     */
    public TransformerAll(final DataMatrix data,
                          final GeneralizationHierarchy[] hierarchies,
                          final DataMatrix dataAnalyzed,
                          final int dataAnalyzedNumberOfColumns,
                          final IntArrayDictionary dictionarySensValue,
                          final IntArrayDictionary dictionarySensFreq,
                          final ARXConfigurationInternal config) {
        super(data, hierarchies, dataAnalyzed, dataAnalyzedNumberOfColumns, dictionarySensValue, dictionarySensFreq, config);
    }

    @Override
    protected void processAll() {
        
        int[][][] mHierarchies = new int[dimensions][][];
        for (int i = 0; i < dimensions; i++) {
            mHierarchies[i] = hierarchies[i].getArray();
        }
        
        for (int i = startIndex; i < stopIndex; i++) {

            // Transform
            buffer.setRow(i);
            data.setRow(i);
            for (int d = 0; d < dimensions; d++) {
                buffer.setValueAtColumn(d, mHierarchies[d][data.getValueAtColumn(d)][generalization[d]]);
            }

            // Call
            delegate.callAll(i, i);
        }
    }

    @Override
    protected void processGroupify() {

        int[][][] mHierarchies = new int[dimensions][][];
        for (int i = 0; i < dimensions; i++) {
            mHierarchies[i] = hierarchies[i].getArray();
        }
        
        while (element != null) {

            // Transform
            buffer.setRow(element.representative);
            data.setRow(element.representative);

            for (int d = 0; d < dimensions; d++) {
                buffer.setValueAtColumn(d, mHierarchies[d][data.getValueAtColumn(d)][generalization[d]]);
            }

            // Call
            delegate.callGroupify(element.representative, element);

            // Next element
            element = element.nextOrdered;
        }
    }

    @Override
    protected void processSnapshot() {

        int[][][] mHierarchies = new int[dimensions][][];
        for (int i = 0; i < dimensions; i++) {
            mHierarchies[i] = hierarchies[i].getArray();
        }
        
        startIndex *= ssStepWidth;
        stopIndex *= ssStepWidth;

        for (int i = startIndex; i < stopIndex; i += ssStepWidth) {
            buffer.setRow(snapshot[i]);
            data.setRow(snapshot[i]);
            for (int d = 0; d < dimensions; d++) {
                buffer.setValueAtColumn(d, mHierarchies[d][data.getValueAtColumn(d)][generalization[d]]);
            }

            // Call
            delegate.callSnapshot(snapshot[i], snapshot, i);
        }
    }
}
