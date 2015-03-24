/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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

import org.deidentifier.arx.Data;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.DataType;

/**
 * This class implements an example of how to compute summary statistics
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Example30 extends Example {

    /**
     * Entry point.
     * 
     * @param args
     *            the arguments
     */
    public static void main(final String[] args) {

        // Define data
        DefaultData data = Data.create();
        data.add("age", "gender", "zipcode", "date");
        data.add("45", "female", "81675", "01.01.1982");
        data.add("34", "male", "81667", "11.05.1982");
        data.add("NULL", "male", "81925", "31.08.1982");
        data.add("70", "female", "81931", "02.07.1982");
        data.add("34", "female", null, "05.01.1982");
        data.add("70", "male", "81931", "24.03.1982");
        data.add("45", "male", "81931", "NULL");
        
        System.out.println(data.getHandle().getStatistics().getSummaryStatistics(true));
        
        data.getDefinition().setDataType("age", DataType.INTEGER);
        data.getDefinition().setDataType("zipcode", DataType.DECIMAL);
        data.getDefinition().setDataType("date", DataType.DATE);

        System.out.println(data.getHandle().getStatistics().getSummaryStatistics(true));
    }
}
