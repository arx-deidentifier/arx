/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2014 Karol Babioch <karol@babioch.de>
 * Copyright (C) 2014 Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.examples;

import java.io.IOException;
import java.sql.SQLException;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataSource;
import org.deidentifier.arx.DataType;

/**
 * This class demonstrates the use of data import functionality provided by ARX
 * 
 * @author Karol Babioch
 * @author Fabian Prasser
 */
public class Example21 extends Example {

    /**
     * Main entry point
     * @throws SQLException 
     * @throws ClassNotFoundException 
     */
    public static void main(final String[] args) throws IOException, SQLException, ClassNotFoundException {

        exampleCSV();
        exampleExcel();
        exampleJDBC();
    }

    /**
     * This class implements an example on how to use the API to import data from
     * a CSV file. It uses more advanced features than {@link #Example2}. Columns
     * are renamed and could be ignored altogether.
     */
    private static void exampleCSV() throws IOException {
        
        // Define configuration for CSV file
        DataSource source = DataSource.createCSVSource("data/test.csv", ';', true);

        // Add columns (index, name and datatype) to configuration
        // The name is optional and can be detected/assigned automatically
        source.addColumn(0, "renamed", DataType.INTEGER);
        source.addColumn(1, DataType.STRING);
        source.addColumn(2, DataType.STRING);

        // Create data object
        final Data data = Data.create(source);

        // Print to console
        print(data.getHandle());
    }

    /**
     * This class demonstrates how to use the API to import data from an Excel
     * source It is loosely based upon previous examples
     * as the API is quite similar.
     */
    private static void exampleExcel() throws IOException {
        

        // Define configuration for Excel file
        DataSource source = DataSource.createExcelSource("data/test.xls", 0, true);

        // Add columns (index, name and datatype) to configuration
        // The name is optional and can be detected/assigned automatically
        source.addColumn("age", "renamed", DataType.INTEGER);
        source.addColumn(1, DataType.STRING);
        source.addColumn(2, DataType.STRING);

        // Create data object
        final Data data = Data.create(source);

        // Print to console
        print(data.getHandle());
    }
    
    /**
     * This class demonstrates how to use the API to import data from a JDBC
     * source (SQLite in this case). It is loosely based upon previous examples
     * as the API is quite similar.
     * @throws ClassNotFoundException 
     */
    private static void exampleJDBC() throws IOException, SQLException, ClassNotFoundException {

        // Configuration for JDBC source
        Class.forName("org.sqlite.JDBC");
        DataSource source = DataSource.createJDBCSource("jdbc:sqlite:data/test.db", "test");

        // Add columns (index, name and datatype) to configuration
        // The name is optional and can be detected/assigned automatically
        source.addColumn("age", "renamed", DataType.INTEGER);
        source.addColumn(1, DataType.STRING);
        source.addColumn(2, DataType.STRING);

        // Create data object
        final Data data = Data.create(source);

        // Print to console
        print(data.getHandle());
    }
}
