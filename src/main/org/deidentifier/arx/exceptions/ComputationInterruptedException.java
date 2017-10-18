/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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
package org.deidentifier.arx.exceptions;

/**
 * Internal class for interrupts.
 *
 * @author Fabian Prasser
 */
public class ComputationInterruptedException extends RuntimeException { // NO_UCD
    
    /** SVUID */
    private static final long serialVersionUID = 5339918851212367422L;

    /**
     * Constructor
     */
    public ComputationInterruptedException() {
        // Empty by design
    }
    /**
     * Constructor
     * @param message
     */
    public ComputationInterruptedException(String message) {
        super(message);
    }
    
    /** 
     * Constructor
     * @param message
     * @param cause
     */
    public ComputationInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }

    /** 
     * Constructor
     * @param cause
     */
    public ComputationInterruptedException(Throwable cause) {
        super(cause);
    }
}
