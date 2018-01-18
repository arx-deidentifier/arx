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

package org.deidentifier.arx.gui.view.impl.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataGeneralizationScheme;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IDialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import de.linearbits.swt.table.DynamicTable;
import de.linearbits.swt.table.DynamicTableColumn;

/**
 * A dialog to select a generalization scheme
 *
 * @author Fabian Prasser
 */
public class DialogGeneralizationSelection extends TitleAreaDialog implements IDialog {

    /** Selection */
    private String                  attribute   = null;

    /** Selection */
    private TableItem               item        = null;

    /** Map */
    private Map<String, Integer>    selection   = new HashMap<String, Integer>();

    /** Map */
    private Map<String, String[][]> hierarchies = new HashMap<String, String[][]>();

    /** List */
    private List<String>            attributes = new ArrayList<String>();

    /** Model */
    private DataDefinition          definition;

    /** Button */
    private Button                  ok;

    /**
     * Constructor.
     *
     * @param shell
     * @param controller
     * @param model
     */
    public DialogGeneralizationSelection(final Shell shell, 
                                         final Controller controller,
                                         final Model model,
                                         final DataGeneralizationScheme scheme) {
        super(shell);
        definition = model.getInputDefinition();
        DataHandle handle = model.getInputConfig().getInput().getHandle();
        for (int i=0; i<handle.getNumColumns(); i++) {
            String attribute = handle.getAttributeName(i);
            if (definition.getQuasiIdentifiersWithGeneralization().contains(attribute)) {
                attributes.add(attribute);
                selection.put(attribute, scheme.getGeneralizationLevel(attribute, definition));
                hierarchies.put(attribute, definition.getHierarchy(attribute));
            }
        }
    }

    @Override
    public boolean close() {
        return super.close();
    }

    /**
     * Returns the selected element
     * @return
     */
    public Map<String, Integer> getSelection() {
        return selection;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setImages(Resources.getIconSet(newShell.getDisplay()));
    }
    
    @Override
    protected void createButtonsForButtonBar(final Composite parent) {

        // Create OK Button
        ok = createButton(parent,
                          Window.OK,
                          Resources.getMessage("ProjectDialog.3"), true); //$NON-NLS-1$
        ok.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                setReturnCode(Window.OK);
                close();
            }
        });
        ok.setEnabled(true);
    }
    
    @Override
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        setTitle(Resources.getMessage("DialogGeneralizationSelection.1")); //$NON-NLS-1$
        setMessage(Resources.getMessage("DialogGeneralizationSelection.0"), IMessageProvider.INFORMATION); //$NON-NLS-1$
        return contents;
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        parent.setLayout(SWTUtil.createGridLayout(1));

        // Create table
        final DynamicTable table = SWTUtil.createTableDynamic(parent, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        GridData data = SWTUtil.createFillGridData();
        data.heightHint = 300;
        table.setLayoutData(data);
        
        // Create column
        DynamicTableColumn column1 = new DynamicTableColumn(table, SWT.NONE);
        column1.setText(Resources.getMessage("DialogGeneralizationSelection.2")); //$NON-NLS-1$
        column1.setWidth("33%", "30px"); //$NON-NLS-1$ //$NON-NLS-2$
        
        DynamicTableColumn column2 = new DynamicTableColumn(table, SWT.NONE);
        column2.setText(Resources.getMessage("DialogGeneralizationSelection.3")); //$NON-NLS-1$
        column2.setWidth("33%", "30px"); //$NON-NLS-1$ //$NON-NLS-2$
        
        
        DynamicTableColumn column3 = new DynamicTableColumn(table, SWT.NONE);
        column3.setText(Resources.getMessage("DialogGeneralizationSelection.4")); //$NON-NLS-1$
        column3.setWidth("33%", "30px"); //$NON-NLS-1$ //$NON-NLS-2$
        
        column1.pack();
        column2.pack();
        column3.pack();
        
        // Add
        for (String attribute : attributes) {
            TableItem item = new TableItem(table, SWT.NONE);
            int level = selection.get(attribute);
            item.setText(new String[] {attribute, String.valueOf(level), hierarchies.get(attribute)[0][level]});
        }
        

        final Menu menu = new Menu(table);
        
        table.setMenu(menu);
        menu.addMenuListener(new MenuAdapter() {
            public void menuShown(MenuEvent e) {
                
                MenuItem[] items = menu.getItems();
                for (int i = 0; i < items.length; i++) {
                    items[i].dispose();
                }
                
                if (attribute != null) {
                    
                    for (int i=0; i<hierarchies.get(attribute)[0].length; i++) {
                        MenuItem newItem = new MenuItem(menu, SWT.NONE);
                        newItem.setText("Level " + i);
                        final int level = i;
                        newItem.addSelectionListener(new SelectionAdapter(){
                            @Override
                            public void widgetSelected(SelectionEvent arg0) {
                                selection.put(attribute,  level);
                                item.setText(1, String.valueOf(level));
                                item.setText(2, hierarchies.get(attribute)[0][level]);
                            }
                        });
                    }
                }
            }
        });

        
        table.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (table.getSelectionIndex() != -1) {
                    item = table.getItems()[table.getSelectionIndex()];
                    attribute = item.getText(0);
                    
                } else {
                    item = null;
                    attribute = null;
                }
            }
        });
        
        return parent;
    }
    
    @Override
    protected boolean isResizable() {
        return true;
    }
}
