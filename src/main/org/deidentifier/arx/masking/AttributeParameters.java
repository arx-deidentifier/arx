/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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

/**
 * This class is used to map attributes to their MaskingType and related
 * configuration options, used in MaskingConfiguration.java
 * 
 * @author Sandro Schaeffler
 * @author Peter Bock
 */
public class AttributeParameters {

	/** Masking type */
	public MaskingType maskingType = MaskingType.SUPPRESSED;

	/** 0 as default value represents the "Identity" Distribution */
	public int selectedDistributionIndex = 0;

	/** String length */
	public int stringLength = 15;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "MaskingType: " + maskingType.getLabel() + ", Distribution: " + selectedDistributionIndex;
	}

}
