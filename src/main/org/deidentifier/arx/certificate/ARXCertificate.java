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

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.certificate.elements.Element;
import org.deidentifier.arx.certificate.elements.ElementSubtitle;
import org.deidentifier.arx.certificate.elements.ElementTitle;
import org.deidentifier.arx.criteria.PrivacyCriterion;

import rst.pdfbox.layout.elements.Document;

/**
 * A PDF document
 * 
 * @author Annika Saken
 * @author Fabian Prasser
 */
public class ARXCertificate {

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
     */
    public static ARXCertificate create(DataHandle input, DataDefinition definition,
                ARXConfiguration config, ARXResult result, ARXNode transformation) {
        return new ARXCertificate(input, definition, config, result, transformation);
    }
    /**
     * Creates a new instance
     * @param input
     * @param definition
     * @param config
     * @param result
     * @param transformation
     */
    ARXCertificate(DataHandle input, DataDefinition definition,
                ARXConfiguration config, ARXResult result, ARXNode transformation) {
        this.style = CertificateStyle.create();

        // Check
        if (input == null || definition == null || config == null || result == null || transformation == null) {
            throw new NullPointerException();
        }

        this.add(new ElementTitle("Input specification"));
        this.add(new ElementSubtitle("Input data"));
        this.add(input.render());
        this.add(new ElementSubtitle("Attributes and transformations"));
        this.add(definition.render());
        this.add(config.render());
        if (config.getMetric() != null) {
            this.add(new ElementSubtitle("Data quality"));
            this.add(config.getMetric().render(config));
        }
        if (result.isResultAvailable()) {
            this.add(new ElementTitle("Output specification"));
            this.add(new ElementSubtitle("Solutions"));
            this.add(result.getLattice().render());
            this.add(new ElementSubtitle("Selected transformation"));
            this.add(transformation.render());
            this.add(new ElementSubtitle("Privacy properties"));
            if (transformation.getAnonymity() == Anonymity.ANONYMOUS) {
                for (PrivacyCriterion c : config.getCriteria()) {
                    this.add(c.render());
                }
            }
        }
    }
	
	/**
	 * Adds a new element
	 * @param element
	 */
	void add(Element element) {
	    this.elements.add(element);
	}
	
	/**
	 * Adds a new data element
	 * @param data
	 */
	void add(ElementData data) {
	    this.elements.add(data);
	}

    /**
     * Adds a new list of data elements
     * @param data
     */
    void add(List<ElementData> data) {
        this.elements.addAll(data);
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
        
        // Save
        document.save(stream);
	}
}
