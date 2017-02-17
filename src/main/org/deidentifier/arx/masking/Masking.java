/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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

package org.deidentifier.arx.masking;

import java.util.ArrayList;
import java.util.List;

/**
 * Class describing a masking
 *
 * @author Karol Babioch
 */
public class Masking {

    private MaskingType maskingType;

    private List<MaskingParameter<?>> parameters = new ArrayList<>();

    public Masking(MaskingType maskingType) {

        this.maskingType = maskingType;

    }

    public MaskingType getMaskingType() {

        return maskingType;

    }

    public void addParameter(MaskingParameter<?> parameter) {

        parameters.add(parameter);

    }

    public void removeParameter(MaskingParameter<?> parameter) {

        parameters.remove(parameter);

    }

    public List<MaskingParameter<?>> getParameters() {

        return parameters;

    }

}
