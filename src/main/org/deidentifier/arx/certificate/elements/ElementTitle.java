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

import org.deidentifier.arx.certificate.CertificateStyle;

import rst.pdfbox.layout.elements.Document;
import rst.pdfbox.layout.elements.Paragraph;
import rst.pdfbox.layout.elements.render.VerticalLayoutHint;

/**
 * PDF title element
 * 
 * @author Annika Saken
 * @author Fabian Prasser
 */
public class ElementTitle implements Element {
    
    /** Field*/
    private final String text;
    
    /**
     * Creates a new title
     * @param text
     */
    public ElementTitle(String text) {
        this.text = text;
    }

    @Override
    public void render(Document target, int indent, CertificateStyle style) throws IOException {
        Paragraph paragraph = new Paragraph() {
            @Override public float getHeight() throws IOException {
                return super.getHeight() * 2f;
            }
        };
        paragraph.setAlignment(style.getTitleAlignment());
        paragraph.addText(text, style.getTitleSize(), style.getTitleFont().getBoldFont());
        target.add(paragraph, VerticalLayoutHint.LEFT);
    }
}
