package org.deidentifier.arx.gui.view.impl.common.datatable;

import org.deidentifier.arx.gui.resources.Resources;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.config.DefaultColumnHeaderStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.sort.painter.SortableHeaderTextPainter;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;

public class DataTableColumnHeaderConfiguration extends DefaultColumnHeaderStyleConfiguration {

    private static final Image     IMAGE_COL_BACK   = Resources.getImageStatic("column_header_bg.png");         //$NON-NLS-1$
    private static final Image     IMAGE_COL_SELECT = Resources.getImageStatic("selected_column_header_bg.png"); //$NON-NLS-1$

    private final DataTableContext context;

    public DataTableColumnHeaderConfiguration(DataTableContext context) {
        this.font = GUIHelper.getFont(new FontData("Verdana", 8, SWT.NORMAL)); //$NON-NLS-1$
        this.context = context;
    }

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        super.configureRegistry(configRegistry);
        addNormalModeStyling(configRegistry);
        addSelectedModeStyling(configRegistry);
    }

    private void addNormalModeStyling(final IConfigRegistry configRegistry) {

        final TextPainter txtPainter = new TextPainter(false, false);
        final ICellPainter bgImagePainter = new BackgroundImagePainter(txtPainter,
                                                                       IMAGE_COL_BACK,
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

    private void addSelectedModeStyling(final IConfigRegistry configRegistry) {

        final TextPainter txtPainter = new TextPainter(false, false);
        final ICellPainter selectedCellPainter = new BackgroundImagePainter(txtPainter,
                                                                            IMAGE_COL_SELECT,
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
