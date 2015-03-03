/*
 * ARX: Powerful Data Anonymization
 * Copyright 2014 Karol Babioch <karol@babioch.de>
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

package org.deidentifier.arx.examples;

import java.io.IOException;
import java.sql.SQLException;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataSource;
import org.deidentifier.arx.DataType;

/**
 * This class demonstrates the use of the data import facilities provided by the
 * ARX framework. Data can be imported from various types of sources, e.g. CSV
 * files, Excel files and databases (using JDBC). The API is mostly the same for
 * all of these sources, although not all options might be available in each
 * case. Refer to the comments further down below for details about particular
 * sources.
 * 
 * @author Karol Babioch
 * @author Fabian Prasser
 */
public class Example21 extends Example {

    /**
     * Main entry point.
     *
     * @param args
     * @throws IOException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static void main(final String[] args) throws IOException,
                                                SQLException,
                                                ClassNotFoundException {

        exampleCSV();
        exampleExcel();
        exampleJDBC();
    }

    /**
     * This method demonstrates the import of data from a simple CSV file. It
     * uses more advanced features than {@link #Example2}. Columns are renamed,
     * and individual columns can be ignored. Furthermore a data type for each
     * column is specified, which describes the format of the appropriate data.
     *
     * @throws IOException
     */
    private static void exampleCSV() throws IOException {

        // Define configuration for CSV file
        // The most interesting parameter is the last one, which defines
        // whether or not the file contains a header assigning a name to each
        // individual column, which can be used to address the column later on
        DataSource source = DataSource.createCSVSource("data/test.csv", ';', true);

        // Add columns
        // Note that there are different means to specify a column. The first
        // two columns are addressed based on their name. It is also possible
        // to rename columns, which might be an interesting option to manipulate
        // the output. Be aware however, that name based addressing will only
        // work for types that implement the {@link IImportColumnNamed}
        // interface. CSV and Excel files need to contain a header for this to
        // work. "Index based" addressing on the other hand is currently
        // supported by all types and is therefore guaranteed to work. This
        // is the way the last column is addressed by. If the source does not
        // contain a dedicated name for this column one will be assigned
        // automatically, following the "Column #x" style, where x will be
        // the number of the column.
        source.addColumn(2, DataType.STRING); // zipcode (index based addressing)
        source.addColumn("gender", DataType.STRING); // gender (named addressing)
        source.addColumn("age", "renamed", DataType.INTEGER); // age (named addressing + alias name)

        // In the output dataset, the columns will appear in the same order as
        // specified by the order of calls to addColumn().

        // Create data object
        final Data data = Data.create(source);

        // Print to console
        print(data.getHandle());
        System.out.println("\n");
    }

    /**
     * This method demonstrates the import of data from an Excel file. It uses
     * more advanced features than {@link #Example2}. Columns are renamed, and
     * individual columns can be ignored. Furthermore a data type for each
     * column is specified, which describes the format of the appropriate data.
     * 
     * Internally it makes use of <a href="https://poi.apache.org/">POI<a/>.
     * 
     * Refer to {@link #exampleCSV()} for detailed comments about the meaning of
     * certain parameters, as basically everything mentioned there also applies
     * here.
     * 
     * @throws IOException
     *             In case of IO errors with the given file
     */
    private static void exampleExcel() throws IOException {

        // Define configuration for Excel file
        DataSource source = DataSource.createExcelSource("data/test.xls", 0, true);

        // Add columns
        source.addColumn(2, DataType.STRING); // zipcode (index based addressing)
        source.addColumn("gender", DataType.STRING); // gender (named addressing)
        source.addColumn("age", "renamed", DataType.INTEGER); // age (named addressing + alias name)

        // Create data object
        final Data data = Data.create(source);

        // Print to console
        print(data.getHandle());
        System.out.println("\n");
    }

    /**
     * This method demonstrates the import of data from a JDBC data source.
     * Columns can be renamed, or selected individually. Furthermore a data type
     * for each column is specified, which describes the format of the
     * appropriate data.
     * 
     * This example uses SQLite, and uses the example database that is contained
     * within the `data` directory. Note however, that in principal every JDBC
     * connection can be used here.
     * 
     * Refer to {@link #exampleCSV()} for detailed comments about the meaning of
     * certain parameters, as basically everything mentioned there also applies
     * here. Obviously columns can always be addressed by name in this scenario.
     * 
     * @throws IOException
     *             In case of IO errors with the given file
     * @throws SQLException
     *             In case of SQL errors with given database
     * @throws ClassNotFoundException
     *             In case there is no JDBC driver
     */
    private static void exampleJDBC() throws IOException,
                                     SQLException,
                                     ClassNotFoundException {

        // Load JDBC driver
        Class.forName("org.sqlite.JDBC");

        // Configuration for JDBC source
        DataSource source = DataSource.createJDBCSource("jdbc:sqlite:data/test.db",
                                                        "test");

        // Add columns
        source.addColumn(2, DataType.STRING); // zipcode (index based addressing)
        source.addColumn("gender", DataType.STRING); // gender (named addressing)
        source.addColumn("age", "renamed", DataType.INTEGER); // age (named addressing + alias name)

        // Create data object
        final Data data = Data.create(source);

        // Print to console
        print(data.getHandle());
        System.out.println("\n");
    }
}
