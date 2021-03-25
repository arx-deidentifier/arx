package org.deidentifier.arx;

import java.io.IOException;

import org.deidentifier.arx.ShadowModelBenchmarkSetup.BenchmarkDataset;
import org.deidentifier.arx.criteria.KAnonymity;

public class SMBenchmarkMain {
    
    /**
     * Main entry point
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException  {
        // TODO: The current implementation will not work, when a model with a data subset is being used
        // TODO: Examples: d-presence or k-map.
        
        // TODO: Maybe not anonymize the output again? Might also be realistic to assume that the adversary just
        // TODO: transforms the data in a way that she feels fits to known output, and doesn't care whether privacy 
        // TODO: models are satisfied.
        
        // Example scenario
        
        // Create dataset
        Data data = ShadowModelBenchmarkSetup.getData(BenchmarkDataset.ADULT);
        ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new KAnonymity(1));
        config.setSuppressionLimit(0.0d);
        
        // Anonymize
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        
        DataHandle output = anonymizer.anonymize(data, config).getOutput();
        
        // Perform risk assessment
        ShadowModelMembershipRisk model = new ShadowModelMembershipRisk();
        
        // TODO by setting repetitions to 0 the training is disabled - done for developing
        model.getShadowModelBasedMembershipRisk(output, 0.02d, 0, 100);
    }

}
