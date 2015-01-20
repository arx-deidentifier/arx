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

    /**  TODO */
    private final DataTableContext context;

    /**
     * Creates a new instance.
     *
     * @param context
     */
    public DataTableImagePainter(DataTableContext context) {
        super();
        this.context = context;
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter#paintCell(org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell, org.eclipse.swt.graphics.GC, org.eclipse.swt.graphics.Rectangle, org.eclipse.nebula.widgets.nattable.config.IConfigRegistry)
     */
    @Override
    public void paintCell(final ILayerCell cell,
                          final GC gc,
                          final Rectangle bounds,
                          final IConfigRegistry configRegistry) {

        RowSet rows = context.getRows();
        List<Image> headerImages = context.getImages();
        if ((headerImages != null) && (headerImages.size() > 0)) {
            final int index = cell.getColumnIndex() - (rows != null ? 1 : 0);
            if (index >= 0 && index<headerImages.size()) {
                final Image image = headerImages.get(index);
                if (image != null) {
                    gc.drawImage(image, bounds.x + 3, bounds.y - 8);
                }
            }
        }
    }
}
