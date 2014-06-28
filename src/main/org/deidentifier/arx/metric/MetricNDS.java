package org.deidentifier.arx.metric;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Node;

/**
 * Normalized Domain Share: Each node in the hierarchy is associated with the number of tuples 
 * from the *domain* of the attribute represented by it. A suppressed value represents the
 * complete domain.<br>
 * <br>
 * Preconditions of the current implementation<br>
 * - The complete domain is represented on the first level of the hierarchy<br>
 * - An additional depth-first-search is performed after running the FLASH algorithm<br>
 * <br>
 * Properties of the metric<br>
 * - The share of a suppressed value is 1d<br>
 * - As a result, the maximal information loss in each column = #tuples<br>
 * - This is used for normalization
 * 
 * @author Fabian Prasser
 *
 */
public class MetricNDS extends Metric<InformationLossRCE> {

    /** SUID*/
    private static final long serialVersionUID = 4516435657712614477L;

    /** Total number of tuples, depends on existence of research subset*/
    private double datasetSize = 0d;
    
    /** Domain-size per attribute*/
    private double[] domainSizes = null;
    
    // TODO: This array is unnecessarily complex: dimension->dictionary.length * levels
    /** Initialized at runtime: dimension->level->value(||=dictionary.length)->frequency*/
    private double[][][] frequencies; 

    /** Configuration factor*/
    private final double gFactor; 
    /** Configuration factor*/
    private final double gsFactor; 
    /** Configuration factor*/
    private final double sFactor;
    /** Configuration factor*/
    private final Map<String, Double> aFactorsMap;
    /** Configuration factor*/
    private double[] aFactors;

    /** Max */
    private double[] max = null;
    /** Min */
    private double[] min = null;
    
    /**
     * Default constructor which treats all transformation methods and attributes equally
     */
    public MetricNDS(){
        this(0.5d, new HashMap<String, Double>());
    }

    /**
     * A constructor that allows to define a factor weighting generalization and suppression.
     * 
     * @param gsFactor A factor [0,1] weighting generalization and suppression. 
     *                 The default value is 0.5, which means that generalization
     *                 and suppression will be treated equally. A factor of 0
     *                 will favor generalization, and a factor of 1 will favor
     *                 suppression. The values in between can be used for
     *                 balancing both methods.
     *                  
     */
    public MetricNDS(double gsFactor){
        // TODO: Configure weights via ARXConfig?
        this(gsFactor, new HashMap<String, Double>());
    }

    /**
     * A constructor that allows to define a factors weighting individual attributes
     *                 
     * @param aFactors Attribute factors [0,1] defining their importance, represented in a
     *                 map. An attribute not contained in the map will have a factor of 0. 
     */
    public MetricNDS(Map<String, Double> aFactors){
        // TODO: Configure weights via ARXConfig?
        this(0.5d, aFactors);
    }

    /**
     * A constructor that allows to define a factor weighting generalization, suppression
     * and individual attributes
     * 
     * @param gsFactor A factor [0,1] weighting generalization and suppression. 
     *                 The default value is 0.5, which means that generalization
     *                 and suppression will be treated equally. A factor of 0
     *                 will favor generalization, and a factor of 1 will favor
     *                 suppression. The values in between can be used for
     *                 balancing both methods.
     *                 
     * @param aFactors Attribute factors [0,1] defining their importance, represented in a
     *                 map. An attribute not contained in the map will have a factor of 0. 
     */
    public MetricNDS(double gsFactor, Map<String, Double> aFactors){
        // TODO: Configure weights via ARXConfig?
        super(false, false);
        this.gsFactor = gsFactor;
        this.sFactor = computeSuppressionFactor(gsFactor);
        this.gFactor = computeGeneralizationFactor(gsFactor);
        this.aFactorsMap = new HashMap<String, Double>(aFactors);
    }
    
    @Override
    public InformationLoss<?> createMaxInformationLoss() {
        if (max == null) {
            throw new IllegalStateException("Metric must be initialized first");
        } else {
            return new InformationLossRCE(max);
        }
    }
    
