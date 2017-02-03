package org.deidentifier.arx.masking;

/**
 * Class describing a masking configuration
 *
 * @author Karol Babioch
 */
public class Masking {

    public static enum MaskingType {

        PERTURBATION,

    };

    private MaskingType maskingType;

    public void setMaskingType(MaskingType maskingType) {

        this.maskingType = maskingType;

    }

    public MaskingType getMaskingType() {

        return this.maskingType;

    }

}
