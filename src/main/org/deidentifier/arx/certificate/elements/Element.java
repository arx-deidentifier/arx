package org.deidentifier.arx.certificate.elements;

import org.deidentifier.arx.certificate.ARXDocumentStyle;

import rst.pdfbox.layout.elements.Document;

/**
 * An abstract element
 * 
 * @author Fabian Prasser
 */
public interface Element {
    
    /**
     * Renders the element to the given target
     * @param target
     * @param style
     */
    public abstract void render(Document target, ARXDocumentStyle style);
}
