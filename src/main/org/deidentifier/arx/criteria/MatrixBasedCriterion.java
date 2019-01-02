/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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

import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyArray;
import org.deidentifier.arx.framework.lattice.Transformation;

/**
 * An abstract base class for privacy models based on the actual dataset
 *
 * @author Fabian Prasser
 */
public abstract class MatrixBasedCriterion extends PrivacyCriterion {

    /** SVUID*/
    private static final long serialVersionUID = -1405854357531727963L;

    /**
     * Instantiates a new criterion.
     *
     * @param monotonicWithSuppression
     * @param monotonicWithGeneralization
     */
    public MatrixBasedCriterion(boolean monotonicWithSuppression,
                                boolean monotonicWithGeneralization) {
        super(monotonicWithSuppression, monotonicWithGeneralization);
    }
    
    /**
     * This method enforces the model on the given array. 
     * Risk thresholds can be enforced by suppressing classes.
     * Models may abort early, if the threshold is reached 
     * (<code>distribution.getNumOfSuppressedTuples() > numMaxSuppressedOutliers</code>).
     * 
     * @param array
     * @param numMaxSuppressedOutliers
     * @return Whether the array has been modified
     */
    public abstract boolean enforce(HashGroupifyArray array, 
                                    int numMaxSuppressedOutliers);

    /**
     * Not supported by this type of criterion
     * @param entry
     *
     * @return
     */
    public boolean isAnonymous(Transformation node, HashGroupifyEntry entry) {
        throw new UnsupportedOperationException("Not implemented");
    }
    
    @Override
    public boolean isMatrixBased() {
        return true;
    }
}