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

package org.deidentifier.arx.examples;

import java.io.IOException;
import java.nio.charset.Charset;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataSource;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.criteria.OrderedDistanceTCloseness;

/**
 * This class implements an example of how to use ordered distance t-closeness. Implements Example 3
 * from the paper Li et al. "t-Closeness: Privacy Beyond k-Anonymity and l-Diversity"
 * 
 * @author Fabian Prasser
 */
public class Example48 extends Example {
    
    /**
     * Entry point.
     * 
     * @param args the arguments
     */
    public static void main(String[] args) throws IOException {

        // Load data
        DataSource source = DataSource.createCSVSource("data/test2.csv", Charset.forName("UTF-8"), ';', true);
        source.addColumn("ZIPCode", DataType.STRING);
        source.addColumn("Age", DataType.INTEGER);
        source.addColumn("Salary", DataType.INTEGER); // in k
        source.addColumn("Disease", DataType.STRING);
        Data data = Data.create(source);
        
        // Load hierarchies
        Hierarchy zipcode = Hierarchy.create("data/test2_hierarchy_ZIPCode.csv", Charset.forName("UTF-8"), ';');
        Hierarchy age = Hierarchy.create("data/test2_hierarchy_Age.csv", Charset.forName("UTF-8"), ';');
        
        // Define
        data.getDefinition().setAttributeType("ZIPCode", zipcode);
        data.getDefinition().setAttributeType("Age", age);
        data.getDefinition().setAttributeType("Salary", AttributeType.SENSITIVE_ATTRIBUTE);
        
        // Create an instance of the anonymizer
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new OrderedDistanceTCloseness("Salary", 0.3751));
        config.setSuppressionLimit(0d);
        
        // Anonymize
        ARXResult result = anonymizer.anonymize(data, config);

        // Print results
        System.out.println("Output data:");
        print(result.getOutput());
    }
}