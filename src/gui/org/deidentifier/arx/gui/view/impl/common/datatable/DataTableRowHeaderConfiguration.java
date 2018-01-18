/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.config.DefaultRowHeaderStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Image;

/**
 * A configuration for row headers in the data view.
 *
 * @author Fabian Prasser
 */
public class DataTableRowHeaderConfiguration extends DefaultRowHeaderStyleConfiguration {

    /**  Image */
    private final Image backgroundDefault; 
    
    /**  Image */
    private final Image backgroundSelected;
    
    /**
     * Creates a new instance.
     *
     * @param context
     */
    public DataTableRowHeaderConfiguration(DataTableContext context) {
        this.font = context.getFont();
        this.backgroundDefault   = context.getController().getResources().getManagedImage("row_header_bg.png"); //$NON-NLS-1$
        this.backgroundSelected = context.getController().getResources().getManagedImage("selected_row_header_bg.png"); //$NON-NLS-1$
        final TextPainter txtPainter = new TextPainter(false, false);
        final ICellPainter bgImagePainter = new BackgroundImagePainter(txtPainter, backgroundDefault, null);
        this.cellPainter = bgImagePainter;
    }

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        super.configureRegistry(configRegistry);
        addSelectedModeStyling(configRegistry);
    }

    /**
     * Add selected style.
     *
     * @param configRegistry
     */
    private void addSelectedModeStyling(final IConfigRegistry configRegistry) {

        final TextPainter txtPainter = new TextPainter(false, false);
        final ICellPainter selectedCellPainter = new BackgroundImagePainter(txtPainter,
                                                                            backgroundSelected,
                                                                            GUIHelper.getColor(192, 192, 192));

        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER,
                                               selectedCellPainter,
                                               DisplayMode.SELECT,
                                               GridRegion.ROW_HEADER);
    }
}
