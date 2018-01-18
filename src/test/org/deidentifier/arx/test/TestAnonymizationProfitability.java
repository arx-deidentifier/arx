/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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

package org.deidentifier.arx.test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXCostBenefitConfiguration;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.criteria.ProfitabilityJournalistNoAttack;
import org.deidentifier.arx.criteria.ProfitabilityJournalist;
import org.deidentifier.arx.criteria.ProfitabilityProsecutorNoAttack;
import org.deidentifier.arx.criteria.ProfitabilityProsecutor;
import org.deidentifier.arx.metric.Metric;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test for anonymization based on a monetary cost/benefit analysis
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
@RunWith(Parameterized.class)
public class TestAnonymizationProfitability extends AbstractAnonymizationTest {
    
    /**
     * Returns a set of tests
     * @return
     * @throws IOException 
     */
    @Parameters(name = "{index}:[{0}]")
    public static Collection<Object[]> cases() throws IOException {
        return Arrays.asList(new Object[][] {
                                              /* 0 */{ new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPublisherPayoutMetric(false)).addPrivacyModel(new ProfitabilityJournalistNoAttack(DataSubset.create(Data.create("./data/adult.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/adult_subset.csv", StandardCharsets.UTF_8, ';')))).setCostBenefitConfiguration(getConfig1()), "occupation", "./data/adult.csv", 2974380.2122727702, new int[] { 1, 3, 1, 1, 3, 2, 1, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createLossMetric()).addPrivacyModel(new ProfitabilityJournalist(DataSubset.create(Data.create("./data/adult.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/adult_subset.csv", StandardCharsets.UTF_8, ';')))).setCostBenefitConfiguration(getConfig2()), "occupation", "./data/adult.csv", 0.20171471140567654, new int[] { 0, 4, 0, 0, 2, 1, 0, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createPrecomputedEntropyMetric(0.1d, false)).addPrivacyModel(new ProfitabilityProsecutor()).setCostBenefitConfiguration(getConfig1()), "occupation", "./data/adult.csv", 0.0, new int[] { 0, 0, 0, 0, 0, 0, 0, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createPublisherPayoutMetric(false)).addPrivacyModel(new ProfitabilityJournalistNoAttack(DataSubset.create(Data.create("./data/adult.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/adult_subset.csv", StandardCharsets.UTF_8, ';')))).setCostBenefitConfiguration(getConfig3()), "occupation", "./data/adult.csv", 289703.2817537615, new int[] { 0, 1, 1, 2, 3, 2, 1, 1 }, false) },
                                             /*4*/ { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPublisherPayoutMetric(false)).addPrivacyModel(new ProfitabilityJournalistNoAttack(DataSubset.create(Data.create("./data/adult.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/adult_subset.csv", StandardCharsets.UTF_8, ';')))).setCostBenefitConfiguration(getConfig2()), "occupation", "./data/adult.csv", 2095548.2581994554, new int[] { 1, 3, 0, 1, 3, 0, 0, 1 }, true) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyBasedInformationLossMetric()).addPrivacyModel(new ProfitabilityJournalist(DataSubset.create(Data.create("./data/adult.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/adult_subset.csv", StandardCharsets.UTF_8, ';')))).setCostBenefitConfiguration(getConfig4()), "occupation", "./data/adult.csv", 1982.8859574224414, new int[] { 1, 3, 1, 0, 3, 1, 1, 0 }, true) },
                                             /*6*/ { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createEntropyBasedInformationLossMetric()).addPrivacyModel(new ProfitabilityProsecutorNoAttack()), "Highest level of school completed", "./data/atus.csv", 111673.8361416946, new int[] { 0, 3, 0, 0, 0, 0, 0, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createLossMetric()).addPrivacyModel(new ProfitabilityJournalist(DataSubset.create(Data.create("./data/atus.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/atus_subset.csv", StandardCharsets.UTF_8, ';')))), "Highest level of school completed", "./data/atus.csv", 0.0, new int[] { 0, 0, 0, 0, 0, 0, 0, 0 }, false) },
                                             /*8*/ { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d, false)).addPrivacyModel(new ProfitabilityProsecutorNoAttack()), "Highest level of school completed", "./data/atus.csv", 2650821.4299037806, new int[] { 0, 3, 0, 1, 1, 1, 2, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPublisherPayoutMetric(false)).addPrivacyModel(new ProfitabilityProsecutor()), "Highest level of school completed", "./data/atus.csv", 1.44033E7, new int[] { 0, 0, 0, 0, 0, 0, 0, 0 }, false) },
                                              /* 10 */{ new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createDiscernabilityMetric(true)).addPrivacyModel(new ProfitabilityProsecutorNoAttack()), "Highest level of school completed", "./data/atus.csv", 6.820701679E9, new int[] { 0, 5, 0, 2, 1, 1, 2, 2 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createDiscernabilityMetric(true)).addPrivacyModel(new ProfitabilityJournalist(DataSubset.create(Data.create("./data/atus.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/atus_subset.csv", StandardCharsets.UTF_8, ';')))).setCostBenefitConfiguration(getConfig3()), "Highest level of school completed", "./data/atus.csv", 5.7646609E7, new int[] { 0, 5, 0, 2, 1, 1, 2, 1 }, false) },
                                              /*12*/{ new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createEntropyBasedInformationLossMetric()).addPrivacyModel(new ProfitabilityProsecutorNoAttack()), "Highest level of school completed", "./data/atus.csv", 111673.8361416946, new int[] { 0, 3, 0, 0, 0, 0, 0, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createPublisherPayoutMetric(false)).addPrivacyModel(new ProfitabilityProsecutor()).setCostBenefitConfiguration(getConfig3()), "Highest level of school completed", "./data/atus.csv", 4.095311485910724E7, new int[] { 0, 0, 1, 2, 2, 2, 2, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createLossMetric()).addPrivacyModel(new ProfitabilityJournalistNoAttack(DataSubset.create(Data.create("./data/atus.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/atus_subset.csv", StandardCharsets.UTF_8, ';')))), "Highest level of school completed", "./data/atus.csv", 0.20634008201191079, new int[] { 1, 4, 0, 0, 2, 0, 1, 1 }, true) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createDiscernabilityMetric(true)).addPrivacyModel(new ProfitabilityProsecutor()).setCostBenefitConfiguration(getConfig1()), "Highest level of school completed", "./data/atus.csv", 2.241709429E9, new int[] { 0, 0, 0, 0, 0, 0, 0, 0 }, true) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPublisherPayoutMetric(false)).addPrivacyModel(new ProfitabilityProsecutorNoAttack()).setCostBenefitConfiguration(getConfig3()), "Highest level of school completed", "./data/atus.csv", 1.1135948628393587E7, new int[] { 2, 2, 0, 0, 0, 0, 0, 1 }, true) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createLossMetric()).addPrivacyModel(new ProfitabilityJournalist(DataSubset.create(Data.create("./data/atus.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/atus_subset.csv", StandardCharsets.UTF_8, ';')))), "Highest level of school completed", "./data/atus.csv", 0.0, new int[] { 0, 0, 0, 0, 0, 0, 0, 0 }, true) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createDiscernabilityMetric(true)).addPrivacyModel(new ProfitabilityProsecutorNoAttack()).setCostBenefitConfiguration(getConfig2()), "RAMNTALL", "./data/cup.csv", 63457.0, new int[] { 0, 0, 0, 0, 0, 0, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyBasedInformationLossMetric()).addPrivacyModel(new ProfitabilityProsecutor()).setCostBenefitConfiguration(getConfig4()), "RAMNTALL", "./data/cup.csv", 39707.30534178496, new int[] { 3, 4, 0, 2, 0, 2, 1 }, false) },
                                              /* 20 */{ new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyBasedInformationLossMetric()).addPrivacyModel(new ProfitabilityJournalistNoAttack(DataSubset.create(Data.create("./data/cup.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/cup_subset.csv", StandardCharsets.UTF_8, ';')))).setCostBenefitConfiguration(getConfig3()), "RAMNTALL", "./data/cup.csv", 3494.585486411533, new int[] { 3, 4, 1, 2, 0, 1, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addPrivacyModel(new ProfitabilityJournalist(DataSubset.create(Data.create("./data/cup.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/cup_subset.csv", StandardCharsets.UTF_8, ';')))).setCostBenefitConfiguration(getConfig2()), "RAMNTALL", "./data/cup.csv", 117650.3776118251, new int[] { 2, 4, 0, 1, 0, 3, 2 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createLossMetric()).addPrivacyModel(new ProfitabilityProsecutorNoAttack()).setCostBenefitConfiguration(getConfig1()), "RAMNTALL", "./data/cup.csv", 0.8114473285278123, new int[] { 5, 4, 1, 0, 1, 4, 4 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createEntropyBasedInformationLossMetric()).addPrivacyModel(new ProfitabilityJournalist(DataSubset.create(Data.create("./data/cup.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/cup_subset.csv", StandardCharsets.UTF_8, ';')))).setCostBenefitConfiguration(getConfig4()), "RAMNTALL", "./data/cup.csv", 3882.50773654747, new int[] { 3, 4, 1, 2, 0, 2, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addPrivacyModel(new ProfitabilityProsecutorNoAttack()), "RAMNTALL", "./data/cup.csv", 2032837.6390798881, new int[] { 5, 4, 1, 0, 1, 4, 4 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createLossMetric()).addPrivacyModel(new ProfitabilityProsecutor()).setCostBenefitConfiguration(getConfig1()), "RAMNTALL", "./data/cup.csv", 0.0, new int[] { 0, 0, 0, 0, 0, 0, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addPrivacyModel(new ProfitabilityJournalistNoAttack(DataSubset.create(Data.create("./data/cup.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/cup_subset.csv", StandardCharsets.UTF_8, ';')))).setCostBenefitConfiguration(getConfig1()), "RAMNTALL", "./data/cup.csv", 1445394.0, new int[] { 4, 4, 0, 1, 1, 2, 1 }, true) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createEntropyBasedInformationLossMetric()).addPrivacyModel(new ProfitabilityProsecutor()).setCostBenefitConfiguration(getConfig1()), "RAMNTALL", "./data/cup.csv", 0.0, new int[] { 0, 0, 0, 0, 0, 0, 0 }, true) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addPrivacyModel(new ProfitabilityProsecutorNoAttack()), "RAMNTALL", "./data/cup.csv", 1482557.7957185348, new int[] { 3, 4, 0, 2, 0, 2, 2 }, true) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addPrivacyModel(new ProfitabilityJournalist(DataSubset.create(Data.create("./data/cup.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/cup_subset.csv", StandardCharsets.UTF_8, ';')))), "RAMNTALL", "./data/cup.csv", 0.0, new int[] { 0, 0, 0, 0, 0, 0, 0 }, true) },
                                              /* 30 */{ new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addPrivacyModel(new ProfitabilityJournalistNoAttack(DataSubset.create(Data.create("./data/fars.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/fars_subset.csv", StandardCharsets.UTF_8, ';')))), "istatenum", "./data/fars.csv", 94527.8487035758, new int[] { 3, 0, 2, 3, 0, 1, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addPrivacyModel(new ProfitabilityProsecutor()).setCostBenefitConfiguration(getConfig1()), "istatenum", "./data/fars.csv", 0.0, new int[] { 0, 0, 0, 0, 0, 0, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPublisherPayoutMetric(false)).addPrivacyModel(new ProfitabilityProsecutorNoAttack()).setCostBenefitConfiguration(getConfig3()), "istatenum", "./data/fars.csv", 4140982.974244494, new int[] { 2, 0, 0, 3, 0, 0, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyBasedInformationLossMetric()).addPrivacyModel(new ProfitabilityProsecutor()).setCostBenefitConfiguration(getConfig2()), "istatenum", "./data/fars.csv", 29404.284086440723, new int[] { 1, 0, 0, 3, 0, 0, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createPublisherPayoutMetric(false)).addPrivacyModel(new ProfitabilityJournalistNoAttack(DataSubset.create(Data.create("./data/fars.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/fars_subset.csv", StandardCharsets.UTF_8, ';')))).setCostBenefitConfiguration(getConfig3()), "istatenum", "./data/fars.csv", 751449.1770133902, new int[] { 5, 2, 0, 0, 1, 2, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addPrivacyModel(new ProfitabilityProsecutor()).setCostBenefitConfiguration(getConfig3()), "istatenum", "./data/fars.csv", 1201007.0880104562, new int[] { 0, 2, 3, 3, 1, 2, 2 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createLossMetric()).addPrivacyModel(new ProfitabilityProsecutorNoAttack()).setCostBenefitConfiguration(getConfig4()), "istatenum", "./data/fars.csv", 0.18571390635965068, new int[] { 4, 0, 2, 1, 0, 0, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d,Metric.createPublisherPayoutMetric(false)).addPrivacyModel(new ProfitabilityJournalist(DataSubset.create(Data.create("./data/fars.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/fars_subset.csv", StandardCharsets.UTF_8, ';')))).setCostBenefitConfiguration(getConfig4()), "istatenum", "./data/fars.csv", 822265.8740269239, new int[] { 5, 2, 2, 0, 1, 2, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addPrivacyModel(new ProfitabilityProsecutorNoAttack()).setCostBenefitConfiguration(getConfig1()), "istatenum", "./data/fars.csv", 1177149.7949303142, new int[] { 3, 2, 1, 2, 1, 2, 0 }, true) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyBasedInformationLossMetric()).addPrivacyModel(new ProfitabilityProsecutor()).setCostBenefitConfiguration(getConfig4()), "istatenum", "./data/fars.csv", 44194.12520931992, new int[] { 5, 0, 0, 3, 0, 0, 0 }, true) },
                                              /* 40 */{ new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d, false)).addPrivacyModel(new ProfitabilityJournalistNoAttack(DataSubset.create(Data.create("./data/fars.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/fars_subset.csv", StandardCharsets.UTF_8, ';')))).setCostBenefitConfiguration(getConfig3()), "istatenum", "./data/fars.csv", 63993.0806678014, new int[] { 0, 0, 3, 3, 0, 0, 0 }, true) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d, false)).addPrivacyModel(new ProfitabilityProsecutor()).setCostBenefitConfiguration(getConfig2()), "istatenum", "./data/fars.csv", 548604.0354363323, new int[] { 0, 0, 2, 3, 0, 0, 0 }, true) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createPublisherPayoutMetric(false)).addPrivacyModel(new ProfitabilityProsecutorNoAttack()).setCostBenefitConfiguration(getConfig2()), "EDUC", "./data/ihis.csv", 3.33574202786792E8, new int[] { 0, 2, 0, 0, 1, 0, 1, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPublisherPayoutMetric(false)).addPrivacyModel(new ProfitabilityJournalist(DataSubset.create(Data.create("./data/ihis.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/ihis_subset.csv", StandardCharsets.UTF_8, ';')))).setCostBenefitConfiguration(getConfig2()), "EDUC", "./data/ihis.csv", 5.12685882625751E7, new int[] { 0, 2, 2, 1, 2, 0, 1, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addPrivacyModel(new ProfitabilityProsecutorNoAttack()).setCostBenefitConfiguration(getConfig3()), "EDUC", "./data/ihis.csv", 6053353.046937073, new int[] { 0, 0, 0, 3, 0, 1, 0, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createEntropyBasedInformationLossMetric()).addPrivacyModel(new ProfitabilityProsecutor()).setCostBenefitConfiguration(getConfig3()), "EDUC", "./data/ihis.csv", 297107.71409336117, new int[] { 0, 2, 2, 2, 0, 0, 1, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createDiscernabilityMetric(true)).addPrivacyModel(new ProfitabilityJournalistNoAttack(DataSubset.create(Data.create("./data/ihis.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/ihis_subset.csv", StandardCharsets.UTF_8, ';')))).setCostBenefitConfiguration(getConfig2()), "EDUC", "./data/ihis.csv", 1993662.0, new int[] { 0, 0, 2, 3, 0, 2, 0, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createPublisherPayoutMetric(false)).addPrivacyModel(new ProfitabilityJournalistNoAttack(DataSubset.create(Data.create("./data/ihis.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/ihis_subset.csv", StandardCharsets.UTF_8, ';')))).setCostBenefitConfiguration(getConfig1()), "EDUC", "./data/ihis.csv", 8.993970011390185E7, new int[] { 0, 0, 0, 3, 4, 2, 0, 1 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createPublisherPayoutMetric(false)).addPrivacyModel(new ProfitabilityProsecutorNoAttack()).setCostBenefitConfiguration(getConfig1()), "EDUC", "./data/ihis.csv", 6.396510986400988E8, new int[] { 1, 2, 2, 1, 2, 0, 1, 0 }, false) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.0d, Metric.createPrecomputedEntropyMetric(0.1d, true)).addPrivacyModel(new ProfitabilityJournalist(DataSubset.create(Data.create("./data/ihis.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/ihis_subset.csv", StandardCharsets.UTF_8, ';')))).setCostBenefitConfiguration(getConfig4()), "EDUC", "./data/ihis.csv", 1188661.4931264082, new int[] { 0, 1, 2, 3, 0, 2, 1, 1 }, false) },                                              
                                              /* 50 */{ new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPublisherPayoutMetric(false)).addPrivacyModel(new ProfitabilityProsecutorNoAttack()).setCostBenefitConfiguration(getConfig2()), "EDUC", "./data/ihis.csv", 3.666243355859808E8, new int[] { 0, 2, 2, 2, 0, 0, 1, 0 }, true) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createDiscernabilityMetric(true)).addPrivacyModel(new ProfitabilityProsecutor()).setCostBenefitConfiguration(getConfig2()), "EDUC", "./data/ihis.csv", 3.1317776E7, new int[] { 0, 0, 0, 2, 0, 1, 0, 1 }, true) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(1d, Metric.createPublisherPayoutMetric(false)).addPrivacyModel(new ProfitabilityJournalistNoAttack(DataSubset.create(Data.create("./data/ihis.csv", StandardCharsets.UTF_8, ';'), Data.create("./data/ihis_subset.csv", StandardCharsets.UTF_8, ';')))).setCostBenefitConfiguration(getConfig3()), "EDUC", "./data/ihis.csv", 1.2692151622772004E7, new int[] { 5, 0, 0, 1, 4, 0, 0, 0 }, true) },
                                              { new ARXAnonymizationTestCase(ARXConfiguration.create(0.04d, Metric.createPublisherPayoutMetric(false)).addPrivacyModel(new ProfitabilityProsecutor()).setCostBenefitConfiguration(getConfig1()), "EDUC", "./data/ihis.csv", 1.203978E8, new int[] { 0, 0, 0, 0, 0, 0, 0, 0 }, true) },
        });
    }
    
    /**
     * Returns a configuration
     * @return
     */
    private static ARXCostBenefitConfiguration getConfig1() {
        return ARXCostBenefitConfiguration.create()
                .setAdversaryCost(2d)
                .setAdversaryGain(1200d)
                .setPublisherLoss(300d)
                .setPublisherBenefit(1200d);
    }

    /**
     * Returns a configuration
     * @return
     */
    private static ARXCostBenefitConfiguration getConfig2() {
        return ARXCostBenefitConfiguration.create()
                .setAdversaryCost(20d)
                .setAdversaryGain(120d)
                .setPublisherLoss(3000d)
                .setPublisherBenefit(1200d);
    }
    
    /**
     * Returns a configuration
     * @return
     */
    private static ARXCostBenefitConfiguration getConfig3() {
        return ARXCostBenefitConfiguration.create()
                .setAdversaryCost(20d)
                .setAdversaryGain(120d)
                .setPublisherLoss(3000d)
                .setPublisherBenefit(120d);
    }

    /**
     * Returns a configuration
     * @return
     */
    private static ARXCostBenefitConfiguration getConfig4() {
        return ARXCostBenefitConfiguration.create()
                .setAdversaryCost(20d)
                .setAdversaryGain(1000d)
                .setPublisherLoss(3000d)
                .setPublisherBenefit(120d);
    }
    /**
     * Creates a new instance
     * 
     * @param testCase
     */
    public TestAnonymizationProfitability(final ARXAnonymizationTestCase testCase) {
        super(testCase);
    }
}
