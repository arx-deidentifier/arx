/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
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
package org.deidentifier.arx.certificate.elements;

import org.apache.pdfbox.pdmodel.font.PDType1Font;

import rst.pdfbox.layout.util.Enumerator;
import static rst.pdfbox.layout.util.Enumerators.*;

/**
 * PDF ordered list
 * 
 * @author Annika Saken
 * @author Fabian Prasser
 */
public class ElementListOrdered extends ElementListAbstract {
    final Enumerator enumerator;
    final String seperator;
    public enum Style {
        ARABIC, ROMAN, ALPHABETICAL, LOWERCASE_ALPHABETICAL
    }

    /** Constructor which provides different enumerators and separators by style
     * @param style
     */
    public ElementListOrdered(Style style) {
        super();
        switch (style) {
        case ROMAN:
            enumerator = new RomanEnumerator();
            seperator = ". ";
            break;
        case ALPHABETICAL:
            enumerator = new AlphabeticEnumerator();
            seperator = ") ";
            break;
        case LOWERCASE_ALPHABETICAL:
            enumerator = new LowerCaseAlphabeticEnumerator();
            seperator = ") ";
            break;
        default:
            enumerator = new ArabicEnumerator();
            seperator = ". ";
            break;
        }
    }

    @Override
    public String getListItemIndicator(int deep) {
        return enumerator.next() +  seperator;
    }

    @Override
    protected PDType1Font getListItemIndicatorFont() {
        return null;
    }
}
