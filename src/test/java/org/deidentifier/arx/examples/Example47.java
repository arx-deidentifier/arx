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

package org.deidentifier.arx.examples;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.risk.RiskEstimateBuilder;
import org.deidentifier.arx.risk.RiskModelAttributes;

/**
 * Example for evaluating distinction and separation of attributes as described in
 * R. Motwani et al. "Efficient algorithms for masking and finding quasi-identifiers"
 * Proc. VLDB Conf., 2007.
 * 
 * @author Maximilian Zitzmann
 * @author Fabian Prasser
 */
public class Example47 extends Example {

    /**
     * Entry point.
     *
     * @param args the arguments
     */
    public static void main(String[] args) throws IOException {

        Data data = loadData();

        // Flag every attribute as quasi identifier
        for (int i = 0; i < data.getHandle().getNumColumns(); i++) {
            data.getDefinition().setAttributeType(data.getHandle().getAttributeName(i), AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
        }

        // Perform risk analysis
        System.out.println("\n - Input data");
        print(data.getHandle());

        System.out.println("\n - Quasi-identifiers with values (in percent):");
        analyzeAttributes(data.getHandle());
    }

    /**
     * Calculate Alpha Distinction and Separation
     *
     * @param handle the data handle
     */
    private static void analyzeAttributes(DataHandle handle) {
        ARXPopulationModel populationmodel = ARXPopulationModel.create(ARXPopulationModel.Region.USA);
        RiskEstimateBuilder builder = handle.getRiskEstimator(populationmodel);
        RiskModelAttributes riskmodel = builder.getAttributeRisks();

        // output
        printPrettyTable(riskmodel.getAttributeRisks());
    }

    private static Data loadData() {
        // Define data
        Data.DefaultData data = Data.create();
        data.add("age", "sex", "state");
        data.add("20", "Female", "CA");
        data.add("30", "Female", "CA");
        data.add("40", "Female", "TX");
        data.add("20", "Male", "NY");
        data.add("40", "Male", "CA");
        data.add("53", "Male", "CA");
        data.add("76", "Male", "EU");
        data.add("40", "Female", "AS");
        data.add("32", "Female", "CA");
        data.add("88", "Male", "CA");
        data.add("48", "Female", "AS");
        data.add("76", "Male", "UU");
        return data;
    }

    /**
     * Helper that prints a table
     * @param quasiIdentifiers
     */
    private static void printPrettyTable(RiskModelAttributes.QuasiIdentifierRisk[] quasiIdentifiers) {
        
        // get char count of longest quasi-identifier
        int charCountLongestQi = quasiIdentifiers[quasiIdentifiers.length-1].getIdentifier().toString().length();

        // make sure that there is enough space for the table header strings
        charCountLongestQi = Math.max(charCountLongestQi, 12);

        // calculate space needed
        String leftAlignFormat = "| %-" + charCountLongestQi + "s | %13.2f | %12.2f |%n";

        // add 2 spaces that are in the string above on the left and right side of the first pattern
        charCountLongestQi += 2;

        // subtract the char count of the column header string to calculate
        // how many spaces we need for filling up to the right columnborder
        int spacesAfterColumHeader = charCountLongestQi - 12;

        System.out.format("+" + StringUtils.repeat("-", charCountLongestQi) + "+---------------+--------------+%n");
        System.out.format("| Identifier " + StringUtils.repeat(" ", spacesAfterColumHeader) + "|   Distinction |   Separation |%n");
        System.out.format("+" + StringUtils.repeat("-", charCountLongestQi) + "+---------------+--------------+%n");
        for (RiskModelAttributes.QuasiIdentifierRisk quasiIdentifier : quasiIdentifiers) {
            // print every Quasi-Identifier
            System.out.format(leftAlignFormat, quasiIdentifier.getIdentifier(), quasiIdentifier.getDistinction() * 100, quasiIdentifier.getSeparation() * 100);
        }
        System.out.format("+" + StringUtils.repeat("-", charCountLongestQi) + "+---------------+--------------+%n");
    }
}
