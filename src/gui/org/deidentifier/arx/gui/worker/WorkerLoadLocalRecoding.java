/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataHandleOutput;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.worker.io.LocalRecodingData;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This worker that loads local recoding from a project file
 *
 * @author Fabian Prasser
 */
public class WorkerLoadLocalRecoding extends Worker<Boolean> {

	/** The model. */
    private final Model model;

    /** The zip file. */
    private ZipFile         zipfile;

    /**
     * Creates a new instance.
     *
     * @param model
     * @throws IOException 
     */
    public WorkerLoadLocalRecoding(final Model model, final String path) throws IOException {
        this.model = model;
        this.zipfile = new ZipFile(path);
    }

    @Override
    public void run(final IProgressMonitor arg0) throws InvocationTargetException,
                                            InterruptedException {

        arg0.beginTask(Resources.getMessage("WorkerLoadLocalRecoding.0"), 1); //$NON-NLS-1$

        try {
            final ZipFile zip = zipfile;
            result = readLocalRecoding(zip);
            arg0.worked(1);
            zip.close();
        } catch (final Exception e) {
            error = e;
            result = false;
            arg0.done();
            return;
        }
        arg0.worked(1);
        arg0.done();
    }

    /**
     * Reads local recoding from the zip file
     * @param zip
     * @throws IOException 
     * @throws ClassNotFoundException 
     */
    private boolean readLocalRecoding(ZipFile zip) throws IOException, ClassNotFoundException {
        
        if (model.getOutput() == null) {
            return false;
        }
        
        ZipEntry entry = zip.getEntry("local.dat"); //$NON-NLS-1$
        if (entry == null) { 
            return false; 
        }

        // Read config
        ObjectInputStream oos = new ObjectInputStream(new BufferedInputStream(zip.getInputStream(entry)));
        LocalRecodingData config = (LocalRecodingData) oos.readObject();

        // Prepare reading locally recoded data
        entry = zip.getEntry("data/output.csv"); //$NON-NLS-1$
        DataHandle input = Data.create(new BufferedInputStream(zip.getInputStream(entry)),
                                                               model.getCSVSyntax().getDelimiter()).getHandle();
        
        // Update
        ((DataHandleOutput)model.getOutput()).updateData(input, 
                                                         config.getDataTypes(), 
                                                         config.getOutliers());
        
        // Return
        return true;
    }
}
