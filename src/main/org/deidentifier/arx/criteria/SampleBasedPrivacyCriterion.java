/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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

import org.deidentifier.arx.framework.check.groupify.HashGroupifyDistribution;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;

/**
 * An abstract base class for sample-based privacy criteria.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class SampleBasedPrivacyCriterion extends PrivacyCriterion {

    /** SVUID*/
    private static final long serialVersionUID = 5687067920181297803L;

    /**
     * Instantiates a new criterion.
     *
     * @param monotonic
     */
    public SampleBasedPrivacyCriterion(boolean monotonic){
        super(monotonic);
    }
    
    /**
     * Not supported by this type of criterion
     *
     * @param entry
     * @return
     */
    public boolean isAnonymous(HashGroupifyEntry entry) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isSampleBased() {
        return true;
    }
    
    /**
     * This method enforces the criterion on the current hash table. It must return the total number of tuples
     * that have been suppressed. It is assumed that all sample-based criteria can be fulfilled in any case.
     * Criteria may abort early, if the threshold is reached <code>(numCurrentlySuppressedOutliers > numMaxSuppressedOutliers)</code>.
     * Implementations must return the updated value of <code>numCurrentlySuppressedOutliers</code>.
     * 
     * @param distribution
     * @param numCurrentlySuppressedOutliers
     * @param numMaxSuppressedOutliers
     * @return numCurrentlySuppressedOutliers
     */
    public abstract int enforce(HashGroupifyDistribution distribution, 
                                int numCurrentlySuppressedOutliers, 
                                int numMaxSuppressedOutliers);
}