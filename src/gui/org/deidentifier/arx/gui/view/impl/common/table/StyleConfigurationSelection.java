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
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.SelectionStyleLabels;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * Sets up rendering style used for selected areas and the selection anchor.
 */
public class StyleConfigurationSelection extends CTStyleConfiguration {

    // General style
    /**  TODO */
    public Font        font                       = getConfig().getFont();

    /**  TODO */
    public Color       selectionBgColor           = GUIHelper.COLOR_TITLE_INACTIVE_BACKGROUND;
    
    /**  TODO */
    public Color       selectionFgColor           = GUIHelper.COLOR_BLACK;
    
    /**  TODO */
    public Color       selectedHeaderBgColor      = GUIHelper.COLOR_TITLE_INACTIVE_BACKGROUND;
    
    /**  TODO */
    public Color       selectedHeaderFgColor      = GUIHelper.COLOR_BLACK;
    
    /**  TODO */
    public BorderStyle selectedHeaderBorderStyle  = new BorderStyle(-1,
                                                                    selectedHeaderFgColor,
                                                                    LineStyleEnum.SOLID);
    
    /**  TODO */
    public Color       fullySelectedHeaderBgColor = GUIHelper.COLOR_WIDGET_NORMAL_SHADOW;
    
    /**
     * 
     *
     * @param config
     */
    public StyleConfigurationSelection(CTConfiguration config) {
        super(config);
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.config.IConfiguration#configureRegistry(org.eclipse.nebula.widgets.nattable.config.IConfigRegistry)
     */
    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {
        configureSelectionStyle(configRegistry);
        configureSelectionAnchorStyle(configRegistry);
        configureHeaderHasSelectionStyle(configRegistry);
        configureHeaderFullySelectedStyle(configRegistry);
    }

    /**
     * 
     *
     * @param configRegistry
     */
    protected void configureHeaderFullySelectedStyle(IConfigRegistry configRegistry) {
        // Header fully selected
        Style cellStyle = new Style();  
        cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, fullySelectedHeaderBgColor);

        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_STYLE, 
                cellStyle, 
                DisplayMode.SELECT, 
                SelectionStyleLabels.COLUMN_FULLY_SELECTED_STYLE);
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_STYLE, 
                cellStyle, 
                DisplayMode.SELECT, 
                SelectionStyleLabels.ROW_FULLY_SELECTED_STYLE);
    }

    /**
     * 
     *
     * @param configRegistry
     */
    protected void configureHeaderHasSelectionStyle(IConfigRegistry configRegistry) {
        Style cellStyle = new Style();

        cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, selectedHeaderFgColor);
        cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, selectedHeaderBgColor);
        cellStyle.setAttributeValue(CellStyleAttributes.FONT, font);
        
        switch (getConfig().getHorizontalAlignment()) {
            case SWT.LEFT:
                cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT);
                break;
            case SWT.RIGHT:
                cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
                break;
            case SWT.CENTER:
                cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.CENTER);
                break;
        }
        cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, selectedHeaderBorderStyle);

        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_STYLE, 
                cellStyle, 
                DisplayMode.SELECT, 
                GridRegion.COLUMN_HEADER);
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_STYLE, 
                cellStyle, 
                DisplayMode.SELECT, 
                GridRegion.CORNER);
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_STYLE, 
                cellStyle, 
                DisplayMode.SELECT, 
                GridRegion.ROW_HEADER);
    }

    
    /**
     * 
     *
     * @param configRegistry
     */
    protected void configureSelectionAnchorStyle(IConfigRegistry configRegistry) {
        // Selection anchor style for normal display mode
        Style cellStyle = new Style();
        cellStyle.setAttributeValue(CellStyleAttributes.FONT, font);
        cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, selectionBgColor);
        cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, selectionFgColor);

        switch (getConfig().getHorizontalAlignment()) {
            case SWT.LEFT:
                cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT);
                break;
            case SWT.RIGHT:
                cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
                break;
            case SWT.CENTER:
                cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.CENTER);
                break;
        }
        
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_STYLE, 
                cellStyle, 
                DisplayMode.NORMAL, 
                SelectionStyleLabels.SELECTION_ANCHOR_STYLE);

        // Selection anchor style for select display mode
        cellStyle = new Style();
        cellStyle.setAttributeValue(CellStyleAttributes.FONT, font);
        cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, selectionBgColor);
        cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, selectionFgColor);

        switch (getConfig().getHorizontalAlignment()) {
            case SWT.LEFT:
                cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT);
                break;
            case SWT.RIGHT:
                cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
                break;
            case SWT.CENTER:
                cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.CENTER);
                break;
        }
        
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_STYLE, 
                cellStyle, 
                DisplayMode.SELECT, 
                SelectionStyleLabels.SELECTION_ANCHOR_STYLE);
    }

    /**
     * 
     *
     * @param configRegistry
     */
    protected void configureSelectionStyle(IConfigRegistry configRegistry) {
        Style cellStyle = new Style();
        cellStyle.setAttributeValue(CellStyleAttributes.FONT, font);
        cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, selectionBgColor);
        cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, selectionFgColor);

        switch (getConfig().getHorizontalAlignment()) {
            case SWT.LEFT:
                cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT);
                break;
            case SWT.RIGHT:
                cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
                break;
            case SWT.CENTER:
                cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.CENTER);
                break;
        }
        
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_STYLE, 
                cellStyle, 
                DisplayMode.SELECT);
    }
}

