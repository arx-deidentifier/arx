package org.deidentifier.arx;

import org.deidentifier.arx.ShadowModel.ClassifierType;
import org.deidentifier.arx.ShadowModel.FeatureType;
import org.deidentifier.arx.ShadowModelSetup.AnonymizationMethod;
import org.deidentifier.arx.ShadowModelSetup.BenchmarkDataset;

/**
 * Config for MIA attack
 * 
 * @author Thierry Meurers
 * @author Fabian Prasser
 */
public class SMBenchmarkConfig {
    
    public enum TargetType {CRAFTED, RANDOM, OUTLIER, IMPORT}
    
    /** Dataset */
    public static final BenchmarkDataset    BENCHMARK_DATASET         = BenchmarkDataset.TEXAS;

    /** Anonymization */
    public static final AnonymizationMethod ANONYMIZATION             = ShadowModelSetup.IDENTITY_ANONYMIZATION;

    /** Suppression Limit */
    public static final double              SUPPRESSION_LIMIT          = 1d;
    
    /** Feature type(s) to use */
    public static final FeatureType         FEATURE_TYPE              = FeatureType.ENSEMBLE;

    /** Classifier type to use */
    public static final ClassifierType      CLASSIFIER_TYPE           = ClassifierType.RF;

    /** Number of random targets */
    public static final int                 NUMBER_OF_TARGETS         = 1;

    /** Use crafted target */
    public static final TargetType          TARGET_TYPE               = TargetType.CRAFTED;

    /** File storing list of targets */
    public static final String              TARGET_IMPORT_FILE        = "data_new/targets_TEXAS.txt";

    /** Number of independent tests */
    public static final int                 NUMBER_OF_TESTS           = 25;

    /** Number of subsamples used to train the classifier */
    public static final int                 NUMBER_OF_TRAININGS       = 10;

    /** TODO: Is this a suitable number? What is used in the paper? --> 1000 */
    public static final int                 SAMPLE_SIZE               = 1000;

    /** Size of population available for the adversary in each test */
    public static final int                 ADVERSARY_POPULATION_SIZE = 10000;
    
    /**
     * Avoid initializations.
     */
    private SMBenchmarkConfig() {};
    
    /**
     * return a human readable representation of config.
     * 
     * @return
     */
    public static String asString() {
        StringBuilder stb = new StringBuilder();
        stb.append("Dataset: " + BENCHMARK_DATASET);
        stb.append("\n");
        stb.append("Anonymization Method: " + ANONYMIZATION);
        stb.append("\n");
        stb.append("ASupression Limit: " + SUPPRESSION_LIMIT);
        stb.append("\n");
        stb.append("Feature Type: " + FEATURE_TYPE);
        stb.append("\n");
        stb.append("Classifier Type: " + CLASSIFIER_TYPE);
        stb.append("\n");
        stb.append("Number of Targets: " + NUMBER_OF_TARGETS);
        stb.append("\n");
        stb.append("Target Type: " + TARGET_TYPE);
        stb.append("\n");
        stb.append("Number of Tests: " + NUMBER_OF_TESTS);
        stb.append("\n");
        stb.append("Number of Trainings: " + NUMBER_OF_TRAININGS);
        stb.append("\n");
        stb.append("Sample size: " + SAMPLE_SIZE);
        stb.append("\n");
        stb.append("Adversary population size: " + ADVERSARY_POPULATION_SIZE);
        stb.append("\n");
        return stb.toString();
    }
    
    /**
     * return a machine readable representation of config.
     * 
     * @return
     */
    public static String asCsv() {
        StringBuilder header = new StringBuilder();
        StringBuilder row = new StringBuilder();
        header.append("Dataset;");
        row.append(BENCHMARK_DATASET +";");
        header.append("Method;");
        row.append(ANONYMIZATION +";");
        header.append("SuppressionLimit;");
        row.append(SUPPRESSION_LIMIT +";");
        header.append("FeatureType;");
        row.append(FEATURE_TYPE +";");
        header.append("ClassifierType;");
        row.append(CLASSIFIER_TYPE +";");
        header.append("NumberOfTargets;");
        row.append(NUMBER_OF_TARGETS +";");
        header.append("TargetType;");
        row.append(TARGET_TYPE +";");
        header.append("NumberOfTests;");
        row.append(NUMBER_OF_TESTS +";");
        header.append("NumberOfTrainings;");
        row.append(NUMBER_OF_TRAININGS +";");
        header.append("SampleSize;");
        row.append(SAMPLE_SIZE +";");
        header.append("AversaryPupulationSize");
        row.append(ADVERSARY_POPULATION_SIZE);
        return header.toString() +"\n" + row.toString();
    }

}
