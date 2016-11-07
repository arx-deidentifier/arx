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
import rst.pdfbox.layout.text.BaseFont;
import rst.pdfbox.layout.text.DrawListener;
import rst.pdfbox.layout.text.Position;

/**
 * PDF table row
 * 
 * @author Annika Saken
 * @author Fabian Prasser
 */
public class ElementTableRow implements Element, Drawable {
    private List<ElementTableCell> cells = new ArrayList<ElementTableCell>();

    float maxHeight = 0;
    Position position;

    /**
     * Adds a cell to the cells of a row
     * @param cell
     */
    public void addCell(ElementTableCell cell)  {
        cells.add(cell);
        try {
            if(cell.getHeight() > maxHeight)
                maxHeight = cell.getHeight();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void draw(PDDocument pdDocument, PDPageContentStream contentStream, Position rowPosition,
            DrawListener drawListener) throws IOException {
        float pos = rowPosition.getX();

        for(ElementTableCell cell: cells) {
            cell.addText(" ", 11f, BaseFont.Helvetica.getPlainFont());;
            position = new Position(pos +2, rowPosition.getY());
            contentStream.addRect(position.getX()-2, position.getY()  - cell.getHeight(), cell.getMaxWidth(), cell.getHeight());
            contentStream.stroke();
            cell.draw(pdDocument, contentStream, position, drawListener);
            pos += cell.getMaxWidth();
        }
    }

    @Override
    public Position getAbsolutePosition() throws IOException {
        return null;
    }
    
    @Override
    public float getHeight() throws IOException {
        return maxHeight;
    }

    @Override
    public float getWidth() throws IOException {
        return 500;
    }
    
    @Override
    public Drawable removeLeadingEmptyVerticalSpace() throws IOException {
        return null;
    }
}