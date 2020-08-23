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

package org.deidentifier.arx.examples.large;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataType;

/**
 * This class represents a test for an oracle db with 26 attributes. 
 * To run this example a db must be availlable an the data/257k_persons.sql script be running before. The load time for 
 * this script can take up to 5 hours. So it is possible to minimize the amount of persons in the script.
 *
 * @author Nenad Jevdjenic
 */
public class ExamplePersonDb26Attributes extends ExamplePerson {
	/**
	 * Entry point.
	 * 
	 * @param args the arguments
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
		Class.forName("oracle.jdbc.driver.OracleDriver");
		Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPw);
		try {
			defaultData = Data.create();
			defaultData.add(ID, ORGANISATION_NAME, ORGANISATION_ADDITIONAL_NAME, DEPARTMENT, OFFICIAL_NAME, ORIGINAL_NAME,
					FIRST_NAME, DATE_OF_BIRTH, PLACE_OF_ORIGIN_NAME, SECOND_PLACE_OF_ORIGIN_NAME,
					PLACE_OF_BIRTH_COUNTRY, SEX, LANGUAGE, NATIONALITY, COUNTRY_OF_ORIGIN, DATE_OF_DEATH, REMARK,
					LAST_MEDICAL_CHECKUP, NEXT_MEDICAL_CHECKUP, PHONE_NUMBER, CELL_NUMBER, EMAIL, GUARDIANSHIP,
					CURRENT_TOWN, CURRENT_ZIP_CODE, MANDATOR);
			ResultSet rs = selectData(con);
			while (rs.next()) {
				defaultData.add(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5),
						rs.getString(6), rs.getString(7), formatIvzDate(rs.getDate(8)), rs.getString(9),
						rs.getString(10), rs.getString(11), rs.getString(12), rs.getString(13), rs.getString(14),
						rs.getString(15), formatIvzDate(rs.getDate(16)), rs.getString(17),
						formatIvzDate(rs.getDate(18)), formatIvzDate(rs.getDate(19)), rs.getString(20),
						rs.getString(21), rs.getString(22), rs.getString(23), rs.getString(24), rs.getString(25),
						rs.getString(26));
			}
			System.out.print("------After data PREPARATION: " + LocalDateTime.now());
			
			defaultData.getDefinition().setAttributeType(ID, AttributeType.INSENSITIVE_ATTRIBUTE);
			defaultData.getDefinition().setDataType(ID, DataType.INTEGER);

			createHierarchy(defaultData, FIRST_NAME);
			createHierarchy(defaultData, OFFICIAL_NAME);
			createHierarchy(defaultData, ORIGINAL_NAME);
			createHierarchy(defaultData, ORGANISATION_NAME);
			createHierarchy(defaultData, DEPARTMENT);
			createHierarchy(defaultData, NATIONALITY);

			createDateAnonymization(defaultData, DATE_OF_BIRTH);
			createDateAnonymization(defaultData, DATE_OF_DEATH);
			createDateAnonymization(defaultData, LAST_MEDICAL_CHECKUP);
			createDateAnonymization(defaultData, NEXT_MEDICAL_CHECKUP);

			DefaultHierarchy sex = Hierarchy.create();
			sex.add("MALE", "FEMALE");
			sex.add("FEMALE", "MALE");
			sex.add("null", "MALE");
			sex.add("NULL", "MALE");
			defaultData.getDefinition().setAttributeType(SEX, sex);
			defaultData.getDefinition().setDataType(SEX, DataType.STRING);
			defaultData.getDefinition().setHierarchy(SEX, sex);

			// Perform risk analysis
			System.out.println("\n - Input data");
			print(defaultData.getHandle());

			setKAnonymity();
			runAnonymization(defaultData);
			printResults(defaultData);
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			con.close();
		}
	}

}
