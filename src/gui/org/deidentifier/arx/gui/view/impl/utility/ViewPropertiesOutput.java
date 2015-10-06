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

package org.deidentifier.arx.gui.view.impl.utility;

import java.util.Arrays;
import java.util.Set;

import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.criteria.AverageReidentificationRisk;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.DistinctLDiversity;
import org.deidentifier.arx.criteria.EDDifferentialPrivacy;
import org.deidentifier.arx.criteria.EntropyLDiversity;
import org.deidentifier.arx.criteria.EqualDistanceTCloseness;
import org.deidentifier.arx.criteria.HierarchicalDistanceTCloseness;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.PopulationUniqueness;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.criteria.RecursiveCLDiversity;
import org.deidentifier.arx.criteria.RiskBasedCriterion;
import org.deidentifier.arx.criteria.SampleUniqueness;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.impl.common.ClipboardHandlerTree;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisData;
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
 * This view displays basic properties about output data.
 *
 * @author Fabian Prasser
 */
public class ViewPropertiesOutput extends ViewProperties {
    
    /**
     * A content provider.
     *
     * @author Fabian Prasser
     */
    private class OutputContentProvider implements ITreeContentProvider {

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
     * A content provider.
     *
     * @author Fabian Prasser
     */
    private class OutputLabelProvider implements ITableLabelProvider {

        @Override
        public void addListener(final ILabelProviderListener listener) {
            // Nothing to do
        }

        @Override
        public void dispose() {
            // Nothing to do
        }

        @Override
        public Image getColumnImage(final Object element, final int columnIndex) {
            return null;
        }

