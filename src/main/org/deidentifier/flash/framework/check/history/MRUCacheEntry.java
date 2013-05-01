/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.flash.framework.check.history;

/**
 * The Class MRUCacheEntry.
 * 
 * @param <T>
 *            the generic type
 * @author Prasser, Kohlmayer
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
