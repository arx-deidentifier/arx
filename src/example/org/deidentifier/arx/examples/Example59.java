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

package org.deidentifier.arx.examples;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.AttributeType.MaskingFunction;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.masking.DataMaskingFunction.PermutationFunctionColumns.PermutationType;

/**
 * This class implements an example of permutation function on columns
 * 
 * @author giupardeb
 *
 */
public class Example59 extends Example {
	/**
	 * Entry point.
	 * 
	 * @param args the arguments
	 * @throws IOException
	 */
	public static void main (String[] args) throws IOException {
		
		// Define data
        DefaultData data = Data.create();
        data.add("id","Name", "Surname");
        data.add("1","Gerek", "Macourek");
        data.add("2","Nadia", "Stare");
        data.add("3","Bobby", "Spera");
        data.add("4","Carly", "Avrahamof");
        data.add("5","Morgan", "MacCaughey");
        data.add("6","Leilah", "Yapp");
        data.add("7","Alida", "Stud");
        data.add("8","Shannon", "Diwell");
        data.add("9","Kaitlin", "Farmar");
        data.add("10","Angela", "Pinkett");
        
        DefaultHierarchy id = Hierarchy.create();
        id.add("1", "*");
        id.add("2", "*");
        id.add("3", "*");
        id.add("4", "*");
        id.add("5", "*");
        id.add("6", "*");
        id.add("7", "*");
        id.add("8", "*");
        id.add("9", "*");
        id.add("10", "*");

        data.getDefinition().setAttributeType("id", id);
        data.getDefinition().setAttributeType("Name", AttributeType.IDENTIFYING_ATTRIBUTE);
        data.getDefinition().setMaskingFunction("Name", MaskingFunction.createPermutationFunctionColumns(true, PermutationType.FYKY));
        data.getDefinition().setAttributeType("Surname", AttributeType.IDENTIFYING_ATTRIBUTE);
        data.getDefinition().setMaskingFunction("Surname", MaskingFunction.createPermutationFunctionColumns(true, PermutationType.FYKY));
        
        // Create an instance of the anonymizer
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new KAnonymity(3));
        config.setSuppressionLimit(0d);
        
        ARXResult result = anonymizer.anonymize(data,config);
        
        // Print info
        printResult(result, data);
        
        
        // Process results
        System.out.println(" - Transformed data:");
        Iterator<String[]> transformed = result.getOutput(false).iterator();
        while (transformed.hasNext()) {
            System.out.print("   ");
            System.out.println(Arrays.toString(transformed.next()));
        }
	}
}
