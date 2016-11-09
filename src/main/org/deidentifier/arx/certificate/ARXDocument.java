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

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.certificate.ARXDocumentStyle.ListStyle;
import org.deidentifier.arx.certificate.elements.Element;
import org.deidentifier.arx.certificate.elements.ElementList;
import org.deidentifier.arx.certificate.elements.ElementSubtitle;
import org.deidentifier.arx.certificate.elements.ElementText;
import org.deidentifier.arx.certificate.elements.ElementTitle;

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
	 * Renders the document
	 * @throws IOException 
	 */
    public void render() throws IOException {
        
        // Render
        Document document = new Document(style.gethMargin(), style.gethMargin(), style.getvMargin(), style.getvMargin());
        for (Element element : this.elements) {
            element.render(document, 0, this.style);
        }
        
        // Save
        File file = File.createTempFile("arx", String.valueOf(System.currentTimeMillis()));
        OutputStream outputStream = new FileOutputStream(file);
        document.save(outputStream);
        
        // Open
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file);
        }
    }
	
//
//	@Override
//	public void add(Element element) {
//
//	    
//		else if(element instanceof ElementTable) {
//			((ElementTable) element).setWidth(getPageWidth()-2*vMargin);
//			super.add(element);
//			try {
//				Paragraph paragraph = new Paragraph();
//				paragraph.addText("\n", 11,
//						PDType1Font.HELVETICA);
//				add(paragraph);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		else
//			super.add(element);
//	}
//
    
    public static void main(String[] args) throws IOException {
        ARXDocument document = new ARXDocument(ARXDocumentStyle.create());
        document.addElement(new ElementTitle("Title"));
        document.addElement(new ElementSubtitle("Subtitle"));
        document.addElement(new ElementText("Text1"));
        document.addElement(new ElementText("Text2. Lore ipsum. . Lore ipsum.\n\n. Lore ipsum."));
        
        ElementList list = new ElementList(ListStyle.ARABIC);
        list.addItem(new ElementText("Item1"));
        list.addItem(new ElementText("Item2"));
        list.addItem(new ElementText("Item3"));
        list.addItem(new ElementText("Item4"));
        
        ElementList list2 = new ElementList(ListStyle.ALPHABETICAL);
        list2.addItem(new ElementText("Item1"));
        list2.addItem(new ElementText("Item2"));
        list2.addItem(list);
        list2.addItem(new ElementText("Item4"));
        list2.addItem(new ElementText("Item5"));
        
        document.addElement(list2);
        document.addElement(new ElementText("Text3"));
        document.render();
    }
}
