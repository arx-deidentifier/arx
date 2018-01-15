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

package org.deidentifier.arx.risk;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.DataHandleInternal;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.exceptions.ComputationInterruptedException;
import org.deidentifier.arx.risk.HIPAAIdentifierMatch.HIPAAIdentifier;
import org.deidentifier.arx.risk.HIPAAIdentifierMatch.MatchType;
import org.deidentifier.arx.risk.HIPAAMatcherAttributeValue.HIPAAMatcherAge;
import org.deidentifier.arx.risk.HIPAAMatcherAttributeValue.HIPAAMatcherCity;
import org.deidentifier.arx.risk.HIPAAMatcherAttributeValue.HIPAAMatcherDate;
import org.deidentifier.arx.risk.HIPAAMatcherAttributeValue.HIPAAMatcherEMail;
import org.deidentifier.arx.risk.HIPAAMatcherAttributeValue.HIPAAMatcherFirstName;
import org.deidentifier.arx.risk.HIPAAMatcherAttributeValue.HIPAAMatcherIP;
import org.deidentifier.arx.risk.HIPAAMatcherAttributeValue.HIPAAMatcherLastName;
import org.deidentifier.arx.risk.HIPAAMatcherAttributeValue.HIPAAMatcherSSN;
import org.deidentifier.arx.risk.HIPAAMatcherAttributeValue.HIPAAMatcherState;
import org.deidentifier.arx.risk.HIPAAMatcherAttributeValue.HIPAAMatcherURL;
import org.deidentifier.arx.risk.HIPAAMatcherAttributeValue.HIPAAMatcherZIP;

/**
 * Encapsulates the validation process for the safe harbor method.
 * @author David Gassmann
 * @author Florian Kohlmayer
 *         
 */
class RiskModelHIPAASafeHarbor {

    /** All configurations*/
    private final List<HIPAAIdentifierConfig> configurations;

    /**
     * Constructor
     */
    RiskModelHIPAASafeHarbor() {
        this.configurations = getConfigurations();
    }
    
    /**
     * Returns a list of matches with HIPAA identifiers
     * 
     * @param handle
     * @param threshold
     * @param stop
     * @return An array of warnings
     */
    public HIPAAIdentifierMatch[] getMatches(DataHandleInternal handle, 
                                             double threshold,
                                             WrappedBoolean stop) {
        
        // Prepare
        List<HIPAAIdentifierMatch> results = new ArrayList<HIPAAIdentifierMatch>();
        
        // Check each attribute
        for (int column = 0; column < handle.getNumColumns(); column++) {

            // Init
            String attribute = handle.getAttributeName(column);
            String[] values = handle.getDistinctValues(column);
            
            // Match attribute name
            for (HIPAAIdentifierConfig config : configurations) {
                if (stop.value) {
                    throw new ComputationInterruptedException();
                }
                String match = config.getMatchingAttributeName(attribute);
                if (match != null) {
                    results.add(new HIPAAIdentifierMatch(attribute, 
                                                         config.getIdentifier(), 
                                                         config.getInstance(),
                                                         MatchType.ATTRIBUTE_NAME, 
                                                         match));
                }
            }
            
            // Match attribute values
            for (HIPAAIdentifierConfig config : configurations) {
                int matches = 0;
                int nonmatches = 0;
                for (String value : values) {
                    if (stop.value) {
                        throw new ComputationInterruptedException();
                    }
                    
                    // Count matching values
                    if (config.getMatchingAttributeValue(value) != null) {
                        matches++;
                    } else {
                        
                        // Break if too many non-matching values
                        nonmatches++;
                        double nonpercentage = (double)nonmatches / (double)values.length;
                        if (nonpercentage > 1d - threshold) {
                            break;
                        }
                    }
                }
                double percentage = (double)matches / (double)values.length;
                if (percentage > threshold) {
                    results.add(new HIPAAIdentifierMatch(attribute, 
                                                         config.getIdentifier(), 
                                                         config.getInstance(),
                                                         MatchType.ATTRIBUTE_VALUE, 
                                                         String.valueOf(percentage)));
                }
            }
        }
        
        // Return
        return results.toArray(new HIPAAIdentifierMatch[results.size()]);
    }
    
