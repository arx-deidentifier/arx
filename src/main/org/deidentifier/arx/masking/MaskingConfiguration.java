package org.deidentifier.arx.masking;

import java.util.HashMap;
import java.util.Map;

/**
 * Class containing the masking configuration, i.e. a map of attributes, MaskingTypes related configuration options
 *
 * @author Sandro Schaeffler
 * @author Peter Bock
 */
public class MaskingConfiguration {

    private static Map<String, AttributeParameters> maskings = new HashMap<String, AttributeParameters>();

    public static void addMasking(String attribute, AttributeParameters attributeParameters) {

        maskings.put(attribute, attributeParameters);

    }
    public static void addMasking(String attribute, MaskingType maskingType) {

        maskings.put(attribute, new AttributeParameters(maskingType));
    }
    public static Map<String, AttributeParameters> getMapping()
    {
    	return maskings;
    }
    public static int size()
    {
    	return maskings.size();
    }
    
    public static void addDistribution(String attribute, int distributionIndex)
    {
    	maskings.get(attribute).setDistribution(distributionIndex);
    	System.out.println(maskings);
    }

    public static void removeMasking(String attribute) {

        maskings.remove(attribute);
    }

    public static MaskingType getMaskingType(String attribute) {
    	
    	AttributeParameters parameter;
    	parameter = maskings.get(attribute);
    	if (parameter==null)
    		return MaskingType.SUPPRESSED;
    	else
    		return parameter.getMaskingType();

    }
    public static int getDistributionIndex(String attribute) {
    	
    	AttributeParameters parameter;
    	parameter = maskings.get(attribute);
    	if (parameter==null)
    	{
    		return 0;	//0 equals "Identity" Distribution
    	}
    	else
    		return parameter.getDistributionIndex();
    }

}
