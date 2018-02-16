package org.deidentifier.arx.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.criteria.AverageReidentificationRisk;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.exceptions.RollbackRequiredException;
import org.deidentifier.arx.metric.Metric;
import org.deidentifier.arx.risk.RiskEstimateBuilder;
import org.deidentifier.arx.risk.RiskModelSampleSummary.ProsecutorRisk;
import org.junit.Test;

public class TestSingleTransformation {    
    
    /**
     * This class encapsulates a risk management scenario.
     * 
     * @author Fabian Prasser
     * @author Helmut Spengler
     */
    private class Risks {
        
        /** Threshold for the average risk */
        final private double averageRisk;;
        /** Threshold for the highest risk */
        final private double highestRisk;;
        /** Threshold for records at risk. */
        final private double recordsAtRisk;
        /** The quasi-identifiers */
        final private List<String> qis;

        /**
         * Creates a new instance
         * 
         * @param averageRisk
         * @param highestRisk
         * @param recordsAtRisk
         * @param qis
         */
        public Risks(double averageRisk, double highestRisk, double recordsAtRisk, List<String> qis) {
            this.qis = qis;
            this.averageRisk   = averageRisk;
            this.highestRisk   = highestRisk;
            this.recordsAtRisk = recordsAtRisk;
        }

        @Override
        public String toString() {
            return "Risks [averageRisk=" + averageRisk + ", highestRisk=" + highestRisk +
                   ", recordsAtRisk=" + recordsAtRisk + ", qis=" + qis + "]";
        }
    }
   
    /**
     * Returns a minimal class size for the given risk threshold.
     * 
     * @param threshold
     * @return
     */
    private int getSizeThreshold(double riskThreshold) {
        double size = 1d / riskThreshold;
        double floor = Math.floor(size);
        if ((1d / floor) - (1d / size) >= 0.01d * riskThreshold) {
            floor += 1d;
        }
        return (int) floor;
    }

    /**
     * Returns the generalization hierarchy for the dataset and attribute
     * 
     * @param data
     * @param attribute
     * @return
     * @throws IOException
     */
    private Hierarchy getHierarchy(Data data, String attribute) {
        DefaultHierarchy hierarchy = Hierarchy.create();
        int col = data.getHandle().getColumnIndexOf(attribute);
        String[] values = data.getHandle().getDistinctValues(col);
        for (String value : values) {
            hierarchy.add(value, DataType.ANY_VALUE);
        }
        return hierarchy;
    }

    /**
     * Configure the QIs
     * 
     * @param data
     * @param qis
     */
    private void configureQIs(Data data, List<String> qis) {
        for (int i = 0; i < data.getHandle().getNumColumns(); i++) {
            data.getDefinition().setAttributeType(data.getHandle().getAttributeName(i), AttributeType.INSENSITIVE_ATTRIBUTE);
        }       
        for (String qi : qis) {
            data.getDefinition().setAttributeType(qi, getHierarchy(data, qi));
        }
    }

    /**
     * Check risks
     * @param message
     * @param averageRisk
     * @param highestRisk
     * @param recordsAtRisk
     * @param parametersRisk
     */
    private void checkRisk(String message,
                           double averageRisk,
                           double highestRisk,
                           double recordsAtRisk,
                           Risks parametersRisk) {
        
        assertTrue("Average risk (" + message + ") - actual vs. specified: " + averageRisk + " / " + parametersRisk.averageRisk, averageRisk <= parametersRisk.averageRisk);
        if (recordsAtRisk == 0d) {
            assertTrue("Highest risk (" + message + ") - actual vs. specified: " + highestRisk + " / " + parametersRisk.highestRisk, highestRisk <= parametersRisk.highestRisk);
        }
        assertTrue("Records at risk (" + message + ") - actual vs. specified: " + recordsAtRisk + " / " + parametersRisk.recordsAtRisk, recordsAtRisk <= parametersRisk.recordsAtRisk);
    }

    @Test
    public void test() throws IOException {
        
        // 17-anonymity
        Risks risks = new Risks(1d, 0.05871560541486287, 0d,
                                Arrays.asList("sex", "age", "workclass"));
        
        // Load file
        Data data = Data.create("data/adult.csv", Charset.defaultCharset(), ';');
        configureQIs(data, risks.qis);
        
        
        // Setup anonymization
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXConfiguration config = ARXConfiguration.create();
        double o_min = 0.01d;
        double maxOutliers = 1.0d - o_min;
        config.setSuppressionLimit(maxOutliers);
        config.setQualityModel(Metric.createLossMetric(0d));
        if (risks.recordsAtRisk == 0d) {
            int k = getSizeThreshold(risks.highestRisk);
            if (k != 1) {
                config.addPrivacyModel(new KAnonymity(k));
            }
            if (risks.averageRisk != 1d) {
                config.addPrivacyModel(new AverageReidentificationRisk(risks.averageRisk));
            }
        } else {
            config.addPrivacyModel(new AverageReidentificationRisk(risks.averageRisk, risks.highestRisk, risks.recordsAtRisk));
        }
        config.setHeuristicSearchEnabled(false);
        
        // Perform anonymization
        ARXResult result = anonymizer.anonymize(data, config);
        
        // Perform cell suppression
        DataHandle output = result.getOutput();
        if (result.isOptimizable(output)) {
            try {
                result.optimizeIterativeFast(output, o_min);
            } catch (RollbackRequiredException e) {
                throw new RuntimeException(e);
            }
        }
        
        // Copy data to new handle
        Data anonData =  Data.create(output.iterator());
        anonData.getHandle();
        
        // Assess risks
        configureQIs(anonData, risks.qis);
        RiskEstimateBuilder builder = anonData.getHandle().getRiskEstimator();
        ProsecutorRisk riskModel = builder.getSampleBasedRiskSummary(risks.highestRisk).getProsecutorRisk();
        try {
            checkRisk("", riskModel.getSuccessRate(), riskModel.getHighestRisk(), riskModel.getRecordsAtRisk(), risks);
        } catch (AssertionError e) {
            System.out.println(risks);
            System.out.println(riskModel.getSuccessRate() + " - " + riskModel.getHighestRisk() + " - " + riskModel.getRecordsAtRisk());
            throw(e);
        }
    }

}
