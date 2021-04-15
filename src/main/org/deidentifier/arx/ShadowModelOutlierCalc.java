package org.deidentifier.arx;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Comparator;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.util.Pair;
import org.deidentifier.arx.ShadowModelSetup.AnonymizationMethod;
import org.deidentifier.arx.ShadowModelSetup.BenchmarkDataset;
import org.deidentifier.arx.aggregates.StatisticsFrequencyDistribution;
import org.deidentifier.arx.aggregates.StatisticsSummary;



public class ShadowModelOutlierCalc {

    /** Dataset */
    private static final BenchmarkDataset    BENCHMARK_DATASET = BenchmarkDataset.ADULT_FULL;

    /** Feature type(s) to use */
    private static final FeatureType         FEATURE_TYPE      = FeatureType.HISTOGRAM;

    /** Anonymization */
    private static final AnonymizationMethod ANONYMIZATION     = ShadowModelSetup.IDENTITY_ANONYMIZATION;
    
    /**
     * Entry point for getting unique targets
     * 
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        // Create dataset
        Data rRef = ShadowModelSetup.getData(BENCHMARK_DATASET);

        ShadowModelOutlierCalc outlierCalc = new ShadowModelOutlierCalc(rRef, rRef.getDefinition().getQuasiIdentifyingAttributes(), FEATURE_TYPE);
        
        List<Pair<Integer, Double>> distances = outlierCalc.getRecordDistances();
        
        for(int i = 0; i < 20; i++) {
            Pair<Integer, Double> temp = distances.get(i);
            System.out.println(temp.getFirst() + " --> " + temp.getSecond());
        }
        
    }
    
    
    /**
     * Feature type
     * 
     * @author Fabian Prasser
     * @author Thierry Meurers
     */
    public static enum FeatureType {
        CORRELATION,
        ENSEMBLE,
        HISTOGRAM,
        NAIVE
    }
    
    /**
     * Simple dictionary class
     * 
     * @author Fabian Prasser
     * @author Thierry Meurers
     */
    private class Dictionary {
        
        /** Map for values*/
        private Map<String, Map<String, Integer>> map = new HashMap<>();
        
        /** Probe the dictionary*/
        public int probe(String attribute, String value) {
            
            // Get map
            Map<String, Integer> values = map.get(attribute);
            if (values == null) {
                values = new HashMap<>();
                map.put(attribute, values);
            }
            
            // Probe
            Integer code = values.get(value);
            if (code == null) {
                code = values.size();
                values.put(value, code);
            }
            
            // Done
            return code;
        }

        /**
         * Returns the size for the given dimension
         * @param attribute
         * @return
         */
        public int size(String attribute) {
            return this.map.get(attribute).size();
        }
    }
    
    /**
     * Base for features
     * 
     * @author Fabian Prasser
     * @author Thierry Meurers
     */
    private interface Feature {
        /** Compile feature data*/
        public double[] compile();
    }

    /**
     * Correlation feature
     * 
     * @author Fabian Prasser
     * @author Thierry Meurers
     */
    private class FeatureCorrelation implements Feature {

        /** Features */
        private Map<String, OpenMapRealMatrix> categorical = new HashMap<>();
        /** Features */
        private Map<String, double[]>          numeric     = new HashMap<>();
        /** Rows */
        private int                            rows;

        /**
         * Calculates correlation features using Pearson's product-moment correlation.
         * All columns of continuous attributes are directly used for correlation calculation.
         * Categorical and ordinal attributes are transfered to a sparse representation
         * were each value becomes an own column and whether (or not) the value applies to a row
         * is indicated by the value 1d (or 0d).
         * @param handle
         */
        public FeatureCorrelation(DataHandle handle) {

            // Prepare
            this.rows = handle.getNumRows();
            
            // For each attribute
            for (String attribute : attributes) {

                // Obtain attribute details
                int column = handle.getColumnIndexOf(attribute);
                String attributeName = handle.getAttributeName(column);
                DataType<?> _type = handle.getDefinition().getDataType(attributeName);
                Class<?> _clazz = _type.getDescription().getWrappedClass();

                // Just store numeric values as is
                if (_clazz.equals(Long.class) || _clazz.equals(Double.class) || _clazz.equals(Date.class)) {
                    
                    // Create array
                    double[] values = new double[handle.getNumRows()];
                    
                    // Copy values as double
                    for (int row = 0; row < values.length; row++) {
                        values[row] = getDouble(handle, row, column, _clazz);
                    }
                    
                    // Store
                    numeric.put(attribute, values);
                    
                } else if (_clazz.equals(String.class)) {
                    
                    // Create matrix
                    OpenMapRealMatrix matrix = new OpenMapRealMatrix(handle.getNumRows(), dictionary.size(attribute));
                    
                    // Store values
                    for (int row = 0; row < handle.getNumRows(); row++) {
                        matrix.setEntry(row, dictionary.probe(attribute, handle.getValue(row, column)), 1);
                    }
                    
                    // Store
                    categorical.put(attribute, matrix);
                }
            }
        }

