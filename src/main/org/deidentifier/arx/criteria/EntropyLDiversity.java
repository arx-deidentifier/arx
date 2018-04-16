/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.deidentifier.arx.criteria;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.framework.check.distribution.Distribution;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.lattice.Transformation;

/**
 * The entropy l-diversity privacy model.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 * @author Sebastian Stammler
 */
public class EntropyLDiversity extends LDiversity {

    /**
     * Enumerator of entropy estimators for the entropy-l-diversity privacy model.
     * This enumerator actually holds the logarithm substitute \psi for
     * entropy estimation via the formula
     *   $H = \psi(N) - 1/N \sum n \psi (n)$
     *
     * @author Sebastian Stammler
     *
     */
    public enum EntropyEstimator {

        /** The Shannon estimator*/
    	SHANNON(new IPsi(){public double f(int n) {return Math.log(n);}}, "Shannon"),
    	/** The Grassberger estimator*/
    	GRASSBERGER(new IPsi(){public double f(int n) {return G(n);}}, "Grassberger");

        /** 
         * In Java, we need to use an inner functional interface
         * to have an enumerator of functions... doh
         * 
         * @author Sebastian Stammler
         */
        private interface IPsi {
            public double f(int n);
        }
        
        /** Our inner function */
        private final IPsi            psi;
        /** The name */
        private final String          name;

    	/**
         * Holds precomputed values of G_n for 1 <= n <= 100<br>
         * It is G_1 = G_PRECOMPUTED[0].
         * For n>1, we have G_{2n+1} := G_{2n}, so we only store the values for even index:
         * G_{2n} = G_PRECOMPUTED[n]
         */
        final private static double [] G_PRECOMPUTED = {
                -1.2703628454614782, // G_1
                0.7296371545385218, // G_2
                1.3963038212051886, // G_4
                1.7963038212051885, // G_6
                2.0820181069194743, // G_8
                2.3042403291416966, // G_10
                2.4860585109598783, // G_12
                2.639904664806032, // G_14
                2.7732379981393653, // G_16
                2.8908850569628948, // G_18
                2.9961482148576315, // G_20
                3.091386310095727, // G_22
                3.178342831834857, // G_24
                3.2583428318348573, // G_26
                3.3324169059089312, // G_28
                3.4013824231503107, // G_30
                3.4658985521825687, // G_32
                3.5265046127886293, // G_34
                3.5836474699314866, // G_36
                3.6377015239855406, // G_38
                3.6889835752675917, // G_40
                3.7377640630724698, // G_42
                3.7842756909794466, // G_44
                3.8287201354238913, // G_46
                3.871273326913253, // G_48
                3.912089653443865, // G_50
                3.951305339718375, // G_52
                3.9890411887749786, // G_54
                4.025404825138615, // G_56
                4.060492544436861, // G_58
                4.094390849521607, // G_60
                4.127177734767508, // G_62
                4.1589237665135395, // G_64
                4.18969299728277, // G_66
                4.219543743551427, // G_68
                4.248529250797804, // G_70
                4.276698264882311, // G_72
                4.304095525156284, // G_74
                4.33076219182295, // G_76
                4.356736217796977, // G_78
                4.382052673493179, // G_80
                4.40674403151787, // G_82
                4.430840417060039, // G_84
                4.454369828824745, // G_86
                4.477358334571871, // G_88
                4.49983024468423, // G_90
                4.521808266662253, // G_92
                4.543313643006339, // G_94
                4.564366274585286, // G_96
                4.5849848312863175, // G_98
                4.605186851488337, // G_100
        };
    	
    	/** Static s1 */
        private static final double s1 = 1d/24;
    	/** Static s2 */
        private static final double s2 = 7d/960;
        /** Static s3 */
        private static final double s3 = 31d/8064;
        
        /**
         * Calculates the Grassberger entropy correction term G_n<br>
         * <br>
         * $$G_{2n+1} := G_{2n} = -\gamma -\log2 +\sum_{k=1}^n 2/(2k-1)$$
         * The first 100 values are precomputed. After that, an expansion of the Digamma function at infinity is used.
         *
         * @param n > 0 (not checked!)
         * @return G_n
         */
        private static double G(int n) {
            if (n <= 100) {
                return G_PRECOMPUTED[(n-n%2)/2];
            }

            n -= n%2; // Make n even
            final double m = 1d / ((n/2)*(n/2));

            return Math.log(n) + m *(s1 - m *(s2 - m*s3));
        }
        /**
    	 * Creates a new instance
    	 * @param psi
    	 * @param name
    	 */
    	private EntropyEstimator(IPsi psi, String name) {
    		this.psi = psi;
    		this.name = name;
    	}
    	
