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

import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.lattice.Transformation;

/**
 * The distinct l-diversity privacy criterion.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class DistinctLDiversity extends LDiversity{

    /** SVUID */
    private static final long serialVersionUID = -7973221140269608088L;
    
    /**
     * Creates a new instance of the distinct l-diversity privacy criterion as proposed in
     * Machanavajjhala A, Kifer D, Gehrke J.
     * l-diversity: Privacy beyond k-anonymity.
     * Transactions on Knowledge Discovery from Data (TKDD). 2007;1(1):3.
     *
     * @param attribute
     * @param l
     */
    public DistinctLDiversity(String attribute, int l){
        super(attribute, l, true, true);
    }

    @Override
    public DistinctLDiversity clone() {
        return new DistinctLDiversity(this.getAttribute(), (int)this.getL());
    }

	@Override
    public boolean isAnonymous(Transformation<?> node, HashGroupifyEntry entry) {
        return entry.distributions[index].size() >= minSize; // minSize=(int)l;
    }

    @Override
    public boolean isLocalRecodingSupported() {
        return true;
    }

    @Override
    public ElementData render() {
        ElementData result = new ElementData("Distinct l-diversity");
        result.addProperty("Attribute", attribute);
        result.addProperty("Threshold (l)", minSize);
        return result;
    }

    @Override
	public String toString() {
		return "distinct-"+minSize+"-diversity for attribute '"+attribute+"'";
	}
}
