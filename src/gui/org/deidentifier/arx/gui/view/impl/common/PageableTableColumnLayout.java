/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2020 Fabian Prasser and contributors
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
package org.deidentifier.arx.gui.view.impl.common;

import org.eclipse.nebula.widgets.pagination.table.PageableTable;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Column layout for pageable tables
 * 
 * @author Fabian Prasser
 */
public class PageableTableColumnLayout extends ControlAdapter {

    /** Parent */
    private Composite parent;
    /** Table */
    private Table     table;
    /** Equal size */
    private boolean   equalSize;

    /**
     * Creates a new instance
     * 
     * @param pageableTable
     * @param fill
     * @param equalSize
     */
    public PageableTableColumnLayout(PageableTable pageableTable, boolean fill, boolean equalSize) {
        this.parent = pageableTable.getViewer().getTable().getParent();
        this.table = pageableTable.getViewer().getTable();
        this.equalSize = equalSize;
        if (fill) {
            pageableTable.getViewer().getTable().getParent().addControlListener(this);
        }
    }

    @Override
    public void controlResized(ControlEvent arg0) {
        
        // Table columns
        TableColumn[] columns = table.getColumns();

        // Calculate
        Rectangle area = parent.getClientArea();
        int width = area.width - table.computeTrim(0, 0, 0, 0).width;
        int height = area.height - table.computeTrim(0, 0, 0, 0).height;
        
        // Detect whether the scroll bar will become visible
        int itemHeight = table.getItemHeight();
        int headerHeight = table.getHeaderHeight();
        int visibleCount = (height - headerHeight + itemHeight - 1) / itemHeight;
        boolean scrollBarVisible = table.getItemCount() >= visibleCount;
        

        // Subtract the scroll bar width from the total column
        // width if a vertical scroll bar will be required
        if (scrollBarVisible) {
            width -= table.getVerticalBar().getSize().x;
        }
        int columnWidth = width / (columns.length);

        Point oldSize = table.getSize();

        // Adjust
        if (oldSize.x > area.width) {

            // Table is getting smaller so make the columns
            // smaller first and then resize the table to
            // match the client area width
            setEqualColumnSize(columns, width, columnWidth, equalSize);
            table.setSize(area.width, area.height);

        } else {

            // Table is getting bigger so make the table
            // bigger first and then make the columns wider
            // to match the client area width
            table.setSize(area.width, area.height);
            setEqualColumnSize(columns, width, columnWidth, equalSize);
        }
    }

    /**
     * Sets the column width
     * 
     * @param columns
     * @param width
     * @param columnWidth
     * @param equalSize
     */
    private void setEqualColumnSize(TableColumn[] columns,
                                    int width,
                                    int columnWidth,
                                    boolean equalSize) {
        if (equalSize) {
            for (int i = 0; i < columns.length; i++) {
                if (i < columns.length - 1) {
                    columns[i].setWidth(columnWidth);
                } else {
                    columns[i].setWidth(width - columnWidth * (columns.length - 1));
                }
            }
        } else {
            int sum = 0;
            for (int i = 0; i < columns.length - 1; i++) {
                sum += columns[i].getWidth();
            }
            columns[columns.length - 1].setWidth(width - sum);
        }
    }
}
