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

import java.util.Set;

/**
 * Listener for MSU discoveries
 * 
 * @author Fabian Prasser
 */
public abstract class SUDA2Listener extends SUDA2Result {

    /**
     * A MSU has been discovered
     * 
     * @param row
     * @param size
     */
    public abstract void msuFound(int row, int size);

    @Override
    void registerMSU(Set<SUDA2Item> set) {
        throw new UnsupportedOperationException("");
    }

    @Override
    void registerMSU(SUDA2Item item, SUDA2ItemSet set) {
        SUDA2Item temp = item;
        for (int i = 0; i < set.size(); i++) {
            temp = temp.getProjection(set.get(i).getRows());
        }
        msuFound(temp.getRows().iterator().next().value, set.size() + 1);
    }

    @Override
    void registerMSU(SUDA2ItemSet set) {
        msuFound(set.get(0).getRows().iterator().next().value, set.size());
    }
}
