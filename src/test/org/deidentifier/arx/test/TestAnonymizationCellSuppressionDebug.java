package org.deidentifier.arx.test;

import java.io.IOException;
import java.nio.charset.Charset;

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
import org.deidentifier.arx.exceptions.RollbackRequiredException;
import org.deidentifier.arx.metric.Metric;
import org.deidentifier.arx.risk.RiskModelSampleSummary.ProsecutorRisk;
import org.junit.Test;

public class TestAnonymizationCellSuppressionDebug {    
    
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

    @Test
    public void test() throws IOException {

        // Thresholds
        String[] qis = new String[]{"occupation", "age", "workclass", "sex", "native-country", "education", "marital-status"};
        double highestRisk = 0.05871560541486287;
        double averageRisk = 0.3928991653679357;
        double recordsAtRisk = 0.0699862407603622;
        
        // Load file
        // TODO: Remove debug data
        // TODO: Remove debug data
        // TODO: Remove debug data
        // TODO: Remove debug data
        Data data = Data.create("temp2.csv", Charset.defaultCharset(), ';');
        for (int i = 0; i < data.getHandle().getNumColumns(); i++) {
            data.getDefinition().setAttributeType(data.getHandle().getAttributeName(i), AttributeType.INSENSITIVE_ATTRIBUTE);
        }       
        for (String qi : qis) {
            data.getDefinition().setAttributeType(qi, getHierarchy(data, qi));
        }
        
        // Setup anonymization
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXConfiguration config = ARXConfiguration.create();
        double o_min = 0.01d;
        double maxOutliers = 1.0d - o_min;
        config.setSuppressionLimit(maxOutliers);
        config.setQualityModel(Metric.createLossMetric(0d));
        config.addPrivacyModel(new AverageReidentificationRisk(averageRisk, highestRisk, recordsAtRisk));
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
        Data outputData = Data.create(output.iterator());

        // Assess risks
        for (int i = 0; i < outputData.getHandle().getNumColumns(); i++) {
            outputData.getDefinition().setAttributeType(outputData.getHandle().getAttributeName(i), AttributeType.INSENSITIVE_ATTRIBUTE);
        }
        for (String qi : qis) {
            outputData.getDefinition().setAttributeType(qi, getHierarchy(outputData, qi));
        }
        
        ProsecutorRisk riskModelExternal = outputData.getHandle().getRiskEstimator().getSampleBasedRiskSummary(highestRisk, DataType.ANY_VALUE).getProsecutorRisk();
        ProsecutorRisk riskModelInternal = output.getRiskEstimator().getSampleBasedRiskSummary(highestRisk, DataType.ANY_VALUE).getProsecutorRisk();
        
        System.out.println("Average risk: " + averageRisk + " internal: " + riskModelInternal.getSuccessRate());
        System.out.println("Average risk: " + averageRisk + " external: " + riskModelExternal.getSuccessRate());
        
        System.out.println("Highest risk: " + highestRisk + " internal: " + riskModelInternal.getHighestRisk());
        System.out.println("Highest risk: " + highestRisk + " external: " + riskModelExternal.getHighestRisk());
        
        System.out.println("Records at risk: " + recordsAtRisk + " internal: " + riskModelInternal.getRecordsAtRisk());
        System.out.println("Records at risk: " + recordsAtRisk + " external: " + riskModelExternal.getRecordsAtRisk());
        
    }
}