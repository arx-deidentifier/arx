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

package org.deidentifier.arx.gui.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.ARXProcessStatistics;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.MicroAggregationFunction;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.criteria.Inclusion;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.io.CSVSyntax;
import org.deidentifier.arx.metric.MetricConfiguration;
import org.deidentifier.arx.metric.MetricDescription;

/**
 * This class implements a large portion of the model used by the GUI.
 *
 * @author Fabian Prasser
 * @author James Gaupp
 */
public class Model implements Serializable {
    
    /**
     * The currently selected perspective
     * @author Fabian Prasser
     */
    public static enum Perspective {
        CONFIGURATION,
        EXPLORATION,
        ANALYSIS,
        RISK
    }

    /** SVUID. */
    private static final long                             serialVersionUID                = -7669920657919151279L;

    /* *****************************************
     * TRANSIENT VARIABLES
     *******************************************/

    /** The current anonymizer, if any. */
    private transient ARXAnonymizer                       anonymizer                      = null;

    /** The current output data. */
    private transient DataHandle                          output                          = null;

    /** The currently displayed transformation. */
    private transient ARXNode                             outputNode                      = null;

    /** The path to the project file. */
    private transient String                              path                            = null;

    /** The current result. */
    private transient ARXResult                           result                          = null;

    /** The currently selected node. */
    private transient ARXNode                             selectedNode                    = null;

    /** The clip board. */
    private transient ModelClipboard                      clipboard                       = null;

    /** The perspective */
    private transient Perspective                         perspective                     = Perspective.CONFIGURATION;

    /* *****************************************
     * PARAMETERS AND THRESHOLDS
     *******************************************/

    /** Anonymization parameter. */
    private double                                        snapshotSizeDataset             = 0.2d;

    /** Anonymization parameter. */
    private double                                        snapshotSizeSnapshot            = 0.8d;

    /** Anonymization parameter. */
    private int                                           historySize                     = 200;

    /** Threshold. */
    private int                                           maximalSizeForComplexOperations = 5000000;

    /** Threshold. */
    private int                                           initialNodesInViewer            = 100;

    /** Threshold. */
    private int                                           maxNodesInViewer                = 700;

    /* *****************************************
     * PROJECT METADATA
     ******************************************/

    /** The project description. */
    private String                                        description;

    /** The size of the input file. */
    private long                                          inputBytes                      = 0L;

    /** Is the project file modified. */
    private boolean                                       modified                        = false;

    /** The project name. */
    private String                                        name                            = null;

    /** Left for backwards compatibility only! */
    private char                                          separator                       = ';';                                                    //$NON-NLS-1$

    /** The projects CSV syntax */
    private CSVSyntax                                     csvSyntax;

    /** Execution time of last anonymization. */
    private long                                          time;

    /** Locale. */
    private Locale                                        locale                          = null;

    /** The audit trail */
    private List<ModelAuditTrailEntry>                    auditTrail                      = new ArrayList<ModelAuditTrailEntry>();

    /** Standard charset since ARX > 3.7.1. Older projects will have the value <code>null</code>*/
    private String                                        charset                         = "UTF-8";
    
    /* *****************************************
     * DEBUGGING
     ******************************************/

    /** Is the debugging mode enabled. */
    private boolean                                       debugEnabled                    = false;

    /* *****************************************
     * VISUALIZATIONS
     ******************************************/

    /** Indices of groups in the current output view. */
    private int[]                                         groups;

    /** Label. */
    private String                                        optimalNodeAsString;

    /** Label. */
    private String                                        outputNodeAsString;

    /** Current selection. */
    private String                                        selectedClassValue              = null;

    /** Current selection. */
    private String                                        selectedAttribute               = null;

    /** Enable/disable. */
    private Boolean                                       showVisualization               = true;

    /** Last two selections. */
    private String[]                                      pair                            = new String[] { null, null };

    /* *****************************************
     * SUBSET MANAGEMENT
     ******************************************/

    /** Query. */
    private String                                        query                           = "";                                                     //$NON-NLS-1$

    /** Origin of current subset. */
    private String                                        subsetOrigin                    = "All";                                                  //$NON-NLS-1$

    /* *****************************************
     * SUB-MODELS
     ******************************************/

    /** The current input configuration. */
    private ModelConfiguration                            inputConfig                     = new ModelConfiguration();

    /** A filter describing which transformations are currently selected. */
    private ModelNodeFilter                               nodeFilter                      = null;

    /** Configuration of the data view. */
    private ModelViewConfig                               viewConfig                      = new ModelViewConfig();

    /** The current output configuration. */
    private ModelConfiguration                            outputConfig                    = null;

    /** The current risk model. */
    private ModelRisk                                     riskModel                       = null;

    /* *****************************************
     * PRIVACY CRITERIA
     * *****************************************/

    /** Model for a specific privacy criterion. */
    private ModelDPresenceCriterion                       dPresenceModel                  = new ModelDPresenceCriterion();

    /** Model for a specific privacy criterion. */
    private ModelKMapCriterion                            kMapModel                       = new ModelKMapCriterion();

    /** Model for a specific privacy criterion. */
    private ModelKAnonymityCriterion                      kAnonymityModel                 = new ModelKAnonymityCriterion();

    /** Model for a specific privacy criterion. */
    private Map<String, ModelLDiversityCriterion>         lDiversityModel                 = new HashMap<String, ModelLDiversityCriterion>();

    /** Model for a specific privacy criterion. */
    private Map<String, ModelTClosenessCriterion>         tClosenessModel                 = new HashMap<String, ModelTClosenessCriterion>();

    /** Model for a specific privacy criterion. */
    private Set<ModelRiskBasedCriterion>                  riskBasedModel                  = new HashSet<ModelRiskBasedCriterion>();

    /** Model for a specific privacy criterion. */
    private ModelDifferentialPrivacyCriterion             differentialPrivacyModel        = new ModelDifferentialPrivacyCriterion();

