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

package org.deidentifier.arx.framework.check.history;

/**
 * The Class MRUCacheEntry.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 * @param <T> the generic type
 */
public class MRUCacheEntry<T> {

    /** The data. */
    public final T          data;

    /** The next. */
    public MRUCacheEntry<T> next;

    /** The prev. */
    public MRUCacheEntry<T> prev;

    /**
     * Instantiates a new mRU cache entry.
     * 
     * @param node
     *            the node
     */
    public MRUCacheEntry(final T node) {
        this.data = node;
    }
}
