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

package org.deidentifier.arx.io;


/**
 * Interface to be implemented when columns can be referred to by a name.
 *
 * @author Karol Babioch
 * @author Fabian Prasser
 */
interface IImportColumnNamed {

    /**
     * Returns the name this column refers to.
     *
     * @return
     */
    String getName();

    /**
     * Sets the name this columns refers to.
     *
     * @param name
     */
    void setName(String name);

}
