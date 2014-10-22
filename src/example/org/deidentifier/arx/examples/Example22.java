/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.criteria.EntropyLDiversity;
import org.deidentifier.arx.io.CSVHierarchyInput;
import org.deidentifier.arx.metric.Metric;

/**
 * This class implements an example on how to apply l-diversity criteria 
 * without protecting sensitive assocations
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Example22 extends Example {

    /**
     * Entry point.
     * 
     * @param args
     *            the arguments
     */
    public static void main(final String[] args) throws IOException {
        
        Data data = createData("adult");
        data.getDefinition().setAttributeType("occupation", AttributeType.SENSITIVE_ATTRIBUTE);
        
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXConfiguration config = ARXConfiguration.create();
        config.addCriterion(new EntropyLDiversity("occupation", 5));
        config.setMaxOutliers(0.04d);
        config.setProtectSensitiveAssociations(false);
        config.setMetric(Metric.createEntropyMetric());
        
        // Anonymize
        ARXResult result = anonymizer.anonymize(data, config);
        printResult(result, data);
    }

    public static Data createData(final String dataset) throws IOException {

        final Data data = Data.create("data/" + dataset + ".csv", ';');

        // Read generalization hierachies
        final FilenameFilter hierarchyFilter = new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                if (name.matches(dataset + "_hierarchy_(.)+.csv")) {
                    return true;
                } else {
                    return false;
                }
            }
        };

        final File testDir = new File("data/");
        final File[] genHierFiles = testDir.listFiles(hierarchyFilter);

        final Pattern pattern = Pattern.compile("_hierarchy_(.*?).csv");

        for (final File file : genHierFiles) {
            final Matcher matcher = pattern.matcher(file.getName());
            if (matcher.find()) {
                final CSVHierarchyInput hier = new CSVHierarchyInput(file, ';');
                final String attributeName = matcher.group(1);
                data.getDefinition().setAttributeType(attributeName, Hierarchy.create(hier.getHierarchy()));
            }
        }

        return data;
    }
}
