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
package org.deidentifier.arx.gui.view.impl.common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Handler for GIFs
 */
class ComponentStatusLabelGIFHandler implements Runnable {

    /** The status label*/
    private final ComponentStatusLabel statusLabel;

    /**  Field */
    private int         imageNumber = 0;
    
    /**  Field */
    private ImageLoader loader      = null;
    
    /**  Field */
    private boolean                    stop        = false;
    
    /** Field */
    private Display                    display;

    /**
     * Creates a new instance
     * @param loader
     * @param componentStatusLabel TODO
     */
    public ComponentStatusLabelGIFHandler(ComponentStatusLabel componentStatusLabel, ImageLoader loader, Display display) {
        statusLabel = componentStatusLabel;
        this.loader = loader;
        this.display = display;
    }
    
    @Override
    public void run() {
        int delayTime = loader.data[imageNumber].delayTime;
        if (!statusLabel.isDisposed()) {
            imageNumber = imageNumber == loader.data.length - 1 ? 0 : imageNumber + 1;
            if (!statusLabel.image.isDisposed()) statusLabel.image.dispose();
            ImageData nextFrameData = loader.data[imageNumber];
            statusLabel.image = new Image(statusLabel.getDisplay(), nextFrameData);
            display.addListener(SWT.Dispose, new Listener() {
                public void handleEvent(Event arg0) {
                    if (statusLabel.image != null && !statusLabel.image.isDisposed()) {
                        statusLabel.image.dispose();
                    }
                }
            });
            statusLabel.redraw();
            if (!stop) {
                statusLabel.getDisplay().timerExec(delayTime * 10, this);
            }
        }
    } 
    
    /**
     * Stop
     */
    public void stop(){
        this.stop = true;
    }
}