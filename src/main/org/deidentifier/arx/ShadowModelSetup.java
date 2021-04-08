package org.deidentifier.arx;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.io.CSVDataInput;

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
        TEXAS_10, TEXAS, ADULT
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

        List<String> qiNames = new ArrayList<String>();
        List<String> qiTypes = new ArrayList<String>();
        List<String> qiInclude = new ArrayList<String>();

        while (cfgIter.hasNext()) {
            String[] line = cfgIter.next();
            qiNames.add(line[0]);
            qiTypes.add(line[1]);
            qiInclude.add(line[2]);
        }

        Data data = loadData(dataset);

        for (int i = 0; i < qiNames.size(); i++) {
            if (qiInclude.get(i).equals("TRUE")) {
                data.getDefinition().setAttributeType(qiNames.get(i), loadHierarchy(dataset, qiNames.get(i)));
                switch (qiTypes.get(i)) {
                case "categorical":
                    data.getDefinition().setDataType(qiNames.get(i), DataType.STRING);
                    break;
                case "continuous":
                    data.getDefinition().setDataType(qiNames.get(i), DataType.createDecimal("#.#", Locale.US));
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
            filename = "adult.csv";
            break;
        case TEXAS_10:
            filename = "texas_10.csv";
            break;
        case TEXAS:
            filename = "texas.csv";
            break;
        default:
            throw new RuntimeException("Invalid dataset");
        }
        return Data.create("data/" + filename, Charset.defaultCharset(), ';');
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
            filename = "adult.cfg";
            break;
        case TEXAS_10:
            filename = "texas_10.cfg";
            break;
        case TEXAS:
            filename = "texas.cfg";
            break;
        default:
            throw new RuntimeException("Invalid dataset");
        }
        return new CSVDataInput("data/" + filename, Charset.defaultCharset(), ';');
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
        case TEXAS_10:
            return Hierarchy.create("data/texas_hierarchy_" + attribute + ".csv", Charset.defaultCharset(), ';');
        case TEXAS:
            return Hierarchy.create("data/texas_hierarchy_" + attribute + ".csv", Charset.defaultCharset(), ';');
        default:
            throw new IllegalArgumentException("Unknown dataset");
        }
    }
     
}
