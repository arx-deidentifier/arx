package org.deidentifier.arx;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.io.CSVDataInput;

/**
 * Setup class for ShadowModel MIA benchmark
 * 
 * @author Fabian Prasser
 * @author Thierry Meurers
 *
 */
public class ShadowModelBenchmarkSetup {

    /**
     * Datasets
     */
    public static enum BenchmarkDataset {
        TEXAS_10
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

        while (cfgIter.hasNext()) {
            String[] line = cfgIter.next();
            if (line[2].equals("TRUE")) {
                qiNames.add(line[0]);
                qiTypes.add(line[1]);
            }
        }

        Data data = getProjectedDataset(loadData(dataset), qiNames.toArray(new String[qiNames.size()]));

        for (int i = 0; i < qiNames.size(); i++) {
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
        return data;
    }
    
    
    /**
     * Projects data
     * @param data
     * @param qis
     * @return
     */
    private static Data getProjectedDataset(Data data, String[] qis) {
        DataHandle handle = data.getHandle();
        List<String[]> output = new ArrayList<>();
        output.add(qis);
        for (int i = 0; i < handle.getNumRows(); i++) {
            String[] record = new String[qis.length];
            for (int j = 0; j < qis.length; j++) {
                record[j] = handle.getValue(i, handle.getColumnIndexOf(qis[j]));
            }
            output.add(record);
        }
        return Data.create(output);
    };
    
    /**
     * Returns a dataset
     * @param dataset
     * @return
     * @throws IOException
     */
    public static Data loadData(BenchmarkDataset dataset) throws IOException {
        String filename = null;
        switch (dataset) {

        case TEXAS_10:
            filename = "texas_10.csv";
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

        case TEXAS_10:
            filename = "texas_10_config.csv";
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
        case TEXAS_10:
            return Hierarchy.create("data/texas_hierarchy_" + attribute + ".csv", Charset.defaultCharset(), ';');
        default:
            throw new IllegalArgumentException("Unknown dataset");
        }
    }
     
}
