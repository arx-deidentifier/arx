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
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDisplayConverter;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.summaryrow.ISummaryProvider;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummaryDisplayConverter;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummaryRowConfigAttributes;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummaryRowLayer;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

/**
 * Style config for native headers
 * @author Fabian Prasser
 *
 */
public class StyleConfigurationNative extends CTStyleConfiguration {

    public BorderStyle summaryRowBorderStyle = new BorderStyle(0, GUIHelper.COLOR_BLACK, LineStyleEnum.DOTTED);

    public Color summaryRowFgColor = GUIHelper.COLOR_BLACK;
    public Color summaryRowBgColor = GUIHelper.COLOR_WHITE;
    public Font summaryRowFont = GUIHelper.getFont(new FontData("Verdana", 8, SWT.BOLD)); //$NON-NLS-1$
    public StyleConfigurationNative(CTConfiguration config) {
        super(config);
    }

    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {
        addSummaryRowStyleConfig(configRegistry);
        addSummaryProviderConfig(configRegistry);
        addSummaryRowDisplayConverter(configRegistry);
    }

    protected void addSummaryProviderConfig(IConfigRegistry configRegistry) {
        configRegistry.registerConfigAttribute(
                SummaryRowConfigAttributes.SUMMARY_PROVIDER,
                ISummaryProvider.DEFAULT,
                DisplayMode.NORMAL,
                SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL);
    }

    /**
     * Add a specialized {@link DefaultDisplayConverter} that will show "..." if there is no value
     * to show in the summary row yet.
     */
    protected void addSummaryRowDisplayConverter(IConfigRegistry configRegistry) {
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.DISPLAY_CONVERTER,
                new SummaryDisplayConverter(new DefaultDisplayConverter()), 
                DisplayMode.NORMAL,
                SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL);
    }
    
    protected void addSummaryRowStyleConfig(IConfigRegistry configRegistry) {
        Style cellStyle = new Style();
        cellStyle.setAttributeValue(CellStyleAttributes.FONT, summaryRowFont);
        cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, summaryRowBgColor);
        cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, summaryRowFgColor);
        cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, summaryRowBorderStyle);
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_STYLE,
                cellStyle,
                DisplayMode.NORMAL,
                SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL);
    }
}
