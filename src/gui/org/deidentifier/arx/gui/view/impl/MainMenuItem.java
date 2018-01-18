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

package org.deidentifier.arx.gui.view.impl;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.eclipse.swt.graphics.Image;

/**
 * This abstract class implements a generic menu item
 * 
 * @author Fabian Prasser
 */
public abstract class MainMenuItem {

    /** The item's label*/
    private final String  label;
    /** Image*/
    private final Image   image;
    /** Is this item also a button*/
    private final boolean isButton;
    
    /**
     * Creates a new instance
     * @param label
     * @param image
     * @param isButton
     */
    public MainMenuItem(String label, Image image, boolean isButton) {
        this.label = label;
        this.image = image;
        this.isButton = isButton;
    }

    /**
     * Override this to perform the action
     */
    public abstract void action(Controller controller);
    
    /**
     * @return the image
     */
    public Image getImage() {
        return image;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return the isButton
     */
    public boolean isButton() {
        return isButton;
    }

    /**
     * Override this to return whether the item is enabled 
     * @param model
     * @return
     */
    public abstract boolean isEnabled(Model model);
}
