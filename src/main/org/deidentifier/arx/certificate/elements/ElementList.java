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
import rst.pdfbox.layout.elements.Paragraph;
import rst.pdfbox.layout.text.Alignment;
import rst.pdfbox.layout.text.BaseFont;
import rst.pdfbox.layout.text.Indent;
import rst.pdfbox.layout.text.SpaceUnit;
import rst.pdfbox.layout.util.CompatibilityHelper;
import rst.pdfbox.layout.util.Enumerator;
import rst.pdfbox.layout.util.Enumerators.AlphabeticEnumerator;
import rst.pdfbox.layout.util.Enumerators.ArabicEnumerator;
import rst.pdfbox.layout.util.Enumerators.LowerCaseAlphabeticEnumerator;
import rst.pdfbox.layout.util.Enumerators.RomanEnumerator;

/**
 * PDF list
 * 
 * @author Fabian Prasser
 */
public class ElementList implements Element {

    /** Style */
    private final ListStyle     style;
    /** Elements */
    private final List<Element> items      = new ArrayList<Element>();
    /** Style */
    private final String        bulletOdd  = CompatibilityHelper.getBulletCharacter(1) + " ";
    /** Style */
    private final String        bulletEven = CompatibilityHelper.getBulletCharacter(2) + " ";
    /** Style */
    private final Enumerator    enumerator;
    /** Style */
    private final String        separator;

    /**
     * Creates a new list
     * @param style
     */
    public ElementList(ListStyle style) {
        this.style = style;
        switch (style) {
        case ROMAN:
            enumerator = new RomanEnumerator();
            separator = ". ";
            break;
        case ALPHABETICAL:
            enumerator = new AlphabeticEnumerator();
            separator = ") ";
            break;
        case LOWERCASE_ALPHABETICAL:
            enumerator = new LowerCaseAlphabeticEnumerator();
            separator = ") ";
            break;
        default:
            enumerator = new ArabicEnumerator();
            separator = ". ";
            break;
        }
    }
    
    /**
     * Adds an item to this list
     * @param item
     */
    public void addItem(ElementText item) {
        this.items.add(item);
    }

    /**
     * Adds an item to this list
     * @param item
     */
    public void addItem(ElementList item) {
        this.items.add(item);
    }

    @Override
    public void render(Document target, int indent, ARXDocumentStyle style) throws IOException {
        
        int offset = indent * style.getListIndent();
        for (Element item : items) {
            if (item instanceof ElementList) {
                ((ElementList)item).render(target, indent+1, style);
            } else if (item instanceof ElementText) {
                Paragraph paragraph = new Paragraph();
                paragraph.add(new Indent(getListItemIndicator(indent), offset, SpaceUnit.pt, style.getTextSize(),
                                    getListItemIndicatorFont(style).getPlainFont(), Alignment.Right));
                paragraph.addMarkup(((ElementText)item).getText(), style.getTextSize(), style.getTextFont());
                target.add(paragraph);
            } else {
                throw new IllegalStateException("Unsupported element");
            }
        }

    }

    /**
     * Styling
     * @param deep
     * @return
     */
    public String getListItemIndicator(int indent) {
        switch (style) {
            case BULLETS: return indent % 2 == 1 ? bulletOdd : bulletEven;
            default: return enumerator.next() +  separator;
        }
    }
    
    /**
     * Styling
     * @return
     */
    protected BaseFont getListItemIndicatorFont(ARXDocumentStyle documentStyle) {
        switch (style) {
            case BULLETS: return BaseFont.Times;
            default: return documentStyle.getTextFont();
        }
    }
}
