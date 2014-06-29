/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
 * This view displays basic properties about input data
 * 
 * @author Fabian Prasser
 */
public class ViewInputProperties extends ViewProperties {

    /**
     * A content provider
     * @author Fabian Prasser
     */
    private class InputContentProvider implements ITreeContentProvider {

        @Override
        public void dispose() {
            // Nothing to do
        }

        @Override
        public Object[] getChildren(final Object arg0) {
            return ((Property) arg0).children.toArray();
        }

        @Override
        public Object[] getElements(final Object arg0) {
            return roots.toArray();
        }

        @Override
        public Object getParent(final Object arg0) {
            return ((Property) arg0).parent;
        }

        @Override
        public boolean hasChildren(final Object arg0) {
            return !((Property) arg0).children.isEmpty();
        }

        @Override
        public void inputChanged(final Viewer arg0,
                                 final Object arg1,
                                 final Object arg2) {
            // Nothing to do
        }

    }

    /**
     * A label provider
     * @author Fabian Prasser
     *
     */
    private class InputLabelProvider implements ITableLabelProvider {

        @Override
        public void addListener(final ILabelProviderListener listener) {
            // Nothing to do
        }

        @Override
        public void dispose() {
            // Nothing to do
        }

        @Override
        public Image
                getColumnImage(final Object element, final int columnIndex) {
            return null;
        }

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

        @Override
        public boolean isLabelProperty(final Object element,
                                       final String property) {
            return false;
        }

        @Override
        public void removeListener(final ILabelProviderListener listener) {
            // Nothing to do
        }
    }

    
    /**
     * Constructor
     * @param parent
     * @param controller
     */
    public ViewInputProperties(final Composite parent,
                               final Controller controller) {
        
        super(parent, controller, ModelPart.INPUT, null);
        create(parent);
        reset();
    }

    /**
     * Creates the view
     * @param group
     */
    private void create(final Composite group) {

        Tree tree = new Tree(group, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        tree.setHeaderVisible(true);
        treeViewer = new TreeViewer(tree);

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
     * Update the view
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
        new Property(Resources.getMessage("PropertiesView.114"), new String[] { config.getMetric().getName() }); //$NON-NLS-1$
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
                    values[3] = String.valueOf(definition.getHierarchyHeight(s));
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
