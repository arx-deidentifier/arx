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

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * This class provides access to basic resources.
 *
 * @author Fabian Prasser
 */
public class StringResources {
    
    /** Messages */
    private static final ResourceBundle MESSAGES_BUNDLE = ResourceBundle.getBundle("org.deidentifier.arx.certificate.messages"); //$NON-NLS-1$
                       
    /**
     * 
     * Returns the associated message
     *
     * @param key
     * @return
     */
    public static String getMessage(String key) {
        try {
            return MESSAGES_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
