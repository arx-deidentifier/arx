package org.deidentifier.arx.criteria;

import org.deidentifier.arx.framework.check.distribution.Distribution;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;

/**
 * The entropy l-diversity criterion
 * @author Fabian Prasser
 */
public class EntropyLDiversity extends LDiversity{

    private static final long serialVersionUID = -354688551915634000L;

    /** Helper*/
    private final double logL;
    /** Helper*/
    private static final double  log2 = Math.log(2);
    
    /**
     * Creates a new instance
     * @param l
     */
    public EntropyLDiversity(int l){
        super(l);
        logL = Math.log(l) / Math.log(2d);
    }

    @Override
    public boolean isAnonymous(HashGroupifyEntry entry) {

        Distribution d = entry.distribution;
        
        // If less than l values are present skip
        if (d.size() < l) { return false; }

        // Copy and pack
        int totalElements = 0;
        // TODO: Why do we need a copy here???
        final int[] frequencyCopy = new int[d.size()];
        int count = 0;
        int[] buckets = d.getBuckets();
        for (int i = 0; i < buckets.length; i += 2) {
            if (buckets[i] != -1) { // bucket not empty
                final int frequency = buckets[i + 1];
                frequencyCopy[count++] = frequency;
                totalElements += frequency;
            }
        }

        double val = 0d;
        for (int i = 0; i < frequencyCopy.length; i++) {
            final double p = ((double) frequencyCopy[i] / (double) totalElements);
            val += p * log2(p);
        }
        val = -val;

        // check
        return val >= logL;
    } 
    
    /**
     * Computes log 2
     * 
     * @param num
     * @return
     */
    private final double log2(final double num) {
        return Math.log(num) / log2;
    }
}
