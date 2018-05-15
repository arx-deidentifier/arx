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
 * Interface for data configurations that can contain a header
 *
 * A header describes the columns itself, e.g. by naming them. Usually it will
 * be the first row, but there might be more complex configurations.
 * 
 * @author Karol Babioch
 * @author Fabian Prasser
 */
interface IImportConfigurationWithHeader {

    /**
     * Indicates whether there actually is a header
     *
     * A header is not necessarily mandatory. This returns a boolean value that
     * describes whether or not the configuration actually contains a header or
     * not.
     *
     * @return True if there actually is a header, false otherwise
     */
    boolean getContainsHeader();

    /**
     * @param containsHeader Whether or not a header is actually contained
     */
    void setContainsHeader(boolean containsHeader);

}
