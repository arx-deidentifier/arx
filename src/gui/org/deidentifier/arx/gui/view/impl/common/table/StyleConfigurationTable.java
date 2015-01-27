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
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDisplayConverter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.LineBorderDecorator;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * The table style configuration.
 *
 * @author Fabian Prasser
 */
public class StyleConfigurationTable extends CTStyleConfiguration {

    /**  TODO */
    public Color bgColor = GUIHelper.COLOR_WHITE;
    
    /**  TODO */
    public Color fgColor = GUIHelper.COLOR_BLACK;
    
    /**  TODO */
    public Color gradientBgColor = GUIHelper.COLOR_WHITE;
    
    /**  TODO */
    public Color gradientFgColor = GUIHelper.getColor(136, 212, 215);
    
    /**  TODO */
    public Font font = getConfig().getFont();
    
    /**  TODO */
    public HorizontalAlignmentEnum hAlign = 
            getConfig().getHorizontalAlignment() == SWT.LEFT ? HorizontalAlignmentEnum.LEFT : 
                getConfig().getHorizontalAlignment() == SWT.RIGHT ? HorizontalAlignmentEnum.RIGHT : 
            HorizontalAlignmentEnum.CENTER ;
    
    /**  TODO */
    public VerticalAlignmentEnum vAlign = VerticalAlignmentEnum.MIDDLE;
    
    /**  TODO */
    public BorderStyle borderStyle = null;
    
    /**  TODO */
    public ICellPainter cellPainter = new LineBorderDecorator(new TextPainter());

    /**
     * 
     *
     * @param config
     */
    public StyleConfigurationTable(CTConfiguration config) {
        super(config);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.config.IConfiguration#configureRegistry(org.eclipse.nebula.widgets.nattable.config.IConfigRegistry)
     */
    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, cellPainter);

        Style cellStyle = new Style();
        cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, bgColor);
        cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, fgColor);
        cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, gradientBgColor);
        cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, gradientFgColor);
        cellStyle.setAttributeValue(CellStyleAttributes.FONT, font);
        cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, hAlign);
        cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, vAlign);
        cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, borderStyle);
        
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle);
    
        configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, new DefaultDisplayConverter());
    }
}
    