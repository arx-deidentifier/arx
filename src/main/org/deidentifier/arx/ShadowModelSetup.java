package org.deidentifier.arx;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.MicroAggregationFunction;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.io.CSVDataInput;

import cern.colt.Arrays;

import org.deidentifier.arx.ARXConfiguration.AnonymizationAlgorithm;
import org.deidentifier.arx.ARXLattice.ARXNode;

/**
 * Setup class for ShadowModel MIA benchmark
 * 
 * @author Fabian Prasser
 * @author Thierry Meurers
 *
 */
public class ShadowModelSetup {
    
    /**
     * Interface for anonymization methods
     * 
     * @author Fabian Prasser
     */
    public interface AnonymizationMethod {
        public DataHandle anonymize(Data handle);
    }
    
    public static AnonymizationMethod IDENTITY_ANONYMIZATION = new AnonymizationMethod() {
        @Override
        public DataHandle anonymize(Data data) {

            // Prepare
            ARXConfiguration config = ARXConfiguration.create();
            config.addPrivacyModel(new KAnonymity(1));
            config.setSuppressionLimit(0.0d);
            config.setAlgorithm(AnonymizationAlgorithm.BEST_EFFORT_BOTTOM_UP);
            config.setHeuristicSearchStepLimit(1);
            
            // Anonymize
            ARXAnonymizer anonymizer = new ARXAnonymizer();
            try {
                ARXResult result = anonymizer.anonymize(data, config);
                // TODO remove
                /*
                ARXNode node = result.getGlobalOptimum();
                int[] transformation = node.getTransformation();
                System.out.println(Arrays.toString(transformation));
                */
                return result.getOutput();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    };

    public static AnonymizationMethod K5_ANONYMIZATION = new AnonymizationMethod() {
        @Override
        public DataHandle anonymize(Data data) {

            // Prepare
            ARXConfiguration config = ARXConfiguration.create();
            config.addPrivacyModel(new KAnonymity(5));
            config.setSuppressionLimit(0.0d);
            config.setAlgorithm(AnonymizationAlgorithm.BEST_EFFORT_BOTTOM_UP);
            config.setHeuristicSearchStepLimit(1000);
            
            // Anonymize
            ARXAnonymizer anonymizer = new ARXAnonymizer();
            try {
                return anonymizer.anonymize(data, config).getOutput();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    };
    
    /**
     * Datasets
     */
    public static enum BenchmarkDataset {
        TEXAS_10, TEXAS, TEXAS_OUTLIER, ADULT, ADULT_FULL, ADULT_14, ADULT_14_OUTLIER
    }
    
    /**
     * Configures and returns the dataset.
     * 
     * @param dataset
     * @param tm
     * @param qis
     * @return
     * @throws IOException
     */
    public static Data getData(BenchmarkDataset dataset) throws IOException {

        Iterator<String[]> cfgIter = loadDataConfig(dataset).iterator(false);

        List<String> attributeNames = new ArrayList<String>();
        List<String> attributeTypes = new ArrayList<String>();
        List<String> attributeInclude = new ArrayList<String>();
        List<String> attributeIsQI = new ArrayList<>();

        while (cfgIter.hasNext()) {
            String[] line = cfgIter.next();
            attributeNames.add(line[0]);
            attributeTypes.add(line[1]);
            attributeInclude.add(line[2]);
            attributeIsQI.add(line[3]);
        }

        Data data = loadData(dataset);

        for (int i = 0; i < attributeNames.size(); i++) {
            if (attributeInclude.get(i).equals("TRUE")) {
                
                switch (attributeTypes.get(i)) {
                case "categorical":
                    data.getDefinition().setDataType(attributeNames.get(i), DataType.STRING);
                    if(attributeIsQI.get(i).equals("TRUE")) {
                        // Set hierarchy
                        data.getDefinition().setAttributeType(attributeNames.get(i), loadHierarchy(dataset, attributeNames.get(i)));
                    } 
                    break;
                case "continuous":
                    data.getDefinition().setDataType(attributeNames.get(i), DataType.createDecimal("#.#", Locale.US));
                    if(attributeIsQI.get(i).equals("TRUE")) {
                        // Set aggregation function
                        data.getDefinition().setAttributeType(attributeNames.get(i), MicroAggregationFunction.createGeometricMean());
                    } 
                    break;
                case "ordinal":
                    // TODO
                default:
                    throw new RuntimeException("Invalid datatype");
                }

            }
        }
        return data;
    }
    
    /**
     * Returns a dataset
     * @param dataset
     * @return
     * @throws IOException
     */
    public static Data loadData(BenchmarkDataset dataset) throws IOException {
        String filename = null;
        switch (dataset) {

        case ADULT:
            filename = "data/adult.csv";
            break;
        case ADULT_FULL:
            filename = "data_new/adult_full.csv";
            break;
        case ADULT_14:
            filename = "data_new/adult_14.csv";
            break;
        case ADULT_14_OUTLIER:
            filename = "data_new/adult_14_outlier.csv";
            break;
        case TEXAS_10:
            filename = "data/texas_10.csv";
            break;
        case TEXAS:
            filename = "data/texas.csv";
            break;
        case TEXAS_OUTLIER:
            filename = "data_new/texas_outlier.csv";
            break;
        default:
            throw new RuntimeException("Invalid dataset");
        }
        return Data.create(filename, Charset.defaultCharset(), ';');
    }
    
    /**
     * Returns handle for config file
     * 
     * @param dataset
     * @return
     * @throws IOException
     */
    public static CSVDataInput loadDataConfig(BenchmarkDataset dataset) throws IOException {
        String filename = null;
        switch (dataset) {

        case ADULT:
            filename = "data/adult.cfg";
            break;
        case ADULT_FULL:
            filename = "data_new/adult_full.cfg";
            break;
        case ADULT_14:
        case ADULT_14_OUTLIER:
            filename = "data_new/adult_14.cfg";
            break;
        case TEXAS_10:
            filename = "data/texas_10.cfg";
            break;
        case TEXAS:
            filename = "data_new/texas.cfg";
            break;
        case TEXAS_OUTLIER:
            filename = "data_new/texas.cfg";
            break;
        default:
            throw new RuntimeException("Invalid dataset");
        }
        return new CSVDataInput(filename, Charset.defaultCharset(), ';');
    }
    
    /**
     * Returns the generalization hierarchy for the dataset and attribute
     * @param dataset
     * @param attribute
     * @return
     * @throws IOException
    */
    public static Hierarchy loadHierarchy(BenchmarkDataset dataset, String attribute) throws IOException {
        switch (dataset) {
        case ADULT:
            return Hierarchy.create("data/adult_hierarchy_" + attribute + ".csv", Charset.defaultCharset(), ';');
        case ADULT_FULL:
            return Hierarchy.create("data_new/adult_full_hierarchy_" + attribute + ".csv", Charset.defaultCharset(), ';');
        case ADULT_14:
        case ADULT_14_OUTLIER:
            return Hierarchy.create("data_new/adult_full_hierarchy_" + attribute + ".csv", Charset.defaultCharset(), ',');
        case TEXAS_10:
            return Hierarchy.create("data/texas_hierarchy_" + attribute + ".csv", Charset.defaultCharset(), ';');
        case TEXAS:
            return Hierarchy.create("data_new/texas_hierarchy_" + attribute + ".csv", Charset.defaultCharset(), ';');
        case TEXAS_OUTLIER:
            return Hierarchy.create("data_new/texas_hierarchy_" + attribute + ".csv", Charset.defaultCharset(), ';');
        default:
            throw new IllegalArgumentException("Unknown dataset");
        }
    }
      
}
