/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.flash.test;

import org.deidentifier.flash.AttributeType.Hierarchy;
import org.deidentifier.flash.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.flash.Data;
import org.deidentifier.flash.Data.DefaultData;

/**
 * Provides data for test cases
 * 
 * @author Prasser, Kohlmayer
 */
public class DataProvider {

    protected DefaultData          data;
    protected DefaultHierarchy     age;
    private final DefaultHierarchy ageWrong;
    private final DefaultHierarchy gender;
    private final DefaultHierarchy zipcode;

    private final DefaultHierarchy ageOne;
    private final DefaultHierarchy genderOne;
    private final DefaultHierarchy zipcodeOne;

    private final DefaultHierarchy ageMissing;

    /**
     * Init
     */
    public DataProvider() {

        // Define data
        data = Data.create();
        data.add("age", "gender", "zipcode");
        data.add("34", "male", "81667");
        data.add("45", "female", "81675");
        data.add("66", "male", "81925");
        data.add("70", "female", "81931");
        data.add("34", "female", "81931");
        data.add("70", "male", "81931");
        data.add("45", "male", "81931");

        // Define hierarchies
        age = Hierarchy.create();
        age.add("34", "<50", "*");
        age.add("45", "<50", "*");
        age.add("66", ">=50", "*");
        age.add("70", ">=50", "*");

        // Define hierarchies
        ageWrong = Hierarchy.create();
        ageWrong.add("34", "30-40", "30-69", "*");
        ageWrong.add("45", "40-50", "30-69", "*");
        ageWrong.add("66", "70-80", "30-69", "*");
        ageWrong.add("70", "70-80", "70+", "*");

        gender = Hierarchy.create();
        gender.add("male", "*");
        gender.add("female", "*");

        // Only excerpts for readability
        zipcode = Hierarchy.create();
        zipcode.add("81667", "8166*", "816**", "81***", "8****", "*****");
        zipcode.add("81675", "8167*", "816**", "81***", "8****", "*****");
        zipcode.add("81925", "8192*", "819**", "81***", "8****", "*****");
        zipcode.add("81931", "8193*", "819**", "81***", "8****", "*****");

        // Define hierarchies with height one
        ageOne = Hierarchy.create();
        ageOne.add("34");
        ageOne.add("45");
        ageOne.add("66");
        ageOne.add("70");

        genderOne = Hierarchy.create();
        genderOne.add("male");
        genderOne.add("female");

        zipcodeOne = Hierarchy.create();
        zipcodeOne.add("81667");
        zipcodeOne.add("81675");
        zipcodeOne.add("81925");
        zipcodeOne.add("81931");

        // Define hierarchies
        ageMissing = Hierarchy.create();
        ageMissing.add("34", "<50", "*");
        ageMissing.add("45", "<50", "*");
        ageMissing.add("70", ">=50", "*");
    }

    /**
     * Returns a standard data definition
     * 
     * @return
     */
    public void createDataDefinition() {
        // Create a standard definition
        data.getDefinition().setAttributeType("age", age);
        data.getDefinition().setAttributeType("gender", gender);
        data.getDefinition().setAttributeType("zipcode", zipcode);
    }

    /**
     * Returns a standard data definition
     * 
     * @return
     */
    public void createDataDefinitionMissing() {
        // Create a standard definition
        data.getDefinition().setAttributeType("age", ageMissing);
        data.getDefinition().setAttributeType("gender", gender);
        data.getDefinition().setAttributeType("zipcode", zipcode);
    }

    /**
     * Returns a standard data definition
     * 
     * @return
     */
    public void createDataDefinitionWithHeightOne() {
        // Create a standard definition
        data.getDefinition().setAttributeType("age", ageOne);
        data.getDefinition().setAttributeType("gender", genderOne);
        data.getDefinition().setAttributeType("zipcode", zipcodeOne);
    }

    /**
     * Returns a standard data definition
     * 
     * @return
     */
    public void createWrongDataDefinition() {
        // Create a standard definition
        data.getDefinition().setAttributeType("age", ageWrong);
        data.getDefinition().setAttributeType("gender", gender);
        data.getDefinition().setAttributeType("zipcode", zipcode);
    }

    /**
     * @return the age
     */
    public DefaultHierarchy getAge() {
        return age;
    }

    /**
     * @return the data
     */
    public DefaultData getData() {
        return data;
    }

    /**
     * @return the gender
     */
    public DefaultHierarchy getGender() {
        return gender;
    }

    /**
     * @return the zipcode
     */
    public DefaultHierarchy getZipcode() {
        return zipcode;
    }
}
