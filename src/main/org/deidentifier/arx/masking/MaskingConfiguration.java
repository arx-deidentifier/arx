package org.deidentifier.arx.masking;

import java.util.HashMap;
import java.util.Map;

/**
 * Class describing a masking configuration
 *
 * @author Karol Babioch
 */
public class MaskingConfiguration {

    private Map<String, Masking> maskings = new HashMap<String, Masking>();

    public void addMasking(String attribute, Masking masking) {

        maskings.put(attribute, masking);

    }

    public void removeMasking(String attribute) {

        maskings.remove(attribute);

    }

    public Map<String, Masking> getMaskings() {

        return maskings;

    }

    public Masking getMasking(String attribute) {

        return maskings.get(attribute);

    }

}
