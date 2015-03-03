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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deidentifier.arx.gui.view.def.IComponent;
import org.eclipse.swt.graphics.Image;

/**
 * A basic title bar, which offers some buttons.
 *
 * @author Fabian Prasser
 */
public class ComponentTitledFolderButton implements IComponent{
    
    /**  TODO */
    private List<String> titles = new ArrayList<String>();
    
    /**  TODO */
    private Map<String, Image> images = new HashMap<String, Image>();
    
    /**  TODO */
    private Map<String, Boolean> toggle = new HashMap<String, Boolean>();
    
    /**  TODO */
    private Map<String, Runnable> runnables = new HashMap<String, Runnable>();
    
    /**  TODO */
    private String id;
    
    /**
     * Creates a new instance.
     *
     * @param id
     */
    public ComponentTitledFolderButton(String id) {    
        this.id = id;
    }

    /**
     * Adds a button.
     *
     * @param title
     * @param image
     * @param toggle
     * @param runnable
     */
    public void add(String title, Image image, boolean toggle, Runnable runnable) {
        this.titles.add(title);
        this.toggle.put(title, toggle);
        this.images.put(title, image);
        this.runnables.put(title, runnable);
    }

    /**
     * Adds a new button.
     *
     * @param title
     * @param image
     * @param runnable
     */
    public void add(String title, Image image, Runnable runnable){
        add(title, image, false, runnable);
    }

    /**
     * Returns the id.
     *
     * @return
     */
    public String getId(){
        return id;
    }

    /**
     * Returns the image for the given button.
     *
     * @param title
     * @return
     */
    public Image getImage(String title) {
        return images.get(title);
    }

    /**
     * Returns the runnable for the given button.
     *
     * @param title
     * @return
     */
    public Runnable getRunnable(String title) {
        return runnables.get(title);
    }
    
    /**
     * Returns the titles of all buttons.
     *
     * @return
     */
    public List<String> getTitles() {
        return titles;
    }

    /**
     * Returns whether the given button is a toggle button.
     *
     * @param title
     * @return
     */
    public Boolean isToggle(String title) {
        return toggle.get(title);
    }
}
