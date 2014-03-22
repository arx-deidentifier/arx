/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.gui.view.impl.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deidentifier.arx.gui.view.def.IComponent;
import org.eclipse.swt.graphics.Image;

/**
 * A basic title bar, which offers some buttons
 * 
 * @author Fabian Prasser
 */
public class ComponentTitleBar implements IComponent{
    
    private List<String> titles = new ArrayList<String>();
    private Map<String, Image> images = new HashMap<String, Image>();
    private Map<String, Boolean> toggle = new HashMap<String, Boolean>();
    private Map<String, Runnable> runnables = new HashMap<String, Runnable>();
    private String id;
    
    /**
     * Creates a new instance
     * @param id
     */
    public ComponentTitleBar(String id) {    
        this.id = id;
    }

    /**
     * Adds a button
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
     * Adds a new button
     * @param title
     * @param image
     * @param runnable
     */
    public void add(String title, Image image, Runnable runnable){
        add(title, image, false, runnable);
    }

    /**
     * Returns the id
     * @return
     */
    public String getId(){
        return id;
    }

    /**
     * Returns the image for the given button
     * @param title
     * @return
     */
    public Image getImage(String title) {
        return images.get(title);
    }

    /**
     * Returns the runnable for the given button
     * @param title
     * @return
     */
    public Runnable getRunnable(String title) {
        return runnables.get(title);
    }
    
    /**
     * Returns the titles of all buttons
     * @return
     */
    public List<String> getTitles() {
        return titles;
    }

    /**
     * Returns whether the given button is a toggle button
     * @param title
     * @return
     */
    public Boolean isToggle(String title) {
        return toggle.get(title);
    }
}
