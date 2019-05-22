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
import org.deidentifier.arx.criteria.KAnonymity;

/**
 * This class implements an example of processing highdimensional data
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
        
        // Create a dataset with 3000 columns
        DefaultData data = Data.create();
        
        // Header
        String[] row = new String[3000];
        for (int i=0; i < 1000; i++) {
            row[i*3 + 0] = "age-" + i;
            row[i*3 + 1] = "gender-" + i;
            row[i*3 + 2] = "zipcode-" + i;
        }
        data.add(row);
        
        // Row 1
        row = new String[3000];
        for (int i=0; i < 1000; i++) {
            row[i*3 + 0] = "34";
            row[i*3 + 1] = "male";
            row[i*3 + 2] = "81667";
        }
        data.add(row);

        // Row 2
        row = new String[3000];
        for (int i=0; i < 1000; i++) {
            row[i*3 + 0] = "45";
            row[i*3 + 1] = "female";
            row[i*3 + 2] = "81675";
        }
        data.add(row);

        // Row 3
        row = new String[3000];
        for (int i=0; i < 1000; i++) {
            row[i*3 + 0] = "66";
            row[i*3 + 1] = "male";
            row[i*3 + 2] = "81925";
        }
        data.add(row);

        // Row 4
        row = new String[3000];
        for (int i=0; i < 1000; i++) {
            row[i*3 + 0] = "70";
            row[i*3 + 1] = "female";
            row[i*3 + 2] = "81931";
        }
        data.add(row);

        // Row 5
        row = new String[3000];
        for (int i=0; i < 1000; i++) {
            row[i*3 + 0] = "34";
            row[i*3 + 1] = "female";
            row[i*3 + 2] = "81931";
        }
        data.add(row);

        // Row 6
        row = new String[3000];
        for (int i=0; i < 1000; i++) {
            row[i*3 + 0] = "70";
            row[i*3 + 1] = "male";
            row[i*3 + 2] = "81931";
        }
        data.add(row);

        // Row 7
        row = new String[3000];
        for (int i=0; i < 1000; i++) {
            row[i*3 + 0] = "45";
            row[i*3 + 1] = "male";
            row[i*3 + 2] = "81931";
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
        for (int i=0; i < 1000; i++) {
            data.getDefinition().setAttributeType("age-" + i, age);
            data.getDefinition().setAttributeType("gender-" + i, gender);
            data.getDefinition().setAttributeType("zipcode-" + i, zipcode);
        }

        // Create an instance of the anonymizer
        ARXAnonymizer anonymizer = new ARXAnonymizer();

        // Create config
        ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new KAnonymity(2));
        config.setSuppressionLimit(1d);
        config.setHeuristicSearchStepLimit(1000);
        config.setHeuristicSearchEnabled(true);
        ARXResult result = anonymizer.anonymize(data, config);

        // Obtain results
        ARXLattice lattice = result.getLattice();
        ARXNode topNode = lattice.getTop();
        ARXNode bottomNode = lattice.getBottom();

        // Obtain various data representations
        DataHandle optimal = result.getOutput();
        DataHandle top = result.getOutput(topNode);
        DataHandle bottom = result.getOutput(bottomNode);

        // Print input
        System.out.println(" - Input data:");
        printHandle(data.getHandle());

        // Print results
        System.out.println(" - Top node data:");
        printHandle(top);

        System.out.println(" - Bottom node data:");
        printHandle(bottom);

        System.out.println(" - Optimal data:");
        printHandle(optimal);
    }
}
