/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Handler for GIFs
 */
class ComponentStatusLabelGIFHandler implements Runnable {

    /** The status label */
    private final ComponentStatusLabel statusLabel;

    /** Field */
    private int                        imageNumber = 0;

    /** Field */
    private ImageLoader                loader      = null;

    /** Field */
    private boolean                    stop        = false;

    /**
     * Creates a new instance
     * @param loader
     * @param label
     */
    public ComponentStatusLabelGIFHandler(ComponentStatusLabel label, ImageLoader loader) {
        this.statusLabel = label;
        this.loader = loader;
        label.getDisplay().addListener(SWT.Dispose, new Listener() {
            public void handleEvent(Event arg0) {
                disposeCurrentImage();
            }
        });
    }
    
    @Override
    public void run() {
        int delayTime = loader.data[imageNumber].delayTime;
        if (!statusLabel.isDisposed()) {
            imageNumber = imageNumber == loader.data.length - 1 ? 0 : imageNumber + 1;
            disposeCurrentImage();
            ImageData nextFrameData = loader.data[imageNumber];
            statusLabel.updateImage(new Image(statusLabel.getDisplay(), nextFrameData));
            if (!stop) {
                statusLabel.getDisplay().timerExec(delayTime * 10, this);
            } else {
                disposeCurrentImage();
            }
        }
    } 
    
    /**
     * Stop
     */
    public void stop(){
        this.stop = true;
    }
    
    /**
     * Dispose the current image, if any
     */
    private void disposeCurrentImage() {
        if (statusLabel.getImage() != null && !statusLabel.getImage().isDisposed()) {
            statusLabel.getImage().dispose();
        }
    }
}