/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2020 Fabian Prasser and contributors
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

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXLattice;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.ARXConfiguration.AnonymizationAlgorithm;
import org.deidentifier.arx.criteria.KAnonymity;

/**
 * This class implements an example of processing high-dimensional data
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Example60 extends Example {

    /**
     * Entry point.
     * 
     * @param args
     *            the arguments
     */
    public static void main(String[] args) throws IOException {
        
        // Create a dataset
        int attributeRepetition = 50;
        Data data = createData(attributeRepetition);
        
        // Run top-down search
        solve(data, AnonymizationAlgorithm.BEST_EFFORT_TOP_DOWN, true);
        
        // Run bottom-up search
        solve(data, AnonymizationAlgorithm.BEST_EFFORT_BOTTOM_UP, false);
        
        // Run genetic search
        solve(data, AnonymizationAlgorithm.BEST_EFFORT_GENETIC, false);
    }
    
    /**
     * Anonymize the dataset with k-Anonymity (k=2).
     * 
     * @param data
     * @param algorithm
     * @param printVerbose
     * @throws IOException
     */
    public static void solve(Data data, AnonymizationAlgorithm algorithm, boolean printVerbose) throws IOException {
     
        // Release
        data.getHandle().release();
        
        // Create an instance of the anonymizer
        ARXAnonymizer anonymizer = new ARXAnonymizer();

        // Create config
        ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new KAnonymity(2));
        config.setSuppressionLimit(1d);
        config.setHeuristicSearchStepLimit(10000);
        config.setAlgorithm(algorithm);
        ARXResult result = anonymizer.anonymize(data, config);
        
        // Obtain lattice
        ARXLattice lattice = result.getLattice();

        // Obtain optimal data representations
        DataHandle optimal = result.getOutput();

        if (printVerbose) {
            // Obtain top and bottom representation
            ARXNode topNode = lattice.getTop();
            ARXNode bottomNode = lattice.getBottom();
            DataHandle top = result.getOutput(topNode);
            DataHandle bottom = result.getOutput(bottomNode);
            
            // Print input
            System.out.println(String.format(" - Input data (Granularity: %,.3f):", data.getHandle().getStatistics().getQualityStatistics().getGranularity().getArithmeticMean()));
            printHandle(data.getHandle());

            // Print top and bottom
            System.out.println(String.format("\n - Top node data (Granularity: %,.3f):", top.getStatistics().getQualityStatistics().getGranularity().getArithmeticMean()));
            printHandle(top);

            System.out.println(String.format("\n - Bottom node data (Granularity: %,.3f):", bottom.getStatistics().getQualityStatistics().getGranularity().getArithmeticMean()));
            printHandle(bottom);
        }
        
        // Calculate granularity of optimal result
        double granularity = optimal.getStatistics().getQualityStatistics().getGranularity().getArithmeticMean();
        
        // Print optimal result
        System.out.println(String.format("\n - Optimal output data for %s (Granularity: %,.3f):", algorithm, granularity));
        printHandle(optimal);
    }
        
    /**
     * Creates a high-dimensional dataset.
     * 
     * @param attributeRepetition Defines how often the 3 base attributes (age, gender, zipcode) are repeated.
     * @return
     */
    public static Data createData(int attributeRepetition) {
        // Create a dataset with 3*attributeRepetition columns
        DefaultData data = Data.create();

        // Header
        String[] row = new String[3 * attributeRepetition];
        for (int i = 0; i < attributeRepetition; i++) {
            row[i * 3 + 0] = "age-" + i;
            row[i * 3 + 1] = "gender-" + i;
            row[i * 3 + 2] = "zipcode-" + i;
        }
        data.add(row);

        // Row 1
        row = new String[3 * attributeRepetition];
        for (int i = 0; i < attributeRepetition; i++) {
            row[i * 3 + 0] = "34";
            row[i * 3 + 1] = "male";
            row[i * 3 + 2] = "81667";
        }
        data.add(row);

        // Row 2
        row = new String[3 * attributeRepetition];
        for (int i = 0; i < attributeRepetition; i++) {
            row[i * 3 + 0] = "45";
            row[i * 3 + 1] = "female";
            row[i * 3 + 2] = "81675";
        }
        data.add(row);

        // Row 3
        row = new String[3 * attributeRepetition];
        for (int i = 0; i < attributeRepetition; i++) {
            row[i * 3 + 0] = "66";
            row[i * 3 + 1] = "male";
            row[i * 3 + 2] = "81925";
        }
        data.add(row);

        // Row 4
        row = new String[3 * attributeRepetition];
        for (int i = 0; i < attributeRepetition; i++) {
            row[i * 3 + 0] = "70";
            row[i * 3 + 1] = "female";
            row[i * 3 + 2] = "81931";
        }
        data.add(row);

        // Row 5
        row = new String[3 * attributeRepetition];
        for (int i = 0; i < attributeRepetition; i++) {
            row[i * 3 + 0] = "34";
            row[i * 3 + 1] = "female";
            row[i * 3 + 2] = "81931";
        }
        data.add(row);

        // Row 6
        row = new String[3 * attributeRepetition];
        for (int i = 0; i < attributeRepetition; i++) {
            row[i * 3 + 0] = "70";
            row[i * 3 + 1] = "male";
            row[i * 3 + 2] = "81931";
        }
        data.add(row);

        // Row 7
        row = new String[3 * attributeRepetition];
        for (int i = 0; i < attributeRepetition; i++) {
            row[i * 3 + 0] = "45";
            row[i * 3 + 1] = "male";
            row[i * 3 + 2] = "81931";
        }
        data.add(row);

        // Define hierarchies
        DefaultHierarchy age = Hierarchy.create();
        age.add("34", "<50", "*");
        age.add("45", "<50", "*");
        age.add("66", ">=50", "*");
        age.add("70", ">=50", "*");

        DefaultHierarchy gender = Hierarchy.create();
        gender.add("male", "*");
        gender.add("female", "*");

        // Only excerpts for readability
        DefaultHierarchy zipcode = Hierarchy.create();
        zipcode.add("81667", "8166*", "816**", "81***", "8****", "*****");
        zipcode.add("81675", "8167*", "816**", "81***", "8****", "*****");
        zipcode.add("81925", "8192*", "819**", "81***", "8****", "*****");
        zipcode.add("81931", "8193*", "819**", "81***", "8****", "*****");

        // Assign hierarchies
        for (int i = 0; i < attributeRepetition; i++) {
            data.getDefinition().setAttributeType("age-" + i, age);
            data.getDefinition().setAttributeType("gender-" + i, gender);
            data.getDefinition().setAttributeType("zipcode-" + i, zipcode);
        }

        return data;
    }
}
