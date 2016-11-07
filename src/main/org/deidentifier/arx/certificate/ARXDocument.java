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
package org.deidentifier.arx.certificate;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.deidentifier.arx.certificate.elements.ElementTable;
import org.deidentifier.arx.certificate.elements.ElementTitle;

import rst.pdfbox.layout.elements.ControlElement;
import rst.pdfbox.layout.elements.Document;
import rst.pdfbox.layout.elements.Element;
import rst.pdfbox.layout.elements.Paragraph;
import rst.pdfbox.layout.elements.render.VerticalLayoutHint;

/**
 * @author Annika Saken
 *
 */
public class ARXDocument extends Document {

    private final ARXDocumentStyle style;
    
	public ARXDocument(ARXDocumentStyle style) {
		super(style.gethMargin(), style.gethMargin(), style.getvMargin(), style.getvMargin());
		this.style = style;

	}

	@Override
	public void add(Element element) {
		if(element instanceof ElementTitle)
			super.add(element, VerticalLayoutHint.CENTER);
		else if(element instanceof ElementTable) {
			((ElementTable) element).setWidth(getPageWidth()-2*vMargin);
			super.add(element);
			try {
				Paragraph paragraph = new Paragraph();
				paragraph.addText("\n", 11,
						PDType1Font.HELVETICA);
				add(paragraph);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else
			super.add(element);
	}

    
}
