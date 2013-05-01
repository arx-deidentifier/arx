/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.flash.test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deidentifier.flash.AttributeType;
import org.deidentifier.flash.AttributeType.Hierarchy;
import org.deidentifier.flash.Data;
import org.deidentifier.flash.FLASHConfiguration;
import org.deidentifier.flash.FLASHConfiguration.LDiversityCriterion;
import org.deidentifier.flash.FLASHConfiguration.TClosenessCriterion;
import org.deidentifier.flash.io.CSVHierarchyInput;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for data transformations
 * 
 * @author Prasser, Kohlmayer
 */
public abstract class TestDataTransformationsFromFileAbstract extends
        TestAnonymizer {

    public static class TestCaseResult {
        public int                                    k;
        public int                                    l;
        public double                                 c;
        public FLASHConfiguration.LDiversityCriterion lDiversityCriterion;
        public double                                 t;
        public FLASHConfiguration.TClosenessCriterion tClosenessCriterion;
        public Metric                                 metric;

        public double                                 relativeMaxOutliers;
        public String                                 dataset;
        public String                                 senstitiveAttribute;

        public double                                 optimalInformationLoss;
        public int[]                                  bestResult;
        public boolean                                practical;

        // ldiv
        // {new TestCaseResult(4.0, 5, LDiversityCriterion.ENTROPY ,
        // "occupation", 0.04d, "data/adult.csv", Metric.ENTROPY,
        // 228878.2039109519d, new int[] { 1, 0, 1, 1, 2, 2, 2, 1 })},
        public TestCaseResult(final double c,
                              final int l,
                              final LDiversityCriterion criterion,
                              final String sensitiveAttribute,
                              final double relativeMaxOutliers,
                              final String dataset,
                              final Metric metric,
                              final double optimalInformationLoss,
                              final int[] bestResult,
                              final boolean practical) {
            this.c = c;
            this.l = l;
            lDiversityCriterion = criterion;
            senstitiveAttribute = sensitiveAttribute;
            this.metric = metric;
            this.relativeMaxOutliers = relativeMaxOutliers;
            this.dataset = dataset;
            this.optimalInformationLoss = optimalInformationLoss;
            this.bestResult = bestResult;
            this.practical = practical;
        }

        // t-close
        // {new TestCaseResult(0.2, 100, TClosenessCriterion.EMD_EQUAL ,
        // "occupation", 0.0d, "data/adult.csv", Metric.DMSTAR, 4.56853172E8d,
        // new int[] { 1, 4, 1, 1, 3, 2, 2, 1 })},
        public TestCaseResult(final double t,
                              final int k,
                              final TClosenessCriterion criterion,
                              final String sensitiveAttribute,
                              final double relativeMaxOutliers,
                              final String dataset,
                              final Metric metric,
                              final double optimalInformationLoss,
                              final int[] bestResult,
                              final boolean practical) {
            this.t = t;
            this.k = k;
            tClosenessCriterion = criterion;
            senstitiveAttribute = sensitiveAttribute;
            this.metric = metric;
            this.relativeMaxOutliers = relativeMaxOutliers;
            this.dataset = dataset;
            this.optimalInformationLoss = optimalInformationLoss;
            this.bestResult = bestResult;
            this.practical = practical;
        }

        // k-anon
        // new TestCaseResult(5, 0.04, "data/adult.csv", Metric.ENTROPY,
        // 255559.854557311d, new int[] { 1, 0, 1, 1, 3, 2, 2, 0, 1 })
        public TestCaseResult(final int k,
                              final double relativeMaxOutliers,
                              final String dataset,
                              final Metric metric,
                              final double optimalInformationLoss,
                              final int[] bestResult,
                              final boolean practical) {
            this.k = k;
            this.metric = metric;
            this.relativeMaxOutliers = relativeMaxOutliers;
            this.dataset = dataset;
            this.optimalInformationLoss = optimalInformationLoss;
            this.bestResult = bestResult;
            this.practical = practical;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(final Object obj) {
            if (this == obj) { return true; }
            if (obj == null) { return false; }
            if (getClass() != obj.getClass()) { return false; }
            final TestCaseResult other = (TestCaseResult) obj;
            if (!Arrays.equals(bestResult, other.bestResult)) { return false; }
            if (Double.doubleToLongBits(c) != Double.doubleToLongBits(other.c)) { return false; }
            if (dataset == null) {
                if (other.dataset != null) { return false; }
            } else if (!dataset.equals(other.dataset)) { return false; }
            if (k != other.k) { return false; }
            if (l != other.l) { return false; }
            if (lDiversityCriterion != other.lDiversityCriterion) { return false; }
            if (metric != other.metric) { return false; }
            if (Double.doubleToLongBits(optimalInformationLoss) != Double.doubleToLongBits(other.optimalInformationLoss)) { return false; }
            if (Double.doubleToLongBits(relativeMaxOutliers) != Double.doubleToLongBits(other.relativeMaxOutliers)) { return false; }
            if (senstitiveAttribute == null) {
                if (other.senstitiveAttribute != null) { return false; }
            } else if (!senstitiveAttribute.equals(other.senstitiveAttribute)) { return false; }
            if (Double.doubleToLongBits(t) != Double.doubleToLongBits(other.t)) { return false; }
            if (tClosenessCriterion != other.tClosenessCriterion) { return false; }
            return true;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + Arrays.hashCode(bestResult);
            long temp;
            temp = Double.doubleToLongBits(c);
            result = (prime * result) + (int) (temp ^ (temp >>> 32));
            result = (prime * result) +
                     ((dataset == null) ? 0 : dataset.hashCode());
            result = (prime * result) + k;
            result = (prime * result) + l;
            result = (prime * result) +
                     ((lDiversityCriterion == null) ? 0
                             : lDiversityCriterion.hashCode());
            result = (prime * result) +
                     ((metric == null) ? 0 : metric.hashCode());
            temp = Double.doubleToLongBits(optimalInformationLoss);
            result = (prime * result) + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(relativeMaxOutliers);
            result = (prime * result) + (int) (temp ^ (temp >>> 32));
            result = (prime * result) +
                     ((senstitiveAttribute == null) ? 0
                             : senstitiveAttribute.hashCode());
            temp = Double.doubleToLongBits(t);
            result = (prime * result) + (int) (temp ^ (temp >>> 32));
            result = (prime * result) +
                     ((tClosenessCriterion == null) ? 0
                             : tClosenessCriterion.hashCode());
            return result;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "TestCaseResult [k=" + k + ", l=" + l + ", c=" + c +
                   ", lDiversityCriterion=" + lDiversityCriterion + ", t=" + t +
                   ", tClosenessCriterion=" + tClosenessCriterion +
                   ", metric=" + metric + ", relativeMaxOutliers=" +
                   relativeMaxOutliers + ", dataset=" + dataset +
                   ", senstitiveAttribute=" + senstitiveAttribute +
                   ", optimalInformationLoss=" + optimalInformationLoss +
                   ", bestResult=" + Arrays.toString(bestResult) + "]";
        }

    }

    protected final TestCaseResult testCase;

    public TestDataTransformationsFromFileAbstract(final TestCaseResult testCase) {
        this.testCase = testCase;
    }

    public Data
            createDataObject(final TestCaseResult testCase) throws IOException {

        final Data data = Data.create(testCase.dataset, ';');

        // Read generalization hierachies

        final FilenameFilter hierarchyFilter = new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                if (name.matches(testCase.dataset.substring(testCase.dataset.lastIndexOf("/") + 1,
                                                            testCase.dataset.length() - 4) +
                                 "_hierarchy_(.)+.csv")) {
                    return true;
                } else {
                    return false;
                }
            }
        };

        final File testDir = new File(testCase.dataset.substring(0,
                                                                 testCase.dataset.lastIndexOf("/")));
        final File[] genHierFiles = testDir.listFiles(hierarchyFilter);

        final Pattern pattern = Pattern.compile("_hierarchy_(.*?).csv");

        for (final File file : genHierFiles) {
            final Matcher matcher = pattern.matcher(file.getName());
            if (matcher.find()) {
                final CSVHierarchyInput hier = new CSVHierarchyInput(file, ';');
                final String attributeName = matcher.group(1);

                if (!attributeName.equalsIgnoreCase(testCase.senstitiveAttribute)) {
                    data.getDefinition()
                        .setAttributeType(attributeName,
                                          Hierarchy.create(hier.getHierarchy()));
                } else { // sensitive attribute
                    data.getDefinition()
                        .setAttributeType(attributeName,
                                          AttributeType.SENSITIVE_ATTRIBUTE);
                }

            }
        }

        return data;
    }

    public org.deidentifier.flash.metric.Metric<?>
            createMetric(final TestCaseResult testCase) {
        org.deidentifier.flash.metric.Metric<?> metric = null;
        switch (testCase.metric) {
        case PREC:
            metric = org.deidentifier.flash.metric.Metric.createPrecisionMetric();
            break;
        case HEIGHT:
            metric = org.deidentifier.flash.metric.Metric.createHeightMetric();
            break;
        case DMSTAR:
            metric = org.deidentifier.flash.metric.Metric.createDMStarMetric();
            break;
        case DM:
            metric = org.deidentifier.flash.metric.Metric.createDMMetric();
            break;
        case ENTROPY:
            metric = org.deidentifier.flash.metric.Metric.createEntropyMetric();
            break;
        case NMENTROPY:
            metric = org.deidentifier.flash.metric.Metric.createNMEntropyMetric();
            break;
        default:
            break;
        }
        return metric;
    }

    @Override
    @Before
    public void setUp() {
        // empty by design

    }

    @Test
    public abstract void testTestCases() throws IllegalArgumentException,
                                        IOException;

}
