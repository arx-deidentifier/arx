package org.deidentifier.arx;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math3.util.Pair;
import org.deidentifier.arx.ShadowModelSetup.AnonymizationMethod;


/**
 * Class to perform Shadow MIA Benchmark with multiple threads.
 *
 * @author Thierry Meurers
 * @author Fabian Prasser
 */
public class SMConcBenchmark {
    
    /**
     * Thread to perform a Shadow MIA test run.
     * 
     * @author Thierry Meurers
     * @author Fabian Prasser
     */
    class BenchmarkThread implements Runnable{

        /** Ref. data */
        private Data         rRef;
        
        /** Targets */
        private Set<Integer> targets;
        
        /** Id of thread */
        private int          threadId;
        
        /**
         * Creates new Thread
         * 
         * @param rRef
         * @param targets
         * @param threadId
         */
        BenchmarkThread(Data rRef, Set<Integer> targets, int threadId){
            this.rRef = rRef;
            this.targets = targets;
            this.threadId = threadId;
        }
        
        /**
         * Performs MIA Test run.
         */
        @Override
        public void run() {
            int testRun = 0;
            
            // Perform new test run until number of requried runs is reached
            while ((testRun = nextTestRun.getAndIncrement()) < SMBenchmarkConfig.NUMBER_OF_TESTS && isRunning()) {
                
                System.out.println("Thread " + threadId + " is starting TestRun " + testRun);
                
                // Result lines
                Set<String> resultLines = new HashSet<>();
                
                // Sample without target
                Set<Integer> rOut = getSample(rRef, SMBenchmarkConfig.SAMPLE_SIZE, targets);
                
                // Sample adversary population
                Set<Integer> rA = getSample(rRef, SMBenchmarkConfig.ADVERSARY_POPULATION_SIZE, targets);
                
                // For each target
                for (int target : targets) {

                    // check if still running
                    if(!isRunning()) {
                        break;
                    }

                    // Initialize shadow model
                    ShadowModel model;
                    try {
                        model = new ShadowModel(rRef.getHandle(),
                                                rRef.getDefinition()
                                                    .getQuasiIdentifyingAttributes(),
                                                    SMBenchmarkConfig.FEATURE_TYPE,
                                                    SMBenchmarkConfig.CLASSIFIER_TYPE);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }

                    // Train
                    for (int k = 0; k < SMBenchmarkConfig.NUMBER_OF_TRAININGS && isRunning(); k++) {

                        // Draw samples
                        Set<Integer> rTrainOut = getSubSample(rA, SMBenchmarkConfig.SAMPLE_SIZE);
                        //Set<Integer> rTrainIn = getSampleWithTarget(getSubSample(rA, SAMPLE_SIZE), target);
                        Set<Integer> rTrainIn = getSampleWithTarget(rTrainOut, target);
                        
                        // Anonymize
                        DataHandle rTrainOutHandle = anonymize(rRef, rTrainOut, SMBenchmarkConfig.ANONYMIZATION);
                        DataHandle rTrainInHandle = anonymize(rRef, rTrainIn, SMBenchmarkConfig.ANONYMIZATION);
                        
                        // Train
                        model.train(rTrainOutHandle, false);
                        model.train(rTrainInHandle, true);
                        
                        // Release
                        rTrainOutHandle.release();
                        rTrainInHandle.release();
                        
                    }
                    
                    // Test published datasets
                    Set<Integer> rIn = getSampleWithTarget(rOut, target);
                    DataHandle rOutHandle = anonymize(rRef, rOut, SMBenchmarkConfig.ANONYMIZATION);
                    DataHandle rInHandle = anonymize(rRef, rIn, SMBenchmarkConfig.ANONYMIZATION);

                    // Predict
                    Pair<Boolean, Double>[] prediction = model.predict(new DataHandle[] {rOutHandle, rInHandle});

                    // Update stats (first prediction is expected to be false; second to be true)
                    trueGuesses.get(target).getAndAdd((prediction[0].getFirst() ? 0 : 1) + (prediction[1].getFirst()? 1 : 0));
                    
                    // Add result lines for log file
                    resultLines.add(testRun + ";" + target + ";0;" + (prediction[0].getFirst() ? 1 : 0) + ";" + prediction[0].getSecond());
                    resultLines.add(testRun + ";" + target + ";1;" + (prediction[1].getFirst() ? 1 : 0) + ";" + prediction[1].getSecond());
                    
                    // Release
                    rOutHandle.release();
                    rInHandle.release();
                }
                // Write to log file
                writeToLog(resultLines);
                
                // Update stats
                executedRuns.getAndIncrement(); 
            } 
        }
    }

    /** Number of next test run */
    private AtomicInteger nextTestRun = new AtomicInteger();
    
    /** Map storing TargetId and number of true guesses of this target */
    private Map<Integer, AtomicInteger> trueGuesses = new HashMap<>();
    
    /** Stores number executed runs */
    private AtomicInteger executedRuns = new AtomicInteger();
    
    /** Pool of threads */
    private Thread[] threadPool;
    
    /** Used to interupt benchmark */
    private boolean running = true;
    
