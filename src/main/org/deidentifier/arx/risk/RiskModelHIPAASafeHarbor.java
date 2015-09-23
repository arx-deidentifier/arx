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
import java.util.List;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.risk.HIPAAIdentifierMatch.HIPAAIdentifier;
import org.deidentifier.arx.risk.HIPAAIdentifierMatch.MatchType;
import org.deidentifier.arx.risk.HIPAAMatcherAttributeValue.HIPAAMatcherDate;
import org.deidentifier.arx.risk.HIPAAMatcherAttributeValue.HIPAAMatcherEMail;
import org.deidentifier.arx.risk.HIPAAMatcherAttributeValue.HIPAAMatcherIBAN;
import org.deidentifier.arx.risk.HIPAAMatcherAttributeValue.HIPAAMatcherIP;
import org.deidentifier.arx.risk.HIPAAMatcherAttributeValue.HIPAAMatcherName;
import org.deidentifier.arx.risk.HIPAAMatcherAttributeValue.HIPAAMatcherSSN;
import org.deidentifier.arx.risk.HIPAAMatcherAttributeValue.HIPAAMatcherURL;
import org.deidentifier.arx.risk.HIPAAMatcherAttributeValue.HIPAAMatcherVIN;
import org.deidentifier.arx.risk.HIPAAMatcherAttributeValue.HIPAAMatcherZIP;
import org.deidentifier.arx.risk.RiskEstimateBuilder.ComputationInterruptedException;
import org.deidentifier.arx.risk.RiskEstimateBuilder.WrappedBoolean;

/**
 * Encapsulates the validation process for the safe harbor method.
 * @author David Gaﬂmann
 * @author Florian Kohlmayer
 *         
 */
public class RiskModelHIPAASafeHarbor {
    /**
     * Validates a file with the safe harbor method
     * @param handle A data handle of the file which is to be validated
     * @param stop
     * @return An array of warnings
     */
    public static HIPAAIdentifierMatch[] validate(DataHandle handle, WrappedBoolean stop) {
        RiskModelHIPAASafeHarbor validator = new RiskModelHIPAASafeHarbor();
        List<Integer> columns = validator.getColumns(handle);
        
        List<HIPAAIdentifierMatch> warnings = validator.checkColumnTitles(handle, columns);
        warnings.addAll(validator.checkRows(handle, columns, stop));
        
        return warnings.toArray(new HIPAAIdentifierMatch[warnings.size()]);
    }
    
    private List<HIPAAIdentifierConfig> attributes = new ArrayList<HIPAAIdentifierConfig>();
    
    /**
     * Constructor
     */
    private RiskModelHIPAASafeHarbor() {
        initialize();
    }
    
    /**
     * Checks the column titles
     * @param handle A data handle of the file which is to be validated
     * @param columnsToCheck A list of column indices which should be checked
     * @return
     */
    private List<HIPAAIdentifierMatch> checkColumnTitles(DataHandle handle, List<Integer> columnsToCheck) {
        List<HIPAAIdentifierMatch> warnings = new ArrayList<HIPAAIdentifierMatch>();
        for (int i = 0; i < handle.getNumColumns(); i++) {
            for (HIPAAIdentifierConfig attribute : attributes) {
                if (attribute.matchesAttributeName(handle.getAttributeName(i))) {
                    warnings.add(new HIPAAIdentifierMatch(handle.getAttributeName(i), attribute.getIdentifier(), MatchType.ATTRIBUTE_NAME, handle.getAttributeName(i)));
                    columnsToCheck.remove(Integer.valueOf(i));
                }
            }
        }
        return warnings;
    }
    
    /**
     * Checks the rows
     * @param handle A data handle of the file which is to be validated
     * @param columnsToCheck A list of column indices which should be checked
     * @param stop
     * @return
     */
    private List<HIPAAIdentifierMatch> checkRows(DataHandle handle, List<Integer> columnsToCheck, WrappedBoolean stop) {
        List<HIPAAIdentifierMatch> warnings = new ArrayList<HIPAAIdentifierMatch>();
        
        for (int columnIndex = columnsToCheck.size() - 1; columnIndex >= 0; columnIndex--) {
            int index = columnsToCheck.get(columnIndex);
            String[] distinctValues = handle.getDistinctValues(index);
            
            for (int i = 0; i < distinctValues.length; i++) {
                for (HIPAAIdentifierConfig attribute : attributes) {
                    if (stop.value) {
                        throw new ComputationInterruptedException();
                    }
                    if (attribute.matchesAttributeValue(distinctValues[i])) {
                        warnings.add(new HIPAAIdentifierMatch(handle.getAttributeName(index), attribute.getIdentifier(), MatchType.ATTRIBUTE_VALUE, distinctValues[i]));
                        columnsToCheck.remove(columnIndex);
                        break;
                    }
                }
            }
        }
        return warnings;
        
    }
    
