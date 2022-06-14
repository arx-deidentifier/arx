/*
 * ARX Data Anonymization Tool
 * Copyright 2012 - 2022 Fabian Prasser and contributors
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Control;

/**
 * Configuration for component data table
 * 
 * @author Fabian Prasser
 */
public class CTConfiguration {

    /** Field */
    public static final int COLUMN_HEADER_LAYOUT_DEFAULT    = 0;
    
    /** Field */
    public static final int COLUMN_HEADER_LAYOUT_FILL_EQUAL = 1;
    
    /** Field */
    public static final int COLUMN_HEADER_LAYOUT_FILL  = 2;

    /** Field */
    public static final int ROW_HEADER_LAYOUT_DEFAULT       = 0;
    
    /** Field */
    public static final int ROW_HEADER_LAYOUT_FILL          = 1;

    /** Field */
    public static final int STYLE_TABLE                     = 1;
    
    /** Field */
    public static final int STYLE_GRID                      = 2;

    /** Field */
    private final int       style;
    
    /** Field */
    private int             horizontalAlignment             = SWT.CENTER;
    
    /** Field */
    private int             columnHeaderLayout              = CTConfiguration.COLUMN_HEADER_LAYOUT_DEFAULT;
    
    /** Field */
    private int             rowHeaderLayout                 = CTConfiguration.ROW_HEADER_LAYOUT_DEFAULT;
    
    /** Field */
    private boolean         rowSelectionEnabled             = false;
    
    /** Field */
    private boolean         columnSelectionEnabled          = true;
    
    /** Field */
    private boolean         cellSelectionEnabled            = true;
    
    /** Field */
    private Font            font;

    /**
     * Creates a new grid or table.
     * @param parent
     * @param style
     */
    public CTConfiguration(Control parent, int style){
        this.font = parent.getFont();
        this.style = style;
    }

    /**
     * Configuration options
     *
     * @return
     */
    public int getColumnHeaderLayout() {
        return columnHeaderLayout;
    }

    /**
     * Configuration options
     *
     * @return
     */
    public Font getFont() {
        return font;
    }

    /**
     * Configuration options
     *
     * @return
     */
    public int getHorizontalAlignment() {
        return horizontalAlignment;
    }

    /**
     * Configuration options
     *
     * @return
     */
    public int getRowHeaderLayout() {
        return rowHeaderLayout;
    }

    /**
     * Configuration options
     *
     * @return
     */
    public int getStyle() {
        return style;
    }

    /**
     * Configuration options
     *
     * @return
     */
    public boolean isCellSelectionEnabled() {
        return cellSelectionEnabled;
    }

    /**
     * Configuration options
     *
     * @return
     */
    public boolean isColumnSelectionEnabled() {
        return columnSelectionEnabled;
    }

    /**
     * Configuration options
     *
     * @return
     */
    public boolean isRowSelectionEnabled() {
        return rowSelectionEnabled;
    }

    /**
     * Configuration options
     *
     * @param cellSelectionEnabled
     */
    public void setCellSelectionEnabled(boolean cellSelectionEnabled) {
        this.cellSelectionEnabled = cellSelectionEnabled;
    }

    /**
     * Configuration options
     *
     * @param columnHeaderLayout
     */
    public void setColumnHeaderLayout(int columnHeaderLayout) {
        this.columnHeaderLayout = columnHeaderLayout;
    }

    /**
     * Configuration options
     *
     * @param columnSelectionEnabled
     */
    public void setColumnSelectionEnabled(boolean columnSelectionEnabled) {
        this.columnSelectionEnabled = columnSelectionEnabled;
    }

    /**
     * Configuration options
     *
     * @param font
     */
    public void setFont(Font font) {
        this.font = font;
    }

    /**
     * Configuration options
     *
     * @param horizontalAlignment
     */
    public void setHorizontalAlignment(int horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    /**
     * Configuration options
     *
     * @param rowHeaderLayout
     */
    public void setRowHeaderLayout(int rowHeaderLayout) {
        this.rowHeaderLayout = rowHeaderLayout;
    }

    /**
     * Configuration options
     *
     * @param rowSelectionEnabled
     */
    public void setRowSelectionEnabled(boolean rowSelectionEnabled) {
        this.rowSelectionEnabled = rowSelectionEnabled;
    }
}
