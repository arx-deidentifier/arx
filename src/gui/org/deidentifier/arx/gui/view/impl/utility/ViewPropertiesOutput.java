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

import java.util.Arrays;
import java.util.Set;

import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.criteria.AverageReidentificationRisk;
import org.deidentifier.arx.criteria.BasicBLikeness;
import org.deidentifier.arx.criteria.DDisclosurePrivacy;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.DistinctLDiversity;
import org.deidentifier.arx.criteria.EDDifferentialPrivacy;
import org.deidentifier.arx.criteria.EnhancedBLikeness;
import org.deidentifier.arx.criteria.EntropyLDiversity;
import org.deidentifier.arx.criteria.EqualDistanceTCloseness;
import org.deidentifier.arx.criteria.HierarchicalDistanceTCloseness;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.KMap;
import org.deidentifier.arx.criteria.MinimumKeySize;
import org.deidentifier.arx.criteria.OrderedDistanceTCloseness;
import org.deidentifier.arx.criteria.PopulationUniqueness;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.criteria.ProfitabilityJournalist;
import org.deidentifier.arx.criteria.ProfitabilityJournalistNoAttack;
import org.deidentifier.arx.criteria.ProfitabilityProsecutor;
import org.deidentifier.arx.criteria.ProfitabilityProsecutorNoAttack;
import org.deidentifier.arx.criteria.RecursiveCLDiversity;
import org.deidentifier.arx.criteria.RiskBasedCriterion;
import org.deidentifier.arx.criteria.SampleUniqueness;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.common.ClipboardHandlerTree;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisData;
import org.deidentifier.arx.metric.v2.QualityMetadata;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
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
     * Returns the view type
     * @return
     */
    public LayoutUtility.ViewUtilityType getType() {
        return LayoutUtility.ViewUtilityType.PROPERTIES;
    }

    /**
     * Creates the view.
     *
     * @param root
     */
    private void create(final Composite root) {

        root.setLayout(new FillLayout());
        
        final Tree tree = new Tree(root, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
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
     * @param part
     */
    protected void doUpdate(ModelPart part) {

        AnalysisData context = getContext().getData();

        if (context == null ||
            context.config == null ||
            context.handle == null) {
            reset();
            return; 
        }

        // We don't need to update in many cases
        if (part == ModelPart.ATTRIBUTE_TYPE || part == ModelPart.ATTRIBUTE_TYPE_BULK_UPDATE ||
            part == ModelPart.METRIC || part == ModelPart.ATTRIBUTE_WEIGHT || part == ModelPart.GS_FACTOR ||
            part == ModelPart.MAX_OUTLIERS || part == ModelPart.DATA_TYPE ||
            part == ModelPart.COST_BENEFIT_MODEL) {
            return;
        }
        
        // Disable redrawing
        root.setRedraw(false);
        
        // Obtain data
        ARXResult result = model.getResult();
        ARXNode node = model.getSelectedNode();
        
        // Clear
        roots.clear();
        
        // Print stats
        if (model.getProcessStatistics().isLocalTransformation()) {
            
            // Local transformation
            new Property(Resources.getMessage("PropertiesView.174"), new String[] { SWTUtil.getPrettyString(true) }); //$NON-NLS-1$
            new Property(Resources.getMessage("PropertiesView.175"), new String[] { String.valueOf(model.getProcessStatistics().getNumberOfSteps()) }); //$NON-NLS-1$
        } else {
            
            // Print score
            if (node.getHighestScore().getValue().equals( node.getLowestScore().getValue())) {
                
                final String infoloss = node.getLowestScore().toString() +
                                        " [" + SWTUtil.getPrettyString(asRelativeValue(node.getLowestScore(), result)) + "%]"; //$NON-NLS-1$ //$NON-NLS-2$
                Property score = new Property(Resources.getMessage("PropertiesView.46"), new String[] { infoloss }); //$NON-NLS-1$
    
                // Print metadata
                if (node.isChecked()) {
                    for (QualityMetadata<?> metadata : node.getLowestScore().getMetadata()) {
                        new Property(score, metadata.getParameter(), new String[] { SWTUtil.getPrettyString(metadata.getValue()) });
                    }
                }
            } 
            
            // Print basic info on neighboring nodes
            new Property(Resources.getMessage("PropertiesView.48"), new String[] { String.valueOf(node.getSuccessors().length) }); //$NON-NLS-1$
            new Property(Resources.getMessage("PropertiesView.49"), new String[] { String.valueOf(node.getPredecessors().length) }); //$NON-NLS-1$
            new Property(Resources.getMessage("PropertiesView.50"), new String[] { Arrays.toString(node.getTransformation()) }); //$NON-NLS-1$
        }
        
        // If the node is anonymous
        if (node.getAnonymity() == Anonymity.ANONYMOUS) {

            // Print info about d-presence
            if (context.config.containsCriterion(DPresence.class)) {
                DPresence criterion = context.config.getCriterion(DPresence.class);
                // Only if its not an auto-generated criterion
                if (!(criterion.getDMin()==0d && criterion.getDMax()==1d)){
                    Property n = new Property(Resources.getMessage("PropertiesView.92"), new String[] { Resources.getMessage("PropertiesView.93") }); //$NON-NLS-1$ //$NON-NLS-2$
                    new Property(n, Resources.getMessage("PropertiesView.94"), new String[] { SWTUtil.getPrettyString(criterion.getDMin())}); //$NON-NLS-1$
                    new Property(n, Resources.getMessage("PropertiesView.95"), new String[] { SWTUtil.getPrettyString(criterion.getDMax())}); //$NON-NLS-1$
                }
            }
            // Print info about k-anonymity
            if (context.config.containsCriterion(KAnonymity.class)) {
                KAnonymity criterion = context.config.getCriterion(KAnonymity.class);
                Property n = new Property(Resources.getMessage("PropertiesView.51"), new String[] { Resources.getMessage("PropertiesView.52") }); //$NON-NLS-1$ //$NON-NLS-2$
                new Property(n, Resources.getMessage("PropertiesView.53"), new String[] { SWTUtil.getPrettyString(criterion.getK())}); //$NON-NLS-1$
            }
            // Print info about minimum key size model
            if (context.config.containsCriterion(MinimumKeySize.class)) {
                MinimumKeySize criterion = context.config.getCriterion(MinimumKeySize.class);
                Property n = new Property(Resources.getMessage("PropertiesView.51"), new String[] { Resources.getMessage("PropertiesView.177") }); //$NON-NLS-1$ //$NON-NLS-2$
                new Property(n, Resources.getMessage("PropertiesView.178"), new String[] { SWTUtil.getPrettyString(criterion.getMinimumKeySize())}); //$NON-NLS-1$
            }
            // Print info about k-map
            if (context.config.containsCriterion(KMap.class)) {
                KMap criterion = context.config.getCriterion(KMap.class);
                Property n = new Property(Resources.getMessage("PropertiesView.51"), new String[] { Resources.getMessage("PropertiesView.132") }); //$NON-NLS-1$ //$NON-NLS-2$
                new Property(n, Resources.getMessage("PropertiesView.53"), new String[] { SWTUtil.getPrettyString(criterion.getK())}); //$NON-NLS-1$
                if (!criterion.isAccurate()) {
                    new Property(n, Resources.getMessage("PropertiesView.146"), new String[] { SWTUtil.getPrettyString(criterion.getDerivedK()) }); //$NON-NLS-1$
                    new Property(n, Resources.getMessage("PropertiesView.133"), new String[] { SWTUtil.getPrettyString(((KMap)criterion).getPopulationModel().getPopulationSize())}); //$NON-NLS-1$
                    new Property(n, Resources.getMessage("PropertiesView.147"), new String[] { SWTUtil.getPrettyString(criterion.getSignificanceLevel()) }); //$NON-NLS-1$
                    new Property(n, Resources.getMessage("PropertiesView.148"), new String[] { SWTUtil.getPrettyString(criterion.getType1Error()) }); //$NON-NLS-1$
                }
            }
            // Print info about game-theoretic privacy
            if (context.config.containsCriterion(ProfitabilityProsecutor.class) ||
                context.config.containsCriterion(ProfitabilityJournalist.class) ||
                context.config.containsCriterion(ProfitabilityProsecutorNoAttack.class) ||
                context.config.containsCriterion(ProfitabilityJournalistNoAttack.class)) {
                
                Property n = new Property(Resources.getMessage("PropertiesView.51"), new String[] { Resources.getMessage("PropertiesView.134") }); //$NON-NLS-1$ //$NON-NLS-2$
                new Property(n, Resources.getMessage("PropertiesView.135"), new String[] { SWTUtil.getPrettyString(context.config.getPublisherBenefit())}); //$NON-NLS-1$
                new Property(n, Resources.getMessage("PropertiesView.136"), new String[] { SWTUtil.getPrettyString(context.config.getPublisherLoss())}); //$NON-NLS-1$
                new Property(n, Resources.getMessage("PropertiesView.137"), new String[] { SWTUtil.getPrettyString(context.config.getAdversaryGain())}); //$NON-NLS-1$
                new Property(n, Resources.getMessage("PropertiesView.138"), new String[] { SWTUtil.getPrettyString(context.config.getAdversaryCost())}); //$NON-NLS-1$
                 
                if (context.config.containsCriterion(ProfitabilityJournalist.class) ||
                    context.config.containsCriterion(ProfitabilityJournalistNoAttack.class)) {
                    new Property(n, Resources.getMessage("PropertiesView.139"), new String[] { Resources.getMessage("PropertiesView.161") }); //$NON-NLS-1$ //$NON-NLS-2$
                } else if (context.config.containsCriterion(ProfitabilityProsecutor.class) ||
                    context.config.containsCriterion(ProfitabilityProsecutorNoAttack.class)) {
                    new Property(n, Resources.getMessage("PropertiesView.139"), new String[] { Resources.getMessage("PropertiesView.160") }); //$NON-NLS-1$ //$NON-NLS-2$
                } 
                if (context.config.containsCriterion(ProfitabilityJournalistNoAttack.class)) {
                    ProfitabilityJournalistNoAttack model = context.config.getCriterion(ProfitabilityJournalistNoAttack.class);
                    new Property(n, Resources.getMessage("PropertiesView.162"), new String[] { SWTUtil.getPrettyString(model.getK()) }); //$NON-NLS-1$
                } else  if (context.config.containsCriterion(ProfitabilityProsecutorNoAttack.class)) {
                    ProfitabilityProsecutorNoAttack model = context.config.getCriterion(ProfitabilityProsecutorNoAttack.class);
                    new Property(n, Resources.getMessage("PropertiesView.162"), new String[] { SWTUtil.getPrettyString(model.getK()) }); //$NON-NLS-1$
                }
            }
            
            // Print info about (e,d)-dp
            if (context.config.containsCriterion(EDDifferentialPrivacy.class)) {
                EDDifferentialPrivacy criterion = context.config.getCriterion(EDDifferentialPrivacy.class);
                Property n = new Property(Resources.getMessage("PropertiesView.51"), new String[] { Resources.getMessage("PropertiesView.141") }); //$NON-NLS-1$ //$NON-NLS-2$
                new Property(n, Resources.getMessage("PropertiesView.142"), new String[] { SWTUtil.getPrettyString(criterion.getEpsilon())}); //$NON-NLS-1$
                new Property(n, Resources.getMessage("PropertiesView.143"), new String[] { SWTUtil.getPrettyString(criterion.getDelta())}); //$NON-NLS-1$
                new Property(n, Resources.getMessage("PropertiesView.144"), new String[] { SWTUtil.getPrettyString(criterion.getK())}); //$NON-NLS-1$
                new Property(n, Resources.getMessage("PropertiesView.145"), new String[] { SWTUtil.getPrettyString(criterion.getBeta())}); //$NON-NLS-1$
            }
            
            // Print info about l-diversity, t-closeness and d-disclosure privacy
            int index = 0;
            for (PrivacyCriterion c : context.config.getCriteria()) {
                if (c instanceof DistinctLDiversity){
                    DistinctLDiversity criterion = (DistinctLDiversity)c;
                    Property n = new Property(Resources.getMessage("PropertiesView.57"), new String[] { Resources.getMessage("PropertiesView.58") }); //$NON-NLS-1$ //$NON-NLS-2$
                    new Property(n, Resources.getMessage("PropertiesView.59"), new String[] { SWTUtil.getPrettyString(criterion.getL()) }); //$NON-NLS-1$
                    new Property(n, Resources.getMessage("PropertiesView.100"), new String[] { criterion.getAttribute() }); //$NON-NLS-1$
                } else if (c instanceof EntropyLDiversity){
                    EntropyLDiversity criterion = (EntropyLDiversity)c;
                    Property n = new Property(Resources.getMessage("PropertiesView.63"), new String[] { Resources.getMessage("PropertiesView.64") }); //$NON-NLS-1$ //$NON-NLS-2$
                    new Property(n, Resources.getMessage("PropertiesView.164"), new String[] { criterion.getEstimator().toString() }); //$NON-NLS-1$
                    new Property(n, Resources.getMessage("PropertiesView.65"), new String[] { SWTUtil.getPrettyString(criterion.getL()) }); //$NON-NLS-1$
                    new Property(n, Resources.getMessage("PropertiesView.100"), new String[] { criterion.getAttribute() }); //$NON-NLS-1$
                } else if (c instanceof RecursiveCLDiversity){
                    RecursiveCLDiversity criterion = (RecursiveCLDiversity)c;
                    Property n = new Property(Resources.getMessage("PropertiesView.69"), new String[] { Resources.getMessage("PropertiesView.70") }); //$NON-NLS-1$ //$NON-NLS-2$
                    new Property(n, Resources.getMessage("PropertiesView.71"), new String[] { SWTUtil.getPrettyString(criterion.getC()) }); //$NON-NLS-1$
                    new Property(n, Resources.getMessage("PropertiesView.72"), new String[] { SWTUtil.getPrettyString(criterion.getL()) }); //$NON-NLS-1$
                    new Property(n, Resources.getMessage("PropertiesView.100"), new String[] { criterion.getAttribute() }); //$NON-NLS-1$
                } else if (c instanceof EqualDistanceTCloseness){
                    EqualDistanceTCloseness criterion = (EqualDistanceTCloseness)c;
                    Property n = new Property(Resources.getMessage("PropertiesView.77"), new String[] { Resources.getMessage("PropertiesView.78") }); //$NON-NLS-1$ //$NON-NLS-2$
                    new Property(n, Resources.getMessage("PropertiesView.79"), new String[] { SWTUtil.getPrettyString(criterion.getT()) }); //$NON-NLS-1$
                    new Property(n, Resources.getMessage("PropertiesView.100"), new String[] { criterion.getAttribute() }); //$NON-NLS-1$
                } else if (c instanceof OrderedDistanceTCloseness){
                    OrderedDistanceTCloseness criterion = (OrderedDistanceTCloseness)c;
                    Property n = new Property(Resources.getMessage("PropertiesView.77"), new String[] { Resources.getMessage("PropertiesView.163") }); //$NON-NLS-1$ //$NON-NLS-2$
                    new Property(n, Resources.getMessage("PropertiesView.79"), new String[] { SWTUtil.getPrettyString(criterion.getT()) }); //$NON-NLS-1$
                    new Property(n, Resources.getMessage("PropertiesView.100"), new String[] { criterion.getAttribute() }); //$NON-NLS-1$
                } else if (c instanceof HierarchicalDistanceTCloseness){
                    HierarchicalDistanceTCloseness criterion = (HierarchicalDistanceTCloseness)c;
                    Property n = new Property(Resources.getMessage("PropertiesView.83"), new String[] { Resources.getMessage("PropertiesView.84") }); //$NON-NLS-1$ //$NON-NLS-2$
                    new Property(n, Resources.getMessage("PropertiesView.85"), new String[] { SWTUtil.getPrettyString(criterion.getT()) }); //$NON-NLS-1$
                    new Property(n, Resources.getMessage("PropertiesView.100"), new String[] { criterion.getAttribute() }); //$NON-NLS-1$
                    final int height = context.config.getHierarchy(criterion.getAttribute()).getHierarchy()[0].length;
                    new Property(n, "SE-"+(index++), new String[] { Resources.getMessage("PropertiesView.87") + String.valueOf(height) }); //$NON-NLS-1$ //$NON-NLS-2$
                } else if (c instanceof DDisclosurePrivacy){
                    DDisclosurePrivacy criterion = (DDisclosurePrivacy)c;
                    Property n = new Property(Resources.getMessage("PropertiesView.83"), new String[] { Resources.getMessage("PropertiesView.130") }); //$NON-NLS-1$ //$NON-NLS-2$
                    new Property(n, Resources.getMessage("PropertiesView.131"), new String[] { SWTUtil.getPrettyString(criterion.getD()) }); //$NON-NLS-1$
                    new Property(n, Resources.getMessage("PropertiesView.100"), new String[] { criterion.getAttribute() }); //$NON-NLS-1$
                } else if (c instanceof BasicBLikeness){
                    BasicBLikeness criterion = (BasicBLikeness)c;
                    Property n = new Property(Resources.getMessage("PropertiesView.83"), new String[] { Resources.getMessage("PropertiesView.172") }); //$NON-NLS-1$ //$NON-NLS-2$
                    new Property(n, Resources.getMessage("PropertiesView.171"), new String[] { SWTUtil.getPrettyString(criterion.getB()) }); //$NON-NLS-1$
                    new Property(n, Resources.getMessage("PropertiesView.100"), new String[] { criterion.getAttribute() }); //$NON-NLS-1$
                } else if (c instanceof EnhancedBLikeness){
                    EnhancedBLikeness criterion = (EnhancedBLikeness)c;
                    Property n = new Property(Resources.getMessage("PropertiesView.83"), new String[] { Resources.getMessage("PropertiesView.173") }); //$NON-NLS-1$ //$NON-NLS-2$
                    new Property(n, Resources.getMessage("PropertiesView.171"), new String[] { SWTUtil.getPrettyString(criterion.getB()) }); //$NON-NLS-1$
                    new Property(n, Resources.getMessage("PropertiesView.100"), new String[] { criterion.getAttribute() }); //$NON-NLS-1$
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
                new Property(n, Resources.getMessage("PropertiesView.120"), new String[] { SWTUtil.getPrettyString(criterion.getRiskThreshold())}); //$NON-NLS-1$
                
                if (criterion instanceof PopulationUniqueness) {
                    new Property(n, Resources.getMessage("PropertiesView.133"), new String[] { SWTUtil.getPrettyString(((PopulationUniqueness)criterion).getPopulationModel().getPopulationSize())}); //$NON-NLS-1$
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
