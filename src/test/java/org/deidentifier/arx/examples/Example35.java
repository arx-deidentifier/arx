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

package org.deidentifier.arx.examples;

import java.io.IOException;

import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.ARXPopulationModel.Region;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.risk.HIPAAIdentifierMatch;

/**
 * This class implements an example of how to find HIPAA identifiers.
 * @author David Gassmann
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 *         
 */
public class Example35 {
    
    /**
     * Entry point
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        Data.DefaultData data = createData();
        
        DataHandle handle = data.getHandle();
        
        HIPAAIdentifierMatch[] warnings = handle.getRiskEstimator(ARXPopulationModel.create(Region.USA))
                                                .getHIPAAIdentifiers();
        
        printWarnings(warnings);
    }
    
    /**
     * Creates the data used in the example.
     * @return
     */
    private static Data.DefaultData createData() {
        Data.DefaultData data = Data.create();
        data.add("first name", "age", "gender", "code", "birth", "email-address", "SSN", "Bank", "Vehicle", "URL", "IP", "phone");
        data.add("Max", "34", "male", "81667", "2008-09-02", "", "123-45-6789", "GR16 0110 1250 0000 0001 2300 695", "", "http://demodomain.com", "8.8.8.8", "+49 1234566");
        data.add("Max", "45", "female", "81675", "2008-09-02", "user@arx.org", "", "", "WDD 169 007-1J-236589", "", "2001:db8::1428:57ab", "");
        data.add("Max", "66", "male", "89375", "2008-09-02", "demo@email.com", "", "", "", "", "", "");
        data.add("Max", "70", "female", "81931", "2008-09-02", "", "", "", "", "", "", "");
        data.add("Max", "34", "female", "81931", "2008-09-02", "", "", "", "", "", "", "");
        data.add("Max", "90", "male", "81931", "2008-09-02", "", "", "", "", "", "", "");
        data.add("Max", "45", "male", "81931", "2008-09-02", "", "", "", "", "", "", "");
        return data;
    }
    
    /**
     * Displays the found warnings.
     * @param warnings
     */
    private static void printWarnings(HIPAAIdentifierMatch[] warnings) {
        if (warnings.length == 0) {
            System.out.println("No warnings");
        } else {
            for (HIPAAIdentifierMatch w : warnings) {
                System.out.println(w.toString());
            }
        }
    }
}