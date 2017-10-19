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
package org.deidentifier.arx.framework.check.history;

import org.deidentifier.arx.framework.lattice.Transformation;

/**
 * Data associated with a cache entry
 * @author Fabian Prasser
 *
 */
public class MRUCacheEntryMetadata {

    /** Transformation */
    public final int[] transformation;
    /** Level */
    public final int   level;
    /** Id */
    public final long  id;

    /**
     * Creates a new instance
     * @param transformation
     */
    public MRUCacheEntryMetadata(Transformation transformation) {
        this.transformation = transformation.getGeneralization().clone();
        this.level = transformation.getLevel();
        this.id = transformation.getIdentifier();
    }
}
