package org.deidentifier.arx.distribution.util;

import java.util.Iterator;
import java.util.List;

public class IteratorIterator<T> implements Iterator<T> {
    
    private final List<Iterator<T>> iterators;
    private int               currentIterator;
    
    public IteratorIterator(List<Iterator<T>> _iterators) {
        iterators = _iterators;
        currentIterator = 0;
    }
    
    public boolean hasNext() {
        while (currentIterator < iterators.size() && !iterators.get(currentIterator).hasNext())
            currentIterator++;
        
        return currentIterator < iterators.size();
    }
    
    public T next() {
        while (currentIterator < iterators.size() && !iterators.get(currentIterator).hasNext())
            currentIterator++;
        
        return iterators.get(currentIterator).next();
    }
    
    public void remove() {
        throw new UnsupportedOperationException();
    }
}