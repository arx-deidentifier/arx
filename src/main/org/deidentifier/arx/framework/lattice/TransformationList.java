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

import cern.colt.list.LongArrayList;

/**
 * List of transformations
 * @author Fabian Prasser
 *
 * @param <T>
 */
public abstract class TransformationList<T> {

    /**
     * Creates a new list for transformations managed by longs
     * @param result
     * @return
     */
    public static TransformationList<Long> create(LongArrayList result) {
        return new TransformationListLong(result);
    }

    /**
     * Returns the size
     * @return
     */
    public abstract int size();

    /**
     * Returns an element
     * @param i
     * @return
     */
    public abstract long getQuick(int i);

    /**
     * Adds from to
     * @param successors
     * @param from
     * @param to
     */
    public abstract void addAllOfFromTo(TransformationList<?> successors, int from, int to);

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
        public int size() {
            return list.size();
        }

        @Override
        public long getQuick(int i) {
            return list.getQuick(i);
        }

        @Override
        public void addAllOfFromTo(TransformationList<?> list, int from, int to) {
            this.list.addAllOfFromTo(((TransformationListLong)list).list, from, to);
        }
        
    }
}
