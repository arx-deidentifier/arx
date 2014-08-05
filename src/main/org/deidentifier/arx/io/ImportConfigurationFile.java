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
 * Configuration describing a file in general
 *
 * File based configurations should extend this class as the notion of a
 * {@link #fileLocation} is common to all of them.
 * 
 * @author Karol Babioch
 * @author Fabian Prasser
 */
abstract public class ImportConfigurationFile extends ImportConfiguration {

    /**
     * Location of file
     */
    private String fileLocation;


    /**
     * @return {@link #fileLocation}
     */
    public String getFileLocation() {
        return fileLocation;
    }

    /**
     * @param fileLocation {@link #fileLocation}
     */
    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }
}
