/*
 * ARX: Powerful Data Anonymization
 * Copyright 2020 Fabian Prasser and contributors
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

package org.deidentifier.arx.examples.person;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.criteria.HierarchicalDistanceTCloseness;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.metric.Metric;

/**
 * This class represents an example for person data anonymized with the T-Closeness privacy model.
 * 
 * @author Nenad Jevdjenic
 */
public class ExamplePersonTCloseness extends ExamplePerson {
	/**
	 * Entry point.
	 */
	public static void main(String[] args) {
		try {
			Data data = csvInit26AttrLarge();
			data = setInsensitiveAttr(data);
			data = setQuasiIdentifiersString(data);
	        DefaultHierarchy countryHierarchy = createHierarchyCountry(data, COUNTRY_OF_ORIGIN);
	        DefaultHierarchy nationHierarchy = createHierarchyCountry(data, NATIONALITY);
	        data.getDefinition().setAttributeType(COUNTRY_OF_ORIGIN, AttributeType.SENSITIVE_ATTRIBUTE);
			data.getDefinition().setAttributeType(NATIONALITY, AttributeType.SENSITIVE_ATTRIBUTE);
	        
			config = ARXConfiguration.create();
			config.addPrivacyModel(new KAnonymity(2));
			config.setSuppressionLimit(1d);
			config.setQualityModel(Metric.createEntropyMetric());
	        config.addPrivacyModel(new HierarchicalDistanceTCloseness(COUNTRY_OF_ORIGIN, 0.6d, countryHierarchy));
	        config.addPrivacyModel(new HierarchicalDistanceTCloseness(NATIONALITY, 0.6d, nationHierarchy));
	        runAnonymization(data);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
