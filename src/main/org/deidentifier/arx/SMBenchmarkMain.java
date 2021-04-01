package org.deidentifier.arx;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import org.deidentifier.arx.ShadowModelBenchmarkSetup.BenchmarkDataset;
import org.deidentifier.arx.ShadowModelMembershipRisk.FeatureType;
import org.deidentifier.arx.criteria.KAnonymity;

public class SMBenchmarkMain {
    
    /** Feautre type(s) to use */
    static final FeatureType FEATURE_TYPE = FeatureType.CORR;
    
    
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
        Data data = ShadowModelBenchmarkSetup.getData(BenchmarkDataset.TEXAS);
        ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new KAnonymity(1));
        config.setSuppressionLimit(0.0d);
        
        // Anonymize
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        
        DataHandle output = anonymizer.anonymize(data, config).getOutput();
        
        // Perform risk assessment
        //ShadowModelMembershipRisk model = new ShadowModelMembershipRisk();
        
        // TODO by setting repetitions to 0 the training is disabled - done for developing
        //model.getShadowModelBasedMembershipRisk(output, 0.02d, 0, 100);
        
        Random rnd = new Random(13337);
        int[] targets = createRandomTargets(1, output.getNumRows(), rnd);
        System.out.println(targets.length);
        double performance = performMIA(output, targets);
        System.out.println("Average Performance " + performance); 
    }
    
    /**
     * Create array containing randomly selected target IDs
     * 
     * @param numberOfTargets
     * @param numberOfRows
     * @return
     */
    public static int[] createRandomTargets(int numberOfTargets, int numberOfRows) {
        return createRandomTargets(numberOfTargets, numberOfRows, new Random());
    }
    
    /**
     * Create array containing randomly selected target IDs using a fixed Random-object
     * 
     * @param numberOfTargets
     * @param numberOfRows
     * @return
     */
    public static int[] createRandomTargets(int numberOfTargets, int numberOfRows, Random rnd) {
        Set<Integer> samples = new LinkedHashSet<Integer>();
        
        while(samples.size() < numberOfTargets) {
            samples.add(rnd.nextInt(numberOfRows+1));
        }
        
        return samples.stream().mapToInt(Number::intValue).toArray();
    }
    
    
    
    /**
     * Repeat MIA for multiple targets
     * 
     * @param handle
     * @param targets
     * @return
     */
    public static double performMIA(DataHandle handle, int[] targets) {
        
        ShadowModelMembershipRisk model = new ShadowModelMembershipRisk();
        
        int correctResults = 0;
        for(int i = 0; i < targets.length; i++) {
            double result = model.getShadowModelBasedMembershipRisk(handle, 0.02d, targets[i], 100, FEATURE_TYPE);
            // TODO check if this is really the threshold - probably not
            if (result <= 0.5) {
                correctResults++;
            }
        }
        
        return (double) correctResults / (double) targets.length;
        
    }

}
