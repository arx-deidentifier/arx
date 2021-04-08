package org.deidentifier.arx;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import org.deidentifier.arx.ShadowModelBenchmarkSetup.BenchmarkDataset;
import org.deidentifier.arx.ShadowModelMembershipRisk.FeatureType;
import org.deidentifier.arx.ShadowModelMembershipRisk.ClassifierType;
import org.deidentifier.arx.criteria.KAnonymity;

public class SMBenchmarkMain {
    
    /** ~~~ Dataset & Targets ~~~ */
    /** Dataset */
    static final BenchmarkDataset BENCHMARK_DATASET = BenchmarkDataset.TEXAS_10;
    
    /** Use random targets or not */
    static final boolean USE_RANDOM_TARGETS = false;
    
    /** Number of random targets */
    static final int NUMBER_OF_TARGETS = 25;
    
    /** Target list of non-random targets */
    static final int[] TARGET_IDS = new int[] {0};
    
    
    /** ~~~ Classification ~~~ */
    /** Feature type(s) to use */
    static final FeatureType FEATURE_TYPE = FeatureType.CORR;
    
    /** Classifier tyoe to use */
    static final ClassifierType CLASSIFIER_TYPE = ClassifierType.RF;
    
    /** Number of subsamples used to train the classifier */
    static final int REPETITIONS = 0;
    
    /** Size of subsamples (provided as fraction of original Dataset)*/
    static final double FRACTION = 0.02d;
    
    
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
        Data data = ShadowModelBenchmarkSetup.getData(BENCHMARK_DATASET);
        
        // Prepare config and anonymize
        ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new KAnonymity(1));
        config.setSuppressionLimit(0.0d);
        
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        DataHandle output = anonymizer.anonymize(data, config).getOutput();
        
        // Perform MIA
        double performance = 0;
        if (USE_RANDOM_TARGETS) {
            int[] targets = createRandomTargets(NUMBER_OF_TARGETS, output.getNumRows());
            performance = performMIA(output, targets);
        } else {
            performance = performMIA(output, TARGET_IDS);
        }

        System.out.println("Average Performance " + performance); 
        System.out.println("Feature Calc. Time: " + ShadowModelMembershipRisk.featureTime);
        System.out.println("Measurement#1 Time: " + ShadowModelMembershipRisk.timeMeasurement1);
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
        
        if (numberOfRows < numberOfTargets) {
            throw new RuntimeException("Number of targets exceeds number available rows");
        }
        
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
            System.out.print("(" + (i+1) + "/" + targets.length + ") | ");
            double result = model.getShadowModelBasedMembershipRisk(handle, FRACTION, targets[i], REPETITIONS, FEATURE_TYPE, CLASSIFIER_TYPE);
            // TODO check if this is really the threshold - probably not
            if (result <= 0.5) {
                correctResults++;
            }
        }
        return (double) correctResults / (double) targets.length;
    }

}
