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

import java.util.Date;
import java.util.Map;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.StatisticsSummary;

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
    @SuppressWarnings("unchecked")
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

        // Print everything
        System.out.println("***************************");
        System.out.println("* Dumping the whole object*");
        System.out.println("***************************");
        System.out.println(data.getHandle().getStatistics().getSummaryStatistics(true));
        
        // Alter definition
        data.getDefinition().setDataType("age", DataType.INTEGER);
        data.getDefinition().setDataType("zipcode", DataType.DECIMAL);
        data.getDefinition().setDataType("date", DataType.DATE);

        // Print everything
        System.out.println("");
        System.out.println("***************************");
        System.out.println("* Dumping the whole object*");
        System.out.println("***************************");
        System.out.println(data.getHandle().getStatistics().getSummaryStatistics(true));
        
        // Access individual measures
        Map<String, StatisticsSummary<?>> statistics = data.getHandle().getStatistics().getSummaryStatistics(true);
        
        // For age
        System.out.println("");
        System.out.println("***************************");
        System.out.println("* Individual statistics   *");
        System.out.println("***************************");
        StatisticsSummary<Long> statisticsAge = (StatisticsSummary<Long>)statistics.get("age");
        if (statisticsAge.isGeometricMeanAvailable()) {
            System.out.println("Geometric mean of age");
            System.out.println(" - As double: " + statisticsAge.getGeometricMeanAsDouble());
            System.out.println(" - As value : " + statisticsAge.getGeometricMeanAsValue());
            System.out.println(" - As string: " + statisticsAge.getGeometricMeanAsString());
        }
        
        // For date
        System.out.println("");
        System.out.println("***************************");
        System.out.println("* Individual statistics   *");
        System.out.println("***************************");
        StatisticsSummary<Date> statisticsDate = (StatisticsSummary<Date>)statistics.get("date");
        if (statisticsDate.isSampleVarianceAvailable()) {
            System.out.println("Sample variance of date");
            System.out.println(" - As double: " + statisticsDate.getSampleVarianceAsDouble());
            System.out.println(" - As value : " + statisticsDate.getSampleVarianceAsValue());
            System.out.println(" - As string: " + statisticsDate.getSampleVarianceAsString());
        }
        if (statisticsDate.isArithmeticMeanAvailable()) {
            System.out.println("Arithmetic mean of date");
            System.out.println(" - As double: " + statisticsDate.getArithmeticMeanAsDouble());
            System.out.println(" - As value : " + statisticsDate.getArithmeticMeanAsValue());
            System.out.println(" - As string: " + statisticsDate.getArithmeticMeanAsString());
        }
        if (statisticsDate.isRangeAvailable()) {
            System.out.println("Range of date");
            System.out.println(" - As double: " + statisticsDate.getRangeAsDouble());
            System.out.println(" - As value : " + statisticsDate.getRangeAsValue());
            System.out.println(" - As string: " + statisticsDate.getRangeAsString());
        }
    }
}