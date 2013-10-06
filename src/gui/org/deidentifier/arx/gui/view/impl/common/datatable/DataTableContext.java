package org.deidentifier.arx.gui.view.impl.common.datatable;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.gui.Controller;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.swt.graphics.Image;

public class DataTableContext {

    private List<Image>          images     = new ArrayList<Image>();
    private List<ILayerListener> listeners  = new ArrayList<ILayerListener>();
    private RowSet               rows       = null;
    private int[]                groups     = null;
    private DataHandle           handle     = null;
    private String[][]           array      = null;
    private Controller           controller = null;

    public DataTableContext(Controller controller) {
        this.controller = controller;
    }

    public String[][] getArray() {
        return array;
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

    public RowSet getRows() {
        return rows;
    }

    public List<ILayerListener> getListeners() {
        return listeners;
    }
//
//    public NatTable getTable() {
//        return table;
//    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public void setListeners(List<ILayerListener> listeners) {
        this.listeners = listeners;
    }

    public void setRows(RowSet rows) {
        this.rows = rows;
    }

    public void setGroups(int[] groups) {
        this.groups = groups;
    }

    public void setHandle(DataHandle handle) {
        this.handle = handle;
    }

    public void setArray(String[][] array) {
        this.array = array;
    }
    
    public void reset() {
        this.handle = null;
        this.array = null;
        this.rows = null;
        this.groups = null;
        this.images.clear();
    }
}
