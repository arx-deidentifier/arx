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
 */
public class DataTableImagePainter extends BackgroundPainter {

    private final DataTableContext context;

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
