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
