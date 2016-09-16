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

package org.deidentifier.arx.test;

import org.apache.commons.lang.StringUtils;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.risk.RiskEstimateBuilder;
import org.deidentifier.arx.risk.RiskModelAttributes;
import org.deidentifier.arx.risk.RiskModelQI;
import org.junit.Test;

import java.text.DecimalFormat;

import static org.junit.Assert.assertTrue;

/**
 * Test for QuasiIdentifiers
 *
 * @author Max Zitzmann
 */
public class TestRiskQuasiIdentifiers {

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

        // calculated by hand
        ResultSet[] results = new ResultSet[]{
                new ResultSet("[sex]", 0.4, 0.6),
                new ResultSet("[state]", 0.6, 0.7),
                new ResultSet("[age]", 0.6, 0.8),
                new ResultSet("[sex, state]", 0.8, 0.9),
                new ResultSet("[age, state]", 1.0, 1.0),
                new ResultSet("[sex, age]", 1.0, 1.0),
                new ResultSet("[sex, age, state]", 1.0, 1.0),
        };

        // flag every identifier as quasi identifier
        for (int i = 0; i < data.getHandle().getNumColumns(); i++) {
            data.getDefinition().setAttributeType(data.getHandle().getAttributeName(i), AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
        }

        // perform calculation
        RiskEstimateBuilder builder = data.getHandle().getRiskEstimator(null);
        RiskModelAttributes riskmodel = builder.getAttributeRisks();
        RiskModelAttributes.QuasiIdentifierRisk risks[] = riskmodel.getAttributeRisks();

        for (int i = 0; i < risks.length; i++) {
            assertTrue("Identifier should be: " + results[i].identifier + "; Actually is: " + risks[i].getIdentifier(), results[i].identifier.equals(risks[i].getIdentifier().toString()));
            assertTrue("Distinction should be: " + results[i].calculatedDistinction + "; Actually is: " + risks[i].getDistinction(), results[i].calculatedDistinction == risks[i].getDistinction());
            assertTrue("Separation should be " + results[i].calculatedSeparation + "; Actually is: " + risks[i].getSeparation(), results[i].calculatedSeparation == risks[i].getSeparation());
        }
    }

    @Test
    public void compareWithOldMethod() {
        DataProvider dataProvider = new DataProvider();
        dataProvider.createDataDefinition();
        Data data = dataProvider.getData();

        RiskEstimateBuilder builder = data.getHandle().getRiskEstimator(null);
        RiskModelAttributes riskModel = builder.getAttributeRisks();
        RiskModelAttributes.QuasiIdentifierRisk risksNewMethod[] = riskModel.getAttributeRisks();

        for (RiskModelAttributes.QuasiIdentifierRisk riskNewMethod : risksNewMethod) {
            RiskModelQI riskModelQI = new RiskModelQI(data.getHandle(), riskNewMethod.getIdentifier());
            double newValue = truncate(riskNewMethod.getDistinction(), 7);
            double oldValue = truncate(riskModelQI.getAlphaDistinct(), 7);
            assertTrue("Mismatch: \nNewMethod:\t" + newValue + "\nOldMethod:\t" + oldValue, oldValue == newValue);
        }
    }

    private static double truncate(double value, int places) {
        double multiplier = Math.pow(10, places);
        return Math.floor(multiplier * value) / multiplier;
    }

    private class ResultSet {
        String identifier;
        double calculatedDistinction;
        double calculatedSeparation;

        ResultSet(String identifier, double calculatedDistinction, double calculatedSeparation) {
            this.identifier = identifier;
            this.calculatedDistinction = calculatedDistinction;
            this.calculatedSeparation = calculatedSeparation;
        }

    }
}
