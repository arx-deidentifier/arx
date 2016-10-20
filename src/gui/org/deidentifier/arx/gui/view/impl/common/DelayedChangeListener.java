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
package org.deidentifier.arx.gui.view.impl.common;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;

/**
 * A listener that acts as a selection listener and a modify listener which defers change
 * events for a given amount of time
 * 
 * @author Fabian Prasser
 */
public abstract class DelayedChangeListener implements SelectionListener, ModifyListener {

    /** Tick in milliseconds */
    private static final int TICK  = 100;
    /** Flag */
    private boolean          event = false;
    /** Time */
    private long             time;
    /** Delay in milliseconds */
    private final long       delay;

    /**
     * Delay in milliseconds
     * @param delay
     */
    public DelayedChangeListener(long delay) {
        this.delay = delay;
        
        // Create repeating task
        final Display display = Display.getCurrent();
        display.timerExec(TICK, new Runnable() {
            @Override
            public void run() {
                if (event && System.currentTimeMillis() > time) {
                    delayedEvent();
                    event = false;
                }
                display.timerExec(TICK, this);
            }
        });
    }
    
    /**
     * Implement this
     * @param arg0
     */
    public abstract void delayedEvent();

    @Override
    public void modifyText(ModifyEvent arg0) {
        this.event = true;
        this.time = System.currentTimeMillis() + delay;
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent arg0) {
        this.event = true;
        this.time = System.currentTimeMillis() + delay;
    }

    @Override
    public void widgetSelected(SelectionEvent arg0) {
        this.event = true;
        this.time = System.currentTimeMillis() + delay;
    }
}