        @Override
        public double[] compile() {

            // Count columns
            int columns = 0;

            // For each attribute
            for (String attribute : attributes) {
                if (numeric.containsKey(attribute)) {
                    columns++;
                } else {
                    columns+=dictionary.size(attribute);
                }
            }
            
            // Prepare matrix
            OpenMapRealMatrix matrix = new OpenMapRealMatrix(rows, columns);
            int column = 0;
            for (String attribute : attributes) {
                
                // Copy numeric data
                if (numeric.containsKey(attribute)) {
                    double[] values = numeric.get(attribute);
                    for (int row = 0; row < rows; row++) {
                        matrix.setEntry(row, column, values[row]);
                    }
                    column++;

                // Copy categorical data
                } else {
                    OpenMapRealMatrix _matrix = categorical.get(attribute);
                    for (int _column = 0; _column < dictionary.size(attribute); _column++) {
                        if (_column < _matrix.getColumnDimension()) {
                            for (int row = 0; row < rows; row++) {
                                matrix.setEntry(row, column, _matrix.getEntry(row, _column));
                            }
                        }
                        column++;
                    }
                }
            }
            
            // Calculate
            double[][] result = new PearsonsCorrelation().computeCorrelationMatrix(matrix).getData();
            
            // Done
            return getFlattenedArray(result);
        }
    }

    /**
     * Ensemble feature
     * 
     * @author Fabian Prasser
     * @author Thierry Meurers
     */
    private class FeatureEnsemble implements Feature {

        /** Feature */
        private FeatureCorrelation correlation;
        /** Feature */
        private FeatureHistogram   histogram;
        /** Feature */
        private FeatureNaive       naive;

        /**
         * Creates a new instance
         * @param handle
         */
        public FeatureEnsemble(DataHandle handle) {
            naive = new FeatureNaive(handle);
            histogram = new FeatureHistogram(handle);
            correlation = new FeatureCorrelation(handle);
        }

        @Override
        public double[] compile() {
            double[] _naive = naive.compile();
            double[] _histogram = histogram.compile();
            double[] _correlation = correlation.compile();
            return getFlattenedArray(_naive, _histogram, _correlation);
        }
    }

    /**
     * Histogram feature
     * 
     * @author Fabian Prasser
     * @author Thierry Meurers
     */
    private class FeatureHistogram implements Feature {

        /** Features */
        private Map<String, StatisticsFrequencyDistribution> categorical = new HashMap<>();
        /** Features */
        private Map<String, double[]>                        numeric     = new HashMap<>();

