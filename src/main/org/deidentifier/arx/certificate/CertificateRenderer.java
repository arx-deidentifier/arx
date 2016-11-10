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

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.certificate.elements.ElementSubtitle;
import org.deidentifier.arx.certificate.elements.ElementTitle;
import org.deidentifier.arx.criteria.PrivacyCriterion;

/**
 * Certificate renderer
 * 
 * @author Fabian Prasser
 */
public class CertificateRenderer {

    /** Fields */
    private final DataHandle       input;
    /** Fields */
    private final DataDefinition   definition;
    /** Fields */
    private final ARXConfiguration config;
    /** Fields */
    private final ARXResult        result;
    /** Fields */
    private final ARXNode          transformation;

    /**
     * Creates a new instance
     * @param input
     * @param definition
     * @param config
     * @param result
     * @param transformation
     */
    public CertificateRenderer(DataHandle input, DataDefinition definition,
                               ARXConfiguration config, ARXResult result, ARXNode transformation) {
        
        // Store
        this.input = input;
        this.definition = definition;
        this.config = config;
        this.result = result;
        this.transformation = transformation;
        
        // Check
        if (input == null || definition == null || config == null || result == null || transformation == null) {
            throw new NullPointerException();
        }
    }
    
    /**
     * Renders a certificate
     * @return
     */
    public Certificate render() {
        Certificate certificate = new Certificate();
        certificate.add(new ElementTitle("Input specification"));
        certificate.add(new ElementSubtitle("Input data"));
        certificate.add(input.render());
        certificate.add(new ElementSubtitle("Attributes and transformations"));
        certificate.add(definition.render());
        certificate.add(config.render());
        if (config.getMetric() != null) {
            certificate.add(new ElementSubtitle("Data quality"));
            certificate.add(config.getMetric().render(config));
        }
        if (result.isResultAvailable()) {
            certificate.add(new ElementTitle("Output specification"));
            certificate.add(new ElementSubtitle("Solutions"));
            certificate.add(result.getLattice().render());
            certificate.add(new ElementSubtitle("Selected transformation"));
            certificate.add(transformation.render());
            certificate.add(new ElementSubtitle("Privacy properties"));
            if (transformation.getAnonymity() == Anonymity.ANONYMOUS) {
                for (PrivacyCriterion c : config.getCriteria()) {
                    certificate.add(c.render());
                }
            }
        }
        return certificate;
    }
}