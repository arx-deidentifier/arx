package org.deidentifier.arx.gui.view.impl.common;

/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
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

public class NatTableLayerConfiguration extends AbstractRegistryConfiguration {

    public BorderStyle summaryRowBorderStyle = new BorderStyle(0, GUIHelper.COLOR_BLACK, LineStyleEnum.DOTTED);
    public Color summaryRowFgColor = GUIHelper.COLOR_BLACK;
    public Color summaryRowBgColor = GUIHelper.COLOR_WHITE;
    public Font summaryRowFont = GUIHelper.getFont(new FontData("Verdana", 8, SWT.BOLD)); //$NON-NLS-1$

    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {
        addSummaryRowStyleConfig(configRegistry);
        addSummaryProviderConfig(configRegistry);
        addSummaryRowDisplayConverter(configRegistry);
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
}
