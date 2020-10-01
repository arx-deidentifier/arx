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
			data = setQuasiIdentifiers(data);
			createHierarchyString(data, ORGANISATION_ADDITIONAL_NAME);
			createHierarchyString(data, ORGANISATION_NAME);
			createHierarchyString(data, DEPARTMENT);
			createHierarchyInteger(data, PHONE_NUMBER);
			createHierarchyInteger(data, CURRENT_ZIP_CODE);
			createHierarchyInteger(data, CELL_NUMBER);
			
			data.getDefinition().setResponseVariable(ID, true);
			config = ARXConfiguration.create();
			DataGeneralizationScheme dgs = DataGeneralizationScheme.create(GeneralizationDegree.HIGH);
			config.addPrivacyModel(new EDDifferentialPrivacy(4, 0.9, dgs));
			config.setDPSearchBudget(3);
			
			runAnonymization(data);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

}	