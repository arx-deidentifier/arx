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
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * Style config for native cells and headers
 * @author Fabian Prasser
 *
 */
public class StyleConfigurationNative extends CTStyleConfiguration {
    
    public static final String DEFAULT_NATIVE_CELL_CONFIG_LABEL = "NativeCell_";

    private BorderStyle borderStyle = new BorderStyle(0, GUIHelper.getColor(240, 240, 240), LineStyleEnum.SOLID);
    private Color fgColor = GUIHelper.COLOR_BLACK;
    private Color bgColor = GUIHelper.getColor(245, 245, 245);
    private Font font;
    
    public StyleConfigurationNative(CTConfiguration config) {
        super(config);
        font = config.getFont();
    }

    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {
        addNativeStyleConfig(configRegistry);
//        addSummaryProviderConfig(configRegistry);
//        addSummaryRowDisplayConverter(configRegistry);
    }
//
//    protected void addSummaryProviderConfig(IConfigRegistry configRegistry) {
//        configRegistry.registerConfigAttribute(
//                SummaryRowConfigAttributes.SUMMARY_PROVIDER,
//                ISummaryProvider.DEFAULT,
//                DisplayMode.NORMAL,
//                SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL);
//    }
//
//    /**
//     * Add a specialized {@link DefaultDisplayConverter} that will show "..." if there is no value
//     * to show in the summary row yet.
//     */
//    protected void addSummaryRowDisplayConverter(IConfigRegistry configRegistry) {
//        configRegistry.registerConfigAttribute(
//                CellConfigAttributes.DISPLAY_CONVERTER,
//                new SummaryDisplayConverter(new DefaultDisplayConverter()), 
//                DisplayMode.NORMAL,
//                SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL);
//    }
//    
    protected void addNativeStyleConfig(IConfigRegistry configRegistry) {
        Style cellStyle = new Style();
        cellStyle.setAttributeValue(CellStyleAttributes.FONT, font);
        cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, bgColor);
        cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, fgColor);
        cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, borderStyle);
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_STYLE,
                cellStyle,
                DisplayMode.NORMAL,
                DEFAULT_NATIVE_CELL_CONFIG_LABEL);
    }
}
