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
import org.eclipse.nebula.widgets.nattable.layer.config.DefaultColumnHeaderStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.sort.painter.SortableHeaderTextPainter;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Image;

/**
 * A column style for the data view.
 *
 * @author Fabian Prasser
 */
public class DataTableColumnHeaderConfiguration extends DefaultColumnHeaderStyleConfiguration {

    /** Image */
    private final Image            defaultBackground;

    /** Image */
    private final Image            selectedBackground;

    /** Context */
    private final DataTableContext context;

    /**
     * Creates a new instance.
     *
     * @param context
     */
    public DataTableColumnHeaderConfiguration(DataTableContext context) {
        this.context = context;
        this.font = context.getFont();
        this.defaultBackground   = context.getController().getResources().getManagedImage("column_header_bg.png"); //$NON-NLS-1$
        this.selectedBackground = context.getController().getResources().getManagedImage("selected_column_header_bg.png"); //$NON-NLS-1$
    }

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        super.configureRegistry(configRegistry);
        addNormalModeStyling(configRegistry);
        addSelectedModeStyling(configRegistry);
    }

    /**
     * Add normal styling to the registry.
     *
     * @param configRegistry
     */
    private void addNormalModeStyling(final IConfigRegistry configRegistry) {

        final TextPainter txtPainter = new TextPainter(false, false, true, true);
        final ICellPainter bgImagePainter = new DataTableBackgroundImagePainter(txtPainter,
                                                                                defaultBackground,
                                                                                GUIHelper.getColor(192, 192, 192));
        final SortableHeaderTextPainter headerBasePainter = new SortableHeaderTextPainter(bgImagePainter, false, true);

        final CellPainterDecorator headerPainter = new CellPainterDecorator(headerBasePainter,
                                                                            CellEdgeEnum.LEFT,
                                                                            new DataTableImagePainter(context));

        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER,
                                               headerPainter,
                                               DisplayMode.NORMAL,
                                               GridRegion.COLUMN_HEADER);
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER,
                                               headerBasePainter,
                                               DisplayMode.NORMAL,
                                               GridRegion.CORNER);
    }

    /**
     * Add selected styling to the registry.
     *
     * @param configRegistry
     */
    private void addSelectedModeStyling(final IConfigRegistry configRegistry) {

        final TextPainter txtPainter = new TextPainter(false, false, true, true);
        final ICellPainter selectedCellPainter = new DataTableBackgroundImagePainter(txtPainter,
                                                                                     selectedBackground,
                                                                                     GUIHelper.getColor(192, 192, 192));

        final CellPainterDecorator selectedHeaderPainter = new CellPainterDecorator(selectedCellPainter,
                                                                                    CellEdgeEnum.LEFT,
                                                                                    new DataTableImagePainter(context));

        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER,
                                               selectedHeaderPainter,
                                               DisplayMode.SELECT,
                                               GridRegion.COLUMN_HEADER);
    }
}
