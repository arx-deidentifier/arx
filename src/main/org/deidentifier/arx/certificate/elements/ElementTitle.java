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

import rst.pdfbox.layout.elements.Paragraph;
import rst.pdfbox.layout.text.Alignment;
import rst.pdfbox.layout.text.BaseFont;

/**
 * PDF title element
 * 
 * @author Annika Saken
 * @author Fabian Prasser
 */
public class ElementTitle extends Paragraph {
    
	private static final float TEXT_SIZE = 16;

	/**
	 * Constructor for a centered Title in TEXT_SIZE
	 * @param text 
	 */
	public ElementTitle(String text) {
		setAlignment(Alignment.Center);
		try {
			addText(text, TEXT_SIZE, BaseFont.Helvetica.getBoldFont());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public float getHeight() throws IOException {
		return super.getHeight() * 2f;
	}
}
