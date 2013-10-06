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

package org.deidentifier.arx.gui.view.impl.common.datatable;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.config.DefaultRowHeaderStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;

public class DataTableRowHeaderConfiguration extends DefaultRowHeaderStyleConfiguration {

    private final Image IMAGE_ROW_BACK; //$NON-NLS-1$
    private final Image IMAGE_ROW_SELECT; //$NON-NLS-1$

    public DataTableRowHeaderConfiguration(DataTableContext context) {
        font = GUIHelper.getFont(new FontData("Verdana", 8, SWT.NORMAL)); //$NON-NLS-1$
        IMAGE_ROW_BACK   = context.getController().getResources().getImage("row_header_bg.png");         //$NON-NLS-1$
        IMAGE_ROW_SELECT = context.getController().getResources().getImage("selected_row_header_bg.png"); //$NON-NLS-1$
        final TextPainter txtPainter = new TextPainter(false, false);
        final ICellPainter bgImagePainter = new BackgroundImagePainter(txtPainter, IMAGE_ROW_BACK, null);
        cellPainter = bgImagePainter;
    }

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        super.configureRegistry(configRegistry);
        addSelectedModeStyling(configRegistry);
    }

    private void addSelectedModeStyling(final IConfigRegistry configRegistry) {

        final TextPainter txtPainter = new TextPainter(false, false);
        final ICellPainter selectedCellPainter = new BackgroundImagePainter(txtPainter,
                                                                            IMAGE_ROW_SELECT,
                                                                            GUIHelper.getColor(192, 192, 192));

        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER,
                                               selectedCellPainter,
                                               DisplayMode.SELECT,
                                               GridRegion.ROW_HEADER);
    }
}
