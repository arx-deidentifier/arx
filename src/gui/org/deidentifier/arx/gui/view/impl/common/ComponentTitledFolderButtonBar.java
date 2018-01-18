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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deidentifier.arx.gui.view.def.IComponent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * A basic title bar, which offers some buttons.
 *
 * @author Fabian Prasser
 */
public class ComponentTitledFolderButtonBar implements IComponent{

    /** View */
    private String                       id;

    /** View */
    private List<String>                 titles    = new ArrayList<String>();

    /** View */
    private Map<String, Image>           images    = new HashMap<String, Image>();

    /** View */
    private Map<String, Boolean>         toggle    = new HashMap<String, Boolean>();

    /** View */
    private Map<String, Runnable>        runnables = new HashMap<String, Runnable>();

    /** View */
    private final Map<Composite, String> helpids;
    

    /**
     * Creates a new instance without help button
     */
    public ComponentTitledFolderButtonBar() {    
        this(null);
    }
    
    /**
     * Creates a new instance.
     *
     * @param id
     */
    public ComponentTitledFolderButtonBar(String id) {    
        this.id = id;
        this.helpids = null;
    }

    /**
     * Creates a new instance.
     *
     * @param id
     * @param helpids
     */
    public ComponentTitledFolderButtonBar(String id, Map<Composite, String> helpids) {    
        this.id = id;
        this.helpids = helpids;
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
    public String getHelpId(){
        return id;
    }

    /**
     * Returns the help ids for each composite
     *
     * @return
     */
    public Map<Composite, String> getHelpIds(){
        return helpids;
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
