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

package org.deidentifier.arx.gui.worker;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.certificate.ARXCertificate;
import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.io.CSVSyntax;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This worker creates a certificate.
 *
 * @author Fabian Prasser
 */
public class WorkerCreateCertificate extends Worker<File> {

    /** Data */
    private final CSVSyntax        syntax;
    /** Data */
    private final String           path;
    /** Data */
    private final DataHandle       input;
    /** Data */
    private final DataDefinition   definition;
    /** Data */
    private final ARXConfiguration config;
    /** Data */
    private final ARXResult        result;
    /** Data */
    private final ARXNode          transformation;
    /** Data */
    private final DataHandle       output;
    /** Data */
    private final Model            model;

    /**
     * Creates a new instance.
     * 
     * @param file
     * @param syntax
     * @param input
     * @param definition
     * @param config
     * @param result
     * @param transformation
     * @param output
     */
    public WorkerCreateCertificate(String path,
                                   CSVSyntax syntax,
                                   DataHandle input,
                                   DataDefinition definition,
                                   ARXConfiguration config,
                                   ARXResult result,
                                   ARXNode transformation,
                                   DataHandle output,
                                   Model model) {
        this.syntax = syntax;
        this.path = path;
        this.input = input;
        this.definition = definition;
        this.config = config;
        this.result = result;
        this.transformation = transformation;
        this.output = output;
        this.model = model;
    }

    @Override
    public void run(final IProgressMonitor arg0) throws InvocationTargetException,
                                            			InterruptedException {

        try {
            
            // Begin
            arg0.beginTask(Resources.getMessage("Controller.154"), 100); //$NON-NLS-1$
    
            // Create metadata
            ElementData metadata = new ElementData("Project");
            metadata.addProperty("Name", model.getName());
            metadata.addProperty("Description", model.getDescription());
            
            // Create a renderer
            ARXCertificate certificate = ARXCertificate.create(input, 
                                                               definition, 
                                                               config, 
                                                               result, 
                                                               transformation, 
                                                               output.getView(),
                                                               syntax,
                                                               metadata);
            
            // Check and progress
            if (arg0.isCanceled()) { 
                throw new RuntimeException(Resources.getMessage("WorkerAnonymize.1")); //$NON-NLS-1$ 
            } 
            arg0.worked(50);
            
            // Save
            File file = new File(path);
            certificate.save(file);

            // Check and progress
            if (arg0.isCanceled()) { 
                throw new RuntimeException(Resources.getMessage("WorkerAnonymize.1")); //$NON-NLS-1$ 
            } 
            arg0.worked(50);
         
            // Result
            super.result = file;
            arg0.done();
        } catch (final Exception e) {
            super.error = e;
            arg0.done();
            return;
        }
    }
}
