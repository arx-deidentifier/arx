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

import java.util.List;

import org.deidentifier.arx.framework.lattice.SolutionSpaceIntArray.IntArrayWrapper;

import cern.colt.list.LongArrayList;

/**
 * List of transformations
 * @author Fabian Prasser
 *
 * @param <T>
 */
public abstract class TransformationList<T> {

    /**
     * List for transformations managed by int arrays
     * @author Fabian Prasser
     */
    private static class TransformationListIntArray extends TransformationList<IntArrayWrapper> {
        
        /** The list*/
        private final List<IntArrayWrapper> list;

        /**
         * Creates a new instance
         * @param lattice
         * @param list
         */
        public TransformationListIntArray(List<IntArrayWrapper> list) {
            this.list = list;
        }

        @Override
        public void addAllOfFromTo(TransformationList<?> list, int from, int to) {
            for (int i = from; i <= to; i++) {
                this.list.add(((TransformationListIntArray)list).list.get(i));
            }
        }

        @Override
        public IntArrayWrapper getQuick(int i) {
            return list.get(i);
        }

        @Override
        public int size() {
            return list.size();
        }
    }

    /**
     * List for transformations managed by longs
     * @author Fabian Prasser
     */
    private static class TransformationListLong extends TransformationList<Long> {
        
        /** The list*/
        private final LongArrayList list;

        /**
         * Creates a new instance
         * @param list
         */
        public TransformationListLong(LongArrayList list) {
            this.list = list;
        }

        @Override
        public void addAllOfFromTo(TransformationList<?> list, int from, int to) {
            this.list.addAllOfFromTo(((TransformationListLong)list).list, from, to);
        }

        @Override
        public Long getQuick(int i) {
            return list.getQuick(i);
        }

        @Override
        public int size() {
            return list.size();
        }
    }
    /**
     * Creates a new list for transformations managed by int arrays
     * @param lattice
     * @return
     */
    public static TransformationList<IntArrayWrapper> create(List<IntArrayWrapper> result) {
        return new TransformationListIntArray(result);
    }

    /**
     * Creates a new list for transformations managed by longs
     * @param result
     * @return
     */
    public static TransformationList<Long> create(LongArrayList result) {
        return new TransformationListLong(result);
    }

    /**
     * Adds from to
     * @param successors
     * @param from
     * @param to
     */
    public abstract void addAllOfFromTo(TransformationList<?> successors, int from, int to);

    /**
     * Returns an element
     * @param i
     * @return
     */
    public abstract T getQuick(int i);

    /**
     * Returns the size
     * @return
     */
    public abstract int size();
}
