/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Florian Kohlmayer, Fabian Prasser
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
package org.deidentifier.arx.aggregates.classification;

/**
 * A classification result
 * @author Fabian Prasser
 */
public interface ClassificationResult {

    /**
     * Returns the confidence of the result
     * @return
     */
    public double confidence();

    /**
     * Returns the confidences for all class values
     * @return
     */
    public double[] confidences();
    
    /**
     * Returns whether the result is correct
     * @param clazz
     * @return
     */
    public boolean correct(String clazz);
    
    /**
     * Returns the error
     * @param clazz
     * @return
     */
    public double error(String clazz);

    /**
     * Returns the index of the predicted value
     * @return
     */
    public int index();
}
