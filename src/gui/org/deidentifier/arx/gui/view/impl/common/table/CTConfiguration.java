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
