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
import org.deidentifier.arx.risk.RiskEstimateBuilder.ComputationInterruptedException;
import org.deidentifier.arx.risk.RiskEstimateBuilder.WrappedBoolean;
import org.deidentifier.arx.risk.hipaa.MatcherConfig;
import org.deidentifier.arx.risk.hipaa.Match;
import org.deidentifier.arx.risk.hipaa.Match.HIPAAIdentifier;
import org.deidentifier.arx.risk.hipaa.Match.Classifier;
import org.deidentifier.arx.risk.hipaa.HeaderMatcher;
import org.deidentifier.arx.risk.hipaa.ValueMatcher.DatePattern;
import org.deidentifier.arx.risk.hipaa.ValueMatcher.EMailPattern;
import org.deidentifier.arx.risk.hipaa.ValueMatcher.IBANPattern;
import org.deidentifier.arx.risk.hipaa.ValueMatcher.IPPattern;
import org.deidentifier.arx.risk.hipaa.ValueMatcher.NamePattern;
import org.deidentifier.arx.risk.hipaa.ValueMatcher.SSNPattern;
import org.deidentifier.arx.risk.hipaa.ValueMatcher.URLPattern;
import org.deidentifier.arx.risk.hipaa.ValueMatcher.VINPattern;
import org.deidentifier.arx.risk.hipaa.ValueMatcher.ZIPPattern;

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
    public static Match[] validate(DataHandle handle, WrappedBoolean stop) {
        RiskModelHIPAASafeHarbor validator = new RiskModelHIPAASafeHarbor();
        List<Integer> columns = validator.getColumns(handle);
        
        List<Match> warnings = validator.checkColumnTitles(handle, columns);
        warnings.addAll(validator.checkRows(handle, columns, stop));
        
        return warnings.toArray(new Match[warnings.size()]);
    }
    
    private List<MatcherConfig> attributes = new ArrayList<MatcherConfig>();
    
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
    private List<Match> checkColumnTitles(DataHandle handle, List<Integer> columnsToCheck) {
        List<Match> warnings = new ArrayList<Match>();
        for (int i = 0; i < handle.getNumColumns(); i++) {
            for (MatcherConfig attribute : attributes) {
                if (attribute.matchesLabel(handle.getAttributeName(i))) {
                    warnings.add(new Match(handle.getAttributeName(i), attribute.getCategory(), Classifier.COLUMN_NAME, handle.getAttributeName(i)));
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
    private List<Match> checkRows(DataHandle handle, List<Integer> columnsToCheck, WrappedBoolean stop) {
        List<Match> warnings = new ArrayList<Match>();
        
        for (int columnIndex = columnsToCheck.size() - 1; columnIndex >= 0; columnIndex--) {
            int index = columnsToCheck.get(columnIndex);
            String[] distinctValues = handle.getDistinctValues(index);
            
            for (int i = 0; i < distinctValues.length; i++) {
                for (MatcherConfig attribute : attributes) {
                    if (stop.value) {
                        throw new ComputationInterruptedException();
                    }
                    if (attribute.matchesPattern(distinctValues[i])) {
                        warnings.add(new Match(handle.getAttributeName(i), attribute.getCategory(), Classifier.ATTRIBUTE_VALUE, distinctValues[i]));
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
        attributes.add(new MatcherConfig(HIPAAIdentifier.NAME, new HeaderMatcher("name", 1), new NamePattern()));
        attributes.add(new MatcherConfig(HIPAAIdentifier.GEOGRAPHIC_SUBDIVISION, new HeaderMatcher[] { new HeaderMatcher("address", 1), new HeaderMatcher("city"), new HeaderMatcher("country", 1), new HeaderMatcher("precinct", 1) }));
        attributes.add(new MatcherConfig(HIPAAIdentifier.GEOGRAPHIC_SUBDIVISION, new HeaderMatcher[] { new HeaderMatcher("zip"), new HeaderMatcher("zip code", 1) }, new ZIPPattern()));
        
        attributes.add(new MatcherConfig(HIPAAIdentifier.DATE, new HeaderMatcher[] { new HeaderMatcher("age", 1), new HeaderMatcher("year", 1), new HeaderMatcher("birth date", 2), new HeaderMatcher("admission date", 2), new HeaderMatcher("discharge date", 2), new HeaderMatcher("death date", 2), new HeaderMatcher("date", 1) }, new DatePattern()));
        
        attributes.add(new MatcherConfig(HIPAAIdentifier.TELEPHONE_NUMBER, new HeaderMatcher[] { new HeaderMatcher("number", 1), new HeaderMatcher("telephone, 1"), new HeaderMatcher("fax"), new HeaderMatcher("phone", 1) }));
        attributes.add(new MatcherConfig(HIPAAIdentifier.EMAIL_ADDRESS, new HeaderMatcher[] { new HeaderMatcher("email"), new HeaderMatcher("E-Mail address") }, new EMailPattern()));
        
        attributes.add(new MatcherConfig(HIPAAIdentifier.SOCIAL_SECURITY_NUMBER, new HeaderMatcher[] { new HeaderMatcher("SSN"), new HeaderMatcher("Social Security Number", 1) }, new SSNPattern()));
        attributes.add(new MatcherConfig(HIPAAIdentifier.ACCOUNT_NUMBER, new HeaderMatcher[] { new HeaderMatcher("IBAN"), new HeaderMatcher("account number", 1) }, new IBANPattern()));
        
        attributes.add(new MatcherConfig(HIPAAIdentifier.CERTIFICATE_NUMBER, new HeaderMatcher[] { new HeaderMatcher("license", 1), new HeaderMatcher("certificate", 1) }));
        attributes.add(new MatcherConfig(HIPAAIdentifier.VEHICLE_IDENTIFIER, new HeaderMatcher[] { new HeaderMatcher("VIN"), new HeaderMatcher("vehicle identification number", 2) }, new VINPattern()));
        attributes.add(new MatcherConfig(HIPAAIdentifier.DEVICE_IDENTIFIER, new HeaderMatcher[] { new HeaderMatcher("serial number", 1) }));
        
        attributes.add(new MatcherConfig(HIPAAIdentifier.URL, new HeaderMatcher[] { new HeaderMatcher("url"), new HeaderMatcher("domain", 1) }, new URLPattern()));
        attributes.add(new MatcherConfig(HIPAAIdentifier.IP, new HeaderMatcher[] { new HeaderMatcher("IP"), new HeaderMatcher("IPv4"), new HeaderMatcher("IPv6"), new HeaderMatcher("IP address", 1) }, new IPPattern()));
    }
    
}
