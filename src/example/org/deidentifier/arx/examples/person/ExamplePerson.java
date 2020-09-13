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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;
import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.DataSource;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.MicroAggregationFunction;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.aggregates.HierarchyBuilderRedactionBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderRedactionBased.Order;
import org.deidentifier.arx.examples.Example;

/**
 * This is the base class for many examples based on CSV and DB input data with
 * the various privacy models.
 * 
 * @author Nenad Jevdjenic
 */
public class ExamplePerson extends Example {
	/** Column names of person data input */
	protected static final String ID = "ID";
	protected static final String ORGANISATION_NAME = "ORGANISATION_NAME";
	protected static final String ORGANISATION_ADDITIONAL_NAME = "ORGANISATION_ADDITIONAL_NAME";
	protected static final String DEPARTMENT = "DEPARTMENT";
	protected static final String OFFICIAL_NAME = "OFFICIAL_NAME";
	protected static final String ORIGINAL_NAME = "ORIGINAL_NAME";
	protected static final String FIRST_NAME = "FIRST_NAME";
	protected static final String DATE_OF_BIRTH = "DATE_OF_BIRTH";
	protected static final String PLACE_OF_ORIGIN_NAME = "PLACE_OF_ORIGIN_NAME";
	protected static final String SECOND_PLACE_OF_ORIGIN_NAME = "SECOND_PLACE_OF_ORIGIN_NAME";
	protected static final String PLACE_OF_BIRTH_COUNTRY = "PLACE_OF_BIRTH_COUNTRY";
	protected static final String SEX = "SEX";
	protected static final String LANGUAGE = "LANGUAGE";
	protected static final String NATIONALITY = "NATIONALITY";
	protected static final String COUNTRY_OF_ORIGIN = "COUNTRY_OF_ORIGIN";
	protected static final String DATE_OF_DEATH = "DATE_OF_DEATH";
	protected static final String REMARK = "REMARK";
	protected static final String LAST_MEDICAL_CHECKUP = "LAST_MEDICAL_CHECKUP";
	protected static final String NEXT_MEDICAL_CHECKUP = "NEXT_MEDICAL_CHECKUP";
	protected static final String PHONE_NUMBER = "PHONE_NUMBER";
	protected static final String CELL_NUMBER = "CELL_NUMBER";
	protected static final String EMAIL = "EMAIL";
	protected static final String GUARDIANSHIP = "GUARDIANSHIP";
	protected static final String CURRENT_TOWN = "CURRENT_TOWN";
	protected static final String CURRENT_ZIP_CODE = "CURRENT_ZIP_CODE";
	protected static final String MANDATOR = "MANDATOR";
	/** ARX specitfic classes */
	protected static ARXAnonymizer anonymizer = new ARXAnonymizer();
	protected static ARXConfiguration config;
	protected static ARXResult result;
	protected static final SimpleDateFormat arxFormat = new SimpleDateFormat("dd.MM.yyyy");
	/** CSV Input files */
	protected static final String CSV_SMALL = "data/20_persons.csv";
	protected static final String CSV_LARGE = "data/146k_persons.csv";
	/** DB connection settings */
	protected static final String ROWNUM = "100000";
	protected static final String TABLE = "PERSON_ARX";
	protected static final String dbUrl = "jdbc:oracle:thin:@172.18.60.83:1521/IVZPDB";
	protected static final String dbUser = "ARX";
	protected static final String dbPw = "ARX";
	protected static boolean syntactic;

