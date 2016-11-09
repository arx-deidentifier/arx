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

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.deidentifier.arx.certificate.ARXDocumentStyle;

import rst.pdfbox.layout.elements.Document;
import rst.pdfbox.layout.elements.Paragraph;
import rst.pdfbox.layout.text.BaseFont;

/**
 * PDF text element
 * 
 * @author Annika Saken
 * @author Fabian Prasser
 */
public class ElementText implements Element {

    /**
     * Style
     * 
     * @author Fabian Prasser
     */
    public static enum TextStyle {
       PLAIN, 
       BOLD,
       ITALIC
    }
    
    /** Style*/
    private final TextStyle style;

    /** Field*/
    private final String text;
    /**
     * Creates a new text
     * @param text
     */
    public ElementText(String text) {
        this(text, TextStyle.PLAIN);
    }

    /**
     * Creates a new text
     * @param text
     */
    public ElementText(String text, TextStyle style) {
        this.text = text;
        this.style = style;
    }
    
    @Override
    public void render(Document target, int indent, ARXDocumentStyle style) throws IOException {
        Paragraph paragraph = new Paragraph();
        paragraph.addText(text, style.getTextSize(), getFont(style));
        target.add(paragraph);
    }

    /**
     * Returns a styled instance
     * @param font
     * @param style
     * @return
     */
    protected PDFont style(BaseFont font, TextStyle style) {
        switch (style) {
        case PLAIN: return font.getPlainFont();
        case BOLD: return font.getBoldFont();
        case ITALIC: return font.getItalicFont();
        default: return font.getPlainFont();
        }
    }
    
    /**
     * Returns the font
     * @param style
     * @return
     */
    PDFont getFont(ARXDocumentStyle style) {
        return this.style(style.getTextFont(), this.style);
    }
    
    /**
     * Returns the text
     * @return
     */
    String getText() {
        return this.text;
    }
}
