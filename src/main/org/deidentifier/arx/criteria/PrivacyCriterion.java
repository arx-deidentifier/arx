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

import java.io.Serializable;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.lattice.Transformation;

/**
 * An abstract base class for privacy criteria.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class PrivacyCriterion implements Serializable{

    /**  SVUID */
    private static final long serialVersionUID = -8460571120677880409L;
    
    /** Is the criterion monotonic with generalization and suppression. */
    private final boolean monotonic;
    

    /** Is the criterion monotonic with generalization. */
    private final Boolean monotonicWithGeneralization;
    
    /**
     * Instantiates a new criterion.
     *
     * @param monotonicWithSuppression
     * @param monotonicWithGeneralization
     */
    public PrivacyCriterion(boolean monotonicWithSuppression,
                            boolean monotonicWithGeneralization){
        this.monotonic = monotonicWithSuppression;
        this.monotonicWithGeneralization = monotonicWithGeneralization;
    }
    
    /**
     * Clone
     */
    public abstract PrivacyCriterion clone();

    /**
     * Clone for local recoding
     */
    public PrivacyCriterion clone(DataSubset subset) {
        if (!isLocalRecodingSupported()) {
            throw new UnsupportedOperationException("Local recoding is not supported by this model");
        } else if (this.isSubsetAvailable()) {
            throw new UnsupportedOperationException("This model must override clone(subset)");
        }
        return this.clone();
    }
    
    /**
     * If a privacy model uses a data subset, it must overwrite this method
     * @return
     */
    public DataSubset getDataSubset() {
    return null;
    }

    /**
     * If a privacy model provides a prosecutor risk threshold, it should override this method to enable optimizations
     * @return
     */
    public int getMinimalClassSize() {
    return 0;
    }
    
    /**
     * Returns the associated population model, <code>null</code> if there is none.
     * @return the populationModel
     */
    public ARXPopulationModel getPopulationModel() {
        return null;
    }

    /**
     * Returns the criterion's requirements.
     *
     * @return
     */
    public abstract int getRequirements();
    
    /**
     * Return journalist risk threshold, 1 if there is none
     * @return
     */
    public double getRiskThresholdJournalist() {
        return 1d;
    }
    
    /**
     * Return marketer risk threshold, 1 if there is none
     * @return
     */
    public double getRiskThresholdMarketer() {
        return 1d;
    }
    
    /**
     * Return prosecutor risk threshold, 1 if there is none
     * @return
     */
    public double getRiskThresholdProsecutor() {
        return 1d;
    }
    
    /**
     * Returns a research subset, <code>null</code> if no subset is available
     * @return
     */
    public DataSubset getSubset() {
        return null;
    }
    
    /**
     * Override this to initialize the criterion.
     *
     * @param manager
     * @param config TODO
     */
    public void initialize(DataManager manager, ARXConfiguration config){
        // Empty by design
    }
    
    /**
     * Implement this, to enforce the criterion.
     * @param transformation
     * @param entry
     *
     * @return
     */
    public abstract boolean isAnonymous(Transformation transformation, HashGroupifyEntry entry);

    /**
     * Returns whether the criterion supports local recoding.
     * @return
     */
    public abstract boolean isLocalRecodingSupported();
    
    /**
     * If a privacy model provides a prosecutor risk threshold, it should override this method to enable optimizations
     * @return
     */
    public boolean isMinimalClassSizeAvailable() {
        return false;
    }

    /**
     * Returns whether the criterion is monotonic with generalization.
     * @return
     */
    public boolean isMonotonicWithGeneralization() {
        // Default
        if (this.monotonicWithGeneralization == null) {
            return true;
        } else {
            return this.monotonicWithGeneralization;
        }
    }

    /**
     * Returns whether the criterion is monotonic with tuple suppression.
     *
     * @return
     */
    public boolean isMonotonicWithSuppression() {
        return this.monotonic;
    }

    /**
     * Returns whether the model supports reliable data anonymization.
     * Must be called after the model has been initialized.
     * The default is <code>false</code>.
     * @return
     */
    public boolean isReliableAnonymizationSupported() {
        return false;
    }
    

    /**
     * Overwrite this, to reliably enforce the criterion.
     * @param transformation 
     * @param entry
     *
     * @return
     */
    public boolean isReliablyAnonymous(Transformation transformation, HashGroupifyEntry entry) {
        return isAnonymous(transformation, entry);
    }

    /**
     * Is this criterion based on the overall sample
     * @return
     */
    public boolean isSampleBased() {
        return false;
    }

    /**
     * If a privacy model uses a data subset, it must overwrite this method
     * @return
     */
    public boolean isSubsetAvailable() {
        return false;
    }

    /**
     * Renders the privacy model
     * @return
     */
    public abstract ElementData render();
    
    /**
     * Returns a string representation.
     *
     * @return
     */
    public abstract String toString();
}
