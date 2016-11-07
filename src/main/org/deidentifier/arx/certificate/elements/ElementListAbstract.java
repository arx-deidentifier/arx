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

import rst.pdfbox.layout.elements.Paragraph;
import rst.pdfbox.layout.text.Alignment;
import rst.pdfbox.layout.text.BaseFont;
import rst.pdfbox.layout.text.Indent;
import rst.pdfbox.layout.text.SpaceUnit;

/**
 * PDF abstract list
 * 
 * @author Annika Saken
 * @author Fabian Prasser
 */
public abstract class ElementListAbstract extends Paragraph {

    protected abstract String getListItemIndicator(int deep);
    protected abstract PDFont getListItemIndicatorFont();

    protected ElementListAbstract parentList = null;
    protected int deep = 1;

    /**
     * Adds a list item to this list element
     * @param text
     * @param fontSize
     * @param font
     */
    public void addListItem(String text, float fontSize, BaseFont font) {
        try {
            float indent = 30 * deep;
            ElementListAbstract root = findRootList();
            root.add(new Indent(getListItemIndicator(deep), indent, SpaceUnit.pt, fontSize,
                    getListItemIndicatorFont() == null ? font.getPlainFont() : getListItemIndicatorFont(), Alignment.Right));
            root.addMarkup(text + '\n', fontSize, font);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets this list as parent list for an other list
     * @param list
     */
    public void addList(ElementListAbstract list) {
        list.parentList = this; 
        list.deep = this.deep + 1;
    }

    /** 
     * Finds the root list of a list  
     * @return  the list that is the root parent
     */
    protected ElementListAbstract findRootList() {
        ElementListAbstract list = this;
        while(list.parentList != null)
            list = list.parentList;
        return list;
    }
}
