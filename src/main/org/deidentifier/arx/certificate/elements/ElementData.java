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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.certificate.ARXDocumentStyle;
import org.deidentifier.arx.certificate.ARXDocumentStyle.ListStyle;

import rst.pdfbox.layout.elements.Document;

/**
 * Complex element of data items
 * 
 * @author Fabian Prasser
 */
public class ElementData implements Element {
    
    /**
     * Property
     * 
     * @author Fabian Prasser
     */
    private class ElementDataProperty {
       
        /** Property*/
        public final String property;
        /** Value*/
        public final String value;
        
        /**
         * Creates a new instance
         * @param indent
         * @param text
         */
        ElementDataProperty(String property, String value) {
            this.property = property;
            this.value = value;
        }
    }
    
    /** List of elements*/
    private final List<ElementDataProperty> properties = new ArrayList<>();
    /** Text*/
    private final String text;
    
    /**
     * Creates a new instance
     * @param text
     */
    public ElementData(String text) {
        this.text = text;
    }
    
    /**
     * Adds a property
     * @param property
     * @param value
     */
    public void addProperty(String property, String value) {
        this.properties.add(new ElementDataProperty(property, value));
    }

    @Override
    public void render(Document target, int indent, ARXDocumentStyle style) throws IOException {
        ElementText text = new ElementText(this.text);
        text.render(target, indent, style);
        ElementList list = new ElementList(ListStyle.BULLETS);
        for (ElementDataProperty property : this.properties) {
            list.addItem(new ElementText(property.property + ": " + property.value));
        }
        list.render(target, indent, style);
    }
}