    /** Model for a specific privacy criterion. */
    private Map<String, ModelDDisclosurePrivacyCriterion> dDisclosurePrivacyModel         = new HashMap<String, ModelDDisclosurePrivacyCriterion>();

    /** Model for a specific privacy criterion. */
    private ModelProfitabilityCriterion                   stackelbergPrivacyModel         = new ModelProfitabilityCriterion();

    /** Model for a specific privacy criterion. */
    private Map<String, ModelBLikenessCriterion>          bLikenessModel                  = new HashMap<String, ModelBLikenessCriterion>();

    /* *****************************************
     * UTILITY ANALYSIS
     ******************************************/

    /** Configuration. */
    private MetricConfiguration                           metricConfig                    = null;

    /** Description. */
    private MetricDescription                             metricDescription               = null;

    /** Summary statistics */
    private Boolean                                       useListwiseDeletion             = true;

    /** Utility estimation during anonymization */
    private Boolean                                       useFunctionalHierarchies        = true;

    /* *****************************************
     * RISK ANALYSIS
     ******************************************/
    /** Selected quasi identifiers */
    private Set<String>                                   selectedQuasiIdentifiers        = null;    

    /* *****************************************
     * LOCAL RECODING
     ******************************************/
    /** The local recoding model */
    private ModelLocalRecoding                            localRecodingModel              = new ModelLocalRecoding();

    /** Heuristic search threshold */
    private Integer                                       heuristicSearchThreshold;

    /** Heuristic search threshold */
    private Integer                                       heuristicSearchTimeLimit;

    /** Heuristic search threshold */
    private Integer                                       heuristicSearchStepLimit;

    /** General anonymization configuration. Proxy for some fields for backwards compatibility */
    private ModelAnonymizationConfiguration               anonymizationConfiguration;

    /* *****************************************
     * Data Mining
     * *****************************************
     */
    /** Selected attributes */
    private Set<String>                                   selectedFeatures                = null;

    /** Selected attributes */
    private Set<String>                                   selectedClasses                 = null;

    /** Model */
    private ModelClassification                           classificationModel             = new ModelClassification();

    /* *****************************************
     * Information about the last anonymization process
     * *****************************************
     */
    /** Statistics about the last optimization process */
    private ARXProcessStatistics                          optimizationStatistics          = null;

    /**
     * Creates a new instance.
     *
     * @param name
     * @param description
     * @param locale
     */
    public Model(final String name, final String description, Locale locale) {
        this.name = name;
        this.description = description;
        this.locale = locale;
        setModified();
    }

    /**
     * Adds an entry to the audit trail
     * @param entry
     */
    public void addAuditTrailEntry(ModelAuditTrailEntry entry) {
        this.getAuditTrail().add(entry);
        this.setModified();
    }

    /**
     * Creates an anonymizer for the current config.
     *
     * @return
     */
    public ARXAnonymizer createAnonymizer() {
        
        // Initialize anonymizer
        this.anonymizer = new ARXAnonymizer();
        this.anonymizer.setHistorySize(getHistorySize());
        this.anonymizer.setMaximumSnapshotSizeDataset(getSnapshotSizeDataset());
        this.anonymizer.setMaximumSnapshotSizeSnapshot(getSnapshotSizeSnapshot());
        
        // Add all criteria
        this.createConfig();

        // Return the anonymizer
        return anonymizer;
    }

    /**
     * Replaces the output config with a clone of the input config.
     */
    public void createClonedConfig() {
        outputConfig = inputConfig.clone();
        this.setModified();
    }

