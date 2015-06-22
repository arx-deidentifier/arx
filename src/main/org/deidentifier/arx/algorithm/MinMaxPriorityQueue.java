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

package org.deidentifier.arx.algorithm;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Min-max-queue without duplicates
 * 
 * @author Fabian Prasser
 * @author Raffael Bild
 * 
 * @param <T>
 */
public class MinMaxPriorityQueue<T> extends PriorityQueue<T> {

    /** SVUID */
    private static final long      serialVersionUID = -3114256836247244397L;
    /** Queue*/
    private final PriorityQueue<T> queue;
    /** Queue with inverse order*/
    private final PriorityQueue<T> inverseQueue;
    /** Set*/
    private final Set<T>           elements;

    /**
     * Constructor
     * @param initialSize
     * @param comparator
     */
    public MinMaxPriorityQueue(int initialSize, Comparator<T> comparator) {
        this.queue = new PriorityQueue<T>(initialSize, comparator);
        this.inverseQueue = new PriorityQueue<T>(initialSize, getInverseComparator(comparator));
        this.elements = new HashSet<T>(initialSize);
    }

    /**
     * Unsupported operation
     */
    public boolean add(T e) {
        if (this.elements.contains(e)) { return false; }
        this.elements.add(e);
        this.inverseQueue.add(e);
        return this.queue.add(e);
    }

    /**
     * Unsupported operation
     */
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported operation
     */
    public void clear() {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported operation
     */
    public Comparator<? super T> comparator() {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported operation
     */
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported operation
     */
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported operation
     */
    public T element() {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported operation
     */
    public boolean equals(Object obj) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported operation
     */
    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported operation
     */
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported operation
     */
    public Iterator<T> iterator() {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported operation
     */
    public boolean offer(T e) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported operation
     */
    public T peek() {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported operation
     */
    public T poll() {
        T t = this.queue.poll();
        if (t != null) {
            this.elements.remove(t);
            this.inverseQueue.remove(t);
        }
        return t;
    }

    /**
     * Unsupported operation
     */
    public T remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes the object from both queue and the inverseQueue
     */
    public boolean remove(Object o) {
        this.elements.remove(o);
        return queue.remove(o) && inverseQueue.remove(0);
    }

    /**
     * Unsupported operation
     */
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported operation
     */
    public T removeTail() {
        T t = this.inverseQueue.poll();
        if (t != null) {
            this.queue.remove(t);
            this.elements.remove(t);
        }
        return t;
    }

    /**
     * Unsupported operation
     */
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported operation
     */
    public int size() {
        return queue.size();
    }

    /**
     * Unsupported operation
     */
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported operation
     */
    public <U> U[] toArray(U[] a) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported operation
     */
    public String toString() {
        throw new UnsupportedOperationException();
    }

    private Comparator<? super T>
            getInverseComparator(final Comparator<T> comparator) {
        return new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return -comparator.compare(o1, o2);
            }
        };
    }
}
