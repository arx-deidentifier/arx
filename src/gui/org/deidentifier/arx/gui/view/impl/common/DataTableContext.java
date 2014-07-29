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
import java.util.List;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.gui.Controller;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * A context for the data view. It provides all necessary data to be displayed.
 * @author Fabian Prasser
 */
public class DataTableContext {

    private Font                 font          = null;
    private List<Image>          images        = new ArrayList<Image>();
    private List<ILayerListener> listeners     = new ArrayList<ILayerListener>();
    private RowSet               rows          = null;
    private int[]                groups        = null;
    private DataHandle           handle        = null;
    private String[][]           array         = null;
    private Controller           controller    = null;
    private int                  selectedIndex = -1;

    /**
     * Creates a new instance
     * @param controller
     */
    public DataTableContext(Controller controller) {
        this.controller = controller;
    }

    /**
     * Returns the data array, if any
     * @return
     */
    public String[][] getArray() {
        return array;
    }

    /**
     * Returns the controller
     * @return
     */
    public Controller getController() {
        return controller;
    }
    
    /**
     * Returns information about the equivalence classes
     * @return
     */
    public int[] getGroups() {
        return groups;
    }

    /**
     * Returns the data handle, if any
     * @return
     */
    public DataHandle getHandle() {
        return handle;
    }

    /**
     * Returns the header images
     * @return
     */
    public List<Image> getImages() {
        return images;
    }

    /**
     * Returns all layer listeners
     * @return
     */
    public List<ILayerListener> getListeners() {
        return listeners;
    }

    /**
     * Returns the research subset
     * @return
     */
    public RowSet getRows() {
        return rows;
    }

    /**
     * Returns the selected index
     * @return
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }

    /**
     * Resets the context
     */
    public void reset() {
        this.handle = null;
        this.array = null;
        this.rows = null;
        this.groups = null;
        this.images.clear();
    }

    /**
     * Sets the array data
     * @param array
     */
    public void setArray(String[][] array) {
        this.array = array;
    }

    /**
     * Sets the groups
     * @param groups
     */
    public void setGroups(int[] groups) {
        this.groups = groups;
    }

    /**
     * Sets a data handle
     * @param handle
     */
    public void setHandle(DataHandle handle) {
        this.handle = handle;
    }

    /**
     * Sets the header images
     * @param images
     */
    public void setImages(List<Image> images) {
        this.images = images;
    }

    /**
     * Sets the layer listeners
     * @param listeners
     */
    public void setListeners(List<ILayerListener> listeners) {
        this.listeners = listeners;
    }
    
    /**
     * Sets the research subset
     * @param rows
     */
    public void setRows(RowSet rows) {
        this.rows = rows;
    }
    
    /**
     * Sets the selected column index
     * @param index
     */
    public void setSelectedIndex(int index) {
        this.selectedIndex = index;
    }

    /**
     * Sets the font
     * @return
     */
    public Font getFont() {
        return font;
    }

    /**
     * Gets the font
     * @param font
     */
    public void setFont(Font font) {
        this.font = font;
    }
}
