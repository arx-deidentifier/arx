package org.deidentifier.arx.risk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.common.WrappedInteger;
import org.deidentifier.arx.exceptions.ComputationInterruptedException;

/**
 * A class for attribute-related risks
 * 
 * @author Fabian Prasser
 */
public abstract class RiskModelAttributes {

    /**
     * Risks associated to a certain quasi-identifier
     * 
     * @author Fabian Prasser
     */
    public final class QuasiIdentifierRisk implements
            Comparable<QuasiIdentifierRisk> {

        /** Field */
        private final Set<String> identifier;
        /** Field */
        private double 			  alphaDistinction;
        /** Field */
        private double 			  alphaSeparation;

        /**
         * Creates a new instance
         * 
         * @param identifier
         */
        private QuasiIdentifierRisk(Set<String> identifier) {
            RiskProvider provider = getRiskProvider(identifier, stop);
            this.identifier = identifier;
            this.alphaDistinction = provider.getAlphaDistinction();
            this.alphaSeparation = provider.getAlphaSeparation();
        }

        @Override
        public int compareTo(QuasiIdentifierRisk other) {
            int cmp = Integer.compare(this.identifier.size(), other.identifier.size());
            if (cmp != 0) {
                return cmp;
            }

            cmp = Double.compare(this.alphaDistinction, other.alphaDistinction);
            if (cmp != 0) {
                return cmp;
            }

            cmp = Double.compare(this.alphaSeparation, other.alphaSeparation);
            if (cmp != 0) {
                return cmp;
            }
           return 0; // TODO Max: fallback value?
        }
        /**
         * @return the alpha distinction
         */
        public double getDistinction() {
        	return alphaDistinction;
        }
        
        /**
         * @return alpha separation
         */
        public double getSeparation() {
        	return alphaSeparation;
        }

        /**
         * @return the identifier
         */
        public Set<String> getIdentifier() {
            return identifier;
        }
    }

    /**
     * Helper interface
     * 
     * @author Fabian Prasser
     */
    interface RiskProvider {
        double getAlphaDistinction();

        double getAlphaSeparation();
    }

    /** Stop */
    private final WrappedBoolean        stop;
    /** Result */
    private final QuasiIdentifierRisk[] risks;
    /** Result */
    private final int                   numIdentifiers;

    /**
     * Creates a new instance
     * 
     * @param identifiers
     * @param stop
     */
    RiskModelAttributes(Set<String> identifiers,
                        WrappedBoolean stop,
                        WrappedInteger percentageDone) {
        this.stop = stop;
        this.numIdentifiers = identifiers.size();

        // Compute risk estimates for all elements in the power set
        Set<Set<String>> powerset = getPowerSet(identifiers);
        Map<Set<String>, QuasiIdentifierRisk> scores = new HashMap<Set<String>, QuasiIdentifierRisk>();
        int done = 0;
        for (Set<String> set : powerset) {
            checkInterrupt();
            if (!set.isEmpty()) {
                scores.put(set, new QuasiIdentifierRisk(set));
                percentageDone.value = (int) Math.round((double) done++ /
                                                        (double) (powerset.size() - 1) *
                                                        100d);
            }
        }

        // Now create sorted array
        risks = new QuasiIdentifierRisk[scores.size()];
        int idx = 0;
        for (QuasiIdentifierRisk value : scores.values()) {
            risks[idx++] = value;
        }
        Arrays.sort(risks);
    }

    /**
     * Returns the quasi-identifiers, sorted by risk
     * 
     * @return
     */
    public QuasiIdentifierRisk[] getAttributeRisks() {
        return this.risks;
    }

    /**
     * Returns the number of identifiers
     * 
     * @return
     */
    public int getNumIdentifiers() {
        return this.numIdentifiers;
    }

    /**
     * Checks for interrupts
     */
    private void checkInterrupt() {
        if (stop.value) { throw new ComputationInterruptedException(); }
    }

    /**
     * Returns the power set
     * 
     * @param originalSet
     * @return
     */
    private <T> Set<Set<T>> getPowerSet(Set<T> originalSet) {
        checkInterrupt();
        Set<Set<T>> sets = new HashSet<Set<T>>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<T>());
            return sets;
        }
        List<T> list = new ArrayList<T>(originalSet);
        T head = list.get(0);
        Set<T> rest = new HashSet<T>(list.subList(1, list.size()));
        for (Set<T> set : getPowerSet(rest)) {
            checkInterrupt();
            Set<T> newSet = new HashSet<T>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);
        }
        return sets;
    }

    /**
     * Implement this to provide risk estimates
     * 
     * @param attributes
     * @param stop
     * @return
     */
    protected abstract RiskProvider getRiskProvider(Set<String> attributes,
                                                    WrappedBoolean stop);
}
