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

package org.deidentifier.arx.gui.view.impl.analyze;

/**
 * A generic interface for analyses that are performed asynchronously.
 *
 * @author Fabian Prasser
 */
public abstract class Analysis {

    /**
     * Called to stop the analysis.
     */
    public abstract void stop();
    
    /**
     * Implement the analysis here.
     *
     * @throws InterruptedException
     */
    public abstract void run() throws InterruptedException;
    
    /**
     * Perform finish.
     */
    public abstract void onFinish();
    
    /**
     * Perform error handling.
     */
    public abstract void onError();
    
    /**
     * Perform interrupt handling.
     */
    public abstract void onInterrupt();
}