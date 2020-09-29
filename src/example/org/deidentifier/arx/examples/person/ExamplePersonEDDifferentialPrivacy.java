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
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataGeneralizationScheme;
import org.deidentifier.arx.DataGeneralizationScheme.GeneralizationDegree;
import org.deidentifier.arx.criteria.EDDifferentialPrivacy;

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
			data.getDefinition().setResponseVariable(ID, true);
			data = setQuasiIdentifiers(data);
			createHierarchyString(data, DATE_OF_BIRTH);
			createHierarchyString(data, DATE_OF_DEATH);
			createHierarchyInteger(data, PHONE_NUMBER);
			createHierarchyInteger(data, CURRENT_ZIP_CODE);
			createHierarchyInteger(data, CELL_NUMBER);
			
			setEDDifferentialPrivacy(2d, 0.9d, DataGeneralizationScheme.create(GeneralizationDegree.HIGH), true, 100);
			runAnonymization(data);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	protected static ARXConfiguration setEDDifferentialPrivacy(double epsilon, double delta,
			DataGeneralizationScheme dgs, boolean deterministic, double searchBudget) {
		config = ARXConfiguration.create();
		config.addPrivacyModel(new EDDifferentialPrivacy(epsilon, delta, dgs, deterministic));
		config.setDPSearchBudget(searchBudget);
		return config;
	}
}	