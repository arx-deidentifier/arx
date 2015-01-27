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

package org.deidentifier.arx.examples;

import java.io.IOException;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXLattice;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.criteria.KAnonymity;

/**
 * This class implements an example of how to use the API for creating different
 * output representations of an input dataset.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Example19 extends Example {

    /**
     * Entry point.
     * 
     * @param args
     *            the arguments
     */
    public static void main(final String[] args) {

        // Define data
        final DefaultData data = Data.create();
        data.add("age", "gender", "zipcode");
        data.add("34", "male", "81667");
        data.add("45", "female", "81675");
        data.add("66", "male", "81925");
        data.add("70", "female", "81931");
        data.add("34", "female", "81931");
        data.add("70", "male", "81931");
        data.add("45", "male", "81931");

        // Define hierarchies
        final DefaultHierarchy age = Hierarchy.create();
        age.add("34", "<50", "*");
        age.add("45", "<50", "*");
        age.add("66", ">=50", "*");
        age.add("70", ">=50", "*");

        final DefaultHierarchy gender = Hierarchy.create();
        gender.add("male", "*");
        gender.add("female", "*");

        // Only excerpts for readability
        final DefaultHierarchy zipcode = Hierarchy.create();
        zipcode.add("81667", "8166*", "816**", "81***", "8****", "*****");
        zipcode.add("81675", "8167*", "816**", "81***", "8****", "*****");
        zipcode.add("81925", "8192*", "819**", "81***", "8****", "*****");
        zipcode.add("81931", "8193*", "819**", "81***", "8****", "*****");

        data.getDefinition().setAttributeType("age", age);
        data.getDefinition().setAttributeType("gender", gender);
        data.getDefinition().setAttributeType("zipcode", zipcode);

        try {
            // Create an instance of the anonymizer
            final ARXAnonymizer anonymizer = new ARXAnonymizer();

            final ARXConfiguration config = ARXConfiguration.create();
            config.addCriterion(new KAnonymity(2));
            config.setMaxOutliers(0d);
            final ARXResult result = anonymizer.anonymize(data, config);

            ARXLattice lattice = result.getLattice();

            ARXNode topNode = lattice.getTop();
            ARXNode bottomNode = lattice.getBottom();

            // get various handle copies
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

        } catch (final IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
