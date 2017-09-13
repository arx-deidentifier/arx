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
 * The class Transformer14.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Transformer14 extends AbstractTransformer {

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
    public Transformer14(final DataMatrix data,
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
            buffer.setValueAtColumn(outindex0, idindex0[data.getValueAtColumn(index0)][generalizationindex0]);
            buffer.setValueAtColumn(outindex1, idindex1[data.getValueAtColumn(index1)][generalizationindex1]);
            buffer.setValueAtColumn(outindex2, idindex2[data.getValueAtColumn(index2)][generalizationindex2]);
            buffer.setValueAtColumn(outindex3, idindex3[data.getValueAtColumn(index3)][generalizationindex3]);
            buffer.setValueAtColumn(outindex4, idindex4[data.getValueAtColumn(index4)][generalizationindex4]);
            buffer.setValueAtColumn(outindex5, idindex5[data.getValueAtColumn(index5)][generalizationindex5]);
            buffer.setValueAtColumn(outindex6, idindex6[data.getValueAtColumn(index6)][generalizationindex6]);
            buffer.setValueAtColumn(outindex7, idindex7[data.getValueAtColumn(index7)][generalizationindex7]);
            buffer.setValueAtColumn(outindex8, idindex8[data.getValueAtColumn(index8)][generalizationindex8]);
            buffer.setValueAtColumn(outindex9, idindex9[data.getValueAtColumn(index9)][generalizationindex9]);
            buffer.setValueAtColumn(outindex10, idindex10[data.getValueAtColumn(index10)][generalizationindex10]);
            buffer.setValueAtColumn(outindex11, idindex11[data.getValueAtColumn(index11)][generalizationindex11]);
            buffer.setValueAtColumn(outindex12, idindex12[data.getValueAtColumn(index12)][generalizationindex12]);
            buffer.setValueAtColumn(outindex13, idindex13[data.getValueAtColumn(index13)][generalizationindex13]);

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
            buffer.setValueAtColumn(outindex0, idindex0[data.getValueAtColumn(index0)][generalizationindex0]);
            buffer.setValueAtColumn(outindex1, idindex1[data.getValueAtColumn(index1)][generalizationindex1]);
            buffer.setValueAtColumn(outindex2, idindex2[data.getValueAtColumn(index2)][generalizationindex2]);
            buffer.setValueAtColumn(outindex3, idindex3[data.getValueAtColumn(index3)][generalizationindex3]);
            buffer.setValueAtColumn(outindex4, idindex4[data.getValueAtColumn(index4)][generalizationindex4]);
            buffer.setValueAtColumn(outindex5, idindex5[data.getValueAtColumn(index5)][generalizationindex5]);
            buffer.setValueAtColumn(outindex6, idindex6[data.getValueAtColumn(index6)][generalizationindex6]);
            buffer.setValueAtColumn(outindex7, idindex7[data.getValueAtColumn(index7)][generalizationindex7]);
            buffer.setValueAtColumn(outindex8, idindex8[data.getValueAtColumn(index8)][generalizationindex8]);
            buffer.setValueAtColumn(outindex9, idindex9[data.getValueAtColumn(index9)][generalizationindex9]);
            buffer.setValueAtColumn(outindex10, idindex10[data.getValueAtColumn(index10)][generalizationindex10]);
            buffer.setValueAtColumn(outindex11, idindex11[data.getValueAtColumn(index11)][generalizationindex11]);
            buffer.setValueAtColumn(outindex12, idindex12[data.getValueAtColumn(index12)][generalizationindex12]);
            buffer.setValueAtColumn(outindex13, idindex13[data.getValueAtColumn(index13)][generalizationindex13]);

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

            // Transform
            buffer.setRow(snapshot[i]);
            data.setRow(snapshot[i]);
            buffer.setValueAtColumn(outindex0, idindex0[data.getValueAtColumn(index0)][generalizationindex0]);
            buffer.setValueAtColumn(outindex1, idindex1[data.getValueAtColumn(index1)][generalizationindex1]);
            buffer.setValueAtColumn(outindex2, idindex2[data.getValueAtColumn(index2)][generalizationindex2]);
            buffer.setValueAtColumn(outindex3, idindex3[data.getValueAtColumn(index3)][generalizationindex3]);
            buffer.setValueAtColumn(outindex4, idindex4[data.getValueAtColumn(index4)][generalizationindex4]);
            buffer.setValueAtColumn(outindex5, idindex5[data.getValueAtColumn(index5)][generalizationindex5]);
            buffer.setValueAtColumn(outindex6, idindex6[data.getValueAtColumn(index6)][generalizationindex6]);
            buffer.setValueAtColumn(outindex7, idindex7[data.getValueAtColumn(index7)][generalizationindex7]);
            buffer.setValueAtColumn(outindex8, idindex8[data.getValueAtColumn(index8)][generalizationindex8]);
            buffer.setValueAtColumn(outindex9, idindex9[data.getValueAtColumn(index9)][generalizationindex9]);
            buffer.setValueAtColumn(outindex10, idindex10[data.getValueAtColumn(index10)][generalizationindex10]);
            buffer.setValueAtColumn(outindex11, idindex11[data.getValueAtColumn(index11)][generalizationindex11]);
            buffer.setValueAtColumn(outindex12, idindex12[data.getValueAtColumn(index12)][generalizationindex12]);
            buffer.setValueAtColumn(outindex13, idindex13[data.getValueAtColumn(index13)][generalizationindex13]);

            // Call
            delegate.callSnapshot(snapshot[i], snapshot, i);
        }
    }
}