    /**
     * Creates an ARXConfiguration.
     */
    public void createConfig() {

        ModelConfiguration config = getInputConfig();
        DataDefinition definition = getInputDefinition();

        // Initialize the config
        config.removeAllCriteria();
        if (definition == null) return;
        
        // Initialize the metric
        config.setMetric(this.getMetricDescription().createInstance(this.getMetricConfiguration()));

        // Initialize definition
        for (String attr : definition.getQuasiIdentifyingAttributes()) {

            // Reset
            definition.resetAttributeType(attr);
            definition.resetHierarchy(attr);
            definition.resetHierarchyBuilder(attr);
            definition.resetMaximumGeneralization(attr);
            definition.resetMicroAggregationFunction(attr);
            definition.resetMinimumGeneralization(attr);
            
            // This increases the precision of the Loss utility measure
            if (this.getUseFunctionalHierarchies() && config.getHierarchyBuilder(attr) != null) {
                definition.setHierarchy(attr, config.getHierarchyBuilder(attr));
            } else {
                definition.setHierarchy(attr, (HierarchyBuilder<?>)null);
            }
            
            // Set hierarchy
            Hierarchy hierarchy = config.getHierarchy(attr);
            if (hierarchy != null && hierarchy.getHierarchy() != null) {
                definition.setHierarchy(attr, hierarchy);
            }
            
            // Set attribute type
            definition.setAttributeType(attr, AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);

            // Prepare for micro-aggregation
            if (config.getTransformationMode(attr) == ModelTransformationMode.MICRO_AGGREGATION) {

                MicroAggregationFunction function = config.getMicroAggregationFunction(attr)
                                                          .createInstance(config.getMicroAggregationIgnoreMissingData(attr));
                definition.setMicroAggregationFunction(attr, function);

            // Prepare for micro-aggregation with clustering
            } else if (config.getTransformationMode(attr) == ModelTransformationMode.CLUSTERING_AND_MICRO_AGGREGATION) {

                MicroAggregationFunction function = config.getMicroAggregationFunction(attr)
                                                          .createInstance(config.getMicroAggregationIgnoreMissingData(attr));
                definition.setMicroAggregationFunction(attr, function, true);
                
            // Prepare for generalization
            } else {

                definition.setMicroAggregationFunction(attr, null);
                Integer min = config.getMinimumGeneralization(attr);
                Integer max = config.getMaximumGeneralization(attr);
                if (min != null) {
                    definition.setMinimumGeneralization(attr, min);
                }
                if (max != null) {
                    definition.setMaximumGeneralization(attr, max);
                }
            }
        }

        if (this.differentialPrivacyModel != null &&
            this.differentialPrivacyModel.isEnabled()) {
            config.addCriterion(this.differentialPrivacyModel.getCriterion(this));
        }

        if (this.kAnonymityModel != null &&
            this.kAnonymityModel.isEnabled()) {
            config.addCriterion(this.kAnonymityModel.getCriterion(this));
        }

        if (this.kMapModel != null &&
            this.kMapModel.isEnabled()) {
            config.addCriterion(this.kMapModel.getCriterion(this));
        }

        if (this.dPresenceModel != null && 
            this.dPresenceModel.isEnabled()) {
            config.addCriterion(this.dPresenceModel.getCriterion(this));
        }
        
        if (this.stackelbergPrivacyModel != null &&
            this.stackelbergPrivacyModel.isEnabled()) {
            config.addCriterion(this.stackelbergPrivacyModel.getCriterion(this));
        }
        
        for (Entry<String, ModelLDiversityCriterion> entry : this.lDiversityModel.entrySet()){
            if (entry.getValue() != null &&
                entry.getValue().isEnabled()) {
                config.addCriterion(entry.getValue().getCriterion(this));
            }
        }
        
        for (Entry<String, ModelTClosenessCriterion> entry : this.tClosenessModel.entrySet()){
            if (entry.getValue() != null &&
                entry.getValue().isEnabled()) {
                
                if (entry.getValue().getVariant()==1){ // EMD with hierarchy
                    if (config.getHierarchy(entry.getValue().getAttribute()) == null){
                        config.setHierarchy(entry.getValue().getAttribute(), Hierarchy.create());
                    }
                }
                
                PrivacyCriterion criterion = entry.getValue().getCriterion(this);
                config.addCriterion(criterion);
            }
        }

        for (Entry<String, ModelDDisclosurePrivacyCriterion> entry : this.dDisclosurePrivacyModel.entrySet()){
            if (entry.getValue() != null &&
                entry.getValue().isEnabled()) {
                config.addCriterion(entry.getValue().getCriterion(this));
            }
        }
        
        for (Entry<String, ModelBLikenessCriterion> entry : this.bLikenessModel.entrySet()){
            if (entry.getValue() != null &&
                entry.getValue().isEnabled()) {
                config.addCriterion(entry.getValue().getCriterion(this));
            }
        }
        
        for (ModelRiskBasedCriterion entry : this.riskBasedModel){
            if (entry != null && entry.isEnabled()) {
                PrivacyCriterion criterion = entry.getCriterion(this);
                config.addCriterion(criterion);
            }
        }

        // If a subset has been defined
        if (config.getInput() != null && config.getResearchSubset() != null && 
            config.getResearchSubset().size() != config.getInput().getHandle().getNumRows()) {
            
            // Configure it, if not done already
            boolean subsetDefined = false;
            for (PrivacyCriterion c : config.getCriteria()) {

                // (e,d)-DP will return false at this point, 
                // but consistency will be checked by ARXConfiguration.initialize(...) 
                subsetDefined |= c.isSubsetAvailable();
            }
            // Add, if missing
            if (!subsetDefined) {
                DataSubset subset = DataSubset.create(config.getInput(), config.getResearchSubset());
                config.addCriterion(new Inclusion(subset));
            }            
        }
    }
    /**
     * Returns the current anonymization configuration
     * @return
     */
    public ModelAnonymizationConfiguration getAnonymizationConfiguration() {
        if (anonymizationConfiguration == null) {
            anonymizationConfiguration = new ModelAnonymizationConfiguration(this);
        }
        return anonymizationConfiguration;
    }
    
    /**
     * Returns the current anonymizer.
     *
     * @return
     */
    public ARXAnonymizer getAnonymizer() {
        return anonymizer;
    }

    /**
     * Returns the last two selected attributes.
     *
     * @return
     */
    public String[] getAttributePair() {
        if (pair == null) pair = new String[] { null, null };
        return pair;
    }

    /**
     * Returns the audit trail
     * @return
     */
    public List<ModelAuditTrailEntry> getAuditTrail() {
        if (this.auditTrail == null) {
            this.auditTrail = new ArrayList<ModelAuditTrailEntry>();
        }
        return auditTrail;
    }

    /**
     * Returns the b-Likeness privacy model.
     *
     * @return
     */
    public Map<String, ModelBLikenessCriterion> getBLikenessModel() {
        if (this.bLikenessModel == null) {
            this.bLikenessModel = new HashMap<String, ModelBLikenessCriterion>();
            DataHandle handle = inputConfig.getInput().getHandle();
            for (int col = 0; col < handle.getNumColumns(); col++) {
                String attribute = handle.getAttributeName(col);
                bLikenessModel.put(attribute, new ModelBLikenessCriterion(attribute));
            }
        }
        return bLikenessModel;
    }

    /**
     * Return charset. Returns <code>null</code> for projects with unknown charset, "UTF-8" else.
     * @return
     */
    public String getCharset() {
        return this.charset;
    }

    /**
     * Returns the classification model
     * @return
     */
    public ModelClassification getClassificationModel() {
        if (this.classificationModel == null) {
            this.classificationModel = new ModelClassification();
        }
        return this.classificationModel;
    }

    /**
     * Returns the clipboard.
     *
     * @return
     */
    public ModelClipboard getClipboard(){
        if (clipboard==null){
            clipboard = new ModelClipboard();
        }
        return clipboard;
    }

    /**
     * Gets the csv config model.
     * @return
     */
    public CSVSyntax getCSVSyntax() {
        if (csvSyntax == null) {
            csvSyntax = new CSVSyntax();
            csvSyntax.setDelimiter(separator);
        }
        return csvSyntax;
    }
    
