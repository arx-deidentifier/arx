package org.deidentifier.arx.gui.view.impl.menu;

import org.deidentifier.arx.DataSelector;

public class DialogQueryResult {

    public final DataSelector selector;
    public final String query;
    
    protected DialogQueryResult(String query, DataSelector selector) {
        this.query = query;
        this.selector = selector;
    }
}
