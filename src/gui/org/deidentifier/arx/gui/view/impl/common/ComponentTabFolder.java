/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2021 Fabian Prasser and contributors
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

/**
 * This wrapper around CTabFolder fixes SWT bug 507611 for ARX
 * TODO: Check whether this can be removed in future releases
 *  
 * @author Fabian Prasser
 */
public class ComponentTabFolder extends CTabFolder {

    /**
     * Creates a new instance
     * @param parent
     * @param style
     */
    public ComponentTabFolder(Composite parent, int style) {
        super(parent, style);
    }

    @Override
    public Rectangle getClientArea() {
        
        try {
            updateTabHeight();
        } catch (Exception e) {
            /* Catch silently, as it's just a bugfix */
        }
        
        // Now call method in superclass
        return super.getClientArea();
    }

    /**
     * Call private methods to fix bug in CTabFolder. See:
     * 
     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=507611
     * 
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    private void updateTabHeight() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        
        // Update buttons
        Method updateButtons = this.getClass().getSuperclass().getDeclaredMethod("updateButtons");
        updateButtons.setAccessible(true);
        updateButtons.invoke(this);
        
        // Update tab height
        Method updateTabHeight = this.getClass().getSuperclass().getDeclaredMethod("updateTabHeight", boolean.class);
        updateTabHeight.setAccessible(true);
        updateTabHeight.invoke(this, false);
    }
}