    /**
     * Returns the d-disclosure privacy model.
     *
     * @return
     */
    public Map<String, ModelDDisclosurePrivacyCriterion> getDDisclosurePrivacyModel() {
        if (this.dDisclosurePrivacyModel == null) {
            this.dDisclosurePrivacyModel = new HashMap<String, ModelDDisclosurePrivacyCriterion>();
            DataHandle handle = inputConfig.getInput().getHandle();
            for (int col = 0; col < handle.getNumColumns(); col++) {
                String attribute = handle.getAttributeName(col);
                dDisclosurePrivacyModel.put(attribute, new ModelDDisclosurePrivacyCriterion(attribute));
            }
        }
        return dDisclosurePrivacyModel;
    }

    /**
     * Returns the project description.
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the (e,d)-DP model.
     *
     * @return
     */
    public ModelDifferentialPrivacyCriterion getDifferentialPrivacyModel() {
        if (this.differentialPrivacyModel == null) {
            this.differentialPrivacyModel = new ModelDifferentialPrivacyCriterion();
        }
        return differentialPrivacyModel;
    }
    
    /**
     * Returns the d-presence model.
     *
     * @return
     */
    public ModelDPresenceCriterion getDPresenceModel() {
        return dPresenceModel;
    }

    /**
     * Returns a list of indices of all equivalence classes.
     *
     * @return
     */
    public int[] getGroups() {
        // TODO: Refactor to colors[groups[row]]
        return this.groups;
    }

    /**
     * @return the heuristicSearchStepLimit
     */
    public Integer getHeuristicSearchStepLimit() {
        if (this.heuristicSearchStepLimit == null) {
            return this.heuristicSearchStepLimit = 1000;
        }
        return heuristicSearchStepLimit;
    }

    /**
     * @return the heuristicSearchThreshold
     */
    public Integer getHeuristicSearchThreshold() {
        if (this.heuristicSearchThreshold == null) {
            return this.heuristicSearchThreshold = 100000;
        }
        return heuristicSearchThreshold;
    }

    /**
     * @return the heuristicSearchTimeLimit
     */
    public Integer getHeuristicSearchTimeLimit() {
        if (this.heuristicSearchTimeLimit == null) {
            return this.heuristicSearchTimeLimit = 30000;
        }
        return heuristicSearchTimeLimit;
    }
    
    /**
     * Returns the according parameter.
     *
     * @return
     */
    public int getHistorySize() {
        return historySize;
    }

    /**
     * Returns an upper bound on the number of nodes that will initially
     * be displayed in the lattice viewer.
     *
     * @return
     */
    public int getInitialNodesInViewer() {
        return initialNodesInViewer;
    }

    /**
     * Returns the size in bytes of the input file.
     *
     * @return
     */
    public long getInputBytes() {
        return inputBytes;
    }

    /**
     * Returns the input configuration.
     *
     * @return
     */
    public ModelConfiguration getInputConfig() {
        return inputConfig;
    }

    /**
     * Returns the input definition.
     *
     * @return
     */
    public DataDefinition getInputDefinition(){
        if (inputConfig==null) return null;
        else if (inputConfig.getInput()==null) return null;
        else return inputConfig.getInput().getDefinition();
    }

    /**
     * Returns the input population model
     * @return
     */
    public ARXPopulationModel getInputPopulationModel() {
        return getRiskModel().getPopulationModel();
    }
    
    /**
     * Returns the k-anonymity model.
     *
     * @return
     */
    public ModelKAnonymityCriterion getKAnonymityModel() {
        return kAnonymityModel;
    }

    /**
     * Returns the k-map model.
     *
     * @return
     */
    public ModelKMapCriterion getKMapModel() {
        if (kMapModel == null) {
            kMapModel = new ModelKMapCriterion();
        }
        return kMapModel;
    }
    
    /**
     * Returns the l-diversity model.
     *
     * @return
     */
    public Map<String, ModelLDiversityCriterion> getLDiversityModel() {
           if (this.lDiversityModel == null) {
                this.lDiversityModel = new HashMap<String, ModelLDiversityCriterion>();
            }
        return lDiversityModel;
    }

    /**
     * Returns the project locale.
     *
     * @return
     */
    public Locale getLocale() {
        if (this.locale == null) {
            return Locale.getDefault();
        } else {
            return locale;
        }
    }

    /**
     * Returns the model for local recoding
     * @return
     */
    public ModelLocalRecoding getLocalRecodingModel() {
        if (this.localRecodingModel == null) {
            this.localRecodingModel = new ModelLocalRecoding();
        }
        return localRecodingModel;
    }

    /**
     * When a dataset has more records than this threshold,
     * visualization of statistics will be disabled.
     *
     * @return
     */
    public int getMaximalSizeForComplexOperations(){
        return this.maximalSizeForComplexOperations;
    }

    /**
     * Returns the maximal size of a sub-lattice that will be displayed
     * by the viewer.
     *
     * @return
     */
    public int getMaxNodesInViewer() {
        return maxNodesInViewer;
    }

    /**
     * Returns the configuration of the metric.
     *
     * @return
     */
    public MetricConfiguration getMetricConfiguration() {
        
        if (this.metricConfig == null) {
            if (this.inputConfig == null || this.inputConfig.getMetric() == null) {
                this.metricConfig = ARXConfiguration.create().getQualityModel().getConfiguration();
            } else {
                this.metricConfig = this.inputConfig.getMetric().getConfiguration();
            }
        }
        return this.metricConfig;
    }

    /**
     * Returns a description of the metric.
     *
     * @return
     */
    public MetricDescription getMetricDescription() {
        if (this.metricDescription == null) {
            if (this.inputConfig == null || this.inputConfig.getMetric() == null) {
                this.metricDescription = ARXConfiguration.create().getQualityModel().getDescription();
            } else {
                this.metricDescription = this.inputConfig.getMetric().getDescription();
            }
        }
        return this.metricDescription;
    }
    
    /**
     * Returns the name of this project.
     *
     * @return
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the current filter.
     *
     * @return
     */
    public ModelNodeFilter getNodeFilter() {
        return nodeFilter;
    }

    /**
     * Returns a string representation of the current optimum.
     *
     * @return
     */
    public String getOptimalNodeAsString() {
        return optimalNodeAsString;
    }

