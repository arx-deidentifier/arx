/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
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

public class ComponentTitleBar implements IComponent{
    
    private List<String> titles = new ArrayList<String>();
    private Map<String, Image> images = new HashMap<String, Image>();
    private Map<String, Boolean> toggle = new HashMap<String, Boolean>();
    private Map<String, Runnable> runnables = new HashMap<String, Runnable>();
    private String id;
    
    public ComponentTitleBar(String id) {    
        this.id = id;
    }

    public void add(String title, Image image, Runnable runnable){
        add(title, image, false, runnable);
    }

    public void add(String title, Image image, boolean toggle, Runnable runnable) {
        this.titles.add(title);
        this.toggle.put(title, toggle);
        this.images.put(title, image);
        this.runnables.put(title, runnable);
    }

    public List<String> getTitles() {
        return titles;
    }

    public Image getImage(String title) {
        return images.get(title);
    }

    public Runnable getRunnable(String title) {
        return runnables.get(title);
    }
    
    public Boolean isToggle(String title) {
        return toggle.get(title);
    }

    public String getId(){
        return id;
    }
}
