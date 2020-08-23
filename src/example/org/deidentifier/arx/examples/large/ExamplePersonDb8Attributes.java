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
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataType;

/**
 * This class represents a test for an oracle db with 8 attributes.
 * To run this example a db must be availlable an the data/257k_persons.sql script be running before. The load time for 
 * this script can take up to 5 hours. So it is possible to minimize the amount of persons in the script.
 *
 * @author Nenad Jevdjenic
 */
public class ExamplePersonDb8Attributes extends ExamplePerson {
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
					FIRST_NAME, DATE_OF_BIRTH);
			ResultSet rs = selectData(con);
			while (rs.next()) {
				defaultData.add(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5),
						rs.getString(6), rs.getString(7), formatIvzDate(rs.getDate(8)));
			}
			System.out.println("------After data PREPARATION: " + LocalDateTime.now());
			
			defaultData.getDefinition().setAttributeType(ID, AttributeType.INSENSITIVE_ATTRIBUTE);
			defaultData.getDefinition().setDataType(ID, DataType.INTEGER);
			createHierarchy(defaultData, FIRST_NAME);
			createHierarchy(defaultData, OFFICIAL_NAME);
			createHierarchy(defaultData, ORIGINAL_NAME);
			createHierarchy(defaultData, ORGANISATION_NAME);
			createHierarchy(defaultData, DEPARTMENT);
			createDateAnonymization(defaultData, DATE_OF_BIRTH);
			
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
