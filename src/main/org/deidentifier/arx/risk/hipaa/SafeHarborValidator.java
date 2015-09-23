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

package org.deidentifier.arx.risk.hipaa;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.DataHandle;

/**
 * Encapsulates the validation process for the safe harbor method.
 * @author David Gaﬂmann
 * @author Florian Kohlmayer
 *         
 */
public class SafeHarborValidator {
    /**
     * Validates a file with the safe harbor method
     * @param handle A data handle of the file which is to be validated
     * @return An array of warnings
     */
    public static Identifier[] validate(DataHandle handle) {
        SafeHarborValidator validator = new SafeHarborValidator();
        List<Integer> columns = validator.getColumns(handle);
        
        List<Identifier> warnings = validator.checkColumnTitles(handle, columns);
        warnings.addAll(validator.checkRows(handle, columns));
        
        return warnings.toArray(new Identifier[warnings.size()]);
    }
    
    private List<Attribute> attributes = new ArrayList<Attribute>();
    
    /**
     * Constructor
     */
    private SafeHarborValidator() {
        initialize();
    }
    
    /**
     * Checks the column titles
     * @param handle A data handle of the file which is to be validated
     * @param columnsToCheck A list of column indices which should be checked
     * @return
     */
    private List<Identifier> checkColumnTitles(DataHandle handle, List<Integer> columnsToCheck) {
        List<Identifier> warnings = new ArrayList<Identifier>();
        for (int i = 0; i < handle.getNumColumns(); i++) {
            for (Attribute attribute : attributes) {
                if (attribute.matchesLabel(handle.getAttributeName(i))) {
                    warnings.add(new Identifier(handle.getAttributeName(i), attribute.getCategory(), Classifier.COLUMN_NAME, handle.getAttributeName(i)));
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
     * @return
     */
    private List<Identifier> checkRows(DataHandle handle, List<Integer> columnsToCheck) {
        List<Identifier> warnings = new ArrayList<Identifier>();
        
        for (int columnIndex = columnsToCheck.size() - 1; columnIndex >= 0; columnIndex--) {
            int index = columnsToCheck.get(columnIndex);
            String[] distinctValues = handle.getDistinctValues(index);
            
            for (int i = 0; i < distinctValues.length; i++) {
                for (Attribute attribute : attributes) {
                    if (attribute.matchesPattern(distinctValues[i])) {
                        warnings.add(new Identifier(handle.getAttributeName(i), attribute.getCategory(), Classifier.ATTRIBUTE_VALUE, distinctValues[i]));
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
        attributes.add(new Attribute(Category.NAME, new Label("name", 1), new NamePattern()));
        attributes.add(new Attribute(Category.GEOGRAPHIC_SUBDIVISION, new Label[] { new Label("address", 1), new Label("city"), new Label("country", 1), new Label("precinct", 1) }));
        attributes.add(new Attribute(Category.GEOGRAPHIC_SUBDIVISION, new Label[] { new Label("zip"), new Label("zip code", 1) }, new ZIPPattern()));
        
        attributes.add(new Attribute(Category.DATE, new Label[] { new Label("age", 1), new Label("year", 1), new Label("birth date", 2), new Label("admission date", 2), new Label("discharge date", 2), new Label("death date", 2), new Label("date", 1) }, new DatePattern()));
        
        attributes.add(new Attribute(Category.TELEPHONE_NUMBER, new Label[] { new Label("number", 1), new Label("telephone, 1"), new Label("fax"), new Label("phone", 1) }));
        attributes.add(new Attribute(Category.EMAIL_ADDRESS, new Label[] { new Label("email"), new Label("E-Mail address") }, new EMailPattern()));
        
        attributes.add(new Attribute(Category.SOCIAL_SECURITY_NUMBER, new Label[] { new Label("SSN"), new Label("Social Security Number", 1) }, new SSNPattern()));
        attributes.add(new Attribute(Category.ACCOUNT_NUMBER, new Label[] { new Label("IBAN"), new Label("account number", 1) }, new IBANPattern()));
        
        attributes.add(new Attribute(Category.CERTIFICATE_NUMBER, new Label[] { new Label("license", 1), new Label("certificate", 1) }));
        attributes.add(new Attribute(Category.VEHICLE_IDENTIFIER, new Label[] { new Label("VIN"), new Label("vehicle identification number", 2) }, new VINPattern()));
        attributes.add(new Attribute(Category.DEVICE_IDENTIFIER, new Label[] { new Label("serial number", 1) }));
        
        attributes.add(new Attribute(Category.URL, new Label[] { new Label("url"), new Label("domain", 1) }, new URLPattern()));
        attributes.add(new Attribute(Category.IP, new Label[] { new Label("IP"), new Label("IPv4"), new Label("IPv6"), new Label("IP address", 1) }, new IPPattern()));
    }
    
}
