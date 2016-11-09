package org.deidentifier.arx.certificate.elements;

import java.io.IOException;

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
     * @param indent
     * @param style
     * @throws IOException 
     */
    public abstract void render(Document target, int indent, ARXDocumentStyle style) throws IOException;
    
}
