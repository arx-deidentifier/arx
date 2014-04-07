/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2014 Karol Babioch <karol@babioch.de>
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

package org.deidentifier.arx.io;


/**
 * Interface for data configurations that can contain a header
 *
 * A header describes the columns itself, e.g. by naming them. Usually it will
 * be the first row, but there might be more complex configurations.
 *
 * TODO: Add getHeader() method and implement it for CSV and XLS
 */
public interface IDataSourceCanContainHeader {

    /**
     * Indicates whether there is header
     *
     * A header is not necessarily mandatory. This returns a boolean value that
     * describes whether or not the configuration contains a header or not.
     *
     * @return True if there is header, false otherwise
     */
    public boolean getContainsHeader();

    /**
     * @param containsHeader Whether or not a header is contained
     */
    public void setContainsHeader(boolean containsHeader);

}
