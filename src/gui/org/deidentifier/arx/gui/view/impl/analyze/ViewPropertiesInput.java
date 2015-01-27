/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.arx.gui.view.impl.analyze;

import java.text.DecimalFormat;

import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeWithFormat;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelConfiguration;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.impl.common.ClipboardHandlerTree;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * This view displays basic properties about input data.
 *
 * @author Fabian Prasser
 */
public class ViewPropertiesInput extends ViewProperties {

    /**
     * A content provider.
     *
     * @author Fabian Prasser
     */
    private class InputContentProvider implements ITreeContentProvider {

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        @Override
        public void dispose() {
            // Nothing to do
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
         */
        @Override
        public Object[] getChildren(final Object arg0) {
            return ((Property) arg0).children.toArray();
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(java.lang.Object)
         */
        @Override
        public Object[] getElements(final Object arg0) {
            return roots.toArray();
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
         */
        @Override
        public Object getParent(final Object arg0) {
            return ((Property) arg0).parent;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
         */
        @Override
        public boolean hasChildren(final Object arg0) {
            return !((Property) arg0).children.isEmpty();
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
         */
        @Override
        public void inputChanged(final Viewer arg0,
                                 final Object arg1,
                                 final Object arg2) {
            // Nothing to do
        }

    }

    /**
     * A label provider.
     *
     * @author Fabian Prasser
     */
    private class InputLabelProvider implements ITableLabelProvider {

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
         */
        @Override
        public void addListener(final ILabelProviderListener listener) {
            // Nothing to do
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
         */
        @Override
        public void dispose() {
            // Nothing to do
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
         */
        @Override
        public Image
                getColumnImage(final Object element, final int columnIndex) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
         */
        @Override
        public String
                getColumnText(final Object element, final int columnIndex) {
            switch (columnIndex) {
            case 0:
                return ((Property) element).property;
            default:
                if ((columnIndex - 1) >= ((Property) element).values.length) {
                    return null;
                } else {
                    return ((Property) element).values[columnIndex - 1];
                }
            }
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
         */
        @Override
        public boolean isLabelProperty(final Object element,
                                       final String property) {
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
         */
        @Override
        public void removeListener(final ILabelProviderListener listener) {
            // Nothing to do
        }
    }

    
    /**
     * Constructor.
     *
     * @param parent
     * @param controller
     */
    public ViewPropertiesInput(final Composite parent,
                               final Controller controller) {
        
        super(parent, controller, ModelPart.INPUT, null);
        create(parent);
        reset();
    }

    /**
     * Creates the view.
     *
     * @param group
     */
    private void create(final Composite group) {

        Tree tree = new Tree(group, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        tree.setHeaderVisible(true);
        
        treeViewer = new TreeViewer(tree);
        tree.setMenu(new ClipboardHandlerTree(treeViewer).getMenu());
        
        final TreeColumn column1 = new TreeColumn(tree, SWT.LEFT);
        tree.setLinesVisible(true);
        column1.setAlignment(SWT.LEFT);
        column1.setText(Resources.getMessage("PropertiesView.3")); //$NON-NLS-1$
        column1.setWidth(160);
        final TreeColumn column2 = new TreeColumn(tree, SWT.RIGHT);
        column2.setAlignment(SWT.LEFT);
        column2.setText(Resources.getMessage("PropertiesView.4")); //$NON-NLS-1$
        column2.setWidth(100);
        final TreeColumn column6 = new TreeColumn(tree, SWT.RIGHT);
        column6.setAlignment(SWT.LEFT);
        column6.setText(Resources.getMessage("PropertiesView.5")); //$NON-NLS-1$
        column6.setWidth(100);
        final TreeColumn column7 = new TreeColumn(tree, SWT.RIGHT);
        column7.setAlignment(SWT.LEFT);
        column7.setText(Resources.getMessage("PropertiesView.101")); //$NON-NLS-1$
        column7.setWidth(80);
        final TreeColumn column3 = new TreeColumn(tree, SWT.RIGHT);
        column3.setAlignment(SWT.LEFT);
        column3.setText(Resources.getMessage("PropertiesView.6")); //$NON-NLS-1$
        column3.setWidth(50);
        final TreeColumn column4 = new TreeColumn(tree, SWT.RIGHT);
        column4.setAlignment(SWT.LEFT);
        column4.setText(Resources.getMessage("PropertiesView.7")); //$NON-NLS-1$
        column4.setWidth(50);
        final TreeColumn column5 = new TreeColumn(tree, SWT.RIGHT);
        column5.setAlignment(SWT.LEFT);
        column5.setText(Resources.getMessage("PropertiesView.8")); //$NON-NLS-1$
        column5.setWidth(50);
        final TreeColumn column8 = new TreeColumn(tree, SWT.RIGHT);
        column8.setAlignment(SWT.LEFT);
        column8.setText(Resources.getMessage("PropertiesView.113")); //$NON-NLS-1$
        column8.setWidth(50);

        treeViewer.setContentProvider(new InputContentProvider());
        treeViewer.setLabelProvider(new InputLabelProvider());

        treeViewer.setInput(roots);
        treeViewer.expandAll();
    }

    /**
     * Update the view.
     */
    protected void update() {

    	// Check model
        if (model == null) { return; }
        
        // Obtain definition
        DataDefinition definition = model.getOutputDefinition();
        if (definition == null) definition = model.getInputDefinition();

        // Obtain config
        ModelConfiguration config = model.getOutputConfig();
        if (config == null) config = model.getInputConfig();

        // Check
        if (definition == null || config == null || model.getInputConfig().getInput()==null){
        	reset();
            return;
        }

        // Obtain handle
        DataHandle data = model.getInputConfig().getInput().getHandle();
                
        // Disable redrawing
        root.setRedraw(false);
        
        // Clear
        roots.clear();
        
        // Print basic properties
        new Property(Resources.getMessage("PropertiesView.9"), new String[] { String.valueOf(data.getNumRows()) }); //$NON-NLS-1$
        new Property(Resources.getMessage("PropertiesView.10"), new String[] { String.valueOf(config.getAllowedOutliers() * 100d) + Resources.getMessage("PropertiesView.11") }); //$NON-NLS-1$ //$NON-NLS-2$
        new Property(Resources.getMessage("PropertiesView.114"), new String[] { config.getMetric().toString() }); //$NON-NLS-1$
        final Property attributes = new Property(Resources.getMessage("PropertiesView.12"), new String[] { String.valueOf(data.getNumColumns()) }); //$NON-NLS-1$
        
        // Print identifying attributes
        final Property identifying = new Property(attributes, Resources.getMessage("PropertiesView.13"), new String[] { String.valueOf(definition.getIdentifyingAttributes().size()) }); //$NON-NLS-1$
        int index = 0;
        for (int i = 0; i < data.getNumColumns(); i++) {
            final String s = data.getAttributeName(i);
            if (definition.getIdentifyingAttributes().contains(s)) {
                final String[] values = new String[] { "", "", "", "", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                values[0] = s;
                values[1] = definition.getDataType(s).toString();
                new Property(identifying, Resources.getMessage("PropertiesView.19") + (index++), values); //$NON-NLS-1$
            }
        }

        // Print quasi-identifying attributes
        final DecimalFormat format = new DecimalFormat("0.000");
        final Property quasiIdentifying = new Property(attributes, Resources.getMessage("PropertiesView.20"), new String[] { String.valueOf(definition.getQuasiIdentifyingAttributes().size()) }); //$NON-NLS-1$
        index = 0;
        for (int i = 0; i < data.getNumColumns(); i++) {
            final String s = data.getAttributeName(i);
            if (definition.getQuasiIdentifyingAttributes().contains(s)) {
                final String[] values = new String[] { "", "", "", "", "" , "", ""}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                values[0] = s;
                if (definition.getHierarchy(s) != null) {
                    DataType<?> type = definition.getDataType(s);
                    values[1] = type.getDescription().getLabel();
                    if (type.getDescription().hasFormat() && 
                        ((DataTypeWithFormat)type).getFormat() != null){
                        values[2] = ((DataTypeWithFormat)type).getFormat();
                    }
                    
                    // Determine height of hierarchy
                    int height = 0;
                    String[][] hierarchy = definition.getHierarchy(s);
                    if (hierarchy != null && hierarchy.length != 0 && hierarchy[0] != null) {
                        height = hierarchy[0].length;
                    }
                    values[3] = String.valueOf(height);
                    values[4] = String.valueOf(definition.getMinimumGeneralization(s));
                    values[5] = String.valueOf(definition.getMaximumGeneralization(s));
                }
                values[6] = format.format(config.getAttributeWeight(s));
                new Property(quasiIdentifying, Resources.getMessage("PropertiesView.26") + (index++), values); //$NON-NLS-1$
            }
        }
        
        // Print sensitive attributes
        final Property sensitive = new Property(attributes, Resources.getMessage("PropertiesView.27"), new String[] { String.valueOf(definition.getSensitiveAttributes().size()) }); //$NON-NLS-1$
        index = 0;
        for (int i = 0; i < data.getNumColumns(); i++) {
            final String s = data.getAttributeName(i);
            if (definition.getSensitiveAttributes().contains(s)) {
                final String[] values = new String[] { "", "", "", "", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                values[0] = s;
                if (config.getHierarchy(s) != null) {
                    int height = 0;
                    if (config.getHierarchy(s).getHierarchy().length > 0) {
                        height = config.getHierarchy(s).getHierarchy()[0].length;
                    }
                    values[1] = definition.getDataType(s).toString();
                    values[2] = String.valueOf(height);
                }
                new Property(sensitive, Resources.getMessage("PropertiesView.33") + (index++), values); //$NON-NLS-1$
            }
        }

        // Print insensitive attributes
        final Property insensitive = new Property(attributes, Resources.getMessage("PropertiesView.34"), new String[] { String.valueOf(definition.getInsensitiveAttributes().size()) }); //$NON-NLS-1$

        index = 0;
        for (int i = 0; i < data.getNumColumns(); i++) {
            final String s = data.getAttributeName(i);
            if (definition.getInsensitiveAttributes().contains(s)) {
                final String[] values = new String[] { "", "", "", "", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                values[0] = s;
                values[1] = definition.getDataType(s).toString();
                new Property(insensitive, Resources.getMessage("PropertiesView.40") + (index++), values); //$NON-NLS-1$
            }
        }

        // Refresh and initialize
        treeViewer.refresh();
        treeViewer.expandAll();

        // Redraw
        root.setRedraw(true);
    }
}
