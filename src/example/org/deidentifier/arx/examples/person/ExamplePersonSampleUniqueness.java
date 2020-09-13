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
import org.deidentifier.arx.criteria.SampleUniqueness;
import org.deidentifier.arx.metric.Metric;

/**
 * This class represents an example for person data anonymized with the Sample Uniqueness privacy model.
 * 
 * @author Nenad Jevdjenic
 */
public class ExamplePersonSampleUniqueness extends ExamplePerson {
	/**
	 * Entry point.
	 */
	public static void main(String[] args) {
		try {
			Data data = csvInit26AttrLarge();
			data = setInsensitiveAttr(data);
			data = setQuasiIdentifiersString(data);
			data = setQuasiIdentifiersDate(data);

			config = ARXConfiguration.create(1d, Metric.createAECSMetric());
			config.addPrivacyModel(new SampleUniqueness(0.7d));
			runAnonymization(data);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
