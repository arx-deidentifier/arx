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
 * This exception is raised, if the method that was called has left something in an
 * invalid state that may breach privacy. To ensure privacy, you must roll-back the operation that you performed.
 *
 * @author Fabian Prasser
 */
public class RollbackRequiredException extends Exception {
    
    /** SVUID */
    private static final long serialVersionUID = -7587463020191596936L;

    /**
     * Constructor
     */
    public RollbackRequiredException() {
        // Empty by design
    }
    /**
     * Constructor
     * @param message
     */
    public RollbackRequiredException(String message) {
        super(message);
    }
    
    /** 
     * Constructor
     * @param message
     * @param cause
     */
    public RollbackRequiredException(String message, Throwable cause) {
        super(message, cause);
    }

    /** 
     * Constructor
     * @param cause
     */
    public RollbackRequiredException(Throwable cause) {
        super(cause);
    }
}