    @Override
    public InformationLoss<?> createMinInformationLoss() {
        if (min == null) {
            throw new IllegalStateException("Metric must be intialized first");
        } else {
            return new InformationLossRCE(min);
        }
    }

    /**
     * Returns the factor used weight generalized values
     * @return
     */
    public double getGeneralizationFactor() {
        return gFactor;
    }
    
    /**
     * Returns the factor weighting generalization and suppression
     * 
     * @return A factor [0,1] weighting generalization and suppression. 
     *         The default value is 0.5, which means that generalization
     *         and suppression will be treated equally. A factor of 0
     *         will favor generalization, and a factor of 1 will favor
     *         suppression. The values in between can be used for
     *         balancing both methods.
     */
    public double getGsFactor() {
        return gsFactor;
    }
    
    /**
     * Returns the attribute factors
     * @return
     */
    public Map<String, Double> getAttributeFactors() {
        return new HashMap<String, Double>(this.aFactorsMap);
    }
    
    /**
     * Returns the factor used to weight suppressed values
     * @return
     */
    public double getSuppressionFactor() {
        return sFactor;
    }

    /**
     * Returns the generalization factor for a given gs factor
     * @param gsFactor
     * @return
     */
    private double computeGeneralizationFactor(double gsFactor){
        return gsFactor <=0.5d ? 1d : 1d - 2d * gsFactor;
    }

    /**
     * Returns the suppression factor for a given gs factor
     * @param gsFactor
     * @return
     */
    private double computeSuppressionFactor(double gsFactor){
        return gsFactor <0.5d ? 1d - 2d * gsFactor : 1d;
    }

    /**
     * Normalizes the aggregate
     * @param aggregate
     * @param dimension
     * @return
     */
    private double normalize(double aggregate, int dimension) {

        double min = datasetSize / domainSizes[dimension];
        double max = datasetSize;
        double result = (aggregate - min) / (max - min);
        return result >= 0d ? result : 0d;
    }

    /**
     * Computes the number of values from the domain mapped by each value in the hierarchy
     * @param hierarchy
     * @param maps
     */
    private void prepareInitialization(String[][] hierarchy, Map<String, Double>[] maps) {
        
        // Prepare levels
        int levels = hierarchy[0].length;
        for (int i=0; i<levels; i++){
            Map<String, Double> map = new HashMap<String, Double>();
            for (int j=0; j<hierarchy.length; j++){
                String value = hierarchy[j][i];
                Double current = map.get(value);
                map.put(value, current == null ? 1d : current + 1d);
            }
            maps[i] = map;
        }
        
        // Normalize with domain size
        double domainSize = hierarchy.length;
        for (Map<String, Double> map : maps) {
            for (Entry<String, Double> entry : map.entrySet()) {
                entry.setValue(entry.getValue() / domainSize);
            }
        }
    }

    @Override
    protected InformationLossRCE evaluateInternal(Node node, 
                                                  IHashGroupify g) {
        
        // Prepare
        int[] transformation = node.getTransformation();
        int dimensions = transformation.length;
        double[] scores = new double[dimensions];

        // m.count only counts tuples from the research subset
        HashGroupifyEntry m = g.getFirstEntry();
        while (m != null) {
            if (m.count>0) {
                
                // Only respect outliers in case of anonymous nodes
                if (m.isNotOutlier || !node.isAnonymous()) {
                    for (int dimension=0; dimension<dimensions; dimension++){
                        
                        int value = m.key[dimension];
                        double share = (double)m.count * frequencies[dimension][transformation[dimension]][value];
                        scores[dimension] += share * gFactor;
                    }
                } else {
                    for (int dimension=0; dimension<dimensions; dimension++){
                        
                        if (sFactor == 1d){
                            double share = (double)m.count; // *1d
                            scores[dimension] += share;
                        } else {
                            int value = m.key[dimension];
                            double share = (double)m.count * frequencies[dimension][transformation[dimension]][value];
                            scores[dimension] += share + sFactor * (1d - share);
                        }
                    }
                }
            }
            m = m.nextOrdered;
        }
        
        // Normalize
        for (int dimension=0; dimension<dimensions; dimension++){
            scores[dimension] = normalize(scores[dimension], dimension);
        }
        
        // Return infoloss
        return new InformationLossRCE(scores, aFactors);
    }

