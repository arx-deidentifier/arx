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

import java.util.HashSet;
import java.util.Set;

/**
 * Pattern which matches a ZIP code
 * @author David Gaﬂmann
 */
public class ZIPPattern implements ValuePattern {
    private Set<String> zipCodes;

    public ZIPPattern(){
        this.zipCodes = new HashSet<>();

        this.zipCodes.add("036");
        this.zipCodes.add("059");
        this.zipCodes.add("063");
        this.zipCodes.add("102");
        this.zipCodes.add("203");
        this.zipCodes.add("556");

        this.zipCodes.add("692");
        this.zipCodes.add("790");
        this.zipCodes.add("821");
        this.zipCodes.add("823");
        this.zipCodes.add("830");
        this.zipCodes.add("831");

        this.zipCodes.add("878");
        this.zipCodes.add("879");
        this.zipCodes.add("884");
        this.zipCodes.add("890");
        this.zipCodes.add("893");
    }

    @Override
    public boolean matches(String value){
        if(!value.contains("-"))
            return false;

        value = value.replaceAll("\\s+","").replaceAll("-", "");

        if(value.length() < 3)
            return false;

        String zipCode = value.substring(0, 3);
        return this.zipCodes.contains(zipCode);
    }
}
