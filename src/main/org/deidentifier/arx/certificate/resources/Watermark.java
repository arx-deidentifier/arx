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
package org.deidentifier.arx.certificate.resources;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * Class for accessing the water mark
 * 
 * @author Fabian Prasser
 */
public class Watermark {
    
    /** Image*/
    private BufferedImage image;
    
    /**
     * Creates a new instance
     * @param document
     * @throws IOException
     */
    public Watermark(PDDocument document) throws IOException {
        BufferedImage tmp_image = ImageIO.read(Watermark.class.getResourceAsStream("watermark.png"));
        BufferedImage image = new BufferedImage(tmp_image.getWidth(), tmp_image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);        
        image.createGraphics().drawRenderedImage(tmp_image, null);
    }
    
    @SuppressWarnings("deprecation")
    public void mark(PDDocument document, int page) throws IOException {   

        PDPage pdPage = (PDPage)document.getDocumentCatalog().getPages().get(page);
        PDImageXObject xImage = LosslessFactory.createFromImage(document, image);
        PDPageContentStream contentStream = new PDPageContentStream(document, pdPage, true, true);
        contentStream.drawXObject(xImage, 10, 10, xImage.getWidth(), xImage.getHeight());
        contentStream.close();
    }
}
