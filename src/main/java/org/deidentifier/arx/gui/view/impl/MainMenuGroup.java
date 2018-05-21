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

import java.util.List;

import org.deidentifier.arx.gui.Controller;

/**
 * This abstract class implements a menu group
 * @author Fabian Prasser
 *
 */
public abstract class MainMenuGroup extends MainMenuItem {

    /** All children*/
    private final List<MainMenuItem> items;

    /**
     * Creates a new instance
     * @param label
     * @param items
     */
    public MainMenuGroup(String label, List<MainMenuItem> items) {
        super(label, null, true);
        this.items = items;
    }
    
    @Override
    public void action(Controller controller) {
        // Empty by design
    }
    
    /**
     * Returns all children
     * @return
     */
    public List<MainMenuItem> getItems() {
        return items;
    }
}
