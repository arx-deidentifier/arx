package org.deidentifier.arx;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.deidentifier.arx.ShadowModel.ClassifierType;
import org.deidentifier.arx.ShadowModel.FeatureType;
import org.deidentifier.arx.ShadowModelSetup.AnonymizationMethod;
import org.deidentifier.arx.ShadowModelSetup.BenchmarkDataset;

/**
 * Main method to run the experiments
 * @author Fabian Prasser
 * @author Thierry Meurers
 */
public class ShadowModelMain {

    /** Dataset */
    private static final BenchmarkDataset    BENCHMARK_DATASET   = BenchmarkDataset.ADULT;

    /** Anonymization */
    private static final AnonymizationMethod ANONYMIZATION       = ShadowModelSetup.IDENTITY_ANONYMIZATION;

    /** Feature type(s) to use */
    private static final FeatureType         FEATURE_TYPE        = FeatureType.HISTOGRAM;

    /** Classifier tyoe to use */
    private static final ClassifierType      CLASSIFIER_TYPE     = ClassifierType.RF;

    /** Number of random targets */
    private static final int                 NUMBER_OF_TARGETS   = 50;

    /** Number of random targets */
    private static final int                 NUMBER_OF_TESTS     = 25;

    /** Number of subsamples used to train the classifier */
    private static final int                 NUMBER_OF_TRAININGS = 10;

    /** TODO: Is this a suitable number? What is used in the paper? */
    private static final int                 SAMPLE_SIZE         = 100;

    /**
     * Main entry point
     * @param args
     * @throws IOException 
     * @throws ParseException 
     */
    public static void main(String[] args) throws IOException, ParseException  {
        
        // Statistics
        int trueGuesses = 0;
        int numberOfGuesses = 0;

        // Create dataset
        Data rRef = ShadowModelSetup.getData(BENCHMARK_DATASET);
        
        // Draw targets
        Set<Integer> targets = getTargets(rRef, NUMBER_OF_TARGETS);
                
        // Perform tests
        for (int j = 0; j < NUMBER_OF_TESTS; j++) {
            
            // Sample without target
            Set<Integer> rOut = getSample(rRef, SAMPLE_SIZE, targets);
            
            // For each target
            for (int target : targets) {

                // Initialize shadow model
                ShadowModel model = new ShadowModel(rRef.getHandle(),
                                                    rRef.getDefinition().getQuasiIdentifyingAttributes(),
                                                    FEATURE_TYPE,
                                                    CLASSIFIER_TYPE);

                // Train
                for (int k = 0; k < NUMBER_OF_TRAININGS; k++) {
                 
                    // Draw samples
                    Set<Integer> rTrainOut = getSample(rRef, SAMPLE_SIZE, targets);
                    Set<Integer> rTrainIn = getSampleWithTarget(rTrainOut, target);
                    
                    // Anonymize
                    DataHandle rTrainOutHandle = anonymize(rRef, rTrainOut, ANONYMIZATION);
                    DataHandle rTrainInHandle = anonymize(rRef, rTrainIn, ANONYMIZATION);
                    
                    // Train
                    model.train(rTrainOutHandle, false);
                    model.train(rTrainInHandle, true);
                    
                    // Release
                    rTrainOutHandle.release();
                    rTrainInHandle.release();
                    
                }
                
                // Test published datasets
                Set<Integer> rIn = getSampleWithTarget(rOut, target);
                DataHandle rOutHandle = anonymize(rRef, rOut, ANONYMIZATION);
                DataHandle rInHandle = anonymize(rRef, rIn, ANONYMIZATION);

                // Train
                Pair<Boolean, Double>[] prediction = model.predict(new DataHandle[] {rOutHandle, rInHandle});
                System.out.println(prediction[0] + " -> Should be: false");
                System.out.println(prediction[1] + " -> Should be: true");
                trueGuesses += prediction[0].getFirst() == false ? 1 : 0;
                trueGuesses += prediction[1].getFirst() == true ? 1 : 0;
                numberOfGuesses += 2;
                System.out.println("Success rate: " + (double)trueGuesses / (double)numberOfGuesses);
                
                // Release
                rOutHandle.release();
                rInHandle.release();
            }
        }
    }
    
    /**
     * Anonymize
     * @param dataset
     * @param indices
     * @param anonymization
     * @return
     */
    private static DataHandle anonymize(Data dataset, Set<Integer> indices, AnonymizationMethod anonymization) {

        // Extract sample
        Data input = getCopy(dataset, indices);
        input.getDefinition().read(dataset.getDefinition());
        
        // Anonymize
        return anonymization.anonymize(input);
    }

    /**
     * Adds the target to the sample
     * @param samples
     * @param target
     * @return
     */
    private static Set<Integer> getSampleWithTarget(Set<Integer> samples, int target) {
        
        // Prepare
        Set<Integer> result = new HashSet<>(samples);
        
        // Remove one random element
        int index = new Random().nextInt(result.size() - 1) + 1;
        Iterator<Integer> iter = result.iterator();
        for (int i = 0; i < index; i++) {
            iter.next();
        }
        iter.remove();
        
        // Add target
        result.add(target);
        
        // Done
        return result;
    }

    /**
     * Gets a set of targets
     * @param dataset
     * @param targets
     * @return
     */
    private static Set<Integer> getTargets(Data dataset, int targets) {
        
        // Collect random numbers
        int size = dataset.getHandle().getNumRows();
        Random random = new Random();
        Set<Integer> samples = new HashSet<>();
        while (samples.size() < targets) {
            samples.add(random.nextInt(size));
        }
        return samples;
    }

    /**
     * Create random sample of ids excluding the targets
     * 
     * @param dataset
     * @param sampleSize
     * @param targets 
     * @return
     */
    private static Set<Integer> getSample(Data data, int sampleSize, Set<Integer> targets) {
        
        // Shuffled indices
        List<Integer> lists = new ArrayList<>();
        for (int row = 0; row < data.getHandle().getNumRows(); row++) {
            if (!targets.contains(row)) {
                lists.add(row);
            }
        }
        Collections.shuffle(lists);
        
        // Extract
        return new HashSet<>(lists.subList(0, sampleSize));
    }
  
    /**
     * Create a copy of data
     * @param dataset
     * @param indices 
     * @return
     */
    private static Data getCopy(Data dataset, Set<Integer> indices) {
        List<String[]> rows = new ArrayList<>();
        rows.add(dataset.getHandle().iterator().next());
        for (int row=0; row < dataset.getHandle().getNumRows(); row++) {
            if (indices.contains(row)) {
                rows.add(getRow(dataset.getHandle(), row));
            }
        }
        return Data.create(rows);
    }

    /**
     * Extracts a row from the handle
     * @param handle
     * @param row
     * @return
     */
    private static String[] getRow(DataHandle handle, int row) {
        String[] result = new String[handle.getNumColumns()];
        for (int column = 0; column < result.length; column++) {
            result[column] = handle.getValue(row, column);
        }
        return result;
    }
}
