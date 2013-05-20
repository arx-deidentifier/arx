package org.deidentifier.arx.criteria;

import java.util.Arrays;

import org.deidentifier.arx.framework.check.distribution.Distribution;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;

/**
 * The recursive-(c,l)-diversity criterion
 * @author Fabian Prasser
 */
public class RecursiveCLDiversity extends LDiversity{

    private static final long serialVersionUID = -5893481096346270328L;

    /** The parameter c */
    public final double c;
    
    /**
     * Creates a new instance
     * @param c
     * @param l
     */
    public RecursiveCLDiversity(double c, int l){
        super(l);
        this.c = c;
    }

    @Override
    public boolean isAnonymous(HashGroupifyEntry entry) {

        Distribution d = entry.distribution;
        
        // if less than l values are present skip
        if (d.size() < l) { return false; }

        // Copy and pack
        int[] buckets = d.getBuckets();
        final int[] frequencyCopy = new int[d.size()];
        int count = 0;
        for (int i = 0; i < buckets.length; i += 2) {
            if (buckets[i] != -1) { // bucket not empty
                frequencyCopy[count++] = buckets[i + 1];
            }
        }

        // Sort
        Arrays.sort(frequencyCopy);
        
        // Compute threshold
        double threshold = 0;
        for (int i = frequencyCopy.length - l; i >= 0; i--) {
            threshold += frequencyCopy[i];
        }
        threshold *= c;

        // Check
        return frequencyCopy[frequencyCopy.length - 1] < threshold;
    }
}
