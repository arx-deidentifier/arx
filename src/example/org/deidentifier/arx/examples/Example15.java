/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXLattice;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.metric.InformationLossCombined;
import org.deidentifier.arx.metric.Metric;

/**
 * This class implements an example on how to pretty-print information loss
 * 
 * @author Prasser, Kohlmayer
 */
public class Example15 extends Example {

    /**
     * Entry point.
     * 
     * @param args
     *            the arguments
     */
    public static void main(final String[] args) {

        // Define data
        final DefaultData data = Data.create();
        data.add("age", "gender", "zipcode");
        data.add("34", "male", "81667");
        data.add("45", "female", "81675");
        data.add("66", "male", "81925");
        data.add("70", "female", "81931");
        data.add("34", "female", "81931");
        data.add("70", "male", "81931");
        data.add("45", "male", "81931");

        // Define hierarchies
        final DefaultHierarchy age = Hierarchy.create();
        age.add("34", "<50", "*");
        age.add("45", "<50", "*");
        age.add("66", ">=50", "*");
        age.add("70", ">=50", "*");

        final DefaultHierarchy gender = Hierarchy.create();
        gender.add("male", "*");
        gender.add("female", "*");

        // Only excerpts for readability
        final DefaultHierarchy zipcode = Hierarchy.create();
        zipcode.add("81667", "8166*", "816**", "81***", "8****", "*****");
        zipcode.add("81675", "8167*", "816**", "81***", "8****", "*****");
        zipcode.add("81925", "8192*", "819**", "81***", "8****", "*****");
        zipcode.add("81931", "8193*", "819**", "81***", "8****", "*****");

        // Define the different attribute types
        data.getDefinition().setAttributeType("age", age);
        data.getDefinition().setAttributeType("gender", gender);
        data.getDefinition().setAttributeType("zipcode", zipcode);

        // set the minimal generalization height
        data.getDefinition().setMinimumGeneralization("zipcode", 3);
        data.getDefinition().setMinimumGeneralization("gender", 1);

        // Create an instance of the anonymizer
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        final ARXConfiguration config = ARXConfiguration.create();
        config.addCriterion(new KAnonymity(2));
        config.setMaxOutliers(0.1d);
        
        // Evaluate multiple metrics at once
        Set<Metric<?>> metrics = new HashSet<Metric<?>>();
        metrics.add(Metric.createAECSMetric());
        metrics.add(Metric.createDMMetric());
        metrics.add(Metric.createDMStarMetric());
        metrics.add(Metric.createEntropyMetric());
        metrics.add(Metric.createHeightMetric());
        metrics.add(Metric.createNMEntropyMetric());
        metrics.add(Metric.createPrecisionMetric());
        config.setMetric(Metric.createCombinedMetric(Metric.createNMEntropyMetric(), metrics));
        
        try {

            // Now anonymize
            ARXResult result = anonymizer.anonymize(data, config);
            
            // Obtain results
            ARXLattice lattice = result.getLattice();
            ARXNode bottom = lattice.getBottom();
            ARXNode top = lattice.getTop();
            ARXNode optimum = result.getGlobalOptimum();

            // Make sure bottom and top are checked
            result.getHandle(bottom);
            result.getHandle(top);
            
            // Obtain infoloss
            InformationLossCombined bottomLoss = (InformationLossCombined)bottom.getMinimumInformationLoss();
            InformationLossCombined topLoss = (InformationLossCombined)top.getMinimumInformationLoss();
            InformationLossCombined optimumLoss = (InformationLossCombined)optimum.getMinimumInformationLoss();
            
            // Print results for all metrics
            System.out.println("Information loss per metric:");
            for (Metric<?> metric : metrics) {

                double loss = (optimumLoss.getValue(metric).getValue() - bottomLoss.getValue(metric).getValue()) /
                              (topLoss.getValue(metric).getValue() - bottomLoss.getValue(metric).getValue());
                
                System.out.print(" - ");
                System.out.print(metric.getClass().getSimpleName());
                System.out.print(": ");
                System.out.print(loss * 100);
                System.out.println(" [%]");
            }
            
            // Print results for all QIs
            System.out.println("Information loss per QI:");
            for (String attr : data.getDefinition().getQuasiIdentifyingAttributes()) {
                
                double loss = (double)optimum.getGeneralization(attr) / (double)(data.getDefinition().getHierarchyHeight(attr)-1);
                System.out.print(" - ");
                System.out.print(attr);
                System.out.print(": ");
                System.out.print(loss * 100);
                System.out.println(" [%]");
            }

        } catch (final IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