        @Override
        public String getColumnText(final Object element, final int columnIndex) {
            switch (columnIndex) {
            case 0:
                return ((Property) element).property;
            case 1:
                return ((Property) element).values[0];
            }
            return null;
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
     * Constructor.
     *
     * @param parent
     * @param controller
     */
    public ViewPropertiesOutput(final Composite parent,
                          final Controller controller) {
        
        super(parent, controller, ModelPart.OUTPUT, ModelPart.INPUT);
        create(parent);
        reset();
    }

    /**
     * Creates the view.
     *
     * @param group
     */
    private void create(final Composite group) {

        final Tree tree = new Tree(group, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        tree.setHeaderVisible(true);
        
        treeViewer = new TreeViewer(tree);
        tree.setMenu(new ClipboardHandlerTree(treeViewer).getMenu());
        
        final TreeColumn column1 = new TreeColumn(tree, SWT.LEFT);
        tree.setLinesVisible(true);
        column1.setAlignment(SWT.LEFT);
        column1.setText(Resources.getMessage("PropertiesView.1")); //$NON-NLS-1$
        column1.setWidth(160);
        final TreeColumn column2 = new TreeColumn(tree, SWT.RIGHT);
        column2.setAlignment(SWT.LEFT);
        column2.setText(Resources.getMessage("PropertiesView.2")); //$NON-NLS-1$
        column2.setWidth(100);

        treeViewer.setContentProvider(new OutputContentProvider());
        treeViewer.setLabelProvider(new OutputLabelProvider());

        treeViewer.setInput(roots);
        treeViewer.expandAll();
    }

    /**
     * Update the view.
     */
    protected void update() {

        AnalysisData context = getContext().getData();

        if (context == null ||
            context.config == null ||
            context.handle == null) {
            reset();
            return; 
        }
        
        // Disable redrawing
        root.setRedraw(false);
        
        // Obtain data
        ARXResult result = model.getResult();
        ARXNode node = model.getSelectedNode();
        
        // Clear
        roots.clear();
        
        // Print information loss
        if (node.getMaximumInformationLoss().getValue().equals( 
            node.getMinimumInformationLoss().getValue())) {
            
            final String infoloss = node.getMinimumInformationLoss().toString() +
                                    " [" + format.format(asRelativeValue(node.getMinimumInformationLoss(), result)) + "%]"; //$NON-NLS-1$ //$NON-NLS-2$
            new Property(Resources.getMessage("PropertiesView.46"), new String[] { infoloss }); //$NON-NLS-1$

        } 
        
        // Print basic info on neighboring nodes
        new Property(Resources.getMessage("PropertiesView.48"), new String[] { String.valueOf(node.getSuccessors().length) }); //$NON-NLS-1$
        new Property(Resources.getMessage("PropertiesView.49"), new String[] { String.valueOf(node.getPredecessors().length) }); //$NON-NLS-1$
        new Property(Resources.getMessage("PropertiesView.50"), new String[] { Arrays.toString(node.getTransformation()) }); //$NON-NLS-1$

        // If the node is anonymous
        if (node.getAnonymity() == Anonymity.ANONYMOUS) {

            // Print info about d-presence
            if (context.config.containsCriterion(DPresence.class)) {
                DPresence criterion = context.config.getCriterion(DPresence.class);
                // Only if its not an auto-generated criterion
                if (!(criterion.getDMin()==0d && criterion.getDMax()==1d)){
                    Property n = new Property(Resources.getMessage("PropertiesView.92"), new String[] { Resources.getMessage("PropertiesView.93") }); //$NON-NLS-1$ //$NON-NLS-2$
                    new Property(n, Resources.getMessage("PropertiesView.94"), new String[] { String.valueOf(criterion.getDMin())}); //$NON-NLS-1$
                    new Property(n, Resources.getMessage("PropertiesView.95"), new String[] { String.valueOf(criterion.getDMax())}); //$NON-NLS-1$
                }
            }
            // Print info about k-anonymity
            if (context.config.containsCriterion(KAnonymity.class)) {
                KAnonymity criterion = context.config.getCriterion(KAnonymity.class);
                Property n = new Property(Resources.getMessage("PropertiesView.51"), new String[] { Resources.getMessage("PropertiesView.52") }); //$NON-NLS-1$ //$NON-NLS-2$
                new Property(n, Resources.getMessage("PropertiesView.53"), new String[] { String.valueOf(criterion.getK())}); //$NON-NLS-1$
            }
            
            // Print info about (e,d)-dp
            if (context.config.containsCriterion(EDDifferentialPrivacy.class)) {
                EDDifferentialPrivacy criterion = context.config.getCriterion(EDDifferentialPrivacy.class);
                Property n = new Property(Resources.getMessage("PropertiesView.51"), new String[] { Resources.getMessage("PropertiesView.141") }); //$NON-NLS-1$ //$NON-NLS-2$
                new Property(n, Resources.getMessage("PropertiesView.142"), new String[] { String.valueOf(criterion.getEpsilon())}); //$NON-NLS-1$
                new Property(n, Resources.getMessage("PropertiesView.143"), new String[] { String.valueOf(criterion.getDelta())}); //$NON-NLS-1$
                new Property(n, Resources.getMessage("PropertiesView.144"), new String[] { String.valueOf(criterion.getK())}); //$NON-NLS-1$
                new Property(n, Resources.getMessage("PropertiesView.145"), new String[] { String.valueOf(criterion.getBeta())}); //$NON-NLS-1$
            }
            
            // Print info about l-diversity or t-closeness
            int index = 0;
            for (PrivacyCriterion c : context.config.getCriteria()) {
                if (c instanceof DistinctLDiversity){
                    DistinctLDiversity criterion = (DistinctLDiversity)c;
                    Property n = new Property(Resources.getMessage("PropertiesView.57"), new String[] { Resources.getMessage("PropertiesView.58") }); //$NON-NLS-1$ //$NON-NLS-2$
                    new Property(n, Resources.getMessage("PropertiesView.59"), new String[] { String.valueOf(criterion.getL()) }); //$NON-NLS-1$
                    new Property(n, Resources.getMessage("PropertiesView.100"), new String[] { criterion.getAttribute() }); //$NON-NLS-1$
                } else if (c instanceof EntropyLDiversity){
                    EntropyLDiversity criterion = (EntropyLDiversity)c;
                    Property n = new Property(Resources.getMessage("PropertiesView.63"), new String[] { Resources.getMessage("PropertiesView.64") }); //$NON-NLS-1$ //$NON-NLS-2$
                    new Property(n, Resources.getMessage("PropertiesView.65"), new String[] { String.valueOf(criterion.getL()) }); //$NON-NLS-1$
                    new Property(n, Resources.getMessage("PropertiesView.100"), new String[] { criterion.getAttribute() }); //$NON-NLS-1$
                } else if (c instanceof RecursiveCLDiversity){
                    RecursiveCLDiversity criterion = (RecursiveCLDiversity)c;
                    Property n = new Property(Resources.getMessage("PropertiesView.69"), new String[] { Resources.getMessage("PropertiesView.70") }); //$NON-NLS-1$ //$NON-NLS-2$
                    new Property(n, Resources.getMessage("PropertiesView.71"), new String[] { String.valueOf(criterion.getC()) }); //$NON-NLS-1$
                    new Property(n, Resources.getMessage("PropertiesView.72"), new String[] { String.valueOf(criterion.getL()) }); //$NON-NLS-1$
                    new Property(n, Resources.getMessage("PropertiesView.100"), new String[] { criterion.getAttribute() }); //$NON-NLS-1$
                } else if (c instanceof EqualDistanceTCloseness){
                    EqualDistanceTCloseness criterion = (EqualDistanceTCloseness)c;
                    Property n = new Property(Resources.getMessage("PropertiesView.77"), new String[] { Resources.getMessage("PropertiesView.78") }); //$NON-NLS-1$ //$NON-NLS-2$
                    new Property(n, Resources.getMessage("PropertiesView.79"), new String[] { String.valueOf(criterion.getT()) }); //$NON-NLS-1$
                    new Property(n, Resources.getMessage("PropertiesView.100"), new String[] { criterion.getAttribute() }); //$NON-NLS-1$
                } else if (c instanceof HierarchicalDistanceTCloseness){
                    HierarchicalDistanceTCloseness criterion = (HierarchicalDistanceTCloseness)c;
                    Property n = new Property(Resources.getMessage("PropertiesView.83"), new String[] { Resources.getMessage("PropertiesView.84") }); //$NON-NLS-1$ //$NON-NLS-2$
                    new Property(n, Resources.getMessage("PropertiesView.85"), new String[] { String.valueOf(criterion.getT()) }); //$NON-NLS-1$
                    new Property(n, Resources.getMessage("PropertiesView.100"), new String[] { criterion.getAttribute() }); //$NON-NLS-1$
                    final int height = context.config.getHierarchy(criterion.getAttribute()).getHierarchy()[0].length;
                    new Property(n, "SE-"+(index++), new String[] { Resources.getMessage("PropertiesView.87") + String.valueOf(height) }); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }

            // Print info about risk-based criteria
            Set<RiskBasedCriterion> criteria = context.config.getCriteria(RiskBasedCriterion.class);
            for (RiskBasedCriterion criterion : criteria) {
                
                String type = ""; //$NON-NLS-1$
                if (criterion instanceof AverageReidentificationRisk) {
                    type = Resources.getMessage("PropertiesView.123"); //$NON-NLS-1$
                } else if (criterion instanceof PopulationUniqueness) {
                    type = Resources.getMessage("PropertiesView.125"); //$NON-NLS-1$
                } else if (criterion instanceof SampleUniqueness) {
                    type = Resources.getMessage("PropertiesView.124"); //$NON-NLS-1$
                }
                        
                Property n = new Property(Resources.getMessage("PropertiesView.51"), new String[] { type }); //$NON-NLS-1$ //$NON-NLS-2$
                new Property(n, Resources.getMessage("PropertiesView.120"), new String[] { String.valueOf(criterion.getRiskThreshold())}); //$NON-NLS-1$
                
                if (criterion instanceof PopulationUniqueness) {
                    new Property(n, Resources.getMessage("PropertiesView.121"), new String[] { String.valueOf(((PopulationUniqueness)criterion).getPopulationModel().getSamplingFraction(context.handle))}); //$NON-NLS-1$
                    new Property(n, Resources.getMessage("PropertiesView.122"), new String[] { ((PopulationUniqueness)criterion).getStatisticalModel().toString()}); //$NON-NLS-1$
                }
            }
        } else {
            new Property(Resources.getMessage("PropertiesView.90"), new String[] { Resources.getMessage("PropertiesView.91") }); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // Initialize
        refresh();
        
        // Redraw
        root.setRedraw(true);
    }
}
