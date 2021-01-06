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

import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

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
        
        // Add refresh functionality for MacOS
        if (SWTUtil.isMac() && parent instanceof Shell) {
            
            // Print
            System.out.println("Installing update service");
            
            // Register trigger
            this.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetDefaultSelected(SelectionEvent arg0) {
                    widgetSelected(arg0);
                }

                @Override
                public void widgetSelected(SelectionEvent arg0) {
                    
                    CTabItem item = ComponentTabFolder.this.getSelection();
                    if (item == null) {
                        return;
                    }
                    
                    ComponentTabFolder.this.getDisplay().timerExec(1000, new Runnable() {
                       @Override
                        public void run() {
                           System.out.println("Forcing redraw on folder: " + item.getText());
                            try {
                                forceRedraw(ComponentTabFolder.this);
                            } catch (NoSuchMethodException | SecurityException
                                    | IllegalAccessException | IllegalArgumentException
                                    | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        }
    }

    /**
     * Force redraw on anything
     * @param componentTabFolder
     * @throws InvocationTargetException 
     * @throws IllegalArgumentException 
     * @throws IllegalAccessException 
     * @throws SecurityException 
     * @throws NoSuchMethodException 
     */
    private void forceRedraw(Control control) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (control instanceof CTabFolder) {
            redraw((CTabFolder) control);
        } else {
            redraw(control);
        }
        if (control instanceof Composite) {
            for (Control c : ((Composite) control).getChildren()) {
                forceRedraw(c);
            }
        }
    } 

    /**
     * Redraw
     * @param control
     */
    private void redraw(Control control) {
        control.update();
        control.redraw();
    }

    /**
     * Redraw a tab folder
     * @param folder
     * @throws SecurityException 
     * @throws NoSuchMethodException 
     * @throws InvocationTargetException 
     * @throws IllegalArgumentException 
     * @throws IllegalAccessException 
     */
    private void redraw(CTabFolder folder) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        // Request layout
        folder.requestLayout();

        // Update
        Method updateFolder = this.getClass().getSuperclass().getDeclaredMethod("updateFolder", int.class);
        updateFolder.setAccessible(true);
        updateFolder.invoke(folder, (1 << 1) | (1 << 2) | (1 << 3));
        
        // Redraw
        folder.update();
        folder.layout(true, true);
        folder.redraw();
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