        /**
         * Creates a new instance.
         * For categorical attributes this simply refers to the counts of distinct values.
         * For continuous and ordinal attributes, the domain of the values is separated into 10 bins.
         * @param handle
         */
        public FeatureHistogram(DataHandle handle) {

            // For each attribute
            for (String attribute : attributes) {

                // Obtain attribute details
                int column = handle.getColumnIndexOf(attribute);
                DataType<?> _type = handle.getDefinition().getDataType(attribute);
                Class<?> _clazz = _type.getDescription().getWrappedClass();
                checkDataType(attribute, _type);

                // Bining for numerical attributes
                if (_clazz.equals(Long.class) || _clazz.equals(Double.class) || _clazz.equals(Date.class)) {

                    // Prepare
                    double min = minimum.get(attribute);
                    double max = maximum.get(attribute);
                    double binSize = (max - min) / NUM_BINS;
                    double[] freqs = new double[NUM_BINS];

                    // For each value
                    for (int row = 0; row < handle.getNumRows(); row++) {

                        // Parse value
                        double value = getDouble(handle, row, column, _clazz);

                        // Calculate bin
                        int bin = (int) ((value - min) / binSize);

                        // Check range
                        if (0 > bin || bin > NUM_BINS) {
                            throw new RuntimeException("Value out of histogram range");
                        }

                        // TODO Dirty quick-fix
                        if (bin == NUM_BINS) {
                            bin -= 1;
                        }

                        // Increment frequency of bin
                        freqs[bin] += 1d;
                    }
                    
                    // Store
                    numeric.put(attribute, freqs);

                // Frequency distribution for categorial attributes
                } else if (_clazz.equals(String.class)) {
                    categorical.put(attribute, handle.getStatistics().getFrequencyDistribution(column));
                }
            }
        }

        @Override
        public double[] compile() {
            
            // Prepare
            List<double[]> features = new ArrayList<>();
            
            // For each attribute
            for (String attribute : attributes) {
                
                // Numeric attribute
                if (numeric.containsKey(attribute)) {
                    features.add(numeric.get(attribute));
                    
                // Categorical attribute
                } else {
                    StatisticsFrequencyDistribution distribution = categorical.get(attribute);
                    double[] feature = new double[dictionary.size(attribute)];
                    for (int i = 0; i < distribution.values.length; i++) {
                        String value = distribution.values[i];
                        double count = distribution.frequency[i] * distribution.count;
                        int code = dictionary.probe(attribute, value);
                        feature[code] = count;
                    }
                    features.add(feature);
                }
            }
            
            // Done
            return getFlattenedArray(features.toArray(new double[features.size()][]));
        }
    }
    
    /**
     * Naive feature
     * 
     * @author Fabian Prasser
     * @author Thierry Meurers
     */
    private class FeatureNaive implements Feature {
        
        // Features
        private double[] features;
        
        /**
         * Creates a new instance
         * @param handle
         */
        @SuppressWarnings("unchecked")
        public FeatureNaive(DataHandle handle) {
            
            // Prepare
            features = new double[attributes.length * 3];
            
            // Calculate statistics
            Map<String, StatisticsSummary<?>> statistics = handle.getStatistics().getSummaryStatistics(false);
            
            // For each attribute
            int index = 0;
            for (String attribute : attributes) {
                
                // Index
                int column = handle.getColumnIndexOf(attribute);

                // Obtain statistics
                StatisticsSummary<?> summary = statistics.get(attribute);
                DataType<?> _type = handle.getDefinition().getDataType(attribute);
                Class<?> _clazz = _type.getDescription().getWrappedClass();
                checkDataType(attribute, _type);

                // Parameters to calculate
                Double mostFreq = null;
                Double leastFreq = null;
                Double uniqueElements = null;
                Double mean = null;
                Double median = null;
                Double var = null;

                // Calculate depending on data type
                if (_clazz.equals(Long.class)) {
                    
                    // Handle data type represented as long
                    DataType<Long> type = (DataType<Long>)_type;
                    mean = summary.getArithmeticMeanAsDouble();
                    var = summary.getSampleVarianceAsDouble();
                    Long _median = type.parse(summary.getMedianAsString());
                    median = _median != null ? _median.doubleValue() : 0d; // TODO: how to handle null here
                    
                } else if (_clazz.equals(Double.class)) {
                    
                    // Handle data type represented as double
                    DataType<Double> type = (DataType<Double>)_type;
                    mean = summary.getArithmeticMeanAsDouble();
                    var = summary.getSampleVarianceAsDouble();
                    Double _median = type.parse(summary.getMedianAsString());
                    median = _median != null ? _median : 0d; // TODO: how to handle null here
                    
                } else if (_clazz.equals(Date.class)) {
                    
                    // Handle data type represented as date
                    DataType<Date> type = (DataType<Date>)_type;
                    mean = summary.getArithmeticMeanAsDouble();
                    var = summary.getSampleVarianceAsDouble();
                    Date _median = type.parse(summary.getMedianAsString());
                    median = _median != null ? _median.getTime() : 0d; // TODO: how to handle null here
                    
                } else if (_clazz.equals(String.class)) {
                    
                    // Count frequencies of values
                    Map<String, Integer> map = new HashMap<>();
                    for (int row = 0; row < handle.getNumRows(); row++) {
                        String value = handle.getValue(row, column);
                        Integer count = map.get(value);
                        if (count == null) {
                            count = 1;
                        } else {
                            count++;
                        }
                        map.put(value, count);
                    }
                    
                    // Determine codes with highest and lowest frequencies
                    int minFreq = Integer.MAX_VALUE;
                    int maxFreq = Integer.MIN_VALUE;
                    
                    // Find most and least frequent
                    for (Entry<String, Integer> entry : map.entrySet()) {
                        String value = entry.getKey();
                        Integer count = entry.getValue();
                        double code = dictionary.probe(attribute, value);
                        if (count < minFreq) {
                            minFreq = count;
                            leastFreq = code;
                        }
                        if (count > maxFreq) {
                            maxFreq = count;
                            mostFreq = code;
                        }
                    }

                    // Get number of assigned keys
                    uniqueElements = (double) map.size();

                    
                } else {
                    throw new IllegalStateException("Unknown data type");
                }
                
                // Switch feature type
                if (mean != null && var != null && median != null) {
                    features[index] = mean;
                    features[index + 1] = median;
                    features[index + 2] = var;
                    
                } else if (mostFreq != null && leastFreq != null && uniqueElements != null) {
                    features[index] = uniqueElements;
                    features[index + 1] = mostFreq;
                    features[index + 2] = leastFreq;
                } else {
                    throw new IllegalStateException("Features unavailable");
                }
                
                // Increment feature index
                index += 3;
            }
        }