    @Override
    protected void initializeInternal(final DataDefinition definition,
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {
        

        // Initialized during construction: attribute->size
        Map<String, Double> _domainSizes = 
                new HashMap<String, Double>();
                
        // Initialized during construction: attribute->level->value->frequency
        Map<String, Map<String, Double>[]> _frequencies = 
                new HashMap<String, Map<String, Double>[]>();
                
        // Check
        if (definition.getQuasiIdentifyingAttributes().isEmpty()) {
            throw new IllegalArgumentException("No quasi-identifiers defined");
        }
        
        // For each quasi-identifier
        for (String qi : definition.getQuasiIdentifyingAttributes()) {
            
            // Check
            String[][] hierarchy = definition.getHierarchy(qi);
            if (hierarchy == null || hierarchy.length==0 || hierarchy[0].length==0) {
                throw new IllegalArgumentException("No hierarchy defined for attribute ("+qi+")");
            }
            

            // Store domain-size
            _domainSizes.put(qi, Double.valueOf(hierarchy.length));
            
            // Initialize
            int levels = hierarchy[0].length;

            @SuppressWarnings("unchecked")
            Map<String, Double>[] map = (Map<String, Double>[]) new Map<?,?>[levels];
            _frequencies.put(qi, map);
            prepareInitialization(hierarchy, map);
        }
        
        // Check
        String[] header = input.getHeader();
        for (String qi : header) {
            if (!_frequencies.containsKey(qi)) {
                throw new IllegalStateException("The attribute ("+qi+") was not defined as a quasi-identifier when the metric was created");
            }
        }
        
        // Init
        domainSizes = new double[header.length];
        frequencies = new double[header.length][][];
        
        // Create array of frequencies for encoded data
        for (int i=0; i < header.length; i++){
            
            // Store domain-size
            domainSizes[i] = _domainSizes.get(header[i]);
            
            // Init
            Map<String, Double>[] maps = _frequencies.get(header[i]);
            GeneralizationHierarchy hierarchy = hierarchies[i];
            int[][] array = hierarchy.getArray();
            String[] dictionary = input.getDictionary().getMapping()[i];
            
            // Create result: value->level
            double[][] frequency = new double[array[0].length][dictionary.length];
            for (int j=0; j<array[0].length; j++){
                frequency[j] = new double[dictionary.length];
            }
                
            // Transform
            for (int level=0; level < hierarchy.getHeight(); level++){
                
                double[] levelFrequency = frequency[level];
                Map<String, Double> map = maps[level];
                for (int valIdx = 0; valIdx < array.length; valIdx++){
                    int val = array[valIdx][level];
                    double freq = map.get(dictionary[val]);
                    levelFrequency[val] = freq;
                }
            }
            
            // Store
            frequencies[i] = frequency;
        }
        
        // Determine total number of tuples
        if (config.containsCriterion(DPresence.class)) {
            Set<DPresence> criteria = config.getCriteria(DPresence.class);
            if (criteria.size() > 1) { 
                throw new IllegalStateException("Only one d-presence criterion supported!"); 
            } else {
                DPresence criterion = criteria.iterator().next();
                this.datasetSize = criterion.getSubset().getArray().length;
            }   
        } else {
            this.datasetSize = input.getDataLength();
        }
        
        // Min and max
        this.min = new double[this.domainSizes.length];
        Arrays.fill(min, 0d);
        this.max = new double[this.domainSizes.length];
        Arrays.fill(max, 1d);
        
        // Prepare weights
        double totalFactor = 0d;
        this.aFactors = new double[this.domainSizes.length];
        for (int i=0; i< aFactors.length; i++){
            Double factor = aFactorsMap.get(header[i]);
            factor = factor != null ? factor : 0d;
            aFactors[i] = factor;
            totalFactor += factor;
        }

        // Default case
        if (totalFactor == 0d) {
            Arrays.fill(aFactors, 1d);
        // Weighted case
        } else {
            for (int i=0; i<aFactors.length; i++){
                aFactors[i] /= totalFactor;
            }
        }
    }
}
