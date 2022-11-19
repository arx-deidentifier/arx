/*
 * ARX Data Anonymization Tool
 * Copyright 2012 - 2022 Fabian Prasser and contributors
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
package org.deidentifier.arx.distributed;

import java.util.Iterator;
import java.util.List;

/**
 * Iterator over iterators
 * @author Fabian Prasser
 *
 * @param <T>
 */
public class CombinedIterator<T> implements Iterator<T> {
    
    /** Iterators*/
    private final List<Iterator<T>> iterators;
    /** Current*/
    private int               currentIterator;
    
    /**
     * Creates a new instance
     * @param _iterators
     */
    public CombinedIterator(List<Iterator<T>> _iterators) {
        iterators = _iterators;
        currentIterator = 0;
    }
    
    @Override
    public boolean hasNext() {
        while (currentIterator < iterators.size() && !iterators.get(currentIterator).hasNext())
            currentIterator++;
        return currentIterator < iterators.size();
    }
    
    @Override
    public T next() {
        while (currentIterator < iterators.size() && !iterators.get(currentIterator).hasNext())
            currentIterator++;
        
        return iterators.get(currentIterator).next();
    }
    
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}