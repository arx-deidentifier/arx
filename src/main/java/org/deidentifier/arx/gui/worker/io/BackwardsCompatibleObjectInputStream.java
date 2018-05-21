/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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
package org.deidentifier.arx.gui.worker.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * This class handles compatibility issues with object deserialization
 * 
 * @author Fabian Prasser
 */
public class BackwardsCompatibleObjectInputStream extends ObjectInputStream {

    /**
     * Creates a new instance
     * @param in
     * @throws IOException
     */
    public BackwardsCompatibleObjectInputStream(InputStream in) throws IOException {
        super(in);
    }

    @Override
    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
        
        // Read from stream
        ObjectStreamClass result = super.readClassDescriptor();

        // Handle movement of ARXLogisticRegressionConfiguration
        if (result.getName().equals("org.deidentifier.arx.ARXLogisticRegressionConfiguration")) {
            result = ObjectStreamClass.lookup(org.deidentifier.arx.aggregates.ClassificationConfigurationLogisticRegression.class);

        // Handle movement of ARXLogisticRegressionConfiguration$PriorFunction
        } else if (result.getName().equals("org.deidentifier.arx.ARXLogisticRegressionConfiguration$PriorFunction")) {
            result = ObjectStreamClass.lookup(org.deidentifier.arx.aggregates.ClassificationConfigurationLogisticRegression.PriorFunction.class);
        }

        // Return potentially mapped descriptor
        return result;
    }
}