	/**
	 * Initializes data anonymization input with 8 attributes from defined db table.
	 * 
	 * @return data anonymization input
	 * @throws SQLException
	 */
	protected static Data dbInit8Attr() {
		DefaultData data = Data.create();
		try (Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPw);
				Statement stmt = con.createStatement();) {
			data.add(ID, ORGANISATION_NAME, ORGANISATION_ADDITIONAL_NAME, DEPARTMENT, OFFICIAL_NAME, ORIGINAL_NAME,
					FIRST_NAME, DATE_OF_BIRTH);
			System.out.println("-----Before select EXECUTION: " + LocalDateTime.now());
			ResultSet rs = stmt.executeQuery(
					"SELECT ID, ORGANISATION_NAME, ORGANISATION_ADDITIONAL_NAME, DEPARTMENT, OFFICIAL_NAME, ORIGINAL_NAME, FIRST_NAME, DATE_OF_BIRTH FROM "
							+ TABLE + " WHERE rownum <= " + ROWNUM);
			System.out.println("------After select EXECUTION: " + LocalDateTime.now());
			while (rs.next()) {
				data.add(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5),
						rs.getString(6), rs.getString(7), formatInputDate(rs.getDate(8)));
			}
			System.out.println("------After data PREPARATION: " + LocalDateTime.now());
			printInput(data);
		} catch (SQLException e) {
			System.err.println(e);
		}
		return data;
	}

	/**
	 * Initializes data anonymization input with 26 attributes from defined db
	 * table.
	 * 
	 * @return data anonymization input
	 * @throws SQLException
	 */
	protected static Data dbInit26Attr() {
		DefaultData data = Data.create();
		try (Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPw);
				Statement stmt = con.createStatement();) {
			data.add(ID, ORGANISATION_NAME, ORGANISATION_ADDITIONAL_NAME, DEPARTMENT, OFFICIAL_NAME, ORIGINAL_NAME,
					FIRST_NAME, DATE_OF_BIRTH, PLACE_OF_ORIGIN_NAME, SECOND_PLACE_OF_ORIGIN_NAME,
					PLACE_OF_BIRTH_COUNTRY, SEX, LANGUAGE, NATIONALITY, COUNTRY_OF_ORIGIN, DATE_OF_DEATH, REMARK,
					LAST_MEDICAL_CHECKUP, NEXT_MEDICAL_CHECKUP, PHONE_NUMBER, CELL_NUMBER, EMAIL, GUARDIANSHIP,
					CURRENT_TOWN, CURRENT_ZIP_CODE, MANDATOR);
			System.out.println("-----Before select EXECUTION: " + LocalDateTime.now());
			ResultSet rs = stmt.executeQuery(
					"SELECT ID, ORGANISATION_NAME, ORGANISATION_ADDITIONAL_NAME, DEPARTMENT, OFFICIAL_NAME, ORIGINAL_NAME, FIRST_NAME, DATE_OF_BIRTH, PLACE_OF_ORIGIN_NAME, "
							+ "SECOND_PLACE_OF_ORIGIN_NAME, PLACE_OF_BIRTH_COUNTRY, SEX, LANGUAGE, NATIONALITY, COUNTRY_OF_ORIGIN, DATE_OF_DEATH, REMARK, LAST_MEDICAL_CHECKUP, "
							+ "NEXT_MEDICAL_CHECKUP, PHONE_NUMBER, CELL_NUMBER, EMAIL, GUARDIANSHIP, CURRENT_TOWN, CURRENT_ZIP_CODE, MANDATOR FROM "
							+ TABLE + " WHERE rownum <= " + ROWNUM);
			System.out.println("------After select EXECUTION: " + LocalDateTime.now());
			while (rs.next()) {
				data.add(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5),
						rs.getString(6), rs.getString(7), formatInputDate(rs.getDate(8)), rs.getString(9),
						rs.getString(10), rs.getString(11), rs.getString(12), rs.getString(13), rs.getString(14),
						rs.getString(15), formatInputDate(rs.getDate(16)), rs.getString(17),
						formatInputDate(rs.getDate(18)), formatInputDate(rs.getDate(19)), rs.getString(20),
						rs.getString(21), rs.getString(22), rs.getString(23), rs.getString(24), rs.getString(25),
						rs.getString(26));
			}
			System.out.println("------After data PREPARATION: " + LocalDateTime.now());
			printInput(data);
		} catch (Exception e) {
			System.out.println(e);
		} 
		return data;
	}

	/**
	 * @return Loaded large CSV example data/146k_persons.csv
	 * @throws IOException
	 */
	protected static Data csvInit26AttrLarge() throws IOException {
		DataSource source;
		System.out.println("-----Before data PREPARATION: " + LocalDateTime.now());
		source = DataSource.createCSVSource(CSV_LARGE, StandardCharsets.UTF_8, ';', true);
		addColumns(source);
		Data data = Data.create(source);
		System.out.println("------After data PREPARATION: " + LocalDateTime.now());
		printInput(data);
		return data;
	}

	/**
	 * @return Loaded small CSV example data/146k_persons.csv
	 * @throws IOException
	 */
	protected static Data csvInit26AttrSmall() throws IOException {
		DataSource source;
		System.out.println("-----Before data PREPARATION: " + LocalDateTime.now());
		source = DataSource.createCSVSource(CSV_SMALL, StandardCharsets.UTF_8, ';', true);
		addColumns(source);
		Data data = Data.create(source);
		System.out.println("------After data PREPARATION: " + LocalDateTime.now());
		printInput(data);
		return data;
	}

	/**
	 * Add columns for a csv file
	 * 
	 * @param source without columns
	 * @return prepared dataSource
	 */
	private static DataSource addColumns(DataSource source) {
		source.addColumn(ID, DataType.INTEGER, true);
		source.addColumn(ORGANISATION_NAME, DataType.STRING);
		source.addColumn(ORGANISATION_ADDITIONAL_NAME, DataType.STRING);
		source.addColumn(DEPARTMENT, DataType.STRING);
		source.addColumn(OFFICIAL_NAME, DataType.STRING);
		source.addColumn(ORIGINAL_NAME, DataType.STRING);
		source.addColumn(FIRST_NAME, DataType.STRING);
		source.addColumn(DATE_OF_BIRTH, DataType.DATE, true);
		source.addColumn(PLACE_OF_ORIGIN_NAME, DataType.STRING);
		source.addColumn(SECOND_PLACE_OF_ORIGIN_NAME, DataType.STRING);
		source.addColumn(PLACE_OF_BIRTH_COUNTRY, DataType.STRING);
		source.addColumn(SEX, DataType.STRING);
		source.addColumn(LANGUAGE, DataType.STRING);
		source.addColumn(NATIONALITY, DataType.STRING);
		source.addColumn(COUNTRY_OF_ORIGIN, DataType.STRING);
		source.addColumn(DATE_OF_DEATH, DataType.DATE, true);
		source.addColumn(REMARK, DataType.STRING);
		source.addColumn(LAST_MEDICAL_CHECKUP, DataType.DATE, true);
		source.addColumn(NEXT_MEDICAL_CHECKUP, DataType.DATE, true);
		source.addColumn(PHONE_NUMBER, DataType.INTEGER, true);;
		source.addColumn(CELL_NUMBER, DataType.INTEGER, true);
		source.addColumn(EMAIL, DataType.STRING);
		source.addColumn(GUARDIANSHIP, DataType.STRING);
		source.addColumn(CURRENT_TOWN, DataType.STRING);
		source.addColumn(CURRENT_ZIP_CODE, DataType.INTEGER, true);
		source.addColumn(MANDATOR, DataType.STRING);
		return source;
	}

	/**
	 * Anonymization method ARX
	 * 
	 * @param data
	 * @throws IOException
	 */
	protected static void runAnonymization(Data data) throws IOException {
		System.out.println("---Before data ANONYMIZATION: " + LocalDateTime.now());
		anonymizer = new ARXAnonymizer();
		result = anonymizer.anonymize(data, config);
		System.out.println("----After data ANONYMIZATION: " + LocalDateTime.now());
		printResults(data);
	}

	/**
	 * Set insensitive attributes 
	 * @param data
	 * @return prepared data
	 */
	protected static Data setInsensitiveAttr(Data data) {
		data.getDefinition().setAttributeType(ID, AttributeType.INSENSITIVE_ATTRIBUTE);
		data.getDefinition().setDataType(ID, DataType.INTEGER);
		data.getDefinition().setAttributeType(GUARDIANSHIP, AttributeType.INSENSITIVE_ATTRIBUTE);
		data.getDefinition().setDataType(GUARDIANSHIP, DataType.INTEGER);
		return data;
	}

	/**
	 * Set quasi identifiers for attributes of type STRING
	 * @param data
	 * @return prepared data
	 */
	protected static Data setQuasiIdentifiersString(Data data) {
		createHierarchy(data, ORGANISATION_NAME, DataType.STRING);
		createHierarchy(data, ORGANISATION_ADDITIONAL_NAME, DataType.STRING);
		createHierarchy(data, DEPARTMENT, DataType.STRING);
		createHierarchy(data, OFFICIAL_NAME, DataType.STRING);
		createHierarchy(data, ORIGINAL_NAME, DataType.STRING);
		createHierarchy(data, FIRST_NAME, DataType.STRING);
		createHierarchy(data, PLACE_OF_ORIGIN_NAME, DataType.STRING);
		createHierarchy(data, SECOND_PLACE_OF_ORIGIN_NAME, DataType.STRING);
		createHierarchy(data, PLACE_OF_BIRTH_COUNTRY, DataType.STRING);
		createHierarchy(data, SEX, DataType.STRING);
		createHierarchy(data, REMARK, DataType.STRING);
		createHierarchy(data, EMAIL, DataType.STRING);
		createHierarchy(data, CURRENT_TOWN, DataType.STRING);
		return data;
	}
	
	/**
	 * Set quasi identifiers for attributes of type DATE
	 * @param data
	 * @return prepared data
	 */
	protected static Data setQuasiIdentifiersDate(Data data) {
		setMicroAggregation(data, DATE_OF_BIRTH, DataType.DATE);
		setMicroAggregation(data, DATE_OF_DEATH, DataType.DATE);
		setMicroAggregation(data, LAST_MEDICAL_CHECKUP, DataType.DATE);
		setMicroAggregation(data, NEXT_MEDICAL_CHECKUP, DataType.DATE);
		return data;
	}
	
	/**
	 * Set quasi identifiers for attributes of type INTEGER
	 * @param data
	 * @return prepared data
	 */
	protected static Data setQuasiIdentifiersInteger(Data data) {
		data.getDefinition().setAttributeType(CELL_NUMBER, AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
		setMicroAggregation(data, CELL_NUMBER, DataType.INTEGER);
		data.getDefinition().setAttributeType(PHONE_NUMBER, AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
		setMicroAggregation(data, PHONE_NUMBER, DataType.INTEGER);
		data.getDefinition().setAttributeType(CURRENT_ZIP_CODE, AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
		setMicroAggregation(data, CURRENT_ZIP_CODE, DataType.INTEGER);
		return data;
	}
	
	/**
	 * @param data
	 * @param attribute
	 * @param dataType
	 * @return Hierarchy for attribute transformation
	 */
	protected static HierarchyBuilderRedactionBased<?> createHierarchy(Data data, String attribute,
			DataType<?> dataType) {
		HierarchyBuilderRedactionBased<?> builder = HierarchyBuilderRedactionBased.create(Order.RIGHT_TO_LEFT,
				Order.RIGHT_TO_LEFT, ' ', generateRandomString());
		data.getDefinition().setAttributeType(attribute, builder);
		data.getDefinition().setDataType(attribute, dataType);
		return builder;
	}
	
	/**
	 * Sets micro aggregation
	 * @param data
	 * @param attribute
	 * @param dataType
	 */
	protected static void setMicroAggregation(Data data, String attribute, DataType<?> dataType) {
		setQuasiIdentifier(data, attribute, dataType);
		data.getDefinition().setAttributeType(attribute, MicroAggregationFunction.createArithmeticMean());
	}

	/**
	 * Sets quasi identifier
	 * @param data
	 * @param attribute
	 * @param dataType
	 */
	protected static void setQuasiIdentifier(Data data, String attribute, DataType<?> dataType) {
		data.getDefinition().setAttributeType(attribute, AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
		data.getDefinition().setDataType(attribute, dataType);
	}

	/**
	 * @param data
	 */
	protected static void createHierarchySex(Data data) {
		DefaultHierarchy sex = Hierarchy.create();
		sex.add("MALE", "FEMALE");
		sex.add("FEMALE", "MALE");
		sex.add("null", "MALE");
		sex.add("", "MALE");
		sex.add("NULL", "MALE");
		data.getDefinition().setAttributeType(SEX, sex);
		data.getDefinition().setDataType(SEX, DataType.STRING);
		data.getDefinition().setHierarchy(SEX, sex);
	}

	/**
	 * @param data
	 * @param attribute
	 */
	protected static void createHierarchyLanguage(Data data, String attribute) {
		DefaultHierarchy lang = Hierarchy.create();
		for (String l : Locale.getISOLanguages()) {
			lang.add(l, "D");
		}
		lang.add("A", "I");
		lang.add("B", "I");
		lang.add("C", "I");
		lang.add("D", "I");
		lang.add("E", "I");
		lang.add("F", "I");
		lang.add("G", "I");
		lang.add("H", "I");
		lang.add("I", "I");
		lang.add("J", "I");
		lang.add("K", "I");
		lang.add("L", "I");
		lang.add("M", "I");
		lang.add("N", "I");
		lang.add("O", "I");
		lang.add("P", "I");
		lang.add("Q", "I");
		lang.add("R", "I");
		lang.add("S", "I");
		lang.add("T", "I");
		lang.add("U", "I");
		lang.add("V", "I");
		lang.add("W", "I");
		lang.add("X", "I");
		lang.add("Y", "I");
		lang.add("Z", "I");
		lang.add("null", "E");
		lang.add("", "I");
		lang.add("NULL", "F");
		data.getDefinition().setAttributeType(attribute, lang);
		data.getDefinition().setDataType(attribute, DataType.STRING);
		data.getDefinition().setHierarchy(attribute, lang);
	}

	/**
	 * @param data
	 * @param attribute
	 * @return hierarchy for countries
	 */
	protected static DefaultHierarchy createHierarchyCountry(Data data, String attribute) {
		DefaultHierarchy country = Hierarchy.create();
		for (String c : Locale.getISOCountries()) {
			country.add(c, "USA");
		}
		country.add("AFG", "CH");country.add("AND", "CH");country.add("ARM", "CH");country.add("AUS", "CH");country.add("BDI", "CH");country.add("BDS", "CH");
		country.add("BIH", "CH");country.add("BOL", "CH");country.add("BRN", "CH");country.add("BRU", "CH");country.add("CAM", "CH");country.add("CDN", "CH");
		country.add("CHN", "CH");country.add("COM", "CH");country.add("DJI", "CH");country.add("DOM", "CH");country.add("EAK", "CH");country.add("EAT", "CH");
		country.add("EAU", "CH");country.add("EST", "CH");country.add("ETH", "CH");country.add("FIN", "CH");country.add("FJI", "CH");country.add("GAB", "CH");
		country.add("GBG", "CH");country.add("GBJ", "CH");country.add("GBM", "CH");country.add("GBZ", "CH");country.add("GCA", "CH");country.add("GUY", "CH");
		country.add("HKJ", "CH");country.add("IND", "CH");country.add("IRL", "CH");country.add("IRQ", "CH");country.add("KIR", "CH");country.add("KWT", "CH");
		country.add("LAO", "CH");country.add("LAR", "CH");country.add("MAL", "CH");country.add("MEX", "CH");country.add("MGL", "CH");country.add("MNE", "CH");
		country.add("MOC", "CH");country.add("MYA", "CH");country.add("NAM", "CH");country.add("NAU", "CH");country.add("NEP", "CH");country.add("NIC", "CH");
		country.add("PAL", "CH");country.add("PNG", "CH");country.add("PRK", "CH");country.add("RCA", "CH");country.add("RCB", "CH");country.add("RCH", "CH");
		country.add("RDC", "CH");country.add("RIM", "CH");country.add("RMM", "CH");country.add("ROK", "CH");country.add("ROU", "CH");country.add("RSM", "CH");
		country.add("RUS", "CH");country.add("RWA", "CH");country.add("SCG", "CH");country.add("SCN", "CH");country.add("SGC", "CH");country.add("SGP", "CH");
		country.add("SLO", "CH");country.add("SME", "CH");country.add("SOM", "CH");country.add("SRB", "CH");country.add("SUD", "CH");country.add("SYR", "CH");
		country.add("TCH", "CH");country.add("TWN", "CH");country.add("UAE", "CH");country.add("UNK", "CH");country.add("USA", "CH");country.add("WAG", "CH");
		country.add("WAL", "CH");country.add("WAN", "CH");country.add("XXX", "CH");country.add("YMN", "CH");country.add("ITA", "CH");country.add("SPA", "CH");
		country.add("IRZ", "SPA");country.add("RZK", "SPA");country.add("HKZ", "SPA");country.add("NAU", "CH");country.add("111", "DE");country.add(" CH", "DE");
		country.add("AUT", "DE");country.add("BHI", "DE");country.add("BRU", "DE");country.add("CEI", "DE");country.add("CHN", "DE");country.add("CH6", "DE");
		country.add("COM", "DE");country.add("CRO", "DE");country.add("CSF", "DE");country.add("CSI", "DE");country.add("DDR", "DE");country.add("D-O", "DE");
		country.add("ERY", "DE");country.add("EUA", "DE");country.add("FRU", "DE");country.add("FSM", "DE");country.add("GBA", "DE");country.add("GRI", "DE");
		country.add("ISR", "DE");country.add("JOR", "DE");country.add("KOR", "DE");country.add("PAK", "DE");country.add("PLW", "DE");country.add("POL", "DE");
		country.add("PTM", "DE");country.add("ROC", "DE");country.add("RPB", "DE");country.add("RPC", "DE");country.add("RZK", "DE");country.add("SAL", "DE");
		country.add("scg", "DE");country.add("SRI", "DE");country.add("STA", "DE");country.add("STL", "DE");country.add("THA", "DE");country.add("TIB", "DE");
		country.add("TOG", "DE");country.add("TWN", "DE");country.add("VRC", "DE");country.add("ZRE", "DE");country.add("ZZZ", "DE");country.add("A", "DE");
		country.add("B", "DE");country.add("C", "DE");country.add("D", "DE");country.add("E", "DE");country.add("F", "DE");country.add("G", "DE");
		country.add("H", "DE");country.add("I", "DE");country.add("J", "DE");country.add("K", "DE");country.add("L", "DE");country.add("M", "DE");
		country.add("N", "DE");country.add("O", "DE");country.add("P", "DE");country.add("Q", "DE");country.add("R", "DE");country.add("S", "DE");
		country.add("T", "DE");country.add("U", "DE");country.add("V", "DE");country.add("W", "DE");country.add("X", "DE");country.add("Y", "DE");
		country.add("Z", "DE");country.add("TU", "CH");country.add("YU", "CH");country.add("RI", "CH");country.add("RL", "CH");country.add("FL", "CH");
		country.add("RA", "CH");country.add("RC", "CH");country.add("YV", "CH");country.add("RP", "CH");country.add("KS", "CH");country.add("RH", "CH");
		country.add("RG", "CH");country.add("RN", "CH");country.add("GZ", "CH");country.add("null", "FR");country.add("", "ITA");country.add("NULL", "");
		data.getDefinition().setDataType(attribute, DataType.STRING);
		data.getDefinition().setHierarchy(attribute, country);
		data.getDefinition().setAttributeType(attribute, country);
		return country;
	}

	/**
	 * Print data input before anonymization
	 * 
	 * @param data
	 */
	protected static void printInput(Data data) {
		System.out.println("------------------Input data: ");
		Iterator<String[]> inputIterator = data.getHandle().iterator();
		for (int i = 0; i < 20; i++) {
			System.out.println(Arrays.toString(inputIterator.next()));
		}
	}

	/**
	 * Print data output after anonymization
	 * 
	 * @param data
	 */
	protected static void printResults(Data data) {
		// Print info
		printResult(result, data);
		System.out.println();

		// Process results
		System.out.println("-------------Transformed data: ");
		Iterator<String[]> transformed = result.getOutput(false).iterator();
		for (int i = 0; i < 100; i++) {
			System.out.print(" ");
			System.out.println(Arrays.toString(transformed.next()));
		}
	}

	/**
	 * Generates a random string for transformation
	 * @return random char a-z
	 */
	private static char generateRandomString() {
		Random r = new Random();
		char c = (char) (r.nextInt(26) + 'a');
		return c;
	}

	/**
	 * Generates a random integer for transformation
	 * @return random char 1-9
	 */
	@SuppressWarnings("unused")
	private static char generateRandomInt() {
		String r = RandomStringUtils.randomNumeric(10);
		char c = r.charAt(0);
		return c;
	}

	/**
	 * Format input date to ARX format
	 * @param date
	 * @return
	 */
	private static String formatInputDate(Date date) {
		if (date == null) {
			return "null";
		} else {
			return arxFormat.format(date);
		}
	}

}
