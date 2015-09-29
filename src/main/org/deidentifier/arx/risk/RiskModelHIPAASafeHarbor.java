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

package org.deidentifier.arx.risk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.risk.HIPAAIdentifierMatch.HIPAAIdentifier;
import org.deidentifier.arx.risk.HIPAAIdentifierMatch.MatchType;
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
import org.deidentifier.arx.risk.RiskEstimateBuilder.ComputationInterruptedException;
import org.deidentifier.arx.risk.RiskEstimateBuilder.WrappedBoolean;

/**
 * Encapsulates the validation process for the safe harbor method.
 * @author David Gaﬂmann
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
     * @param stop
     * @return An array of warnings
     */
    public HIPAAIdentifierMatch[] getMatches(DataHandle handle, 
                                             WrappedBoolean stop) {
        
        // Prepare
        List<HIPAAIdentifierMatch> results = new ArrayList<HIPAAIdentifierMatch>();
        
        // Build list of columns
        List<Integer> columns = new ArrayList<Integer>();
        for (int i = 0; i < handle.getNumColumns(); i++) {
            columns.add(i);
        }
        
        // Match attribute names
        Iterator<Integer> iter = columns.iterator();
        while (iter.hasNext()) {
            Integer column = iter.next();
            String attribute = handle.getAttributeName(column);
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
                    iter.remove();
                    break;
                }
            }
        }
        
        // Match attribute values
        iter = columns.iterator();
        while (iter.hasNext()) {
            Integer column = iter.next();
            String attribute = handle.getAttributeName(column);
            outer: for (HIPAAIdentifierConfig config : configurations) {
                String[] values = handle.getDistinctValues(column);
                for (String value : values) {
                    if (stop.value) {
                        throw new ComputationInterruptedException();
                    }
                    String match = config.getMatchingAttributeValue(value);
                    if (match != null) {
                        results.add(new HIPAAIdentifierMatch(attribute, 
                                                             config.getIdentifier(), 
                                                             config.getInstance(),
                                                             MatchType.ATTRIBUTE_VALUE, 
                                                             match));
                        iter.remove();
                        break outer;
                    }
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
        
        List<HIPAAIdentifierConfig> configurations = new ArrayList<HIPAAIdentifierConfig>();
        
        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.NAME,
                                                     "Generic name",
                                                     new HIPAAMatcherAttributeName("name", 1)));
        
        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.NAME,
                                                     "First name",
                                                     new HIPAAMatcherFirstName(),
                                                     new HIPAAMatcherAttributeName("first name", 1),
                                                     new HIPAAMatcherAttributeName("firstname", 1),
                                                     new HIPAAMatcherAttributeName("forename", 1),
                                                     new HIPAAMatcherAttributeName("christian name", 2),
                                                     new HIPAAMatcherAttributeName("given name", 1),
                                                     new HIPAAMatcherAttributeName("baptismal name", 2)));
        

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.NAME,
                                                     "Last name",
                                                     new HIPAAMatcherLastName(),
                                                     new HIPAAMatcherAttributeName("last name", 1),
                                                     new HIPAAMatcherAttributeName("family name", 2),
                                                     new HIPAAMatcherAttributeName("family", 1),
                                                     new HIPAAMatcherAttributeName("surname", 1),
                                                     new HIPAAMatcherAttributeName("byname", 1),
                                                     new HIPAAMatcherAttributeName("cognomen", 1)));

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.GEOGRAPHIC_SUBDIVISION, 
                                                     "City",
                                                     new HIPAAMatcherCity(),
                                                     new HIPAAMatcherAttributeName("city", 1),
                                                     new HIPAAMatcherAttributeName("municipality", 2),
                                                     new HIPAAMatcherAttributeName("town", 1)));
                                                     

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.GEOGRAPHIC_SUBDIVISION, 
                                                     "ZIP Code",
                                                     new HIPAAMatcherZIP(),
                                                     new HIPAAMatcherAttributeName("zip", 1),
                                                     new HIPAAMatcherAttributeName("zip code", 1),
                                                     new HIPAAMatcherAttributeName("postal code", 2),
                                                     new HIPAAMatcherAttributeName("zip+4", 1)));
                                                     
        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.GEOGRAPHIC_SUBDIVISION, 
                                                     "State",
                                                     new HIPAAMatcherState(),
                                                     new HIPAAMatcherAttributeName("state", 1),
                                                     new HIPAAMatcherAttributeName("region", 1),
                                                     new HIPAAMatcherAttributeName("territory", 2),
                                                     new HIPAAMatcherAttributeName("canton", 1),
                                                     new HIPAAMatcherAttributeName("department", 2),
                                                     new HIPAAMatcherAttributeName("county", 1),
                                                     new HIPAAMatcherAttributeName("area", 1),
                                                     new HIPAAMatcherAttributeName("district", 1),
                                                     new HIPAAMatcherAttributeName("province", 1),
                                                     new HIPAAMatcherAttributeName("federal state", 2)));
        

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.GEOGRAPHIC_SUBDIVISION, 
                                                     "Generic geographic subdivision",
                                                     new HIPAAMatcherState(),
                                                     new HIPAAMatcherAttributeName("street", 1),
                                                     new HIPAAMatcherAttributeName("road", 1),
                                                     new HIPAAMatcherAttributeName("thoroughfare", 2),
                                                     new HIPAAMatcherAttributeName("way", 1),
                                                     new HIPAAMatcherAttributeName("address", 1),
                                                     new HIPAAMatcherAttributeName("location", 1),
                                                     new HIPAAMatcherAttributeName("locality", 1),
                                                     new HIPAAMatcherAttributeName("place", 1),
                                                     new HIPAAMatcherAttributeName("situation", 2),
                                                     new HIPAAMatcherAttributeName("whereabouts", 2),
                                                     new HIPAAMatcherAttributeName("road", 1),
                                                     new HIPAAMatcherAttributeName("territory", 2),
                                                     new HIPAAMatcherAttributeName("canton", 1),
                                                     new HIPAAMatcherAttributeName("department", 2),
                                                     new HIPAAMatcherAttributeName("county", 1),
                                                     new HIPAAMatcherAttributeName("area", 1),
                                                     new HIPAAMatcherAttributeName("district", 1),
                                                     new HIPAAMatcherAttributeName("province", 1),
                                                     new HIPAAMatcherAttributeName("federal state", 2)));


        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.DATE,
                                                     "Date/Time",
                                                     new HIPAAMatcherDate(),
                                                     new HIPAAMatcherAttributeName("year", 1),
                                                     new HIPAAMatcherAttributeName("age", 1),
                                                     new HIPAAMatcherAttributeName("birth date", 2),
                                                     new HIPAAMatcherAttributeName("dob", 1),
                                                     new HIPAAMatcherAttributeName("date of birth", 2),
                                                     new HIPAAMatcherAttributeName("date of death", 2),
                                                     new HIPAAMatcherAttributeName("date of admission", 3),
                                                     new HIPAAMatcherAttributeName("admission date", 2),
                                                     new HIPAAMatcherAttributeName("discharge date", 2),
                                                     new HIPAAMatcherAttributeName("death date", 2),
                                                     new HIPAAMatcherAttributeName("date", 1),
                                                     new HIPAAMatcherAttributeName("day", 1),
                                                     new HIPAAMatcherAttributeName("day of the month", 3),
                                                     new HIPAAMatcherAttributeName("occasion", 2),
                                                     new HIPAAMatcherAttributeName("period", 1),
                                                     new HIPAAMatcherAttributeName("era", 1),
                                                     new HIPAAMatcherAttributeName("epoch", 1),
                                                     new HIPAAMatcherAttributeName("century", 1),
                                                     new HIPAAMatcherAttributeName("decade", 1),
                                                     new HIPAAMatcherAttributeName("stage", 1)));
        
        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.TELEPHONE_NUMBER, 
                                                     "Phone number",
                                                     new HIPAAMatcherAttributeName("number", 1),
                                                     new HIPAAMatcherAttributeName("telephone", 2),
                                                     new HIPAAMatcherAttributeName("fax", 1),
                                                     new HIPAAMatcherAttributeName("phone", 1),
                                                     new HIPAAMatcherAttributeName("fon", 1),
                                                     new HIPAAMatcherAttributeName("skype", 1),
                                                     new HIPAAMatcherAttributeName("tel", 1),
                                                     new HIPAAMatcherAttributeName("phone number", 2),
                                                     new HIPAAMatcherAttributeName("number", 1)));
          
        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.EMAIL_ADDRESS, 
                                                     "Email address",
                                                     new HIPAAMatcherEMail(),
                                                     new HIPAAMatcherAttributeName("email", 1),
                                                     new HIPAAMatcherAttributeName("e-mail", 1),
                                                     new HIPAAMatcherAttributeName("mail", 1),
                                                     new HIPAAMatcherAttributeName("e-mail address", 2),
                                                     new HIPAAMatcherAttributeName("email address", 2),
                                                     new HIPAAMatcherAttributeName("mail address", 2)));
        
        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.SOCIAL_SECURITY_NUMBER, 
                                                     "Social security number",
                                                     new HIPAAMatcherSSN(),
                                                     new HIPAAMatcherAttributeName("taxpayer identification number", 1),
                                                     new HIPAAMatcherAttributeName("taxpayer number", 1),
                                                     new HIPAAMatcherAttributeName("identification number", 1),
                                                     new HIPAAMatcherAttributeName("social security number", 3),
                                                     new HIPAAMatcherAttributeName("security number", 3),
                                                     new HIPAAMatcherAttributeName("social number", 2),
                                                     new HIPAAMatcherAttributeName("social security", 3)));
        
        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.CERTIFICATE_NUMBER, 
                                                     "Certificate number",
                                                     new HIPAAMatcherAttributeName("license", 1), 
                                                     new HIPAAMatcherAttributeName("certificate", 1)));
        
        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.VEHICLE_IDENTIFIER, 
                                                     "Vehicle identifier",
                                                     new HIPAAMatcherAttributeName("vehicle identification number", 5),
                                                     new HIPAAMatcherAttributeName("vin", 1),
                                                     new HIPAAMatcherAttributeName("vehicle id", 2),
                                                     new HIPAAMatcherAttributeName("vehicle identifier", 3),
                                                     new HIPAAMatcherAttributeName("vehicle identification", 4)));
        
        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.DEVICE_IDENTIFIER, 
                                                     "Defive identifier",
                                                     new HIPAAMatcherAttributeName("serial number", 2),
                                                     new HIPAAMatcherAttributeName("defive identifier", 3)));

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.URL, 
                                                     "URL",
                                                     new HIPAAMatcherURL(),
                                                     new HIPAAMatcherAttributeName("url", 1),
                                                     new HIPAAMatcherAttributeName("domain", 1),
                                                     new HIPAAMatcherAttributeName("domain name", 2),
                                                     new HIPAAMatcherAttributeName("web address", 2),
                                                     new HIPAAMatcherAttributeName("internet address", 3),
                                                     new HIPAAMatcherAttributeName("website", 2),
                                                     new HIPAAMatcherAttributeName("homepage", 2),
                                                     new HIPAAMatcherAttributeName("webpage", 2),
                                                     new HIPAAMatcherAttributeName("web site", 2),
                                                     new HIPAAMatcherAttributeName("home page", 2),
                                                     new HIPAAMatcherAttributeName("web page", 2)));

        configurations.add(new HIPAAIdentifierConfig(HIPAAIdentifier.IP, 
                                                     "IP Address",
                                                     new HIPAAMatcherIP(),
                                                     new HIPAAMatcherAttributeName("ip", 1),
                                                     new HIPAAMatcherAttributeName("ip address", 2),
                                                     new HIPAAMatcherAttributeName("ipv4", 1),
                                                     new HIPAAMatcherAttributeName("ipv6", 1),
                                                     new HIPAAMatcherAttributeName("internet protocol", 2),
                                                     new HIPAAMatcherAttributeName("internet", 2)));
        
        return configurations;
    }
    
}
