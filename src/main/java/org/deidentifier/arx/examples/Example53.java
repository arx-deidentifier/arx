/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
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

import java.awt.Desktop;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.certificate.ARXCertificate;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.io.CSVHierarchyInput;
import org.deidentifier.arx.io.CSVSyntax;
import org.deidentifier.arx.metric.Metric;

/**
 * This class implements an example on how to generate reports
 *
 * @author Fabian Prasser
 */
public class Example53 extends Example {
    
    /**
     * Loads a dataset from disk
     * @param dataset
     * @return
     * @throws IOException
     */
    public static Data createData(final String dataset) throws IOException {
        
        // Load data
        Data data = Data.create("data/" + dataset + ".csv", StandardCharsets.UTF_8, ';');
        
        // Read generalization hierarchies
        FilenameFilter hierarchyFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.matches(dataset + "_hierarchy_(.)+.csv")) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        
        // Create definition
        File testDir = new File("data/");
        File[] genHierFiles = testDir.listFiles(hierarchyFilter);
        Pattern pattern = Pattern.compile("_hierarchy_(.*?).csv");
        for (File file : genHierFiles) {
            Matcher matcher = pattern.matcher(file.getName());
            if (matcher.find()) {
                CSVHierarchyInput hier = new CSVHierarchyInput(file, StandardCharsets.UTF_8, ';');
                String attributeName = matcher.group(1);
                data.getDefinition().setAttributeType(attributeName, Hierarchy.create(hier.getHierarchy()));
            }
        }
        
        return data;
    }
    
    /**
     * Entry point.
     * 
     * @param args the arguments
     * @throws ParseException
     * @throws IOException
     * @throws NoSuchAlgorithmException 
     */
    public static void main(String[] args) throws ParseException, IOException, NoSuchAlgorithmException {
        
        Data data = createData("adult");
        
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new KAnonymity(5));
        config.setSuppressionLimit(1d);
        config.setQualityModel(Metric.createLossMetric());
        
        ARXResult result = anonymizer.anonymize(data, config);

        // Create certificate
        ARXCertificate certificate = ARXCertificate.create(data.getHandle(), data.getDefinition(),
                                                          config, result, result.getGlobalOptimum(), 
                                                          result.getOutput(),
                                                          new CSVSyntax());
        File file = File.createTempFile("arx", ".pdf");
        certificate.save(file);
        
        // Open
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file);
        }
    }
}