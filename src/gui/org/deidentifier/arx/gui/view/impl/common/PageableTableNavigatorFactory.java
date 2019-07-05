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
package org.deidentifier.arx.gui.view.impl.common;

import org.eclipse.nebula.widgets.pagination.PageableController;
import org.eclipse.nebula.widgets.pagination.renderers.ICompositeRendererFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Adapted from the Nebula source. 
 * 
 * Contributors:
 * Angelo ZERR - initial API and implementation
 * Pascal Leclercq - initial API and implementation
 */
public class PageableTableNavigatorFactory implements ICompositeRendererFactory {

    /** Number of  items*/
    private static final PageableTableNavigatorFactory FACTORY = new PageableTableNavigatorFactory();
    
    /**
     * Returns the factory
     */
    public static PageableTableNavigatorFactory getFactory() {
        return FACTORY;
    }

    /**
     * Creates the composite
     */
    public Composite createComposite(Composite parent, int style, PageableController controller) {
        return new PageableTableNavigator(parent, SWT.NONE, controller);
    }
}