    /**
     * @return the output
     */
    public DataHandle getOutput() {
        return output;
    }

    /**
     * Returns the output config.
     *
     * @return
     */
    public ModelConfiguration getOutputConfig() {
        return outputConfig;
    }

    /**
     * Returns the output definition.
     *
     * @return
     */
    public DataDefinition getOutputDefinition(){
        if (this.output == null){
            if (this.result != null) {
                return this.result.getDataDefinition();
            } else {
                return null;
            }
        }
        else return this.output.getDefinition();
    }

    /**
     * Returns a string representation of the currently applied transformation.
     *
     * @return
     */
    public String getOutputNodeAsString() {
        return outputNodeAsString;
    }

    /**
     * Returns the output population model, if any. Null otherwise.
     * @return
     */
    public ARXPopulationModel getOutputPopulationModel() {
        ModelConfiguration config = getOutputConfig();
        if (config != null) {
            for (PrivacyCriterion c : config.getCriteria()) {
                if (c.getPopulationModel() != null) {
                    return c.getPopulationModel();
                }
            }
        }
        return null;
    }
    
    /**
     * Returns the currently applied transformation.
     *
     * @return
     */
    public ARXNode getOutputTransformation() {
        return outputNode;
    }

    /**
     * Returns the path of the project.
     *
     * @return
     */
    public String getPath() {
        return path;
    }

    /**
     * @return the perspective
     */
    public Perspective getPerspective() {
        if (perspective == null) {
            perspective = Perspective.CONFIGURATION;
        }
        return perspective;
    }

    /**
     * @return the optimizationStatistics
     */
    public ARXProcessStatistics getProcessStatistics() {
        if (optimizationStatistics == null && this.result != null) {
            return this.result.getProcessStatistics();
        }
        return optimizationStatistics;
    }

    /**
     * Returns the current query.
     *
     * @return
     */
    public String getQuery() {
        return query;
    }

    /**
     * Returns the current result.
     *
     * @return the result
     */
    public ARXResult getResult() {
        return result;
    }

    /**
     * Returns the risk-based model.
     *
     * @return
     */
    public Set<ModelRiskBasedCriterion> getRiskBasedModel() {
        if (this.riskBasedModel == null) {
            this.riskBasedModel = new HashSet<ModelRiskBasedCriterion>();
            this.riskBasedModel.add(new ModelRiskBasedCriterion(ModelRiskBasedCriterion.VARIANT_AVERAGE_RISK));
            this.riskBasedModel.add(new ModelRiskBasedCriterion(ModelRiskBasedCriterion.VARIANT_SAMPLE_UNIQUES));
            this.riskBasedModel.add(new ModelRiskBasedCriterion(ModelRiskBasedCriterion.VARIANT_POPULATION_UNIQUES_DANKAR));
        }
        return riskBasedModel;
    }

    /**
     * Returns the risk model
     * @return the risk model
     */
    public ModelRisk getRiskModel() {
        if (this.riskModel == null) {
            this.riskModel = new ModelRisk();
        }
        return riskModel;
    }

    /**
     * Returns the currently selected attribute.
     *
     * @return
     */
    public String getSelectedAttribute() {
        return selectedAttribute;
    }
    
    /**
     * Returns the selected classes
     * @return
     */
    public Set<String> getSelectedClasses() {
        if (this.selectedClasses == null) {
            this.selectedClasses = new HashSet<String>();
        }
        return this.selectedClasses;
    }
    
    /**
     * Returns the selected classes, ordered by occurrence in the dataset
     * @return
     */
    public String[] getSelectedClassesAsArray() {
        return this.getAttributesAsArray(this.getSelectedClasses());
    }

    /**
     * Returns the currently selected class value.
     * 
     * @return
     */
    public String getSelectedClassValue() {
        return selectedClassValue;
    }

    /**
     * Returns the selected features
     * @return
     */
    public Set<String> getSelectedFeatures() {
        if (this.selectedFeatures == null) {
            this.selectedFeatures = new HashSet<String>();
        }
        return this.selectedFeatures;
    }

    /**
     * Returns the selected features, ordered by occurrence in the dataset
     * @return
     */
    public String[] getSelectedFeaturesAsArray() {
        return this.getAttributesAsArray(this.getSelectedFeatures());
    }
    
    /**
     * Returns the selected transformation.
     *
     * @return
     */
    public ARXNode getSelectedNode() {
        return selectedNode;
    }

    /**
     * Returns a set of quasi identifiers selected for risk analysis
     * @return
     */
    public Set<String> getSelectedQuasiIdentifiers() {
        
        if (this.selectedQuasiIdentifiers == null) {
            
            // Add qis or other attributes
            if (this.getInputConfig() != null && this.getInputConfig().getInput() != null) {
                DataHandle handle = this.getInputConfig().getInput().getHandle();
                this.selectedQuasiIdentifiers = new HashSet<String>();
                Set<String> qis = this.getInputDefinition().getQuasiIdentifyingAttributes();
                
                // Add standard attributes
                if (qis.isEmpty()) {
                    int max = handle.getNumColumns();
                    max = Math.min(max, getRiskModel().getMaxQiSize());
                    for (int i=0; i<max; i++) {
                        this.selectedQuasiIdentifiers.add(handle.getAttributeName(i));
                    }
                    
                // Add QIs
                } else {
                    int max = qis.size();
                    max = Math.min(max, getRiskModel().getMaxQiSize());
                    for (int i = 0; i < handle.getNumColumns() && selectedQuasiIdentifiers.size() <= max; i++) {
                        String attr = handle.getAttributeName(i);
                        if (qis.contains(attr)) {
                            this.selectedQuasiIdentifiers.add(attr);
                        }
                    }
                }
            } else {
                
                // Return empty set
                return new HashSet<String>();
            }
        }
        return this.selectedQuasiIdentifiers;
    }
    
