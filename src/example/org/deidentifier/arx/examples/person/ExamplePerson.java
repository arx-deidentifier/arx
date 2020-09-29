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
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.aggregates.HierarchyBuilderRedactionBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderRedactionBased.Order;
import org.deidentifier.arx.aggregates.StatisticsEquivalenceClasses;
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
	protected static final String ROWNUM = "10000";
	protected static final String TABLE = "PERSON_ARX";
	protected static final String dbUrl = "jdbc:oracle:thin:@localhost:1521/IVZPDB";
	protected static final String dbUser = "ARX";
	protected static final String dbPw = "ARX";

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
	 * Set quasi identifiers for attributes
	 * @param data
	 * @return prepared data
	 */
	protected static Data setQuasiIdentifiers(Data data) {
		createHierarchyString(data, OFFICIAL_NAME);
		createHierarchyString(data, ORIGINAL_NAME);
		createHierarchyString(data, FIRST_NAME);
		createHierarchySex(data);
		createHierarchyCountry(data, COUNTRY_OF_ORIGIN);
		createHierarchyCountry(data, NATIONALITY);
		createHierarchyCanton(data, MANDATOR);
		createHierarchyLanguage(data, LANGUAGE);
		return data;
	}
	
	/**
	 * Set quasi identifiers for attributes of type STRING
	 * @param data
	 * @return prepared data
	 */
	protected static Data setQuasiIdentifiersString(Data data) {
		createHierarchyString(data, ORGANISATION_NAME);
		createHierarchyString(data, ORGANISATION_ADDITIONAL_NAME);
		createHierarchyString(data, DEPARTMENT);
		createHierarchyString(data, OFFICIAL_NAME);
		createHierarchyString(data, ORIGINAL_NAME);
		createHierarchyString(data, FIRST_NAME);
		createHierarchyString(data, PLACE_OF_ORIGIN_NAME);
		createHierarchyString(data, SECOND_PLACE_OF_ORIGIN_NAME);
		createHierarchyString(data, PLACE_OF_BIRTH_COUNTRY);
		createHierarchyString(data, SEX);
		createHierarchyString(data, REMARK);
		createHierarchyString(data, EMAIL);
		createHierarchyString(data, CURRENT_TOWN);
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
	protected static HierarchyBuilderRedactionBased<?> createHierarchyString(Data data, String attribute) {
		HierarchyBuilderRedactionBased<?> builder = HierarchyBuilderRedactionBased.create(Order.RIGHT_TO_LEFT,
				Order.RIGHT_TO_LEFT, ' ', generateRandomString());
		data.getDefinition().setAttributeType(attribute, builder);
		data.getDefinition().setDataType(attribute, DataType.STRING);
		return builder;
	}
	
	/**
	 * @param data
	 * @param attribute
	 * @param dataType
	 * @return Hierarchy for attribute transformation
	 */
	protected static HierarchyBuilderRedactionBased<?> createHierarchyInteger(Data data, String attribute) {
		HierarchyBuilderRedactionBased<?> builder = HierarchyBuilderRedactionBased.create(Order.RIGHT_TO_LEFT,
				Order.RIGHT_TO_LEFT, ' ', generateRandomInt());
		data.getDefinition().setAttributeType(attribute, builder);
		data.getDefinition().setDataType(attribute, DataType.INTEGER);
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
	 * Hierarchy for possible sex values
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
	 * Hierarchy for languages
	 * @param data
	 * @param attribute
	 */
	protected static void createHierarchyLanguage(Data data, String attribute) {
		DefaultHierarchy lang = Hierarchy.create();
		for (String l : Locale.getISOLanguages()) {
			lang.add(l, "D");
		}
		lang.add("A", "F");lang.add("B", "F");lang.add("C", "F");lang.add("D", "F");lang.add("E", "F");lang.add("F", "F");lang.add("G", "F");
		lang.add("H", "D");lang.add("I", "D");lang.add("J", "D");lang.add("K", "D");lang.add("L", "D");lang.add("M", "D");lang.add("N", "D");
		lang.add("O", "F");lang.add("P", "F");lang.add("Q", "F");lang.add("R", "F");lang.add("S", "F");lang.add("T", "F");lang.add("U", "F");
		lang.add("V", "I");lang.add("W", "I");lang.add("X", "I");lang.add("Y", "I");lang.add("Z", "I");
		lang.add("null", "F");lang.add("", "I");lang.add("NULL", "F");
		data.getDefinition().setAttributeType(attribute, lang);
		data.getDefinition().setDataType(attribute, DataType.STRING);
		data.getDefinition().setHierarchy(attribute, lang);
	}
	
	/**
	 * Hierarchy for cantons in Switzerland
	 * @param data
	 * @param attribute
	 */
	protected static void createHierarchyCanton(Data data, String attribute) {
		DefaultHierarchy canton = Hierarchy.create();
		canton.add("M", "ZH");canton.add("##", "ZH");canton.add("AG", "ZH");canton.add("AI", "ZH");canton.add("AR", "ZH");canton.add("AS", "ZH");
		canton.add("BA", "GE");canton.add("BE", "GE");canton.add("BL", "GE");canton.add("BP", "GE");canton.add("BS", "GE");canton.add("BU", "GE");
		canton.add("FL", "BS");canton.add("FR", "BS");canton.add("GE", "BS");canton.add("GL", "BS");canton.add("GR", "BS");canton.add("JU", "BS");
		canton.add("LU", "ZH");canton.add("NE", "ZH");canton.add("NW", "ZH");canton.add("OW", "ZH");canton.add("PT", "ZG");canton.add("RP", "ZG");
		canton.add("SG", "SO");canton.add("SH", "SO");canton.add("SO", "SO");canton.add("SZ", "SO");canton.add("TG", "SO");canton.add("TI", "SO");
		canton.add("UR", "ZH");canton.add("VD", "ZH");canton.add("VS", "ZH");canton.add("ZG", "ZH");canton.add("ZH", "ZH");canton.add("12", "ZH");
		canton.add("ZU", "ZH");canton.add("null", "BS");canton.add("", "BS");canton.add("NULL", "BS");
		data.getDefinition().setAttributeType(attribute, canton);
		data.getDefinition().setDataType(attribute, DataType.STRING);
		data.getDefinition().setHierarchy(attribute, canton);
	}

	/**
	 * Hierarchy for countries
	 * @param data
	 * @param attribute
	 * @return hierarchy
	 */
	protected static DefaultHierarchy createHierarchyCountry(Data data, String attribute) {
		DefaultHierarchy country = Hierarchy.create();
		for (String c : Locale.getISOCountries()) {
			country.add(c, "USA");
		}
		country.add("AFG", "FR");country.add("AND", "FR");country.add("ARM", "FR");country.add("AUS", "FR");country.add("BDI", "FR");country.add("BDS", "FR");
		country.add("BIH", "FR");country.add("BOL", "FR");country.add("BRN", "FR");country.add("BRU", "FR");country.add("CAM", "FR");country.add("CDN", "FR");
		country.add("CHN", "FR");country.add("COM", "FR");country.add("DJI", "FR");country.add("DOM", "FR");country.add("EAK", "FR");country.add("EAT", "FR");
		country.add("EAU", "FR");country.add("EST", "FR");country.add("ETH", "FR");country.add("FIN", "FR");country.add("FJI", "FR");country.add("GAB", "FR");
		country.add("GBG", "FR");country.add("GBJ", "FR");country.add("GBM", "FR");country.add("GBZ", "FR");country.add("GCA", "FR");country.add("GUY", "FR");
		country.add("HKJ", "IT");country.add("IND", "IT");country.add("IRL", "IT");country.add("IRQ", "IT");country.add("KIR", "IT");country.add("KWT", "IT");
		country.add("LAO", "IT");country.add("LAR", "IT");country.add("MAL", "IT");country.add("MEX", "IT");country.add("MGL", "IT");country.add("MNE", "IT");
		country.add("MOC", "IT");country.add("MYA", "IT");country.add("NAM", "IT");country.add("NAU", "IT");country.add("NEP", "IT");country.add("NIC", "IT");
		country.add("PAL", "IT");country.add("PNG", "IT");country.add("PRK", "IT");country.add("RCA", "IT");country.add("RCB", "IT");country.add("RCH", "IT");
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
		
		// Check heuristic search
		System.out.println("Heuristic search threshold (config.getHeuristicSearchThreshold()): " + config.getHeuristicSearchThreshold() + ", ARXLattice.size(): " + result.getLattice().getSize());
		
		// Print info
		printResult(result, data);
		StatisticsEquivalenceClasses stat = result.getOutput(result.getGlobalOptimum(), false).getStatistics().getEquivalenceClassStatistics();
		System.out.print(stat.getAverageEquivalenceClassSize() + ", ");
		System.out.print(stat.getMaximalEquivalenceClassSize() + ", ");
		System.out.print(stat.getMinimalEquivalenceClassSize() + ", ");
		System.out.print(stat.getNumberOfEquivalenceClasses() + ", ");
		System.out.print(stat.getNumberOfRecords() + ", ");
		System.out.print(stat.getNumberOfSuppressedRecords());
		
		// Print results
        DataHandle optimum = result.getOutput(false);
        printOutput(optimum);
	}
	
	/**
	 * Print data input before anonymization
	 * 
	 * @param data
	 */
	private static void printInput(Data data) {
		System.out.println("------------------Input data: ");
		Iterator<String[]> inputIterator = data.getHandle().iterator();
		for (int i = 0; i < 20; i++) {
			System.out.println(Arrays.toString(inputIterator.next()));
		}
	}

	protected static void printOutput(DataHandle handle) {
		// Process results
		System.out.println("-------------Transformed data: ");
        final Iterator<String[]> itHandle = handle.iterator();
		for (int i = 0; i < 120; i++) {
			System.out.print(" ");
			System.out.println(Arrays.toString(itHandle.next()));
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
