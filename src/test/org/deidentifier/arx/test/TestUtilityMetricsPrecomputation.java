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

package org.deidentifier.arx.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.metric.Metric;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test for utility transformations.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
@RunWith(Parameterized.class)
public class TestUtilityMetricsPrecomputation extends TestUtilityMetricsPrecomputationAbstract {

    /**  TODO */
    private final static double threshold = 1d;

    /**
     * 
     *
     * @return
     * @throws IOException
     */
    @Parameters(name = "{index}:[{0}]")
    public static Collection<Object[]> cases() throws IOException {
        return Arrays.asList(new Object[][] {

                // entropy: criterion monotone metric monotone
                { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.0d).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", Metric.createEntropyMetric(true), Metric.createPrecomputedEntropyMetric(threshold, true)) },
                { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.0d).addCriterion(new DPresence(0.05, 0.15, DataSubset.create(Data.create("../arx-data/data-junit/adult.csv", ';'), Data.create("../arx-data/data-junit/adult_subset.csv", ';')))), "occupation", "../arx-data/data-junit/adult.csv", Metric.createEntropyMetric(true), Metric.createPrecomputedEntropyMetric(threshold, true)) },

                // entropy: criterion monotone metric non-monotone
                { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.0d).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", Metric.createEntropyMetric(false), Metric.createPrecomputedEntropyMetric(threshold, false)) },
                { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.0d).addCriterion(new DPresence(0.05, 0.15, DataSubset.create(Data.create("../arx-data/data-junit/adult.csv", ';'), Data.create("../arx-data/data-junit/adult_subset.csv", ';')))), "occupation", "../arx-data/data-junit/adult.csv", Metric.createEntropyMetric(false), Metric.createPrecomputedEntropyMetric(threshold, false)) },

                // loss: criterion monotone metric monotone
                { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.0d).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", Metric.createLossMetric(), Metric.createPrecomputedLossMetric(threshold)) },
                { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.0d).addCriterion(new DPresence(0.05, 0.15, DataSubset.create(Data.create("../arx-data/data-junit/adult.csv", ';'), Data.create("../arx-data/data-junit/adult_subset.csv", ';')))), "occupation", "../arx-data/data-junit/adult.csv", Metric.createLossMetric(), Metric.createPrecomputedLossMetric(threshold)) },

                // entropy: criterion non-monotone metric monotone
                { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.5d).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", Metric.createEntropyMetric(true), Metric.createPrecomputedEntropyMetric(threshold, true)) },
                { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.5d).addCriterion(new DPresence(0.05, 0.15, DataSubset.create(Data.create("../arx-data/data-junit/adult.csv", ';'), Data.create("../arx-data/data-junit/adult_subset.csv", ';')))), "occupation", "../arx-data/data-junit/adult.csv", Metric.createEntropyMetric(true), Metric.createPrecomputedEntropyMetric(threshold, true)) },

                // entropy: criterion non-monotone metric non-monotone
                { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.5d).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", Metric.createEntropyMetric(false), Metric.createPrecomputedEntropyMetric(threshold, false)) },
                { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.5d).addCriterion(new DPresence(0.05, 0.15, DataSubset.create(Data.create("../arx-data/data-junit/adult.csv", ';'), Data.create("../arx-data/data-junit/adult_subset.csv", ';')))), "occupation", "../arx-data/data-junit/adult.csv", Metric.createEntropyMetric(false), Metric.createPrecomputedEntropyMetric(threshold, false)) },

                // loss: criterion non-monotone metric monotone
                { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.5d).addCriterion(new KAnonymity(5)), "occupation", "../arx-data/data-junit/adult.csv", Metric.createLossMetric(), Metric.createPrecomputedLossMetric(threshold)) },
                { new ARXUtilityMetricsTestCase(ARXConfiguration.create(0.5d).addCriterion(new DPresence(0.05, 0.15, DataSubset.create(Data.create("../arx-data/data-junit/adult.csv", ';'), Data.create("../arx-data/data-junit/adult_subset.csv", ';')))), "occupation", "../arx-data/data-junit/adult.csv", Metric.createLossMetric(), Metric.createPrecomputedLossMetric(threshold)) },

        });
    }

    /**
     * 
     *
     * @param testCase
     */
    public TestUtilityMetricsPrecomputation(final ARXUtilityMetricsTestCase testCase) {
        super(testCase);
    }
}
