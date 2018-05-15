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

import java.io.IOException;
import java.util.HashMap;

import org.apache.pdfbox.multipdf.Overlay;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * Class for accessing the water mark
 * 
 * @author Fabian Prasser
 */
public class Watermark {
    
    /** Watermark */
    private PDDocument watermark;
    
    /**
     * Creates a new instance
     * @param document
     * @throws IOException
     */
    public Watermark(PDDocument document) throws IOException {
        this.watermark = PDDocument.load(Watermark.class.getResourceAsStream("watermark.pdf"));
    }
    
    /**
     * Marks the document
     * @param document
     * @throws IOException
     */
    public void mark(PDDocument document) throws IOException {   
        
        Overlay overlay = new Overlay();
        overlay.setInputPDF(document);
        overlay.setAllPagesOverlayPDF(watermark);
        overlay.setOverlayPosition(Overlay.Position.BACKGROUND);
        overlay.overlay(new HashMap<Integer, String>());
    }
}