    /** Log File */
    private File logFile;
    
    /** Summary File  */
    private File summaryFile;

    /**
     * Main entry point
     * @param args
     * @throws IOException 
     * @throws ParseException 
     * @throws InterruptedException 
     */
    public SMConcBenchmark(int threadCount, String resultDir, String expName) throws IOException, ParseException, InterruptedException  {
        
        // Create dataset
        Data rRef = ShadowModelSetup.getData(SMBenchmarkConfig.BENCHMARK_DATASET);
        
        // Get targets
        Set<Integer> targets = getTargets(rRef);
        
        // Initialize Map
        for(Integer target : targets) {
            trueGuesses.put(target, new AtomicInteger(0));    
        }
        
        logFile = new File(resultDir + expName + "_log.csv");
        summaryFile = new File(resultDir + expName + "_summary.txt");
        if (!logFile.createNewFile() || !summaryFile.createNewFile()) {
            throw new RuntimeException("File(s) already exist(s)!");
        }
        writeToLog(Set.of("TestRun;TargetId;TrueLabel;PredictedLabel;PredictionProbability"));
        
        // Create Threads
        threadPool = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threadPool[i] = new Thread(new BenchmarkThread(ShadowModelSetup.getData(SMBenchmarkConfig.BENCHMARK_DATASET), targets, i));
        }
    }
    
    public void execute() throws InterruptedException, IOException, ParseException {
        // Start threads
        for (Thread t : threadPool) {
            t.start();
        }

        // Wait for threads to finish (throws InterruptedException)
        for (Thread t : threadPool) {
            t.join();
        }
        
        // write Summary file
        writeSummaryFile();
    }

    /**
     * Call to interrupt benchmark
     */
    public void stop() {
        running = false;
    }
    
    /**
     * Called by thread to see if benchmark was interrupted.
     * 
     * @return
     */
    private boolean isRunning() {
        return running;
    }
    
    /**
     * Used to append lines to the result log.
     * 
     * @param resultLines
     */
    private synchronized void writeToLog(Set<String> resultLines) {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(logFile, true));

            for (String line : resultLines) {
                writer.append(line);
                writer.newLine();
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to write to log file");
        }
    }
    
    /**
     * Used to write summary file.
     * 
     * @throws IOException
     * @throws ParseException
     */
    private void writeSummaryFile() throws IOException, ParseException {

        BufferedWriter writer = new BufferedWriter(new FileWriter(summaryFile, true));
        writer.append(SMBenchmarkConfig.asString());
        writer.newLine();
        writer.newLine();
        writer.append("TargetId;Distance;Accuracy");
        writer.newLine();
        
        // Create dataset and model to calculate distance
        Data rRef = ShadowModelSetup.getData(SMBenchmarkConfig.BENCHMARK_DATASET);
        ShadowModel model = new ShadowModel(rRef.getHandle(),
                                            rRef.getDefinition().getQuasiIdentifyingAttributes(),
                                            SMBenchmarkConfig.FEATURE_TYPE,
                                            SMBenchmarkConfig.CLASSIFIER_TYPE);

        for (Map.Entry<Integer, AtomicInteger> entry : trueGuesses.entrySet()) {
            int targetId = entry.getKey();
            double distance = model.getDistance(targetId);
            double acc = (double) entry.getValue().get() / (executedRuns.get() * 2);
            writer.append(targetId + ";" + distance + ";" + acc);
            writer.newLine();
        }
        writer.close();

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
    
    private static Set<Integer> getTargets(Data rRef) throws ParseException {
        switch (SMBenchmarkConfig.TARGET_TYPE) {
        case CRAFTED:
            return new HashSet<>(Arrays.asList(0));
        case RANDOM:
            return getRandomTargets(rRef, SMBenchmarkConfig.NUMBER_OF_TARGETS);
        case OUTLIER:
            return getOutlierTargets(rRef, SMBenchmarkConfig.NUMBER_OF_TARGETS);
        default:
            throw new RuntimeException("Invalid targettype");
        } 
    }
    
    /**
     * Gets a set of random targets
     * @param dataset
     * @param targets
     * @return
     */
    private static Set<Integer> getRandomTargets(Data dataset, int targets) {
        
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
     * Method to receive set of IDs corresponding to the most outlierish records
     * 
     * @param rRef
     * @param targets
     * @return
     * @throws ParseException
     */
    private static Set<Integer> getOutlierTargets(Data rRef, int targets) throws ParseException {
        
        // Collect random numbers
        int size = rRef.getHandle().getNumRows();
        
        // initlaize result set
        Set<Integer> samples = new HashSet<>();

        // initialize list used of pairs used to store distances
        List<Pair<Integer, Double>> distances = new ArrayList<Pair<Integer, Double>>();

        ShadowModel model = new ShadowModel(rRef.getHandle(),
                                            rRef.getDefinition().getQuasiIdentifyingAttributes(),
                                            SMBenchmarkConfig.FEATURE_TYPE,
                                            SMBenchmarkConfig.CLASSIFIER_TYPE);

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
     * Create a copy of a subset of the data
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