    	/**
    	 * The logarithm substitute of the current estimator
    	 * 
    	 * The difference in estimating the entropy by the naive Shannon or Grassberger
    	 * estimator is actually using log or G for \psi in the entropy formula
    	 *    $H = \psi(N) - 1/N \sum n \psi(n)$
    	 * where N is the size of the set and the sum goes over all values of the
    	 * sensitive attribute, n is the count of the current sensitive attribute
    	 *  
    	 * @param n
    	 * @return The logarithm substitute of the estimator
    	 */
    	public double psi(int n) {
    		return psi.f(n);
    	}
        
        @Override
    	public String toString() {
    	    return name;
    	}
    }
    
    /**  SVUID */
    private static final long   serialVersionUID = -354688551915634000L;

    /** Entropy estimator to be used */
    private EntropyEstimator estimator;
    
    /**
     * Creates a new instance of the entropy l-diversity model as proposed in:<br>
     * Machanavajjhala A, Kifer D, Gehrke J. l-diversity: Privacy beyond k-anonymity.<br>
     * Transactions on Knowledge Discovery from Data (TKDD). 2007;1(1):3.
     *
     * @param attribute
     * @param l
     */
    public EntropyLDiversity(String attribute, double l){
        super(attribute, l, false, true);
        this.estimator = EntropyEstimator.SHANNON;
    }

    /**
     * Creates a new instance of the entropy-l-diversity privacy model,
     * specifying the entropy estimator be to used.
     * Two estimators are available:<br>
     * <ul>
     *   <li> 
     *   SHANNON for the usual naive Shannon estimator:
     *   this amounts to the original entropy-l-diversity definition by Machanavajjhala.
     *   </li>
     *   <li>
     *   GRASSBERGER for the corrected Grassberger estimator as proposed in:
     *   P Grassberger. Entropy Estimates from Insufficient Samplings.
     *   https://arxiv.org/abs/physics/0307138v2<br>
     *   This estimator generally accepts more sets as being entropy-l-diverse than
     *   the naive Shannon estimator, thus increases data utility.
     *   It also guarantees a more consistent meaning of the security
     *   parameter l between different data sets. For details take a look at:
     *   S Stammler, S Katzenbeisser, K Hamacher.
     *   Correcting Finite Sampling Issues in Entropy l-diversity.
     *   Privacy in Statistical Databases 2016. LNCS Vol. 9867 pp 135-146
     *   </li>
     * </ul>
     *   
     * @param attribute The sensitive attribute
     * @param l Security parameter
     * @param estimator Entropy estimator (SHANNON or GRASSBERGER)
     */
    public EntropyLDiversity(String attribute, double l, EntropyEstimator estimator) {
    	super(attribute, l, false, true);
        this.estimator = estimator;
	}

	@Override
    public EntropyLDiversity clone() {
        return new EntropyLDiversity(this.getAttribute(), this.getL(), this.getEstimator());
    }

    /**
     * Returns the entropy estimator used by this instance
     * @return
     */
    public EntropyEstimator getEstimator() {
		return estimator;
	}

    @Override
    public boolean isAnonymous(Transformation node, HashGroupifyEntry entry) {

        Distribution d = entry.distributions[index];

        // If less than l values are present skip
        if (d.size() < minSize) { return false; }

        // Sum of the frequencies in distribution (=number of elements)
        final int total = entry.count;
        // Sum must stay smaller than this constant term
        final double C = total * (estimator.psi(total) - Math.log(l));
        double sum1 = 0d;

        final int[] buckets = d.getBuckets();
        for (int i = 0; i < buckets.length; i += 2) {
            if (buckets[i] != -1) { // bucket not empty
                final int frequency = buckets[i + 1];
                sum1 += frequency * estimator.psi(frequency);
                // If the sum grows over C, we can abort the loop earlier.
                if (C < sum1) { return false; }
            }
        }

        // If we reach this point, the loop did not return false.
        return true;
    }

    @Override
    public boolean isLocalRecodingSupported() {
        return true;
    }
    
    @Override
    public ElementData render() {
        ElementData result = new ElementData("Entropy l-diversity");
        result.addProperty("Attribute", attribute);
        result.addProperty("Threshold (l)", this.l);
        result.addProperty("Entropy estimator", this.estimator.toString());
        return result;
    }
    
    @Override
	public String toString() {
        return estimator.toString().toLowerCase() + "-entropy-" + l + "-diversity for attribute '" + attribute + "'";
	}

    /**
     * Custom de-serialization
     * 
     * If we de-serialize an older object where the entropy estimator
     * could not be chosen, set the estimator to the default: Shannon.
     * 
     * @param ois
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        
    	// Default de-serialization
    	ois.defaultReadObject();
    	
    	// Set default estimator if de-serializing an older object
        if (this.estimator == null) {
            this.estimator = EntropyEstimator.SHANNON;
        }
    }
}
