package org.deidentifier.arx.gui.view.impl.menu;

import org.deidentifier.arx.DataSelector;

/**
 * 
 */
public class DialogQueryResult {

    /**  TODO */
    public final DataSelector selector;
    
    /**  TODO */
    public final String query;
    
    /**
     * 
     *
     * @param query
     * @param selector
     */
    protected DialogQueryResult(String query, DataSelector selector) {
        this.query = query;
        this.selector = selector;
    }
}
