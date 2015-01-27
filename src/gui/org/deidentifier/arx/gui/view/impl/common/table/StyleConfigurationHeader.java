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
