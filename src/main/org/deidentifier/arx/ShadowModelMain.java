package org.deidentifier.arx;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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

    public enum TargetType {CRAFTED, RANDOM, OUTLIER}
    
    /** Dataset */
    private static final BenchmarkDataset    BENCHMARK_DATASET         = BenchmarkDataset.ADULT_FULL;

    /** Anonymization */
    private static final AnonymizationMethod ANONYMIZATION             = ShadowModelSetup.PITMAN_ANONYMIZATION;

    /** Feature type(s) to use */
    private static final FeatureType         FEATURE_TYPE              = FeatureType.ENSEMBLE;

    /** Classifier tyoe to use */
    private static final ClassifierType      CLASSIFIER_TYPE           = ClassifierType.RF;

    /** Number of random targets */
    private static final int                 NUMBER_OF_TARGETS         = 10;

    /** Use crafted target */
    private static final TargetType          TARGET_TYPE               = TargetType.OUTLIER;

    /** Number of independent tests */
    private static final int                 NUMBER_OF_TESTS           = 25;

    /** Number of subsamples used to train the classifier */
    private static final int                 NUMBER_OF_TRAININGS       = 10;

    /** TODO: Is this a suitable number? What is used in the paper? --> 1000 */
    private static final int                 SAMPLE_SIZE               = 1000;

    /** Size of population available for the adversary in each test */
    private static final int                 ADVERSARY_POPULATION_SIZE = 10000;

    

    
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

        int[] trueGuessesPerTarget = new int[NUMBER_OF_TARGETS];
        int[] numberOfGuessesPerTarget = new int[NUMBER_OF_TARGETS];
        
        // Create dataset
        Data rRef = ShadowModelSetup.getData(BENCHMARK_DATASET);
        
        Set<Integer> targets;
        
        switch(TARGET_TYPE) {
            case CRAFTED:
                targets = new HashSet<>(Arrays.asList(0));
                break;
            case RANDOM:
                targets = getTargets(rRef, NUMBER_OF_TARGETS);
                break;
            case OUTLIER:
                targets = getOutlier(rRef, NUMBER_OF_TARGETS);
                break;
            default:
                throw new RuntimeException("Invalid targettype");
        }
   

        
        // Perform tests
        for (int j = 0; j < NUMBER_OF_TESTS; j++) {
            
            System.out.println((j+1)+"/"+NUMBER_OF_TESTS + "--------");
            
            // Sample without target
            Set<Integer> rOut = getSample(rRef, SAMPLE_SIZE, targets);
            
            // Sample adversary population
            Set<Integer> rA = getSample(rRef, ADVERSARY_POPULATION_SIZE, targets);
            
            // For each target
            int targetNum = 0;
            for (int target : targets) {

                System.out.println("Run: " + j + " | Target: " + (targetNum+1)+"/"+targets.size() + " |");
                
                // Initialize shadow model
                ShadowModel model = new ShadowModel(rRef.getHandle(),
                                                    rRef.getDefinition().getQuasiIdentifyingAttributes(),
                                                    FEATURE_TYPE,
                                                    CLASSIFIER_TYPE);
                
                // Train
                for (int k = 0; k < NUMBER_OF_TRAININGS; k++) {

                    // Draw samples
                    Set<Integer> rTrainOut = getSubSample(rA, SAMPLE_SIZE);
                    //Set<Integer> rTrainIn = getSampleWithTarget(getSubSample(rA, SAMPLE_SIZE), target);
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
                
                // Update record statistic
                trueGuessesPerTarget[targetNum] += prediction[0].getFirst() == false ? 1 : 0;
                trueGuessesPerTarget[targetNum] += prediction[1].getFirst() == true ? 1 : 0;
                
                // Update overall statistic
                trueGuesses += prediction[0].getFirst() == false ? 1 : 0;
                trueGuesses += prediction[1].getFirst() == true ? 1 : 0;
                numberOfGuesses += 2;
                
                // Release
                rOutHandle.release();
                rInHandle.release();
                
                // Print record statistic
                System.out.println("Success rate for Target " + target + ": " + (double)trueGuessesPerTarget[targetNum] / (double) ((j+1)*2) + "\n");
                
                targetNum++;
            }
            System.out.println("Overall Success rate: " + (double)trueGuesses / (double)numberOfGuesses);
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
     * Method to receive set of IDs corresponding to the most outlierish records
     * 
     * @param rRef
     * @param targets
     * @return
     * @throws ParseException
     */
    private static Set<Integer> getOutlier(Data rRef, int targets) throws ParseException {
        
        // Collect random numbers
        int size = rRef.getHandle().getNumRows();
        
        // initlaize result set
        Set<Integer> samples = new HashSet<>();

        // initialize list used of pairs used to store distances
        List<Pair<Integer, Double>> distances = new ArrayList<Pair<Integer, Double>>();

        ShadowModel model = new ShadowModel(rRef.getHandle(),
                                            rRef.getDefinition().getQuasiIdentifyingAttributes(),
                                            FEATURE_TYPE,
                                            CLASSIFIER_TYPE);

        // Get distances
        for (int i = 0; i < size; i++) {
            distances.add(new Pair<>(i, model.getDistance(i)));
        }

        // Sort indices by distance in descending order
        Collections.sort(distances, new Comparator<Pair<Integer, Double>>() {
            @Override
            public int compare(final Pair<Integer, Double> p1, final Pair<Integer, Double> p2) {
                if (p1.getSecond() < p2.getSecond()) return 1;
                return -1;
            }
        });
        
        /*
        for(int i = 0; i < 10; i++) {
            Pair<Integer, Double> temp = distances.get(i);
            System.out.println(temp.getFirst() + " --> " + temp.getSecond());
        }
        */
        
        // Copy to set
        for(int i = 0; i < targets; i++) {
            samples.add(distances.get(i).getFirst());
        }
        
        return samples;
    }
    
    /**
     * Obtain a subset from a set.
     * 
     * @param samples
     * @param subSampleSize
     * @return
     */
    private static Set<Integer> getSubSample(Set<Integer> samples, int subSampleSize){
        
        List<Integer> list = new ArrayList<>(samples);
        Collections.shuffle(list);
        return new HashSet<>(list.subList(0, subSampleSize));

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
