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

package org.deidentifier.arx.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.deidentifier.arx.gui.resources.Resources;
import org.junit.Assert;
import org.junit.Test;

/**
 * A simple test which checks for duplicate message in the resource bundle
 * @author Fabian Prasser
 */
public class TestResourceBundle {

    /**
     * Helper class which wraps Properties
     * @author Fabian Prasser
     */
    private static class WrappedProperties extends Properties {

        /** SVUID*/
        private static final long serialVersionUID = 7390022720344169366L;

        @Override
        public synchronized Object put(Object key, Object value) {
            if (get(key) != null) {
                throw new IllegalStateException("Duplicate key: " + key + " - Current value: " + value + " - Original value: " + (String) get(key));
            }
            return super.put(key, value);
        }
    }

    @Test
    public void test() throws IOException {
        try {
            WrappedProperties resources = new WrappedProperties();
            InputStream stream = Resources.class.getClassLoader().getResourceAsStream("org/deidentifier/arx/gui/resources/messages.properties");
            resources.load(stream);
            stream.close();
        } catch (IllegalStateException e) {
            Assert.fail(e.getMessage());
        }
    }
}