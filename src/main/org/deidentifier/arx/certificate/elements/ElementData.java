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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.certificate.CertificateStyle;
import org.deidentifier.arx.certificate.CertificateStyle.ListStyle;

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
        public final Object value;
        
        /**
         * Creates a new instance
         * @param indent
         * @param text
         */
        ElementDataProperty(String property, Object value) {
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
     * Adds an item
     * @param item
     */
    public ElementData addItem(String item) {
        this.properties.add(new ElementDataProperty(item, null));
        return this;
    }

    /**
     * Adds a property
     * @param property
     * @param value
     */
    public ElementData addProperty(String property, Anonymity value) {
        switch (value) {
        case ANONYMOUS: 
            this.addProperty(property, true);
            return this;
        case NOT_ANONYMOUS:
            this.addProperty(property, false);
            return this;
        default:
            this.addProperty(property, "Unknown");
            return this;
        }
    }

    /**
     * Adds a property
     * @param property
     * @param value
     */
    public ElementData addProperty(String property, boolean value) {
        this.properties.add(new ElementDataProperty(property, value ? "Yes" : "No"));
        return this;
    }

    /**
     * Adds a property
     * @param property
     * @param value
     */
    public ElementData addProperty(String property, double value) {
        this.properties.add(new ElementDataProperty(property, String.valueOf(value)));
        return this;
    }

    /**
     * Adds a property
     * @param property
     * @param value
     */
    public ElementData addProperty(String property, int value) {
        this.properties.add(new ElementDataProperty(property, String.valueOf(value)));
        return this;
    }

    /**
     * Adds a property
     * @param property
     * @param value
     */
    public ElementData addProperty(String property, long value) {
        this.properties.add(new ElementDataProperty(property, String.valueOf(value)));
        return this;
    }
    
    /**
     * Adds a property
     * @param property
     * @param value
     */
    public ElementData addProperty(String property, String value) {
        if (value == null || value.isEmpty()) {
            this.properties.add(new ElementDataProperty(property, "Not specified"));
        } else {
            this.properties.add(new ElementDataProperty(property, value));    
        }
        return this;
    }

    /**
     * Adds a property
     * @param property
     * @param value
     */
    public ElementData addProperty(String property, ElementData value) {
        this.properties.add(new ElementDataProperty(property, value));
        return this;
    }
    
    /**
     * Adds a number between 0 and 1 as precentage
     * @param property
     * @param value
     */
    public ElementData addPercentProperty(String property, double value) {
        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        percentFormat.setMaximumFractionDigits(1);
        this.properties.add(new ElementDataProperty(property, percentFormat.format(value)+"%"));
        return this;
    }

    /**
     * Returns a list
     * @return
     */
    public ElementList asList() {
        return asList(null);
    }
    
    /**
     * Returns a list
     * @param prefix
     * @return
     */
    public ElementList asList(String prefix) {
        ElementList master = new ElementList(ListStyle.BULLETS);
        master.addItem(new ElementText((prefix == null ? "" : prefix + ": ") + text));
        ElementList list = new ElementList(ListStyle.BULLETS);
        for (ElementDataProperty property : this.properties) {
            if (property.value == null) {
                list.addItem(new ElementText(property.property));
            } else if (property.value instanceof String){
                list.addItem(new ElementText(property.property + ": " + property.value));
            } else if (property.value instanceof ElementData) {
                list.addItem(((ElementData)property.value).asList(property.property));
            }
        }
        master.addItem(list);
        return master;
    }

    /**
     * Returns the text
     * @return
     */
    public String getText() {
        return this.text;
    }

    @Override
    public void render(Document target, int indent, CertificateStyle style) throws IOException {
        this.asList().render(target, indent, style);
    }
}
