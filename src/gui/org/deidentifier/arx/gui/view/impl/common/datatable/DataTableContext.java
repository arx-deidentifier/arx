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

package org.deidentifier.arx.gui.view.impl.common.datatable;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.gui.Controller;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.swt.graphics.Image;

public class DataTableContext {

    private List<Image>          images        = new ArrayList<Image>();
    private List<ILayerListener> listeners     = new ArrayList<ILayerListener>();
    private RowSet               rows          = null;
    private int[]                groups        = null;
    private DataHandle           handle        = null;
    private String[][]           array         = null;
    private Controller           controller    = null;
    private int                  selectedIndex = -1;

    public DataTableContext(Controller controller) {
        this.controller = controller;
    }

    public String[][] getArray() {
        return array;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }
    
    public Controller getController() {
        return controller;
    }

    public int[] getGroups() {
        return groups;
    }

    public DataHandle getHandle() {
        return handle;
    }

    public List<Image> getImages() {
        return images;
    }

    public List<ILayerListener> getListeners() {
        return listeners;
    }

    public RowSet getRows() {
        return rows;
    }

    public void reset() {
        this.handle = null;
        this.array = null;
        this.rows = null;
        this.groups = null;
        this.images.clear();
    }

    public void setArray(String[][] array) {
        this.array = array;
    }

    public void setSelectedIndex(int index) {
        this.selectedIndex = index;
    }

    public void setGroups(int[] groups) {
        this.groups = groups;
    }

    public void setHandle(DataHandle handle) {
        this.handle = handle;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }
    
    public void setListeners(List<ILayerListener> listeners) {
        this.listeners = listeners;
    }
    
    public void setRows(RowSet rows) {
        this.rows = rows;
    }

}
