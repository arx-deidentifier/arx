package org.deidentifier.arx;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SMConcBenchmarkMain {

    /** Number of threads to use */
    final static int THREAD_COUNT = 16;
    
    /** Directory for sotring files */
    final static String RESULT_DIRECTORY = "results/";
    
    /**
     * Main entry point
     * 
     * @param args
     * @throws IOException
     * @throws ParseException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        
        SMConcBenchmark benchmark = new SMConcBenchmark(THREAD_COUNT, RESULT_DIRECTORY, generateExperimentName());
        benchmark.execute();
        System.out.println(SMBenchmarkConfig.asString());
        
        System.out.println(generateExperimentName());

    }
    
    /**
     * Method to generate experiment name.
     * 
     * @return
     */
    private static String generateExperimentName() {
        String dateTime = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
        return dateTime + "_" + SMBenchmarkConfig.TARGET_TYPE + "_" + SMBenchmarkConfig.ANONYMIZATION;
    }

}
