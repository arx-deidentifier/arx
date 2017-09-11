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
     * @param otherValues
     * @param dictionarySensValue
     * @param dictionarySensFreq
     * @param config
     */
    public TransformerAll(final DataMatrix data,
                          final GeneralizationHierarchy[] hierarchies,
                          final DataMatrix otherValues,
                          final IntArrayDictionary dictionarySensValue,
                          final IntArrayDictionary dictionarySensFreq,
                          final ARXConfigurationInternal config) {
        super(data, hierarchies, otherValues, dictionarySensValue, dictionarySensFreq, config);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.framework.check.transformer.AbstractTransformer
     * #walkAll()
     */
    @Override
    protected void processAll() {
        for (int i = startIndex; i < stopIndex; i++) {

            // Transform
            buffer.setRow(i);
            data.setRow(i);
            for (int d = 0; d < dimensions; d++) {
                buffer.setValueAtColumn(d, map[d][data.getValueAtColumn(d)][generalization[d]]);
            }

            // Call
            delegate.callAll(i, i);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.framework.check.transformer.AbstractTransformer
     * #walkGroupify ()
     */
    @Override
    protected void processGroupify() {
        while (element != null) {

            // Transform
            buffer.setRow(element.representative);
            data.setRow(element.representative);

            for (int d = 0; d < dimensions; d++) {
                buffer.setValueAtColumn(d, map[d][data.getValueAtColumn(d)][generalization[d]]);
            }

            // Call
            delegate.callGroupify(element.representative, element);

            // Next element
            element = element.nextOrdered;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.framework.check.transformer.AbstractTransformer
     * #walkSnapshot ()
     */
    @Override
    protected void processSnapshot() {

        startIndex *= ssStepWidth;
        stopIndex *= ssStepWidth;

        for (int i = startIndex; i < stopIndex; i += ssStepWidth) {
            buffer.setRow(snapshot[i]);
            data.setRow(snapshot[i]);
            for (int d = 0; d < dimensions; d++) {
                buffer.setValueAtColumn(d, map[d][data.getValueAtColumn(d)][generalization[d]]);
            }

            // Call
            delegate.callSnapshot(snapshot[i], snapshot, i);
        }
    }
}
