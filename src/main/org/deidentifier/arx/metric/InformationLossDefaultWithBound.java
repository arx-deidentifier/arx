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
 */
public class InformationLossDefaultWithBound extends InformationLossWithBound<InformationLossDefault> {

    /**
     * Creates a new instance without a lower bound.
     *
     * @param informationLoss
     */
    public InformationLossDefaultWithBound(double informationLoss) {
        super(new InformationLossDefault(informationLoss));
    }

    /**
     * Creates a new instance.
     *
     * @param informationLoss
     * @param lowerBound
     */
    public InformationLossDefaultWithBound(double informationLoss,
                                       double lowerBound) {
        super(new InformationLossDefault(informationLoss), new InformationLossDefault(lowerBound));
    }
}
