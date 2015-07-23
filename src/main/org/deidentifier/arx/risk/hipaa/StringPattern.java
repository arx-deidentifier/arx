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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pattern which matches a string with the provided regular expression
 * @author David Gaﬂmann
 */
public class StringPattern implements ValuePattern {
    Matcher matcher;

    public StringPattern(String regex){
        Pattern pattern = Pattern.compile(regex);
        this.matcher = pattern.matcher("");
    }

    @Override
    public boolean matches(String value){
        return this.matcher.reset(value).matches();

    }
}
