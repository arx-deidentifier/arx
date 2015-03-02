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
 * This class implements an example on how to use data cleansing using the DataSource functionality.
 * 
 * @author Florian Kohlmayer
 * @author Fabian Prasser
 */
public class Example28 extends Example {

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
        // exampleExcel();
        // exampleJDBC();
    }

    /**
     * This method imports data from a simple CSV file, set a data type and replace all non-matching values with NULL values.
     *
     * @throws IOException
     */
    private static void exampleCSV() throws IOException {

        DataSource source = DataSource.createCSVSource("data/test_dirty.csv", ';', true);
        source.addColumn("age", DataType.INTEGER, true);

        // Create data object
        final Data data = Data.create(source);

        // Print to console
        print(data.getHandle());
        System.out.println("\n");
    }

}