    /**
     * Creates the list of attributes
     */
    private List<HIPAAIdentifierConfig> getConfigurations() {
        
        HIPAAConstants constants = HIPAAConstants.getUSData();
        
        List<HIPAAIdentifierConfig> configurations = new ArrayList<HIPAAIdentifierConfig>();

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.NAME,
                                                     "First name",
                                                     new HIPAAMatcherFirstName(constants)));

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.NAME,
                                                     "Last name",
                                                     new HIPAAMatcherLastName(constants)));

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.GEOGRAPHIC_SUBDIVISION, 
                                                     "City",
                                                     new HIPAAMatcherCity(constants)));

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.GEOGRAPHIC_SUBDIVISION, 
                                                     "ZIP Code",
                                                     new HIPAAMatcherZIP(constants)));

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.GEOGRAPHIC_SUBDIVISION, 
                                                     "State",
                                                     new HIPAAMatcherState(constants)));

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.GEOGRAPHIC_SUBDIVISION, 
                                                     "Generic geographic subdivision",
                                                     new HIPAAMatcherState(constants)));

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.DATE,
                                                     "Date/Time",
                                                     new HIPAAMatcherDate(constants)));

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.DATE,
                                                     "Age",
                                                     new HIPAAMatcherAge(constants)));

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.EMAIL_ADDRESS, 
                                                     "Email address",
                                                     new HIPAAMatcherEMail(constants)));

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.SOCIAL_SECURITY_NUMBER, 
                                                     "Social security number",
                                                     new HIPAAMatcherSSN()));

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.URL, 
                                                     "URL",
                                                     new HIPAAMatcherURL(constants)));

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.IP, 
                                                     "IP Address",
                                                     new HIPAAMatcherIP(constants)));
        
        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.NAME,
                                                     "Generic name",
                                                     constants.getNameMatchers("Generic name")));

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.NAME,
                                                     "First name",
                                                     constants.getNameMatchers("First name")));

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.NAME,
                                                     "Last name",
                                                     constants.getNameMatchers("Last name")));

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.GEOGRAPHIC_SUBDIVISION,
                                                     "City",
                                                     constants.getNameMatchers("City")));

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.GEOGRAPHIC_SUBDIVISION,
                                                     "ZIP Code",
                                                     constants.getNameMatchers("ZIP Code")));
        
        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.GEOGRAPHIC_SUBDIVISION,
                                                     "State",
                                                     constants.getNameMatchers("State")));

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.GEOGRAPHIC_SUBDIVISION,
                                                     "Generic geographic subdivision",
                                                     constants.getNameMatchers("Generic geographic subdivision")));

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.DATE,
                                                     "Date/Time",
                                                     constants.getNameMatchers("Date/Time")));
        
        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.TELEPHONE_NUMBER,
                                                     "Phone number",
                                                     constants.getNameMatchers("Phone number")));

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.FAX_NUMBER,
                                                     "Fax number",
                                                     constants.getNameMatchers("Phone number")));

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.EMAIL_ADDRESS,
                                                     "Email address",
                                                     constants.getNameMatchers("Email address")));

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.SOCIAL_SECURITY_NUMBER,
                                                     "Social security number",
                                                     constants.getNameMatchers("Social security number")));

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.CERTIFICATE_NUMBER,
                                                     "Certificate number",
                                                     constants.getNameMatchers("Certificate number")));

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.VEHICLE_IDENTIFIER,
                                                     "Vehicle identifier",
                                                     constants.getNameMatchers("Vehicle identifier")));

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.DEVICE_IDENTIFIER,
                                                     "Device identifier",
                                                     constants.getNameMatchers("Device identifier")));

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.URL,
                                                     "URL",
                                                     constants.getNameMatchers("URL")));

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.IP,
                                                     "IP Address",
                                                     constants.getNameMatchers("IP Address")));
           
        return configurations;
    }
    
}