    /**
     * Returns the separator.
     *
     * @return
     */
    public char getSeparator() {
        return separator;
    }

    
    /**
     * Returns the according parameter.
     *
     * @return
     */
    public double getSnapshotSizeDataset() {
        return snapshotSizeDataset;
    }

    /**
     * Returns the according parameter.
     *
     * @return
     */
    public double getSnapshotSizeSnapshot() {
        return snapshotSizeSnapshot;
    }

    /**
     * Returns the size of the solution space for the current
     * input parameters
     * 
     * @return
     */
    public double getSolutionSpaceSize() {
        
       // Obtain definition
       DataDefinition definition = getInputDefinition();
       if (definition == null) {
           return 0;
       }
       
       // Generalized and clustered QIs
       Set<String> qis = new HashSet<>(definition.getQuasiIdentifiersWithGeneralization());
       qis.addAll(definition.getQuasiIdentifiersWithClusteringAndMicroaggregation());
       double size = 1;
       for (String qi : qis) {
           Hierarchy hierarchy = getInputConfig().getHierarchy(qi);
            if (!(hierarchy == null || hierarchy.getHierarchy() == null || hierarchy.getHierarchy().length == 0 || hierarchy.getHierarchy()[0] == null)) {
                size *= hierarchy.getHierarchy()[0].length;
            }
       }
       
       // Return
       return size;
    }

    /**
     * Returns the configuration object for the stackelberg privacy model
     * @return
     */
    public ModelProfitabilityCriterion getStackelbergModel() {
        if (this.stackelbergPrivacyModel == null) {
            this.stackelbergPrivacyModel = new ModelProfitabilityCriterion();
        }
        return stackelbergPrivacyModel;
    }

    /**
     * Returns the origin of the subset.
     *
     * @return
     */
    public String getSubsetOrigin(){
        return this.subsetOrigin;
    }

    /**
     * Returns the t-closeness model.
     *
     * @return
     */
    public Map<String, ModelTClosenessCriterion> getTClosenessModel() {
        if (this.tClosenessModel == null) {
            this.tClosenessModel = new HashMap<String, ModelTClosenessCriterion>();
        }
        return tClosenessModel;
    }

    /**
     * Returns the execution time of the last anonymization process.
     *
     * @return
     */
    public long getTime() {
        return time;
    }

    /**
     * Returns whether functional hierarchies should be used
     * @return
     */
    public Boolean getUseFunctionalHierarchies() {
        
        // Backwards compatibility
        if (useFunctionalHierarchies == null) {
            useFunctionalHierarchies = true;
        }
        return useFunctionalHierarchies;
    }

    /**
     * Returns whether list-wise deletion is used for summary statistics
     * @return
     */
    public Boolean getUseListwiseDeletion() {
        
        // Backwards compatibility
        if (useListwiseDeletion == null) {
            useListwiseDeletion = true;
        }
        return useListwiseDeletion;
    }

    /**
     * Returns the view configuration.
     *
     * @return
     */
    public ModelViewConfig getViewConfig() {
        return this.viewConfig;
    }

    /**
     * Returns whether debugging is enabled.
     *
     * @return
     */
    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    /**
     * Returns whether this project is modified.
     *
     * @return
     */
    public boolean isModified() {
        if (inputConfig.isModified()) { return true; }
        if (getRiskModel().isModified()) { return true; }
        if (getClassificationModel().isModified()) { return true; }
        if ((outputConfig != null) && outputConfig.isModified()) { return true; }
        if ((clipboard != null) && clipboard.isModified()) { return true; }
        return modified;
    }

