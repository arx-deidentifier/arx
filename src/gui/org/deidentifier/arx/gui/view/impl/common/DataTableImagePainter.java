/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.arx.gui.view.impl.common;

import java.util.List;

import org.deidentifier.arx.RowSet;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Paints an image. If no image is provided, it will attempt to look up an image
 * from the cell style.
 * 
 * @author Fabian Prasser
 */
public class DataTableImagePainter extends BackgroundPainter {

    private final DataTableContext context;

    /**
     * Creates a new instance
     * @param context
     */
    public DataTableImagePainter(DataTableContext context) {
        super();
        this.context = context;
    }

    @Override
    public void paintCell(final ILayerCell cell,
                          final GC gc,
                          final Rectangle bounds,
                          final IConfigRegistry configRegistry) {

        RowSet rows = context.getRows();
        List<Image> headerImages = context.getImages();
        if ((headerImages != null) && (headerImages.size() > 0)) {
            final int index = cell.getColumnIndex() - (rows != null ? 1 : 0);
            if (index >= 0) {
                final Image image = headerImages.get(index);
                if (image != null) {
                    gc.drawImage(image, bounds.x + 3, bounds.y - 8);
                }
            }
        }
    }
}
