/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
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
package org.deidentifier.arx.risk.msu;

/**
 * A simple class encapsulating the pruning logic
 * @author Fabian Prasser
 */
public class SUDA2PruningStrategy {

    /** Upper bound from the first pruning strategy*/
    private final int upperBoundFromSupport; // Pruning-1
    /** Upper bound from the second pruning strategy*/
    private final int upperBoundFromRemainingItems; // Pruning-2
    /** Upper bound from the third pruning strategy*/
    private final int upperBoundFromBound; // Pruning-3
    
    /** Minimal bound*/
    private final int upperBound;
    
    /**
     * Creates an instance which will not result in pruning
     */
    public SUDA2PruningStrategy() {
        this.upperBoundFromSupport = Integer.MAX_VALUE;
        this.upperBoundFromRemainingItems = Integer.MAX_VALUE;
        this.upperBoundFromBound = Integer.MAX_VALUE;
        this.upperBound = Integer.MAX_VALUE;
    }

    /**
     * Creates a new instance
     * @param support
     * @param remainingItems
     * @param upperBound
     */
    SUDA2PruningStrategy(int upperBoundFromSupport,
                            int upperBoundFromRemainingItems,
                            int upperBoundFromBound) {
        this.upperBoundFromSupport = upperBoundFromSupport;
        this.upperBoundFromRemainingItems = upperBoundFromRemainingItems;
        this.upperBoundFromBound = upperBoundFromBound;
        this.upperBound = Math.min(upperBoundFromSupport, Math.min(upperBoundFromRemainingItems, upperBoundFromBound));
    }

    /**
     * Returns whether we can and should prune this path
     * @param depth
     * @return
     */
    public boolean canPrune(int depth) {
//        return false;
        return depth > upperBound;
    }

    /**
     * Returns the current upper bound
     * @return
     */
    public int getUpperBound() {
        return this.upperBound;
    }

    /**
     * @return the upperBoundFromBound
     */
    public int getUpperBoundFromBound() {
        return upperBoundFromBound;
    }
    
    /**
     * @return the upperBoundFromRemainingItems
     */
    public int getUpperBoundFromRemainingItems() {
        return upperBoundFromRemainingItems;
    }

    /**
     * @return the upperBoundFromSupport
     */
    public int getUpperBoundFromSupport() {
        return upperBoundFromSupport;
    }
}
