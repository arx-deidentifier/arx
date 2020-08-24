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

package org.deidentifier.arx.examples.large;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataSource;
import org.deidentifier.arx.DataType;

/**
 * This class represents a test for an oracle db with 26 attributes.
 *
 * @author Nenad Jevdjenic
 */
public class ExamplePersonCsv26Attributes extends ExamplePerson {
	/**
	 * Entry point.
	 * 
	 * @param args the arguments
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		try {
			// Small data input
			// DataSource source = DataSource.createCSVSource("data/21_persons.csv", StandardCharsets.UTF_8, ';', true);
			// Large data input
			DataSource source = DataSource.createCSVSource("data/257k_persons.csv", StandardCharsets.UTF_8, ';', true);
			source.addColumn(ID, DataType.STRING);
			source.addColumn(ORGANISATION_NAME, DataType.STRING);
			source.addColumn(ORGANISATION_ADDITIONAL_NAME, DataType.STRING);
			source.addColumn(DEPARTMENT, DataType.STRING);
			source.addColumn(OFFICIAL_NAME, DataType.STRING);
			source.addColumn(ORIGINAL_NAME, DataType.STRING);
			source.addColumn(FIRST_NAME, DataType.STRING);
			source.addColumn(DATE_OF_BIRTH, DataType.STRING);
			source.addColumn(PLACE_OF_ORIGIN_NAME, DataType.STRING);
			source.addColumn(SECOND_PLACE_OF_ORIGIN_NAME, DataType.STRING);
			source.addColumn(PLACE_OF_BIRTH_COUNTRY, DataType.STRING);
			source.addColumn(SEX, DataType.STRING);
			source.addColumn(LANGUAGE, DataType.STRING);
			source.addColumn(NATIONALITY, DataType.STRING);
			source.addColumn(COUNTRY_OF_ORIGIN, DataType.STRING);
			source.addColumn(DATE_OF_DEATH, DataType.STRING);
			source.addColumn(REMARK, DataType.STRING);
			source.addColumn(LAST_MEDICAL_CHECKUP, DataType.STRING);
			source.addColumn(NEXT_MEDICAL_CHECKUP, DataType.STRING);
			source.addColumn(PHONE_NUMBER, DataType.STRING);
			source.addColumn(CELL_NUMBER, DataType.STRING);
			source.addColumn(EMAIL, DataType.STRING);
			source.addColumn(GUARDIANSHIP, DataType.STRING);
			source.addColumn(CURRENT_TOWN, DataType.STRING);
			source.addColumn(CURRENT_ZIP_CODE, DataType.STRING);
			source.addColumn(MANDATOR, DataType.STRING);
			
			// Create data object
			Data data = Data.create(source);
			System.out.println("------After data PREPARATION: " + LocalDateTime.now());

			data.getDefinition().setAttributeType(ID, AttributeType.INSENSITIVE_ATTRIBUTE);
			data.getDefinition().setDataType(ID, DataType.INTEGER);
			createHierarchy(data, FIRST_NAME);
			createHierarchy(data, OFFICIAL_NAME);
			createHierarchy(data, ORIGINAL_NAME);
			createHierarchy(data, ORGANISATION_NAME);
			createHierarchy(data, DEPARTMENT);
			createHierarchy(data, DATE_OF_BIRTH);
			
			// Perform risk analysis
			System.out.println("\n - Input data");
			print(data.getHandle());

//			setKAnonymity();
			setEDDifferentialPrivacy();
			runAnonymization(data);
			printResults(data);
		} catch (Exception e) {
			System.out.println(e);
		} 
	}

}
