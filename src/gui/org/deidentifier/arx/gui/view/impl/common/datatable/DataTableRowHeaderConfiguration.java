package org.deidentifier.arx.gui.view.impl.common.datatable;

import org.deidentifier.arx.gui.resources.Resources;
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

    private static final Image IMAGE_ROW_BACK   = Resources.getImageStatic("row_header_bg.png");         //$NON-NLS-1$
    private static final Image IMAGE_ROW_SELECT = Resources.getImageStatic("selected_row_header_bg.png"); //$NON-NLS-1$

    public DataTableRowHeaderConfiguration() {
        font = GUIHelper.getFont(new FontData("Verdana", 8, SWT.NORMAL)); //$NON-NLS-1$

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
