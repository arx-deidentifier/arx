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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.certificate.elements.Element;

import rst.pdfbox.layout.elements.Document;

/**
 * A PDF document
 * 
 * @author Annika Saken
 * @author Fabian Prasser
 */
public class ARXDocument {

    /** The document style */
    private final ARXDocumentStyle style;
 
    /** Elements*/
    private final List<Element> elements = new ArrayList<Element>();

	/**
     * Creates a new instance
     * @param style
     */
	public ARXDocument(ARXDocumentStyle style) {
        this.style = style;

	}
	
	/**
	 * Adds a new element
	 * @param element
	 */
	public void addElement(Element element) {
	    this.elements.add(element);
	}
	
	/**
	 * Renders the document into the given output stream
	 * 
	 * @param stream
	 * @throws IOException 
	 */
	public void render(OutputStream stream) throws IOException {
	    
        // Render
        Document document = new Document(style.gethMargin(), style.gethMargin(), style.getvMargin(), style.getvMargin());
        for (Element element : this.elements) {
            element.render(document, 0, this.style);
        }
        
        // Save
        document.save(stream);
	}
	
//  /**
//	 * Renders the document
//	 * @throws IOException 
//	 */
//   public void render() throws IOException {
//        
//        // Open
//        if (Desktop.isDesktopSupported()) {
//            Desktop.getDesktop().open(file);
//        }
//    }
}
