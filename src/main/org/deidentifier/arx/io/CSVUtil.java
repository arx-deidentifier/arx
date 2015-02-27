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
 * The Class CSVUtil.
 * @author Florian Kohlmayer
 */
public class CSVUtil {

    /** Default values. */
    protected static final char   DEFAULT_DELIMITER = ';';

    /** Default values. */
    protected static final char   DEFAULT_QUOTE     = '\"';

    /** Default values. */
    protected static final char   DEFAULT_ESCAPE    = '\"';

    /** Default values. */
    protected static final char[] DEFAULT_LINEBREAK = { '\n' };

    /**
     * Gets the normalized line break character.
     *
     * @param linebreak the line break
     * @return the normalized line break character
     */
    protected static char getNormalizedLinebreakCharacter(char[] linebreak) {
        if (linebreak[0] == '\n') {
            return '\n';
        } else if ((linebreak[0] == '\r') && (linebreak.length < 2)) {
            return '\r';
        } else {
            return '\n';
        }
    }
}