        @Override
        public double[] compile() {
            return features;
        }
    }

    /** Number of bins to use for histogram feature */
    private final static int NUM_BINS = 10;

    /** Attributes to consider */
    private String[]                 attributes;
    /** To ensure consistency of data types */
    private Map<String, DataType<?>> dataTypes    = new HashMap<>();
    /** Dictionary */
    private Dictionary               dictionary   = new Dictionary();
    /** Type */
    private FeatureType              featureType;
    /** Maximum */
    private Map<String, Double>      maximum      = new HashMap<>();
    /** Minimum */
    private Map<String, Double>      minimum      = new HashMap<>();
    /** Population */
    private Data               population;
    /** Population handle */
    private DataHandle               populationHandle;

    /**
     * Creates a new instance
     * @param population
     * @param _attributes
     * @param featureType
     * @param classifierType
     */
    public ShadowModelOutlierCalc(Data population, Set<String> _attributes, FeatureType featureType) {
        this.featureType = featureType;
        this.attributes = new String[_attributes.size()];
        this.population = population;
        this.populationHandle = population.getHandle();
        int index = 0;
        for (int column = 0; column < populationHandle.getNumColumns(); column++) {
            String attribute = populationHandle.getAttributeName(column);
            if (_attributes.contains(attribute)) {
                this.attributes[index++] = attribute;
            }
        }
        this.analyzePopulation(populationHandle, attributes);
    }
    

    /**
     * Return a list of pairs containing the indices of records and their distance to the average of the dataset.
     * 
     * @return
     */
    public List<Pair<Integer, Double>> getRecordDistances(){
        
        // Get features of Ref population
        double[] featuresRef = getFeatures(populationHandle).compile();

        // initialize result list
        List<Pair<Integer, Double>> result = new ArrayList<Pair<Integer, Double>>();
        
        for(int i = 0; i < populationHandle.getNumRows(); i++) {
            Set<Integer> tempList = new HashSet<>();
            tempList.add(i);
            // Create one-rowed data handle
            DataHandle tempHandle = anonymize(population, tempList, ANONYMIZATION);
            //DataHandle tempHandle = Data.create(new String[][] {getRow(population, i)}).getHandle();
            // Get features of row
            double[] tempFeatures = getFeatures(tempHandle).compile();
            // Calculate euc. distance
            
            Double tempDistance = new EuclideanDistance().compute(featuresRef, tempFeatures);
            result.add(new Pair<>(i, tempDistance));
            
            if (i % 100 == 0) {
                System.out.println(i);
            }
            
        }
        
        // Sort indices by distance in descending order
        Collections.sort(result, new Comparator<Pair<Integer, Double>>() {
            @Override
            public int compare(final Pair<Integer, Double> p1, final Pair<Integer, Double> p2) {
                if(p1.getSecond() > p1.getSecond())
                    return 1;
                return -1;           
            }
        });
        
        return result;
    }

