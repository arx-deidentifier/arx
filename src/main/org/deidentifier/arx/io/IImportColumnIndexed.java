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
 * Interface to be implemented when columns can be referred to by an index.
 *
 * @author Karol Babioch
 * @author Fabian Prasser
 */
public interface IImportColumnIndexed {

    /**
     * Returns the index this column refers to.
     *
     * @return
     */
    public int getIndex();

    /**
     * Sets the index this columns refers to.
     *
     * @param index
     */
    public void setIndex(int index);

}
