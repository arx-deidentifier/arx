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
import org.deidentifier.arx.DataGeneralizationScheme;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.ARXConfiguration.SearchStepSemantics;
import org.deidentifier.arx.DataGeneralizationScheme.GeneralizationDegree;
import org.deidentifier.arx.criteria.EDDifferentialPrivacy;
import org.deidentifier.arx.criteria.EntropyLDiversity;
import org.deidentifier.arx.metric.Metric;

/**
 * This class represents an example for person data anonymized with (ε,δ)-Differential Privacy.
 * 
 * @author Nenad Jevdjenic
 */
public class ExamplePersonEDDifferentialPrivacy extends ExamplePerson {
	/**
	 * Entry point.
	 */
	public static void main(String[] args) {
		try {
			Data data = csvInit26AttrLarge();
			data = setInsensitiveAttr(data);
			data = setQuasiIdentifiers(data);
			createHierarchy(data, DATE_OF_BIRTH, DataType.DATE);
			createHierarchy(data, DATE_OF_DEATH, DataType.DATE);
			createHierarchy(data, PHONE_NUMBER, DataType.INTEGER);
			createHierarchy(data, CURRENT_ZIP_CODE, DataType.INTEGER);
			createHierarchy(data, CELL_NUMBER, DataType.INTEGER);
			
			data.getDefinition().setResponseVariable(SEX, true);
			data.getDefinition().setResponseVariable(OFFICIAL_NAME, true);
			data.getDefinition().setResponseVariable(FIRST_NAME, true);

//			setEDDifferentialPrivacy(Metric.createClassificationMetric(), 2d, 1d, 1E-5d, 5);
//			setEDDifferentialPrivacy(2d, 1E-5d, null, true, 50, 0.2, Metric.createLossMetric(), 1d);
//			setEDDifferentialPrivacy(2d, 1E-5d, null, true, 100, 1d, Metric.createClassificationMetric(), 1d);
			setEDDifferentialPrivacy(2d, 1E-6d, DataGeneralizationScheme.create(GeneralizationDegree.MEDIUM_HIGH), true, 10, 0.1, Metric.createClassificationMetric(), 1d);
			data.getDefinition().setAttributeType(PLACE_OF_ORIGIN_NAME, AttributeType.SENSITIVE_ATTRIBUTE);
	        config.addPrivacyModel(new EntropyLDiversity(PLACE_OF_ORIGIN_NAME, 1));
			runAnonymization(data);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	protected static ARXConfiguration setEDDifferentialPrivacy(double epsilon, double delta,
			DataGeneralizationScheme dgs, boolean deterministic, int searchSteps, double searchBudget, Metric<?> metric,
			double suppressionLimit) {
		config = ARXConfiguration.create(1d, metric);
		config.addPrivacyModel(new EDDifferentialPrivacy(epsilon, delta, null, deterministic));
		config.setDPSearchBudget(searchBudget);
		config.setHeuristicSearchThreshold(1);
		config.setHeuristicSearchStepLimit(searchSteps, SearchStepSemantics.EXPANSIONS);
		config.setHeuristicSearchTimeLimit(120000);
		return config;
	}
}	