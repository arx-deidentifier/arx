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

package org.deidentifier.arx.risk.hipaa;

/**
 * Provides information about the occurrence of an HIPPA identifier
 * @author David Gaﬂmann
 */
public class Warning{
    private int column, row;
    private Identifier identifier;
    private String value;

    public Warning(int column, int row, Identifier identifier, String value){
        this.column = column;
        this.row = row;
        this.identifier = identifier;
        this.value = value;
    }


    /**
     * The row number where the identifier was found
     * @return The row number
     */
    public int getRow(){
        return this.row;
    }

    /**
     * The column number where the identifier was found
     * @return The column number
     */
    public int getColumn(){
        return this.column;
    }

    /**
     * The identifier which was found
     * @return The found identifier
     */
    public Identifier getIdentifier() { return this.identifier; }

    /**
     * The value which caused the warning
     * @return The value which caused the warning
     */
    public String getValue(){
        return this.value;
    }

    @Override
    public String toString(){
        return String.format("Column: %d\tRow: %d\tCategory: %s\tValue: %s", this.getColumn(), this.getRow(), this.getIdentifier(), this.getValue());
    }
}