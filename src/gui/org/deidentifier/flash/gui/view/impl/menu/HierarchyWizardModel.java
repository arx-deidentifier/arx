/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.flash.gui.view.impl.menu;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.flash.AttributeType.Hierarchy;
import org.deidentifier.flash.DataType;

public class HierarchyWizardModel {

    private final int          rows;
    private List<Integer>      fanout;
    private final List<String> items;
    private Hierarchy          hierarchy;
    private final String       attribute;
    private final DataType     datatype;
    private final String       suppressionString;

    public HierarchyWizardModel(final String attribute,
                                final DataType datatype,
                                final String suppressionString,
                                final String[] items) {
        rows = items.length;
        this.items = new ArrayList<String>();
        for (final String s : items) {
            this.items.add(s);
        }
        this.attribute = attribute;
        this.datatype = datatype;
        this.suppressionString = suppressionString;
    }

    public String getAttribute() {
        return attribute;
    }

    public DataType getDataType() {
        return datatype;
    }

    public List<Integer> getFanout() {
        return fanout;
    }

    public Hierarchy getHierarchy() {
        return hierarchy;
    }

    public List<String> getItems() {
        return items;
    }

    public int getRows() {
        return rows;
    }

    public String getSuppressionString() {
        return suppressionString;
    }

    protected void setFanout(final List<Integer> vals) {
        fanout = vals;
    }

    protected void setHierarchy(final Hierarchy hierarchy) {
        this.hierarchy = hierarchy;
    }
}
