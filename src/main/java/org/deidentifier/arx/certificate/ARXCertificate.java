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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.certificate.CertificateStyle.ListStyle;
import org.deidentifier.arx.certificate.elements.Element;
import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.certificate.elements.ElementList;
import org.deidentifier.arx.certificate.elements.ElementNewLine;
import org.deidentifier.arx.certificate.elements.ElementSubtitle;
import org.deidentifier.arx.certificate.elements.ElementTitle;
import org.deidentifier.arx.certificate.resources.Watermark;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.io.CSVDataChecksum;
import org.deidentifier.arx.io.CSVSyntax;

import rst.pdfbox.layout.elements.Document;

/**
 * A PDF document
 * 
 * @author Annika Saken
 * @author Fabian Prasser
 */
public class ARXCertificate { // NO_UCD

    /**
     * Creates a new instance
     * @param input
     * @param definition
     * @param config
     * @param result
     * @param transformation
     * @param output
     */
    public static ARXCertificate create(DataHandle input, DataDefinition definition,
                ARXConfiguration config, ARXResult result, ARXNode transformation, DataHandle output) {
        return ARXCertificate.create(input, definition, config, result, transformation, output, null);
    }

    /**
     * Renders the document into the given output stream.
     * Includes a SHA-256 checksum of the output data.
     * 
     * @param input
     * @param definition
     * @param config
     * @param result
     * @param transformation
     * @param output
     * @param syntax
     */
    public static ARXCertificate create(DataHandle input,
                                        DataDefinition definition,
                                        ARXConfiguration config,
                                        ARXResult result,
                                        ARXNode transformation,
                                        DataHandle output,
                                        CSVSyntax syntax) {
        return ARXCertificate.create(input, definition, config, result, transformation, output, syntax, null);
    }

    /**
     * Renders the document into the given output stream.
     * Includes a SHA-256 checksum of the output data and user defined metadata
     * 
     * @param input
     * @param definition
     * @param config
     * @param result
     * @param transformation
     * @param output
     * @param syntax
     * @param metadata
     */
    public static ARXCertificate create(DataHandle input,
                                        DataDefinition definition,
                                        ARXConfiguration config,
                                        ARXResult result,
                                        ARXNode transformation,
                                        DataHandle output,
                                        CSVSyntax syntax,
                                        ElementData metadata) {
        return new ARXCertificate(input, definition, config, result, transformation, output, syntax, metadata);
    }

    /** The document style */
    private final CertificateStyle style;
    /** Elements*/
    private final List<Element> elements = new ArrayList<Element>();
	
    /**
     * Creates a new instance
     * @param input
     * @param definition
     * @param config
     * @param result
     * @param transformation
     * @param output
     * @param csvConfig 
     * @param metadata
     */
    ARXCertificate(DataHandle input, DataDefinition definition,
                   ARXConfiguration config, ARXResult result, 
                   ARXNode transformation, DataHandle output, 
                   CSVSyntax csvConfig, ElementData metadata) {
        
        this.style = CertificateStyle.create();

        // Check
        if (input == null || definition == null || config == null || result == null || transformation == null) {
            throw new NullPointerException();
        }

        int section = 1;
        if (metadata != null) {
            this.add(new ElementTitle("Project"));
            this.add(new ElementSubtitle((section++)+". Properties"));
            this.add(asList(metadata));
            this.add(new ElementNewLine());
        }
        section = 1;
        this.add(new ElementTitle("Input specification"));
        this.add(new ElementSubtitle((section++)+". Input data"));
        this.add(asList(input.render()));
        this.add(new ElementNewLine());
        this.add(new ElementSubtitle((section++)+". Attributes and transformations"));
        this.add(asList(definition.render()));
        this.add(new ElementNewLine());
        this.add(new ElementSubtitle((section++)+". Configuration"));
        this.add(asList(config.render()));
        if (result.isResultAvailable()) {
            section = 1;
            this.add(new ElementNewLine());
            this.add(new ElementTitle("Output properties"));
            this.add(new ElementSubtitle((section++)+". Output data"));
            this.add(asList(output.render()));
            if (csvConfig != null) {
                String checksum = null;
                try {
                    checksum = new CSVDataChecksum(csvConfig).getSHA256Checksum(output.iterator());
                } catch (NoSuchAlgorithmException e) {
                    checksum = "Could not calculate hash";
                }
                this.add(asList(new ElementData("Checksum").addProperty("SHA-256", checksum)));
            }
            this.add(new ElementNewLine());
            this.add(new ElementSubtitle((section++)+". Solutions"));
            this.add(asList(result.getLattice().render()));
            this.add(new ElementNewLine());
            this.add(new ElementSubtitle((section++)+". Transformation"));
            this.add(asList(transformation.render()));
            this.add(new ElementNewLine());
            if (config.getQualityModel() != null) {
                this.add(new ElementSubtitle((section++)+". Data quality model"));
                this.add(asList(config.getQualityModel().render(config)));
                this.add(new ElementNewLine());
            }
            this.add(new ElementSubtitle((section++)+". Privacy models"));
            if (transformation.getAnonymity() == Anonymity.ANONYMOUS) {
                for (PrivacyCriterion c : config.getPrivacyModels()) {
                    this.add(asList(c.render()));
                }
            }
        }
    }

    /**
	 * Renders the document into the given output stream
	 * 
	 * @param file
	 * @throws IOException 
	 */
	public void save(File file) throws IOException {
        FileOutputStream stream = new FileOutputStream(file);
        this.save(stream);
        stream.close();
	}
    /**
     * Renders the document into the given output stream
     * 
     * @param stream
     * @throws IOException 
     */
    public void save(OutputStream stream) throws IOException {
        
        // Render
        Document document = new Document(style.gethMargin(), style.gethMargin(), style.getvMargin(), style.getvMargin());
        for (Element element : this.elements) {
            element.render(document, 0, this.style);
        }
        
        // Save to temp file
        File tmp = File.createTempFile("arx", "certificate");
        document.save(tmp);
        
        // Load and watermark
        PDDocument pdDocument = PDDocument.load(tmp);
        Watermark watermark = new Watermark(pdDocument);
        watermark.mark(pdDocument);
        
        // Save
        pdDocument.save(stream);
        pdDocument.close();
        tmp.delete();
    }
    
	/**
     * Renders as a list
     * @param data
     * @return
     */
	private Element asList(ElementData data) {
	    ElementList list = new ElementList(ListStyle.BULLETS);
	    list.addItem(data.asList());
	    return list;
    }

    /**
     * Renders as a list
     * @param data
     * @return
     */
    private Element asList(List<ElementData> data) {
        ElementList list = new ElementList(ListStyle.BULLETS);
        for (ElementData d : data) {
            list.addItem(d.asList());
        }
        return list;
    }
    
	/**
	 * Adds a new element
	 * @param element
	 */
	void add(Element element) {
	    this.elements.add(element);
	}
}
