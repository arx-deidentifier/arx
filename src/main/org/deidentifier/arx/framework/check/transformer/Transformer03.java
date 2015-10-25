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

package org.deidentifier.arx.framework.check.transformer;

import org.deidentifier.arx.ARXConfiguration.ARXConfigurationInternal;
import org.deidentifier.arx.framework.check.distribution.IntArrayDictionary;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;

/**
 * The class Transformer03.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Transformer03 extends AbstractTransformer {

    /**
     * Instantiates a new transformer.
     *
     * @param data the data
     * @param buffer the buffer
     * @param hierarchies the hierarchies
     * @param otherValues
     * @param dictionarySensValue
     * @param dictionarySensFreq
     * @param config
     */
    public Transformer03(final int[][] data,
                         final int[][] buffer,
                         final GeneralizationHierarchy[] hierarchies,
                         final int[][] otherValues,
                         final IntArrayDictionary dictionarySensValue,
                         final IntArrayDictionary dictionarySensFreq,
                         final ARXConfigurationInternal config) {
        super(data, buffer, hierarchies, otherValues, dictionarySensValue, dictionarySensFreq, config);
    }

    @Override
    protected void processAll() {
        for (int i = startIndex; i < stopIndex; i++) {
            intuple = data[i];
            outtuple = buffer[i];
            outtuple[outindex0] = idindex0[intuple[index0]][generalizationindex0];
            outtuple[outindex1] = idindex1[intuple[index1]][generalizationindex1];
            outtuple[outindex2] = idindex2[intuple[index2]][generalizationindex2];

            // Call
            delegate.callAll(outtuple, i);
        }
    }

    @Override
    protected void processGroupify() {

        int elements = stopIndex - startIndex;     
        HashGroupifyEntry element = getElement(startIndex);
        while (element != null && elements > 0) {

            intuple = data[element.getRepresentative()];
            outtuple = buffer[element.getRepresentative()];
            outtuple[outindex0] = idindex0[intuple[index0]][generalizationindex0];
            outtuple[outindex1] = idindex1[intuple[index1]][generalizationindex1];
            outtuple[outindex2] = idindex2[intuple[index2]][generalizationindex2];

            // Call
            delegate.callGroupify(outtuple, element);

            // Next element
            elements--;
            element = element.getNextOrdered();
        }
    }

    @Override
    protected void processSnapshot() {

        startIndex *= ssStepWidth;
        stopIndex *= ssStepWidth;

        for (int i = startIndex; i < stopIndex; i += ssStepWidth) {
            intuple = data[snapshot[i]];
            outtuple = buffer[snapshot[i]];
            outtuple[outindex0] = idindex0[intuple[index0]][generalizationindex0];
            outtuple[outindex1] = idindex1[intuple[index1]][generalizationindex1];
            outtuple[outindex2] = idindex2[intuple[index2]][generalizationindex2];

            // Call
            delegate.callSnapshot(outtuple, snapshot, i);
        }
    }
}
