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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.mahout.math.Arrays;
import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.ARXStackelbergConfiguration;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataSelector;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.criteria.StackelbergPrivacyModel;
import org.deidentifier.arx.io.CSVHierarchyInput;
import org.deidentifier.arx.metric.Metric;
import org.deidentifier.arx.metric.v2.MetricSDNMPublisherBenefit;

/**
 * Examples of using the Stackelberg game for de-identifying the Adult dataset
 * based on the journalist attacker model with an explicit population table
 *
 * @author Fabian Prasser
 */
public class Example48 extends Example {

    /**
     * Loads a dataset from disk
     * @param dataset
     * @return
     * @throws IOException
     */
    public static Data createData(final String dataset) throws IOException {

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
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        
        // Load the dataset
        Data data = createData("adult");
        
        // We select all records with "sex=Male" to serve as the dataset which we will de-identify.
        // The overall dataset serves as our population table.
        DataSubset subset = DataSubset.create(data, DataSelector.create(data).field("sex").equals("Male"));
        
        // Config from PLOS|ONE paper
        solve(data, ARXStackelbergConfiguration.create()
                                               .setAdversaryCost(4d)
                                               .setAdversaryGain(300d)
                                               .setPublisherLoss(300d)
                                               .setPublisherBenefit(1200d)
                                               .setJournalistAttackerModel(subset));

        // Larger publisher loss
        solve(data, ARXStackelbergConfiguration.create()
                                               .setAdversaryCost(4d)
                                               .setAdversaryGain(300d)
                                               .setPublisherLoss(600d)
                                               .setPublisherBenefit(1200d)
                                               .setJournalistAttackerModel(subset));

        // Even larger publisher loss
        solve(data, ARXStackelbergConfiguration.create()
                                               .setAdversaryCost(4d)
                                               .setAdversaryGain(300d)
                                               .setPublisherLoss(1200d)
                                               .setPublisherBenefit(1200d)
                                               .setJournalistAttackerModel(subset));

        // Larger publisher loss and less adversary costs
        solve(data, ARXStackelbergConfiguration.create()
                                               .setAdversaryCost(2d)
                                               .setAdversaryGain(300d)
                                               .setPublisherLoss(600d)
                                               .setPublisherBenefit(1200d)
                                               .setJournalistAttackerModel(subset));

    }

    private static void solve(Data data, ARXStackelbergConfiguration config) throws IOException {
        
        // Release
        data.getHandle().release();
        
        // Configure
        ARXConfiguration arxconfig = ARXConfiguration.create();
        
        // Create model for measuring publisher's benefit
        MetricSDNMPublisherBenefit stackelbergMetric = Metric.createPublisherBenefitMetric(config);
        
        // Create privacy model for the game-theoretic approach
        StackelbergPrivacyModel stackelbergPrivacyModel = new StackelbergPrivacyModel(config);
        
        // Configure ARX
        arxconfig.setMaxOutliers(1d);
        arxconfig.setMetric(stackelbergMetric);
        arxconfig.addCriterion(stackelbergPrivacyModel);

        // Anonymize
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXResult result = anonymizer.anonymize(data, arxconfig);
        ARXNode node = result.getGlobalOptimum();
        DataHandle handle = result.getOutput(node, false).getView();
        
        // Print stuff
        System.out.println("Data: " + data.getHandle().getView().getNumRows() + " records with " + data.getDefinition().getQuasiIdentifyingAttributes().size() + " quasi-identifiers");
        System.out.println(" - Configuration: " + config.toString());
        System.out.println(" - Solution: " + Arrays.toString(node.getTransformation()));
        System.out.println("   * Optimal: " + result.getLattice().isComplete());
        System.out.println("   * Time needed: " + result.getTime() + "[ms]");
        System.out.println("   * Minimal reduction in publisher benefit: " + result.getConfiguration().getMetric().createMinInformationLoss());
        System.out.println("   * Maximal reduction in publisher benefit: " + result.getConfiguration().getMetric().createMaxInformationLoss());
        System.out.println("   * Reduction in publisher benefit: " + node.getMinimumInformationLoss() + " (" +
                           node.getMinimumInformationLoss().relativeTo(result.getConfiguration().getMetric().createMinInformationLoss(),
                                                                       result.getConfiguration().getMetric().createMaxInformationLoss()) * 100 + "%)");
        System.out.println("   * Suppressed records: " + handle.getStatistics().getEquivalenceClassStatistics().getNumberOfOutlyingTuples());
 
    }
}