    /**
     * @param handle A data handle of the file which is to be validated
     * @return A List of column indices
     */
    private List<Integer> getColumns(DataHandle handle) {
        List<Integer> columns = new ArrayList<Integer>();
        for (int i = 0; i < handle.getNumColumns(); i++) {
            columns.add(i);
        }
        return columns;
    }
    
    /**
     * Creates the list of attributes
     */
    private void initialize() {
        attributes.add(new HIPAAIdentifierConfig(HIPAAIdentifier.NAME, new HIPAAMatcherAttributeName("name", 1), new HIPAAMatcherName()));
        attributes.add(new HIPAAIdentifierConfig(HIPAAIdentifier.GEOGRAPHIC_SUBDIVISION, new HIPAAMatcherAttributeName[] { new HIPAAMatcherAttributeName("address", 1), new HIPAAMatcherAttributeName("city"), new HIPAAMatcherAttributeName("country", 1), new HIPAAMatcherAttributeName("precinct", 1) }));
        attributes.add(new HIPAAIdentifierConfig(HIPAAIdentifier.GEOGRAPHIC_SUBDIVISION, new HIPAAMatcherAttributeName[] { new HIPAAMatcherAttributeName("zip"), new HIPAAMatcherAttributeName("zip code", 1) }, new HIPAAMatcherZIP()));
        
        attributes.add(new HIPAAIdentifierConfig(HIPAAIdentifier.DATE, new HIPAAMatcherAttributeName[] { new HIPAAMatcherAttributeName("age", 1), new HIPAAMatcherAttributeName("year", 1), new HIPAAMatcherAttributeName("birth date", 2), new HIPAAMatcherAttributeName("admission date", 2), new HIPAAMatcherAttributeName("discharge date", 2), new HIPAAMatcherAttributeName("death date", 2), new HIPAAMatcherAttributeName("date", 1) }, new HIPAAMatcherDate()));
        
        attributes.add(new HIPAAIdentifierConfig(HIPAAIdentifier.TELEPHONE_NUMBER, new HIPAAMatcherAttributeName[] { new HIPAAMatcherAttributeName("number", 1), new HIPAAMatcherAttributeName("telephone, 1"), new HIPAAMatcherAttributeName("fax"), new HIPAAMatcherAttributeName("phone", 1) }));
        attributes.add(new HIPAAIdentifierConfig(HIPAAIdentifier.EMAIL_ADDRESS, new HIPAAMatcherAttributeName[] { new HIPAAMatcherAttributeName("email"), new HIPAAMatcherAttributeName("E-Mail address") }, new HIPAAMatcherEMail()));
        
        attributes.add(new HIPAAIdentifierConfig(HIPAAIdentifier.SOCIAL_SECURITY_NUMBER, new HIPAAMatcherAttributeName[] { new HIPAAMatcherAttributeName("SSN"), new HIPAAMatcherAttributeName("Social Security Number", 1) }, new HIPAAMatcherSSN()));
        attributes.add(new HIPAAIdentifierConfig(HIPAAIdentifier.ACCOUNT_NUMBER, new HIPAAMatcherAttributeName[] { new HIPAAMatcherAttributeName("IBAN"), new HIPAAMatcherAttributeName("account number", 1) }, new HIPAAMatcherIBAN()));
        
        attributes.add(new HIPAAIdentifierConfig(HIPAAIdentifier.CERTIFICATE_NUMBER, new HIPAAMatcherAttributeName[] { new HIPAAMatcherAttributeName("license", 1), new HIPAAMatcherAttributeName("certificate", 1) }));
        attributes.add(new HIPAAIdentifierConfig(HIPAAIdentifier.VEHICLE_IDENTIFIER, new HIPAAMatcherAttributeName[] { new HIPAAMatcherAttributeName("VIN"), new HIPAAMatcherAttributeName("vehicle identification number", 2) }, new HIPAAMatcherVIN()));
        attributes.add(new HIPAAIdentifierConfig(HIPAAIdentifier.DEVICE_IDENTIFIER, new HIPAAMatcherAttributeName[] { new HIPAAMatcherAttributeName("serial number", 1) }));
        
        attributes.add(new HIPAAIdentifierConfig(HIPAAIdentifier.URL, new HIPAAMatcherAttributeName[] { new HIPAAMatcherAttributeName("url"), new HIPAAMatcherAttributeName("domain", 1) }, new HIPAAMatcherURL()));
        attributes.add(new HIPAAIdentifierConfig(HIPAAIdentifier.IP, new HIPAAMatcherAttributeName[] { new HIPAAMatcherAttributeName("IP"), new HIPAAMatcherAttributeName("IPv4"), new HIPAAMatcherAttributeName("IPv6"), new HIPAAMatcherAttributeName("IP address", 1) }, new HIPAAMatcherIP()));
    }
    
}
