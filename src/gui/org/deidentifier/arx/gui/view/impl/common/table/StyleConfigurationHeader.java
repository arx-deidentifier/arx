/*
 * ARX: Powerful Data Anonymization
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

package org.deidentifier.arx.gui.view.impl.common.table;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.BeveledBorderDecorator;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;

/**
 * Header style.
 *
 * @author Fabian Prasser
 */
public class StyleConfigurationHeader extends CTStyleConfiguration {

    /**  TODO */
    private final Font                    font;
    
    /**  TODO */
    private final Color                   bgColor         = GUIHelper.COLOR_WIDGET_BACKGROUND;
    
    /**  TODO */
    private final Color                   fgColor         = GUIHelper.COLOR_WIDGET_FOREGROUND;
    
    /**  TODO */
    private final Color                   gradientBgColor = GUIHelper.COLOR_WHITE;
    
    /**  TODO */
    private final Color                   gradientFgColor = GUIHelper.getColor(136,
                                                                               212,
                                                                               215);
    
    /**  TODO */
    private final HorizontalAlignmentEnum hAlign          = HorizontalAlignmentEnum.CENTER;
    
    /**  TODO */
    private final VerticalAlignmentEnum   vAlign          = VerticalAlignmentEnum.MIDDLE;
    
    /**  TODO */
    private final BorderStyle             borderStyle     = null;
    
    /**  TODO */
    private final ICellPainter            cellPainter     = new BeveledBorderDecorator(new TextPainter());
    
    /**  TODO */
    private final Boolean                 renderGridLines = Boolean.FALSE;
    
    /**  TODO */
    private final String                  region;
    
    /**
     * Creates a new instance.
     *
     * @param parent
     * @param region
     * @param config
     */
    public StyleConfigurationHeader(Composite parent, String region, CTConfiguration config){
        super (config);
        this.font = parent.getFont();
        this.region = region;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.config.IConfiguration#configureRegistry(org.eclipse.nebula.widgets.nattable.config.IConfigRegistry)
     */
    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {
        //configure the painter
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_PAINTER, 
                cellPainter, 
                DisplayMode.NORMAL, 
                region);
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_PAINTER, 
                cellPainter, 
                DisplayMode.NORMAL, 
                GridRegion.CORNER);

        //configure whether to render grid lines or not
        //e.g. for the BeveledBorderDecorator the rendering of the grid lines should be disabled
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.RENDER_GRID_LINES, 
                renderGridLines, 
                DisplayMode.NORMAL, 
                region);
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.RENDER_GRID_LINES, 
                renderGridLines, 
                DisplayMode.NORMAL, 
                GridRegion.CORNER);
        
        //configure the normal style
        Style cellStyle = new Style();
        cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, bgColor);
        cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, fgColor);
        cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, gradientBgColor);
        cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, gradientFgColor);
        cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, hAlign);
        cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, vAlign);
        cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, borderStyle);
        cellStyle.setAttributeValue(CellStyleAttributes.FONT, font);

        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_STYLE, 
                cellStyle, 
                DisplayMode.NORMAL, 
                region);
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_STYLE, 
                cellStyle, 
                DisplayMode.NORMAL, 
                GridRegion.CORNER);
    }
}
