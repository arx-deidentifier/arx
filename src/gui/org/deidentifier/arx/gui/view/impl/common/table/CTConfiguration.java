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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Control;

/**
 * 
 * @author Fabian Prasser
 *
 */
public class CTConfiguration {

    /**  TODO */
    public static final int COLUMN_HEADER_LAYOUT_DEFAULT    = 0;
    
    /**  TODO */
    public static final int COLUMN_HEADER_LAYOUT_FILL_EQUAL = 1;
    
    /**  TODO */
    public static final int COLUMN_HEADER_LAYOUT_FILL  = 2;

    /**  TODO */
    public static final int ROW_HEADER_LAYOUT_DEFAULT       = 0;
    
    /**  TODO */
    public static final int ROW_HEADER_LAYOUT_FILL          = 1;

    /**  TODO */
    public static final int STYLE_TABLE                     = 1;
    
    /**  TODO */
    public static final int STYLE_GRID                      = 2;

    /**  TODO */
    private final int       style;
    
    /**  TODO */
    private int             horizontalAlignment             = SWT.CENTER;
    
    /**  TODO */
    private int             columnHeaderLayout              = CTConfiguration.COLUMN_HEADER_LAYOUT_DEFAULT;
    
    /**  TODO */
    private int             rowHeaderLayout                 = CTConfiguration.ROW_HEADER_LAYOUT_DEFAULT;
    
    /**  TODO */
    private boolean         rowSelectionEnabled             = false;
    
    /**  TODO */
    private boolean         columnSelectionEnabled          = true;
    
    /**  TODO */
    private boolean         cellSelectionEnabled            = true;
    
    /**  TODO */
    private Font            font;

    /**
     * Creates a new table.
     * @param parent
     */
    public CTConfiguration(Control parent){
        this.font = parent.getFont();
        this.style = STYLE_TABLE;
    }
    
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
     * 
     *
     * @return
     */
    public int getColumnHeaderLayout() {
        return columnHeaderLayout;
    }

    /**
     * 
     *
     * @return
     */
    public Font getFont() {
        return font;
    }

    /**
     * 
     *
     * @return
     */
    public int getHorizontalAlignment() {
        return horizontalAlignment;
    }

    /**
     * 
     *
     * @return
     */
    public int getRowHeaderLayout() {
        return rowHeaderLayout;
    }

    /**
     * 
     *
     * @return
     */
    public int getStyle() {
        return style;
    }

    /**
     * 
     *
     * @return
     */
    public boolean isCellSelectionEnabled() {
        return cellSelectionEnabled;
    }

    /**
     * 
     *
     * @return
     */
    public boolean isColumnSelectionEnabled() {
        return columnSelectionEnabled;
    }

    /**
     * 
     *
     * @return
     */
    public boolean isRowSelectionEnabled() {
        return rowSelectionEnabled;
    }

    /**
     * 
     *
     * @param cellSelectionEnabled
     */
    public void setCellSelectionEnabled(boolean cellSelectionEnabled) {
        this.cellSelectionEnabled = cellSelectionEnabled;
    }

    /**
     * 
     *
     * @param columnHeaderLayout
     */
    public void setColumnHeaderLayout(int columnHeaderLayout) {
        this.columnHeaderLayout = columnHeaderLayout;
    }

    /**
     * 
     *
     * @param columnSelectionEnabled
     */
    public void setColumnSelectionEnabled(boolean columnSelectionEnabled) {
        this.columnSelectionEnabled = columnSelectionEnabled;
    }

    /**
     * 
     *
     * @param font
     */
    public void setFont(Font font) {
        this.font = font;
    }

    /**
     * 
     *
     * @param horizontalAlignment
     */
    public void setHorizontalAlignment(int horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    /**
     * 
     *
     * @param rowHeaderLayout
     */
    public void setRowHeaderLayout(int rowHeaderLayout) {
        this.rowHeaderLayout = rowHeaderLayout;
    }

    /**
     * 
     *
     * @param rowSelectionEnabled
     */
    public void setRowSelectionEnabled(boolean rowSelectionEnabled) {
        this.rowSelectionEnabled = rowSelectionEnabled;
    }
}
