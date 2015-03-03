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

import java.util.HashMap;
import java.util.Iterator;

/**
 * The Class MRUCache.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 * @param <T> the generic type
 */
public class MRUCache<T> {

    /**
     * The Class MRULinkedListIterator.
     * 
     * @author Fabian Prasser
 * @author Florian Kohlmayer
     */
    public class MRULinkedListIterator implements Iterator<T> {

        /** The currposition. */
        private MRUCacheEntry<T>  currposition;

        /** The list. */
        private final MRUCache<T> list;

        /** The prevposition. */
        private MRUCacheEntry<T>  prevposition;

        /**
         * Instantiates a new mRU linked list iterator.
         * 
         * @param l
         *            the l
         */
        public MRULinkedListIterator(final MRUCache<T> l) {
            this.currposition = l.first;
            this.prevposition = l.first;
            this.list = l;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return this.currposition != null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Iterator#next()
         */
        @Override
        public T next() {
            final T obj = this.currposition.data;
            this.prevposition = this.currposition;
            this.currposition = this.currposition.next;
            return obj;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Iterator#remove()
         */
        @Override
        public void remove() {
            this.list.remove(this.prevposition);
            this.list.elementToEntry.remove(this.prevposition.data);
        }
    }

    /** The element to entry. */
    private HashMap<T, MRUCacheEntry<T>> elementToEntry = null;

    /** The first. */
    private MRUCacheEntry<T>             first          = null;

    /** The last. */
    private MRUCacheEntry<T>             last           = null;

    /**
     * Instantiates a new mRU cache.
     * 
     * @param size
     *            the size
     */
    public MRUCache(final int size) {
        this.elementToEntry = new HashMap<T, MRUCacheEntry<T>>(size);
    }

    /**
     * Append.
     * 
     * @param entry
     *            the entry
     */
    private void append(final MRUCacheEntry<T> entry) {
        if (this.first == null) {
            this.first = entry;
            this.last = entry;
            entry.prev = null;
            entry.next = null;
        } else {
            this.last.next = entry;
            entry.prev = this.last;
            entry.next = null;
            this.last = entry;
        }
    }

    /**
     * Append.
     * 
     * @param node
     *            the node
     */
    public void append(final T node) {
        if (this.elementToEntry.containsKey(node)) {
            this.touch(node);
            return;
        }
        final MRUCacheEntry<T> entry = new MRUCacheEntry<T>(node);
        this.append(entry);
        this.elementToEntry.put(node, entry);
    }

    /**
     * Clear.
     */
    public void clear() {
        elementToEntry.clear();
        first = null;
        last = null;
    }

    /**
     * Gets the first.
     * 
     * @return the first
     */
    public MRUCacheEntry<T> getHead() {
        return first;
    }

    /**
     * Iterator.
     * 
     * @return the iterator
     */
    public Iterator<T> iterator() {
        return new MRULinkedListIterator(this);
    }

    /**
     * Removes the.
     * 
     * @param entry
     *            the entry
     */
    private void remove(final MRUCacheEntry<T> entry) {
        if (entry == this.first) {
            this.first = entry.next;
            if (this.first != null) {
                this.first.prev = null;
            }
        } else if (entry == this.last) {
            this.last = entry.prev;
            this.last.next = null;
        } else {
            if (entry.prev != null) {
                entry.prev.next = entry.next;
            }
            if (entry.next != null) {
                entry.next.prev = entry.prev;
            }
        }
    }

    /**
     * Removes the head.
     * 
     * @return the t
     */
    public T removeHead() {
        final T obj = this.first.data;
        this.remove(this.first);
        this.elementToEntry.remove(obj);
        return obj;
    }

    /**
     * Size.
     * 
     * @return the int
     */
    public int size() {
        return this.elementToEntry.size();
    }

    /**
     * Touch.
     * 
     * @param node
     *            the node
     */
    public void touch(final T node) {
        final MRUCacheEntry<T> entry = this.elementToEntry.get(node);
        if (entry == this.last) { return; }
        this.remove(entry);
        this.append(entry);
    }
}
