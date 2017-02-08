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