    /**
     * Returns whether a quasi-identifier is selected.
     *
     * @return
     */
    public boolean isQuasiIdentifierSelected() {
        return (getInputDefinition().getAttributeType(getSelectedAttribute()) == AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
    }

    /**
     * Returns whether a sensitive attribute is selected.
     *
     * @return
     */
    public boolean isSensitiveAttributeSelected() {
        return (getInputDefinition().getAttributeType(getSelectedAttribute()) == AttributeType.SENSITIVE_ATTRIBUTE);
    }

    /**
     * Returns whether visualization is enabled.
     *
     * @return
     */
    public boolean isVisualizationEnabled(){
        if (this.showVisualization == null) {
            return true;
        } else {
            return this.showVisualization;
        }
    }

    /**
     * Resets the model.
     */
    public void reset() {
        this.resetCriteria();
        this.resetAttributePair();
        this.inputConfig = new ModelConfiguration();
        this.outputConfig = null;
        this.output = null;
        this.result = null;
        if (auditTrail != null) auditTrail.clear();
        this.selectedQuasiIdentifiers = null;
        this.selectedFeatures = null;
        this.selectedClasses = null;
        this.subsetOrigin = Resources.getMessage("Model.0"); //$NON-NLS-1$
        this.groups = null;
        this.classificationModel = new ModelClassification();
        this.anonymizationConfiguration = null;
        this.heuristicSearchStepLimit = null;
        this.heuristicSearchThreshold = null;
        this.heuristicSearchTimeLimit = null;
        this.optimizationStatistics = null;
        this.localRecodingModel = null;
        this.selectedClassValue = null;
        this.selectedAttribute = null;
    }

    /**
     * Returns the last two selected attributes.
     */
    public void resetAttributePair() {
        if (pair == null) {
            pair = new String[] { null, null };
        }
        pair[0] = null;
        pair[1] = null;
    }
    
    /**
     * Resets the configuration of the privacy criteria.
     */
    public void resetCriteria() {
        
        if (inputConfig==null || inputConfig.getInput()==null) return;
        
        differentialPrivacyModel = new ModelDifferentialPrivacyCriterion();
        kAnonymityModel = new ModelKAnonymityCriterion();
        stackelbergPrivacyModel = new ModelProfitabilityCriterion();
        dPresenceModel = new ModelDPresenceCriterion();
        kMapModel = new ModelKMapCriterion();
        lDiversityModel.clear();
        tClosenessModel.clear();
        riskBasedModel.clear();
        dDisclosurePrivacyModel.clear();
        bLikenessModel.clear();
        DataHandle handle = inputConfig.getInput().getHandle();
        for (int col = 0; col < handle.getNumColumns(); col++) {
            String attribute = handle.getAttributeName(col);
            lDiversityModel.put(attribute, new ModelLDiversityCriterion(attribute));
            tClosenessModel.put(attribute, new ModelTClosenessCriterion(attribute));
            dDisclosurePrivacyModel.put(attribute, new ModelDDisclosurePrivacyCriterion(attribute));
            bLikenessModel.put(attribute, new ModelBLikenessCriterion(attribute));
        }
        riskBasedModel.add(new ModelRiskBasedCriterion(ModelRiskBasedCriterion.VARIANT_AVERAGE_RISK));
        riskBasedModel.add(new ModelRiskBasedCriterion(ModelRiskBasedCriterion.VARIANT_SAMPLE_UNIQUES));
        riskBasedModel.add(new ModelRiskBasedCriterion(ModelRiskBasedCriterion.VARIANT_POPULATION_UNIQUES_DANKAR));
    }

    /**
     * Sets the anonymizer.
     *
     * @param anonymizer
     */
    public void setAnonymizer(final ARXAnonymizer anonymizer) {
        setModified();
        this.anonymizer = anonymizer;
    }
    
    /**
     * Sets the charset
     * @param charset
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }
    
    /**
     * Enables debugging.
     *
     * @param value
     */
    public void setDebugEnabled(boolean value){
        this.debugEnabled = value;
        this.setModified();
    }
    
    /**
     * Sets the project description.
     *
     * @param description
     */
    public void setDescription(final String description) {
        this.description = description;
        setModified();
    }

    /**
     * Updates features and classes to reflect the definition provided
     * @param definition
     * @return Whether an update has been performed
     */
    public boolean setFeaturesAndClasses(DataDefinition definition) {

        // Previous
        Set<String> features = this.getSelectedFeatures();
        Set<String> classes = this.getSelectedClasses();

        // New
        this.setSelectedFeatures(new HashSet<String>(definition.getQuasiIdentifyingAttributes()));
        this.setSelectedClasses(new HashSet<String>(definition.getResponseVariables()));
        
        // Return whether an update has been performed
        return (!features.equals(this.getSelectedFeatures()) || !classes.equals(this.getSelectedClasses()));
    }

    /**
     * Sets the indices of equivalence classes.
     *
     * @param groups
     */
    public void setGroups(int[] groups) {
        this.groups = groups;
    }

    /**
     * @param heuristicSearchStepLimit the heuristicSearchStepLimit to set
     */
    public void setHeuristicSearchStepLimit(Integer heuristicSearchStepLimit) {
        this.heuristicSearchStepLimit = heuristicSearchStepLimit;
    }

    /**
     * @param heuristicSearchThreshold the heuristicSearchThreshold to set
     */
    public void setHeuristicSearchThreshold(Integer heuristicSearchThreshold) {
        this.heuristicSearchThreshold = heuristicSearchThreshold;
    }

    /**
     * @param heuristicSearchTimeLimit the heuristicSearchTimeLimit to set
     */
    public void setHeuristicSearchTimeLimit(Integer heuristicSearchTimeLimit) {
        this.heuristicSearchTimeLimit = heuristicSearchTimeLimit;
    }

    /**
     * Sets the according parameter.
     *
     * @param historySize
     */
    public void setHistorySize(final int historySize) {
        this.historySize = historySize;
        setModified();
    }

    /**
     * Sets the according parameter.
     *
     * @param val
     */
    public void setInitialNodesInViewer(final int val) {
        initialNodesInViewer = val;
        setModified();
    }
    
    /**
     * Sets the size of the input in bytes.
     *
     * @param inputBytes
     */
    public void setInputBytes(final long inputBytes) {
        setModified();
        this.inputBytes = inputBytes;
    }
    
    /**
     * Sets the input config.
     *
     * @param config
     */
    public void setInputConfig(final ModelConfiguration config) {
        this.inputConfig = config;
    }
    
    /**
     * Sets the project locale.
     *
     * @param locale Null for default locale
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
        this.setModified();
    }

    /**
     * Sets the according parameter.
     *
     * @param numberOfRows
     */
    public void setMaximalSizeForComplexOperations(int numberOfRows) {
        this.maximalSizeForComplexOperations = numberOfRows;
        this.setModified();
    }

    /**
     * Sets the according parameter.
     *
     * @param maxNodesInViewer
     */
    public void setMaxNodesInViewer(final int maxNodesInViewer) {
        this.maxNodesInViewer = maxNodesInViewer;
        setModified();
    }

    /**
     * Sets the description of the metric.
     *
     * @param description
     */
    public void setMetricDescription(MetricDescription description) {
        this.metricDescription = description;
    }

    /**
     * Marks this project as modified.
     */
    public void setModified() {
        modified = true;
    }

    /**
     * Sets the project name.
     *
     * @param name
     */
    public void setName(final String name) {
        this.name = name;
        setModified();
    }

    /**
     * Sets a filter.
     *
     * @param filter
     */
    public void setNodeFilter(final ModelNodeFilter filter) {
        nodeFilter = filter;
        setModified();
    }
    
    /**
     * Sets the current output.
     *
     * @param output
     * @param node
     */
    public void setOutput(final DataHandle output, final ARXNode node) {
        this.output = output;
        this.outputNode = node;
        if (node != null) {
            outputNodeAsString = Arrays.toString(node.getTransformation());
        } else {
            outputNodeAsString = null;
        }
        setModified();
    }
    
    /**
     * Sets the current output, deserialized from a project
     *
     * @param stream
     * @param node
     * @throws IOException 
     * @throws ClassNotFoundException 
     */
    public void setOutput(final InputStream stream) throws ClassNotFoundException, IOException {
        
        // Backwards compatibility
        if (stream == null) {
            return;
        }
        this.outputNode = this.getSelectedNode();
        if (this.outputNode != null) {
            this.output = this.result.getOutput(stream, outputNode);
            this.outputNodeAsString = Arrays.toString(outputNode.getTransformation());
        } else {
            this.output = null;
            this.outputNodeAsString = null;
        }
    }
    
    /**
     * Sets the output config.
     *
     * @param config
     */
    public void setOutputConfig(final ModelConfiguration config) {
        outputConfig = config;
    }

    /**
     * Sets the project path.
     *
     * @param path
     */
    public void setPath(final String path) {
        this.path = path;
    }
    
    /**
     * @param perspective the perspective to set
     */
    public void setPerspective(Perspective perspective) {
        this.perspective = perspective;
    }

    /**
     * @param optimizationStatistics the optimizationStatistics to set
     */
    public void setProcessStatistics(ARXProcessStatistics optimizationStatistics) {
        this.optimizationStatistics = optimizationStatistics;
        this.setModified();
    }

    /**
     * Sets the query.
     *
     * @param query
     */
    public void setQuery(String query){
        this.query = query;
        setModified();
    }

    /**
     * Sets the result.
     *
     * @param result
     */
    public void setResult(final ARXResult result) {
        this.result = result;
        if ((result != null) && (result.getGlobalOptimum() != null)) {
            optimalNodeAsString = Arrays.toString(result.getGlobalOptimum().getTransformation());
        } else {
            optimalNodeAsString = null;
        }
        setModified();
    }

    /**
     * Sets the selected attribute.
     *
     * @param attribute
     */
    public void setSelectedAttribute(final String attribute) {
        selectedAttribute = attribute;

        // Track last two selected attributes
        if (pair == null)
            pair = new String[] { null, null };
        if (pair[0] == null) {
            pair[0] = attribute;
            pair[1] = null;
        } else if (pair[1] == null) {
            pair[1] = attribute;
        } else {
            pair[0] = pair[1];
            pair[1] = attribute;
        }

        setModified();
    }

    /**
     * Sets a set of selected attributes
     * @param set
     */
    public void setSelectedClasses(Set<String> set) {
        this.selectedClasses = set;
        this.setModified();
    }

    /**
     * Sets the selected class value.
     * @param classValue
     */
    public void setSelectedClassValue(final String classValue) {
        selectedClassValue = classValue;
    }

    /**
     * Sets a set of selected attributes
     * @param set
     */
    public void setSelectedFeatures(Set<String> set) {
        this.selectedFeatures = set;
        this.setModified();
    }

    /**
     * Sets the selected node.
     *
     * @param node
     */
    public void setSelectedNode(final ARXNode node) {
        selectedNode = node;
        setModified();
    }
    
    /**
     * Sets a set of quasi identifiers selected for risk analysis
     * @param set
     */
    public void setSelectedQuasiIdentifiers(Set<String> set) {
        this.selectedQuasiIdentifiers = set;
        this.setModified();
    }
    
    /**
     * 
     *
     * @param snapshotSize
     */
    public void setSnapshotSizeDataset(final double snapshotSize) {
        snapshotSizeDataset = snapshotSize;
        setModified();
    }

    /**
     * Sets the according parameter.
     *
     * @param snapshotSize
     */
    public void setSnapshotSizeSnapshot(final double snapshotSize) {
        setModified();
        snapshotSizeSnapshot = snapshotSize;
    }

    /**
     * Sets how the subset was defined.
     */
    public void setSubsetManual(){
        if (!this.subsetOrigin.endsWith(Resources.getMessage("Model.1"))) { //$NON-NLS-1$
            this.subsetOrigin += Resources.getMessage("Model.2"); //$NON-NLS-1$
        }
    }
    
    /**
     * Sets how the subset was defined.
     *
     * @param origin
     */
    public void setSubsetOrigin(String origin){
        this.subsetOrigin = origin;
    }
    
    /**
     * Sets the execution time of the last anonymization process.
     *
     * @param time
     */
    public void setTime(final long time) {
        this.time = time;
    }

    /**
     * Marks this model as unmodified.
     */
    public void setUnmodified() {
        modified = false;
        inputConfig.setUnmodified();
        getRiskModel().setUnmodified();
        if (outputConfig != null) {
            outputConfig.setUnmodified();
        }
        if (clipboard != null) {
            clipboard.setUnmodified();
        }
        getClassificationModel().setUnmodified();
    }

    /**
     * Sets whether functional hierarchies should be used during anonymization to estimate utility
     * @param useFunctionalHierarchies
     */
    public void setUseFunctionalHierarchies(boolean useFunctionalHierarchies) {
        this.useFunctionalHierarchies = useFunctionalHierarchies;
    }

    /**
     * Sets whether list-wise deletion should be used for summary statistics
     * @param useListwiseDeletion
     */
    public void setUseListwiseDeletion(boolean useListwiseDeletion) {
        this.useListwiseDeletion = useListwiseDeletion;
    }
    
    /**
     * Sets the view configuration.
     *
     * @param viewConfig
     */
    public void setViewConfig(ModelViewConfig viewConfig) {
        this.viewConfig = viewConfig;
    }

    /**
     * Sets visualization as enabled/disabled.
     *
     * @param value
     */
    public void setVisualizationEnabled(boolean value){
        this.showVisualization = value;
        this.setModified();
    }

    /**
     * Converts attributes into an array ordered by occurrence in the dataset
     * @param set
     * @return
     */
    private String[] getAttributesAsArray(Set<String> set) {
        if (this.getInputConfig() == null || this.getInputConfig().getInput() == null ||
            this.getInputConfig().getInput().getHandle() == null || set == null || set.isEmpty()) {
            return new String[0];
        }
        List<String> result = new ArrayList<String>();
        DataHandle handle = this.getInputConfig().getInput().getHandle();
        for (int column = 0; column < handle.getNumColumns(); column++) {
            String attribute = handle.getAttributeName(column);
            if (set.contains(attribute)) {
                result.add(attribute);
            }
        }
        return result.toArray(new String[result.size()]);
    }
}
