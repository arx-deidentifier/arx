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
import org.eclipse.swt.widgets.Display;

/**
 * A listener for the R process. If a display is present, all notification 
 * events will come from the SWT event dispatch thread.
 * 
 * @author Fabian Prasser
 */
public abstract class RListener {

    /** Delay in milliseconds */
    private final int     delay;

    /** SWT Display, if any */
    private final Display display;

    /** Should an event be fired */
    private boolean       fire = false;

    /** Timestamp of the last event */
    private long          time = 0;

    /**
     * Creates a new instance
     * 
     * @param ticksPerSecond Maximal number of events per second
     */
    public RListener(int ticksPerSecond) {
        this(ticksPerSecond, null);
    }
        
    /**
     * Creates a new instance
     * 
     * @param ticksPerSecond Maximal number of events per second
     * @param display Display
     */
    public RListener(int ticksPerSecond, Display display) {
        
        // Calculate delay
        this.display = display;
        this.delay = (int)Math.round(1000d / (double)ticksPerSecond);
        
        // Create repeating task
        if (display != null) {
            display.asyncExec(new Runnable() {
                @Override
                public void run() {
                    repeat(delay, new Runnable() {
                        @Override
                        public void run() {
                            if (fire && System.currentTimeMillis() > time) {
                                bufferUpdated();
                                fire = false;
                            }
                        }
                    });
                }
            });
        } else {
            repeat(delay, new Runnable() {
                @Override
                public void run() {
                    if (fire && System.currentTimeMillis() > time) {
                        bufferUpdated();
                        fire = false;
                    }
                }
            });
        }
    }

    /**
     * Repeatedly executes the runnable
     * @param delay
     * @param runnable
     */
    private void repeat(final int delay, final Runnable runnable) {

        if (display != null) {
            display.timerExec(delay, new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                    display.timerExec(delay, this);
                }
            });
        } else {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            runnable.run();
                            Thread.sleep(delay);
                        }
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }
            });
            t.setDaemon(true);
            t.start();
        }
    }

    /** 
     * Implement this to get notified when the buffer is updated
     */
    public abstract void bufferUpdated();

    /**
     * Implement this to get notified when the R process dies
     */
    public abstract void closed();

    /**
     * Internal method to fire an event
     */
    void fireBufferUpdatedEvent() {
        this.fire = true;
        this.time = System.currentTimeMillis() + delay;
    }

    /**
     * Internal method to fire an event
     */
    void fireClosedEvent() {
        final Display display = Display.getCurrent();
        if (display != null) {
            display.asyncExec(new Runnable() {
                @Override
                public void run() {
                    closed();
                }
            });
        } else {
            closed();
        }
    }
}