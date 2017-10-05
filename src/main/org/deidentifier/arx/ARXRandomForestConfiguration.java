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
package org.deidentifier.arx;

import java.io.Serializable;

/**
 * Configuration for Random Forest classifiers
 * @author Fabian Prasser
 */
public class ARXRandomForestConfiguration extends ARXClassificationConfiguration implements Serializable {

    /** SVUID */
    private static final long serialVersionUID = 7928077920858462047L;

    /**
     * Returns a new instance
     * @return
     */
    public static ARXRandomForestConfiguration create() {
        return new ARXRandomForestConfiguration();
    }

    /** Number of trees */
    private int     numberOfTrees = 10;

    /**
     * Constructor
     */
    private ARXRandomForestConfiguration(){
        // Empty by design
    }

    /**
     * @return the numberOfTrees
     */
    public int getNumberOfTrees() {
        return numberOfTrees;
    }

    /**
     * @param numberOfTrees the numberOfTrees to set
     */
    public ARXRandomForestConfiguration setNumberOfTrees(int numberOfTrees) {
        if (this.numberOfTrees != numberOfTrees) {
            setModified();
            this.numberOfTrees = numberOfTrees;
        }
        return this;
    }    
}
