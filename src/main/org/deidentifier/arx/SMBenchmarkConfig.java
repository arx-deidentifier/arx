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
    public static final BenchmarkDataset    BENCHMARK_DATASET         = BenchmarkDataset.ADULT_FULL;

    /** Anonymization */
    public static final AnonymizationMethod ANONYMIZATION             = ShadowModelSetup.K2_ANONYMIZATION;

    /** Supression Limit */
    public static final double              SUPRESSION_LIMIT          = 1d;
    
    /** Feature type(s) to use */
    public static final FeatureType         FEATURE_TYPE              = FeatureType.ENSEMBLE;

    /** Classifier type to use */
    public static final ClassifierType      CLASSIFIER_TYPE           = ClassifierType.RF;

    /** Number of random targets */
    public static final int                 NUMBER_OF_TARGETS         = 50;

    /** Use crafted target */
    public static final TargetType          TARGET_TYPE               = TargetType.OUTLIER;

    /** File storing list of targets */
    public static final String              TARGET_IMPORT_FILE        = "data_new/targets_ADULT.txt";

    /** Number of independent tests */
    public static final int                 NUMBER_OF_TESTS           = 25;

    /** Number of subsamples used to train the classifier */
    public static final int                 NUMBER_OF_TRAININGS       = 10;

    /** TODO: Is this a suitable number? What is used in the paper? --> 1000 */
    public static final int                 SAMPLE_SIZE               = 1000;

    /** Size of population available for the adversary in each test */
    public static final int                 ADVERSARY_POPULATION_SIZE = 10000;
    
    private SMBenchmarkConfig() {};
    
    public static String asString() {
        StringBuilder stb = new StringBuilder();
        stb.append("Dataset: " + BENCHMARK_DATASET);
        stb.append("\n");
        stb.append("Anonymization Method: " + ANONYMIZATION);
        stb.append("\n");
        stb.append("ASupression Limit: " + SUPRESSION_LIMIT);
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

}
