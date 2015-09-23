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
import java.util.Arrays;
import java.util.List;

/**
 * Encapsulates validation logic for column headers and pattern matching
 * @author David Gaﬂmann
 */
public class Attribute{
    private ValuePattern pattern;
    private Category category;
    private List<Label> labels;

    /**
     * @param category The identifier this attribute belongs to
     * @param labels An array of labels associated which an attribute
     * @param pattern A pattern which is used to check the row contents
     */
    public Attribute(Category category, Label[] labels, ValuePattern pattern){
        this.category = category;
        this.labels = new ArrayList<Label>(Arrays.asList(labels));
        this.pattern = pattern;
    }

    public Attribute(Category category, Label label){
        this(category, label, null);
    }

    public Attribute(Category category, Label[] labels){
        this(category, labels, null);
    }

    public Attribute(Category category, Label label, ValuePattern pattern){
        this(category, new Label[] { label }, pattern);
    }

    /**
     * @param value The column name
     * @return True if input matches label
     */
    public boolean matchesLabel(String value){
       for(Label label : this.labels)
           if(label.equals(value))
               return true;

        return false;
    }

    /**
     * @return True if attribute has a pattern
     */
    public boolean hasPattern(){
        return this.pattern != null;
    }

    public boolean matchesPattern(String value){
        if(!this.hasPattern())
            return false;
        else
            return this.pattern.matches(value);
    }

    /**
     * @return Category which is associated to this label
     */
    public Category getCategory(){
        return this.category;
    }
}