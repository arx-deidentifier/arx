/*
 * ARX: Powerful Data Anonymization
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

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.resize.action.AutoResizeColumnAction;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.action.NoOpMouseAction;
import org.eclipse.swt.events.MouseEvent;

/**
 * Resize column action that only fires for smaller datasets
 * @author Florian Kohlmayer
 */
public class DataTableResizeColumnAction implements IMouseAction {

    /** Determined with voodoo*/
    private static final int        RESIZE_THRESHOLD = 50000;

    /**  Default action */
    private final IMouseAction      defaultResizeAction;

    /**  No-op action */
    private final IMouseAction      noOpResizeAction;

    /**  Layer */
    private final IUniqueIndexLayer bodyDataLayer;

    /**
     * Construct
     * @param bodyDataLayer
     */
    public DataTableResizeColumnAction(IUniqueIndexLayer bodyDataLayer) {
        this.bodyDataLayer = bodyDataLayer;
        this.noOpResizeAction = new NoOpMouseAction();
        this.defaultResizeAction = new AutoResizeColumnAction();
    }

    @Override
    public void run(NatTable paramNatTable, MouseEvent paramMouseEvent) {
        int rowCount = bodyDataLayer.getRowCount();
        if (rowCount > RESIZE_THRESHOLD) {
            noOpResizeAction.run(paramNatTable, paramMouseEvent);
        } else {
            defaultResizeAction.run(paramNatTable, paramMouseEvent);
        }
    }
}
