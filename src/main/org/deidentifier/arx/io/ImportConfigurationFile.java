/*
 * ARX: Powerful Data Anonymization
 * Copyright 2014 Karol Babioch <karol@babioch.de>
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

    /** Location of file. */
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
