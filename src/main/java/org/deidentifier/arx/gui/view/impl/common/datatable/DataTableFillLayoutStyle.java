/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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

package org.deidentifier.arx.gui.view.impl.common.datatable;

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
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
import org.eclipse.swt.widgets.Control;

/**
 * Style config for fill layouts.
 *
 * @author Fabian Prasser
 */
public class DataTableFillLayoutStyle extends AbstractRegistryConfiguration {
    
    /**  TODO */
    public static final String DEFAULT_FILL_LAYOUT_CELL_CONFIG_LABEL = "FillLayoutCell_"; //$NON-NLS-1$

    /**  TODO */
    private BorderStyle borderStyle = new BorderStyle(0, GUIHelper.getColor(240, 240, 240), LineStyleEnum.SOLID);
    
    /**  TODO */
    private Color fgColor = GUIHelper.COLOR_BLACK;
    
    /**  TODO */
    private Color bgColor = GUIHelper.getColor(245, 245, 245);
    
    /**  TODO */
    private Font font;
    
    /**
     * 
     *
     * @param control
     */
    public DataTableFillLayoutStyle(Control control) {
        font = control.getFont();
    }

    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {
        addFillLayoutStyleConfig(configRegistry);
    }
    
    /**
     * 
     *
     * @param configRegistry
     */
    protected void addFillLayoutStyleConfig(IConfigRegistry configRegistry) {
        Style cellStyle = new Style();
        cellStyle.setAttributeValue(CellStyleAttributes.FONT, font);
        cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, bgColor);
        cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, fgColor);
        cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, borderStyle);
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_STYLE,
                cellStyle,
                DisplayMode.NORMAL,
                DEFAULT_FILL_LAYOUT_CELL_CONFIG_LABEL);
    }
}
