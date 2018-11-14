package org.deidentifier.arx.masking;

/** 
 * This class is used to map attributes to their MaskingType and related configuration options, used in MaskingConfiguration.java
 * 
 * @author Sandro Schaeffler
 * @author Peter Bock
 */

public class AttributeParameters{
	
	private MaskingType maskingType;
	
	private int stringLength=-1;
	
	private int selectedDistributionIndex=0;	//0 as default value represensts the "Identity" Distribution
	
	public AttributeParameters(MaskingType maskingType)
	{
		this.maskingType=maskingType;
	}
	
	public AttributeParameters(MaskingType maskingType,int stringLength ,int selectedDistributionIndex)
	{
		this.maskingType=maskingType;
		this.stringLength=stringLength;
		this.selectedDistributionIndex=selectedDistributionIndex;
	}
	
	public MaskingType getMaskingType() {
		return maskingType;
	}
	
	public int getDistributionIndex()
	{
		return selectedDistributionIndex;
	}
	
	public int getStringLength()
	{
		return stringLength;
	}
	
	public void setDistribution(int distributionIndex)
	{
		this.selectedDistributionIndex=distributionIndex;
	}
	
	public void setStringLength(int stringLength)
	{
		this.stringLength=stringLength;
	}
	
	@Override
	public String toString()
	{
		return "MaskingType: "+maskingType.getLabel()+", Distribution: "+selectedDistributionIndex;
	}
	
}