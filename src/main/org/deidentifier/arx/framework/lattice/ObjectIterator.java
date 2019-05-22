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
package org.deidentifier.arx.framework.lattice;

import java.util.Iterator;

import org.deidentifier.arx.framework.lattice.SolutionSpaceIntArray.IntArrayWrapper;

import de.linearbits.jhpl.JHPLIterator.LongIterator;

/**
 * Wrapper for iterator
 * @author Fabian Prasser
 *
 * @param <T>
 */
public abstract class ObjectIterator<T> {

    /**
     * Iterator for int arrays
     * @author Fabian Prasser
     */
    public static class ObjectIteratorIntArray extends ObjectIterator<IntArrayWrapper> {

        /** Iter*/
        private final Iterator<int[]> iter;
        /** Lattice*/
        private final SolutionSpace<?> lattice;
        
        /**
         * Creates a new instance
         * @param lattice
         * @param iter
         */
        private ObjectIteratorIntArray(SolutionSpace<?> lattice, Iterator<int[]> iter) {
            this.iter = iter;
            this.lattice = lattice;
        }


        /**
         * @return
         * @see de.linearbits.jhpl.JHPLIterator.LongIterator#hasNext()
         */
        public boolean hasNext() {
            return iter.hasNext();
        }

        /**
         * @return
         * @see de.linearbits.jhpl.JHPLIterator.LongIterator#next()
         */
        public IntArrayWrapper next() {
            return new IntArrayWrapper(lattice.fromJHPL(iter.next()));
        }
    }

    /**
     * Iterator for longs
     * @author Fabian Prasser
     */
    public static class ObjectIteratorLong extends ObjectIterator<Long> {

        /** Iter*/
        private final LongIterator iter;
        
        /**
         * Creates a new instance
         * @param iter
         */
        private ObjectIteratorLong(LongIterator iter) {
            this.iter = iter;
        }

        /**
         * @return
         * @see de.linearbits.jhpl.JHPLIterator.LongIterator#hasNext()
         */
        public boolean hasNext() {
            return iter.hasNext();
        }

        /**
         * @return
         * @see de.linearbits.jhpl.JHPLIterator.LongIterator#next()
         */
        public Long next() {
            return iter.next();
        }
    }
    /**
     * Creates a new instance for longs
     * @param iter
     * @return
     */
    public static ObjectIterator<Long> create(LongIterator iter) {
        return new ObjectIteratorLong(iter);
    }

    /**
     * Creates a new instance for int arrays
     * @param lattice
     * @param iter
     * @return
     */
    public static ObjectIterator<IntArrayWrapper> create(SolutionSpace<?> lattice, Iterator<int[]> iter) {
        return new ObjectIteratorIntArray(lattice, iter);
    }
    
    /**
     * Is there a next element?
     */
    public abstract boolean hasNext();

    /**
     * Returns the next element
     */
    public abstract T next();
}
