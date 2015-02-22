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

package org.deidentifier.arx.risk;

/**
 * Class for re-identification risks
 * @author Fabian Prasser
 */
public class RiskModelSampleBasedReidentificationRisk extends RiskModelSampleBased{

    /**
     * Creates a new instance
     * @param classes
     */
    public RiskModelSampleBasedReidentificationRisk(RiskModelEquivalenceClasses classes) {
        super(classes);
    }

    /**
     * Returns the average re-identification risk
     * @return
     */
    public double getAverageRisk() {
        return 1.0d / getClasses().getAvgClassSize();
    }
    
    /**
     * Returns the fraction of tuples affected by the highest re-identification risk
     * @return
     */
    public double getFractionOfTuplesAffectedByHighestRisk(){
        return getNumTuplesAffectedByHighestRisk() / getClasses().getNumTuples();
    }

    /**
     * Returns the fraction of tuples affected by the lowest re-identification risk
     * @return
     */
    public double getFractionOfTuplesAffectedByLowestRisk(){
        return getNumTuplesAffectedByLowestRisk() / getClasses().getNumTuples();
    }

    /**
     * Returns the highest re-identification risk of any tuple in the data set
     * @return
     */
    public double getHighestRisk(){
        int[] classes = getClasses().getEquivalenceClasses();
        return 1d / (double)classes[0];
    }
    
    /**
     * Returns the lowest re-identification risk of any tuple in the data set
     * @return
     */
    public double getLowestRisk(){
        int[] classes = getClasses().getEquivalenceClasses();
        int index = classes.length - 2;
        return 1d / (double)classes[index];
    }
    
    /**
     * Returns the number of tuples affected by the highest re-identification risk
     * @return
     */
    public double getNumTuplesAffectedByHighestRisk(){
        int[] classes = getClasses().getEquivalenceClasses();
        return (double)classes[0] * (double)classes[1];
    }
    
    /**
     * Returns the number of tuples affected by the lowest re-identification risk
     * @return
     */
    public double getNumTuplesAffectedByLowestRisk(){
        int[] classes = getClasses().getEquivalenceClasses();
        int index = classes.length - 2;
        return (double)classes[index] * (double)classes[index+1];
    }
}
