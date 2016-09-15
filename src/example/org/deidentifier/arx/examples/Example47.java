package org.deidentifier.arx.examples;

import org.apache.commons.lang.StringUtils;
import org.deidentifier.arx.*;
import org.deidentifier.arx.risk.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Example47 extends Example {

    /**
     * Entry point.
     *
     * @param args the arguments
     */
    public static void main(String[] args) throws IOException {

        Data data = loadData();
        //Data data = loadCsv();

        // flag every identifier as quasi identifier
        for (int i = 0; i < data.getHandle().getNumColumns(); i++) {
            data.getDefinition().setAttributeType(data.getHandle().getAttributeName(i), AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
        }

        // Perform risk analysis
        System.out.println("\n - Input data");
        print(data.getHandle());

        System.out.println("\n - Quasi-identifiers with values (in percent):");
        analyzeAttributes(data.getHandle());
    }

    /**
     * Calculate Alpha Distinction and Separation
     *
     * @param handle the data handle
     */
    private static void analyzeAttributes(DataHandle handle) {
        ARXPopulationModel populationmodel = ARXPopulationModel.create(ARXPopulationModel.Region.USA);
        RiskEstimateBuilder builder = handle.getRiskEstimator(populationmodel);
        RiskModelAttributes riskmodel = builder.getAttributeRisks();

        // output
        printPrettyTable(riskmodel.getAttributeRisks());
    }

    private static Data loadCsv() throws IOException {
        String csvFile = "data/example.csv";

        DataSource source = DataSource.createCSVSource(csvFile, StandardCharsets.UTF_8, ';', true);
        source.addColumn("sex", DataType.STRING);
        source.addColumn("age", DataType.INTEGER);
        source.addColumn("race", DataType.STRING);
        source.addColumn("marital-status", DataType.STRING);
        source.addColumn("education", DataType.STRING);
        source.addColumn("native-country", DataType.STRING);
        source.addColumn("workclass", DataType.STRING);
        source.addColumn("occupation", DataType.STRING);
        source.addColumn("salary-class", DataType.STRING);
        source.addColumn("ldl", DataType.STRING);
        return Data.create(source);
    }

    private static Data loadData() {
        // Define data
        Data.DefaultData data = Data.create();
        data.add("age", "sex", "state");
        data.add("20", "Female", "CA");
        data.add("30", "Female", "CA");
        data.add("40", "Female", "TX");
        data.add("20", "Male", "NY");
        data.add("40", "Male", "CA");

        data.add("53", "Male", "CA");
        data.add("76", "Male", "EU");
        data.add("40", "Female", "AS");
        data.add("32", "Female", "CA");
        data.add("88", "Male", "CA");
        data.add("48", "Female", "AS");
        data.add("76", "Male", "UU");
        return data;
    }

    private static void printPrettyTable(RiskModelAttributes.QuasiIdentifierRisk[] quasiIdentifiers) {
        // get char count of longest quasi-identifier
        ;
        int charCountLongestQi = quasiIdentifiers[quasiIdentifiers.length-1].getIdentifier().toString().length();

        // make sure that there is enough space for the table header strings
        charCountLongestQi = Math.max(charCountLongestQi, 12);

        // calculate space needed
        String leftAlignFormat = "| %-" + charCountLongestQi + "s | %13.2f | %12.2f |%n";

        // add 2 spaces that are in the string above on the left and right side of the first pattern
        charCountLongestQi += 2;

        // subtract the char count of the column header string to calculate
        // how many spaces we need for filling up to the right columnborder
        int spacesAfterColumHeader = charCountLongestQi - 12;

        System.out.format("+" + StringUtils.repeat("-", charCountLongestQi) + "+---------------+--------------+%n");
        System.out.format("| Identifier " + StringUtils.repeat(" ", spacesAfterColumHeader) + "| α-Distinction | α-Separation |%n");
        System.out.format("+" + StringUtils.repeat("-", charCountLongestQi) + "+---------------+--------------+%n");
        for (RiskModelAttributes.QuasiIdentifierRisk quasiIdentifier : quasiIdentifiers) {
            // print every Quasi-Identifier
            System.out.format(leftAlignFormat, quasiIdentifier.getIdentifier(), quasiIdentifier.getDistinction() * 100, quasiIdentifier.getSeparation() * 100);
        }
        System.out.format("+" + StringUtils.repeat("-", charCountLongestQi) + "+---------------+--------------+%n");
    }
}