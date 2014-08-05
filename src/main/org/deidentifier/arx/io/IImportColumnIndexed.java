/*
 * ARX: Powerful Data Anonymization
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
 * Interface to be implemented when columns can be referred to by an index
 * 
 * @author Karol Babioch
 * @author Fabian Prasser
 */
public interface IImportColumnIndexed {

    /**
     * Returns the index this column refers to
     */
    public int getIndex();

    /**
     * Sets the index this columns refers to
     */
    public void setIndex(int index);

}
