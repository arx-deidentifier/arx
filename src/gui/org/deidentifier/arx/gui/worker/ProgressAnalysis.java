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
package org.deidentifier.arx.gui.worker;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.gui.resources.Resources;

/**
 * Simple progress analysis using moving averages
 * @author Fabian Prasser
 */
public class ProgressAnalysis {

    /** Window size */
    private static final int WINDOW_SIZE      = 10;

    /** Start time */
    private long             previousTime     = 0;

    /** Previous progress */
    private int              previousProgress = 0;

    /** Window */
    private List<Double>     window           = new ArrayList<>();
    
    /**
     * Calculates and renders the remaining time
     * @return
     */
    public String getTimeRemaining() {

        // Unknown
        if (this.window.isEmpty()) {
            return Resources.getMessage("Worker.5"); //$NON-NLS-1$ 
        }
        
        // Calculate
        double millisPerWorkUnit = 0d;
        for (double value : window) {
            millisPerWorkUnit += value;
        }
        millisPerWorkUnit /= (double)window.size();
        
        // Render
        return toString(Math.round(millisPerWorkUnit * (double)(100 - previousProgress)));
    }
    
    /**
     * Start
     */
    public void start() {
        this.previousTime = System.currentTimeMillis();
        this.previousProgress = 0;
    }
    
    /**
     * Report progress
     * @param progress
     */
    public void update(int progress) {
        if (progress > 100) {
            progress = 100;
        }
        if (progress < previousProgress) {
            previousProgress = progress;
        } else if (progress != previousProgress) {
            int workDone = progress - previousProgress;
            long currentTime = System.currentTimeMillis();
            long timeElapsed = currentTime - previousTime;
            this.previousTime = currentTime;
            double millisPerWorkUnit = (double)timeElapsed / (double)workDone;
            if (window.size() == WINDOW_SIZE) {
                window.remove(0);
            }
            window.add(millisPerWorkUnit);
            previousProgress = progress;
        }
    }   

    /**
     * Renders milliseconds into a string
     * @param millis
     * @return
     */
    private String toString(long millis) {
        
        // Calculate
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        // Sanity checks
        if (minutes > 59 || minutes < 0) {
            minutes = 0;
        }
        if (hours > 24 || hours < 0) {
            hours = 0;
        }
        
        // Less then a minute
        if (days == 0 && hours == 0 && minutes == 0) {
            return Resources.getMessage("Worker.5"); //$NON-NLS-1$ 
        }
        
        // Render
        StringBuffer result = new StringBuffer();
        
        if (days != 0) {
            result.append(days).append(" ").append(Resources.getMessage("Worker.2")); //$NON-NLS-1$
        }
        if (hours != 0) {
            if (result.length() != 0) {
                result.append(", ");
            }
            result.append(hours).append(" ").append(Resources.getMessage("Worker.3")); //$NON-NLS-1$ 
        }
        if (minutes != 0) {
            if (result.length() != 0) {
                result.append(", ");
            }
            result.append(minutes).append(" ").append(Resources.getMessage("Worker.4")); //$NON-NLS-1$ 
        }
        return result.toString(); 
    }
}