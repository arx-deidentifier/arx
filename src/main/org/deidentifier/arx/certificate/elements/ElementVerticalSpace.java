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

import org.deidentifier.arx.certificate.ARXDocumentStyle;

import rst.pdfbox.layout.elements.Document;
import rst.pdfbox.layout.elements.VerticalSpacer;

/**
 * PDF page break
 * 
 * @author Fabian Prasser
 */
public class ElementVerticalSpace implements Element {
    
    /** Space*/
    private final int space;
    
    /**
     * Creates a new instance
     * @param space
     */
    public ElementVerticalSpace(int space) {
        this.space = space;
    }

    @Override
    public void render(Document target, int indent, ARXDocumentStyle style) throws IOException {
        target.add(new VerticalSpacer(space));
    }
}
