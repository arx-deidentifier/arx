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

import org.deidentifier.arx.RowSet;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;

/**
 * A label accumulator for the data view.
 *
 * @author Fabian Prasser
 */
public class DataTableConfigLabelAccumulator implements IConfigLabelAccumulator {

    /**  TODO */
    private final DataTableContext context;
    
    /**  TODO */
    private final NatTable         table;

    /**
     * Creates a new instance.
     *
     * @param table
     * @param context
     */
    public DataTableConfigLabelAccumulator(NatTable table, DataTableContext context) {
        this.context = context;
        this.table = table;
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator#accumulateConfigLabels(org.eclipse.nebula.widgets.nattable.layer.LabelStack, int, int)
     */
    @Override
    public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {

        int[] groups = context.getGroups();
        RowSet rows = context.getRows();

        if (table != null && groups != null) {
            int row = table.getRowIndexByPosition(rowPosition + 1);
            configLabels.addLabel("background" + (groups[row] % 2)); //$NON-NLS-1$
            if (row < groups.length - 1 && groups[row] != groups[row + 1]) {
                configLabels.addLabel(DataTableDecorator.BOTTOM_LINE_BORDER_LABEL);
            }
        }

        if (table != null && rows != null) {
            int column = table.getColumnIndexByPosition(columnPosition + 1);
            if (column == 0) {
                configLabels.addLabel("checkbox"); //$NON-NLS-1$
            }
        }
    }
}
