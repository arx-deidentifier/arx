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

package org.deidentifier.arx.metric;

/**
 * Information loss with a potential lower bound.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public class InformationLossWithBound<T extends InformationLoss<?>> {

    /** Lower bound, if any. */
    private final T lowerBound;
    
    /** Actual information loss. */
    private final T informationLoss;
    
    /**
     * Creates a new instance without a lower bound.
     *
     * @param informationLoss
     */
    public InformationLossWithBound(T informationLoss) {
        this.lowerBound = null;
        this.informationLoss = informationLoss;
    }

    /**
     * Creates a new instance.
     *
     * @param informationLoss
     * @param lowerBound
     */
    public InformationLossWithBound(T informationLoss, T lowerBound) {
        this.lowerBound = lowerBound;
        this.informationLoss = informationLoss;
    }
    
    /**
     * @return the informationLoss
     */
    public T getInformationLoss() {
        return informationLoss;
    }

    /**
     * @return the lowerBound
     */
    public T getLowerBound() {
        return lowerBound;
    }

    /**
     * Is a lower bound provided.
     *
     * @return
     */
    public boolean hasLowerBound(){
        return this.lowerBound != null;
    }
}