    /**
     * Analyze basic population properties
     * @param population
     * @param attributes
     */
    private void analyzePopulation(DataHandle population, String[] attributes) {

        // Calculate statistics
        Map<String, StatisticsSummary<?>> statistics = population.getStatistics().getSummaryStatistics(false);
        
        // For each attribute
        for (String attribute : attributes) {
            
            // Obtain statistics
            StatisticsSummary<?> summary = statistics.get(attribute);
            DataType<?> _type = population.getDefinition().getDataType(attribute);
            Class<?> _clazz = _type.getDescription().getWrappedClass();
            checkDataType(attribute, _type);

            // Calculate depending on data type
            if (_clazz.equals(Long.class)) {
                // Handle data type represented as long
                double min = (Long)summary.getMinAsValue();
                double max = (Long)summary.getMaxAsValue();
                minimum.put(attribute, min);
                maximum.put(attribute, max);
            } else if (_clazz.equals(Double.class)) {
                // Handle data type represented as double
                double min = (Double)summary.getMinAsValue();
                double max = (Double)summary.getMaxAsValue();
                minimum.put(attribute, min);
                maximum.put(attribute, max);
            } else if (_clazz.equals(Date.class)) {
                // Handle data type represented as date
                double min = ((Date)summary.getMinAsValue()).getTime();
                double max = ((Date)summary.getMaxAsValue()).getTime();
                minimum.put(attribute, min);
                maximum.put(attribute, max);
            } else {
                
                // Pre-encode categorical values considering the order
                int column = population.getColumnIndexOf(attribute);
                for (String value : population.getStatistics().getDistinctValuesOrdered(column, true)) {
                    dictionary.probe(attribute, value);
                }
            }
        }
    }
    
    /**
     * Sanity check to ensure consistency of data types
     * @param attribute
     * @param type
     */
    private void checkDataType(String attribute, DataType<?> type) {
        DataType<?> _type = dataTypes.get(attribute);
        if (_type == null) {
            dataTypes.put(attribute, type);
        } else if (!(_type.equals(type))) {
            throw new IllegalArgumentException("Inconsistent data type detected for attribute: " + attribute);
        }
    }
    
    /**
     * Return double for numerical values
     * @param handle
     * @param row
     * @param column
     * @param _clazz
     * @return
     */
    private double getDouble(DataHandle handle, int row, int column, Class<?> _clazz) {

        try {
            // Calculate depending on data type
            if (_clazz.equals(Long.class)) {
                return handle.getLong(row, column);
            } else if (_clazz.equals(Double.class)) {
                return handle.getDouble(row, column);
            } else if (_clazz.equals(Date.class)) {
                return handle.getDate(row, column).getTime();
            } else {
                throw new IllegalStateException("Attribute is not numeric");
            }
        } catch (ParseException e) {
            // TODO Why caused by short heuristic searches?
            throw new IllegalStateException(e);
        }
    }
    
    /**
     * Calculates features
     * @return
     */
    private Feature getFeatures(DataHandle handle) {
        switch (featureType) {
        case ENSEMBLE:
            return new FeatureEnsemble(handle);    
        case CORRELATION:
            return new FeatureCorrelation(handle);            
        case HISTOGRAM:
            return new FeatureHistogram(handle);
        case NAIVE:
            return new FeatureNaive(handle);
        default:
            throw new IllegalArgumentException("Unknown feature!");
        }
    }
    
    /**
     * Transforms array of arrays to flatten array
     * 
     * @param input
     * @return
     */
    private double[] getFlattenedArray(double[]... input) {
        
        // calculate size of flatten array
        int outputLength = 0;
        for(double[] part : input) {
            outputLength += part.length;
        }
        
        // copy into flatten array
        double[] output = new double[outputLength];
        int posOutput = 0;
        for(double[] part : input) {
            for(double value : part) {
                output[posOutput++] = value;
            }
        }
        return output;
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
