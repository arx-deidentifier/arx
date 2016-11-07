/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
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
package org.deidentifier.arx.certificate.elements;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import rst.pdfbox.layout.util.CompatibilityHelper;

/**
 * PDF bullet list
 * 
 * @author Annika Saken
 * @author Fabian Prasser
 */
public class ElementListBullet extends ElementListAbstract {
    
	String bulletOdd = CompatibilityHelper.getBulletCharacter(1) + " ";
	String bulletEven = CompatibilityHelper.getBulletCharacter(2) + " ";

	@Override
	public String getListItemIndicator(int deep) {
		return deep % 2 == 1 ? bulletOdd : bulletEven;
	}
	
	@Override
	protected PDFont getListItemIndicatorFont() {
		PDFont font = PDType1Font.TIMES_BOLD;
		return font;
	}
}
