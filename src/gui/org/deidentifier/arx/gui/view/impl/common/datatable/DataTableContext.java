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

package org.deidentifier.arx.gui.view.impl.common.datatable;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.gui.Controller;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * A context for the data view. It provides all necessary data to be displayed.
 * @author Fabian Prasser
 */
public class DataTableContext {

    /**  TODO */
    private Font                 font          = null;
    
    /**  TODO */
    private List<Image>          images        = new ArrayList<Image>();
    
    /**  TODO */
    private List<ILayerListener> listeners     = new ArrayList<ILayerListener>();
    
    /**  TODO */
    private RowSet               rows          = null;
    
    /**  TODO */
    private int[]                groups        = null;
    
    /**  TODO */
    private DataHandle           handle        = null;
    
    /**  TODO */
    private Controller           controller    = null;
    
    /**  TODO */
    private int                  selectedIndex = -1;
	
	/**  TODO */
	private NatTable             table         = null;

    /**
     * Creates a new instance.
     *
     * @param controller
     */
    public DataTableContext(Controller controller) {
        this.controller = controller;
    }
    
    /**
     * Sets the underlying table.
     *
     * @param table
     */
    public void setTable(NatTable table) {
		this.table = table;
	}

	/**
     * Returns the underlying table.
     *
     * @return
     */
    public NatTable getTable() {
		return table;
	}

	/**
     * Returns the controller.
     *
     * @return
     */
    public Controller getController() {
        return controller;
    }
    
    /**
     * Returns information about the equivalence classes.
     *
     * @return
     */
    public int[] getGroups() {
        return groups;
    }

    /**
     * Returns the data handle, if any.
     *
     * @return
     */
    public DataHandle getHandle() {
        return handle;
    }

    /**
     * Returns the header images.
     *
     * @return
     */
    public List<Image> getImages() {
        return images;
    }

    /**
     * Returns all layer listeners.
     *
     * @return
     */
    public List<ILayerListener> getListeners() {
        return listeners;
    }

    /**
     * Returns the research subset.
     *
     * @return
     */
    public RowSet getRows() {
        return rows;
    }

    /**
     * Returns the selected index.
     *
     * @return
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }

    /**
     * Resets the context.
     */
    public void reset() {
        this.handle = null;
        this.rows = null;
        this.groups = null;
        this.images.clear();
    }

    /**
     * Sets the groups.
     *
     * @param groups
     */
    public void setGroups(int[] groups) {
        this.groups = groups;
    }

    /**
     * Sets a data handle.
     *
     * @param handle
     */
    public void setHandle(DataHandle handle) {
        this.handle = handle;
    }

    /**
     * Sets the header images.
     *
     * @param images
     */
    public void setImages(List<Image> images) {
        this.images = images;
    }

    /**
     * Sets the layer listeners.
     *
     * @param listeners
     */
    public void setListeners(List<ILayerListener> listeners) {
        this.listeners = listeners;
    }
    
    /**
     * Sets the research subset.
     *
     * @param rows
     */
    public void setRows(RowSet rows) {
        this.rows = rows;
    }
    
    /**
     * Sets the selected column index.
     *
     * @param index
     */
    public void setSelectedIndex(int index) {
        this.selectedIndex = index;
    }

    /**
     * Sets the font.
     *
     * @return
     */
    public Font getFont() {
        return font;
    }

    /**
     * Gets the font.
     *
     * @param font
     */
    public void setFont(Font font) {
        this.font = font;
    }

	/**
     * 
     *
     * @return
     */
	public boolean isRowExpanded() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
     * 
     *
     * @return
     */
	public boolean isColumnExpanded() {
		// TODO Auto-generated method stub
		return false;
	}
}
