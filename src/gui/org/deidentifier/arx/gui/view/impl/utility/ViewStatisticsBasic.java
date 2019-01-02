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
package org.deidentifier.arx.gui.view.impl.utility;

import org.deidentifier.arx.gui.view.def.IView;
import org.eclipse.swt.widgets.Composite;

/**
 * This is a base interface for simple views in this category
 *
 * @author Fabian Prasser
 */
public interface ViewStatisticsBasic extends IView {

    /**
     * Returns the parent composite
     * @return
     */
    public Composite getParent();
    
    /**
     * Returns the view type
     * @return
     */
    public LayoutUtility.ViewUtilityType getType();

}
