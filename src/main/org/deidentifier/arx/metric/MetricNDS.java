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
 * Normalized Domain Share: This metric will respect attribute weights defined in the configuration.<br>
 * <br>
 * Each node in the hierarchy is associated with the number of tuples 
 * from the domain of the attribute represented by it. A suppressed value represents the
 * complete domain.<br>
 * <br>
 * The current implementation assumes that the complete domain is represented on the first level of the hierarchy<br>
 * 
 * @author Fabian Prasser
 */
public class MetricNDS extends MetricWeighted<InformationLossRCE> {

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
    private final double gWeight; 
    /** Configuration factor*/
    private final double gsWeight; 
    /** Configuration factor*/
    private final double sWeight;

    /** Max */
    private double[] max = null;
    /** Min */
    private double[] min = null;
    
    /**
     * Default constructor which treats all transformation methods and attributes equally
     */
    public MetricNDS(){
        this(0.5d);
    }

    /**
     * A constructor that allows to define a factor weighting generalization and suppression
     * 
     * @param gsWeight A factor [0,1] weighting generalization and suppression. 
     *                 The default value is 0.5, which means that generalization
     *                 and suppression will be treated equally. A factor of 0
     *                 will favor suppression, and a factor of 1 will favor
     *                 generalization. The values in between can be used for
     *                 balancing both methods. 
     */
    public MetricNDS(double gsWeight){
        super(false, false);
        this.gsWeight = gsWeight;
        this.sWeight = computeSuppressionFactor(gsWeight);
        this.gWeight = computeGeneralizationFactor(gsWeight);
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
     * Returns the factor weighting generalization and suppression
     * 
     * @return A factor [0,1] weighting generalization and suppression. 
     *         The default value is 0.5, which means that generalization
     *         and suppression will be treated equally. A factor of 0
     *         will favor suppression, and a factor of 1 will favor
     *         generalization. The values in between can be used for
     *         balancing both methods.
     */
    public double getGeneralizationSuppressionWeight() {
        return gsWeight;
    }
    
    /**
     * Returns the factor used weight generalized values
     * @return
     */
    public double getGeneralizationWeight() {
        return gWeight;
    }

    @Override
    public String getName() {
        return "Normalized Domain Share";
    }
    
    /**
     * Returns the factor used to weight suppressed values
     * @return
     */
    public double getSuppressionWeight() {
        return sWeight;
    }
    
    @Override
    public String toString() {
        return "Normalized Domain Share ("+gsWeight+"/"+gWeight+"/"+sWeight+")";
    }

    /**
     * Returns the generalization factor for a given gs factor
     * @param gsFactor
     * @return
     */
    private double computeGeneralizationFactor(double gsFactor){
        return gsFactor <=0.5d ? 1d : 1d - 2d * (gsFactor - 0.5d);
    }

    /**
     * Returns the suppression factor for a given gs factor
     * @param gsFactor
     * @return
     */
    private double computeSuppressionFactor(double gsFactor){
        return gsFactor <0.5d ? 2d * gsFactor : 1d;
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
    protected InformationLossWithBound<InformationLossRCE>
            getInformationLossInternal(Node node, IHashGroupify g) {
        
        // Prepare
        int[] transformation = node.getTransformation();
        int dimensions = transformation.length;
        double[] scores = new double[dimensions];

        // m.count only counts tuples from the research subset
        HashGroupifyEntry m = g.getFirstEntry();
        while (m != null) {
            if (m.count>0) {
                
                if (m.isNotOutlier) {
                    for (int dimension=0; dimension<dimensions; dimension++){
                        int value = m.key[dimension];
                        double share = (double)m.count * frequencies[dimension][transformation[dimension]][value];
                        scores[dimension] += share * gWeight;
                    }
                } else {
                    for (int dimension=0; dimension<dimensions; dimension++){
                        
                        if (sWeight == 1d){
                            double share = (double)m.count; // *1d
                            scores[dimension] += share;
                        } else {
                            int value = m.key[dimension];
                            double share = (double)m.count * frequencies[dimension][transformation[dimension]][value];
                            scores[dimension] += share + sWeight * ((double)m.count - share);
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
        return new InformationLossWithBound<InformationLossRCE>(new InformationLossRCE(scores, weights));
    }

    @Override
    protected InformationLossRCE getLowerBoundInternal(Node node) {
        // TODO: Implement
        return null;
    }

    @Override
    protected InformationLossRCE getLowerBoundInternal(Node node,
                                                           IHashGroupify groupify) {
        // TODO: Implement
        return null;
    }
    
    @Override
    protected void initializeInternal(final DataDefinition definition,
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {
        
        // Prepare weights
        super.initializeInternal(definition, input, hierarchies, config);

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
    }
}
