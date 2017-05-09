/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
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
package org.deidentifier.arx.r;

/**
 * A simple ring buffer for characters
 * 
 * @author Fabian Prasser
 */
public class RBuffer {

    /** The buffer*/
    private final char[] buffer;
    
    /** Start*/
    private int offset = 0;
    
    /** Length*/
    private int length = 0;
    
    /**
     * Creates a buffer of the given size
     * @param size
     */
    public RBuffer(int size) {
        this.buffer = new char[size];
    }
    
    /**
     * Appends the char
     * @param c
     */
    public void append(char c) {
        buffer[offset] = c;
        length = (length == buffer.length) ? length : length + 1;
        offset = (offset == buffer.length - 1) ? 0 : offset + 1;
    }
    
    /**
     * Appends all chars from the buffer
     * @param buffer
     */
    public void append(char[] buffer) {
        for (char c : buffer) {
            this.append(c);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (length < buffer.length) {
            builder.append(buffer, 0, length);
        } else {
            builder.append(buffer, offset, buffer.length - offset);
            builder.append(buffer, 0, offset);
        }
        return builder.toString();
    }
}
