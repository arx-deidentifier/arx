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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import rst.pdfbox.layout.elements.Drawable;
import rst.pdfbox.layout.elements.Element;
import rst.pdfbox.layout.text.DrawListener;
import rst.pdfbox.layout.text.Position;

/**
 * PDF table
 * 
 * @author Annika Saken
 * @author Fabian Prasser
 */
public class ElementTable implements Element, Drawable {
	private List<ElementTableRow> rows = new ArrayList<ElementTableRow>();

	private float width;
	private float height;

	public void addElementTableRow(ElementTableRow row) {
		rows.add(row);
	}

	@Override
	public void draw(PDDocument pdDocument, PDPageContentStream pageContentStream, Position tablePosition,
			DrawListener drawListener) throws IOException {
		float pos = tablePosition.getY();
		height = 0;
		for(ElementTableRow row: rows) {
			Position position = new Position(tablePosition.getX(), pos );
			row.draw(pdDocument, pageContentStream, position, drawListener);
			pos -= row.getHeight();
			height+= row.getHeight();
		}
	}

	@Override
	public Position getAbsolutePosition() throws IOException {
		return null;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	@Override
	public float getHeight() throws IOException {
		return height;
	}

	@Override
	public float getWidth() throws IOException {
		return width;
	}

	@Override
	public Drawable removeLeadingEmptyVerticalSpace() throws IOException {
		return null;
	}
}
