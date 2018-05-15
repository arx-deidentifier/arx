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

package org.deidentifier.arx.test;

import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.risk.RiskEstimateBuilder;
import org.deidentifier.arx.risk.RiskModelAttributes;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Test calculations of alpha-distinction and alpha-separation.
 *
 * @author Maximilan Zitzmann
 * @author Fabian Prasser
 */
public class TestRiskQuasiIdentifiers {

    /**
     * Private helper class
     * 
     * @author Maximilian Zitzmann
     * @author Fabian Prasser
     */
    private class ResultSet {
        
        /** Identifier*/
        private final String identifier;
        /** Distinction*/
        private final double distinction;
        /** Separation*/
        private final double separation;

        /**
         * Creates a new instance
         * @param identifier
         * @param distinction
         * @param separation
         */
        private ResultSet(String identifier, double distinction, double separation) {
            this.identifier = identifier;
            this.distinction = distinction;
            this.separation = separation;
        }
    }

    @Test
    public void testWithDefinedDataSet() {
        
        // Define data
        Data.DefaultData data = Data.create();
        data.add("age", "sex", "state");
        data.add("20", "Female", "CA");
        data.add("30", "Female", "CA");
        data.add("40", "Female", "TX");
        data.add("20", "Male", "NY");
        data.add("40", "Male", "CA");

        // Calculated by hand
        ResultSet[] expectedResults = new ResultSet[]{
                new ResultSet("[sex]", 0.4, 0.6),
                new ResultSet("[state]", 0.6, 0.7),
                new ResultSet("[age]", 0.6, 0.8),
                new ResultSet("[sex, state]", 0.8, 0.9),
                new ResultSet("[age, sex]", 1.0, 1.0),
                new ResultSet("[age, state]", 1.0, 1.0),
                new ResultSet("[age, sex, state]", 1.0, 1.0),
        };

        // Flag every identifier as quasi identifier
        for (int i = 0; i < data.getHandle().getNumColumns(); i++) {
            data.getDefinition().setAttributeType(data.getHandle().getAttributeName(i), AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
        }

        // Perform calculation
        RiskEstimateBuilder builder = data.getHandle().getRiskEstimator(null);
        RiskModelAttributes riskmodel = builder.getAttributeRisks();
        RiskModelAttributes.QuasiIdentifierRisk risks[] = riskmodel.getAttributeRisks();

        // Check length
        assertTrue("Number of potential quasi-identifiers expected: " + risks.length, expectedResults.length == risks.length);
        
        // Check each entry
        for (int i = 0; i < risks.length; i++) {
            assertTrue("Identifier expected: " + expectedResults[i].identifier + "; got: " + risks[i].getIdentifier(), expectedResults[i].identifier.equals(risks[i].getIdentifier().toString()));
            assertTrue("Distinction expected: " + expectedResults[i].distinction + "; got: " + risks[i].getDistinction(), expectedResults[i].distinction == risks[i].getDistinction());
            assertTrue("Separation expected: " + expectedResults[i].separation + "; got: " + risks[i].getSeparation(), expectedResults[i].separation == risks[i].getSeparation());
        }
    }
}
