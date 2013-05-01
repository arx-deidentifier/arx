/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.flash.gui;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.deidentifier.flash.AttributeType;
import org.deidentifier.flash.AttributeType.Hierarchy;
import org.deidentifier.flash.DataHandle;
import org.deidentifier.flash.FLASHAnonymizer;
import org.deidentifier.flash.FLASHLattice.FLASHNode;
import org.deidentifier.flash.FLASHResult;
import org.deidentifier.flash.gui.view.impl.explore.NodeFilter;

public class Model implements Serializable {

    private static final long         serialVersionUID     = -7669920657919151279L;

    // TODO: Check if all initial values are ok
    private transient Set<FLASHNode>  clipboard            = new HashSet<FLASHNode>();
    private transient DataHandle      output               = null;
    private transient FLASHNode       outputNode           = null;
    private transient FLASHResult     result               = null;
    private transient FLASHNode       selectedNode         = null;
    private transient FLASHAnonymizer anonymizer           = null;
    private transient String          path                 = null;

    private String                    name                 = null;
    private char                      separator            = ';';
    private String                    description;
    private int                       historySize          = 200;
    private double                    snapshotSizeDataset  = 0.2d;
    private double                    snapshotSizeSnapshot = 0.8d;
    private int                       initialNodesInViewer = 100;
    private int                       maxNodesInLattice    = 100000;
    private int                       maxNodesInViewer     = 700;

    private String                    selectedAttribute    = null;
    private NodeFilter                nodeFilter           = null;
    private boolean                   modified             = false;
    private long                      inputBytes           = 0L;
    private String[]                  pair                 = new String[] { null,
            null                                          };

    protected String                  optimalNodeAsString;
    protected String                  outputNodeAsString;

    protected long                    time;

    private Configuration             inputConfig          = new Configuration();

    private Configuration             outputConfig         = null;

    private String                    suppressionString    = "*";                     //$NON-NLS-1$

    private int[]                     colors;
    private int[]                     groups;

    public Model(final String name, final String description) {
        this.name = name;
        this.description = description;
        setModified();
    }

    public FLASHAnonymizer createAnonymizer() {
        outputConfig = inputConfig.clone();
        // Initialize anonymizer
        final FLASHAnonymizer anonymizer = new FLASHAnonymizer();
        anonymizer.setHistorySize(getHistorySize());
        anonymizer.setMaximumSnapshotSizeDataset(getSnapshotSizeDataset());
        anonymizer.setSuppressionString(getSuppressionString());
        anonymizer.setMaximumSnapshotSizeSnapshot(getSnapshotSizeSnapshot());
        this.anonymizer = inputConfig.getAnonymizer(anonymizer);
        return anonymizer;
    }

    public FLASHAnonymizer getAnonymizer() {
        return anonymizer;
    }

    public String[] getAttributePair() {
        if (pair == null) pair = new String[] { null, null };
        return pair;
    }

    public Set<FLASHNode> getClipboard() {
        return clipboard;
    }

    public String getDescription() {
        return description;
    }

    /**
     * @return the historySize
     */
    public int getHistorySize() {
        return historySize;
    }

    public int getInitialNodesInViewer() {
        return initialNodesInViewer;
    }

    public long getInputBytes() {
        return inputBytes;
    }

    public Configuration getInputConfig() {
        return inputConfig;
    }

    public int getMaxNodesInLattice() {
        return maxNodesInLattice;
    }

    public int getMaxNodesInViewer() {
        return maxNodesInViewer;
    }

    public String getName() {
        return name;
    }

    public NodeFilter getNodeFilter() {
        return nodeFilter;
    }

    public String getOptimalNodeAsString() {
        return optimalNodeAsString;
    }

    /**
     * @return the output
     */
    public DataHandle getOutput() {
        return output;
    }

    public Configuration getOutputConfig() {
        return outputConfig;
    }

    public FLASHNode getOutputNode() {
        return outputNode;
    }

    public String getOutputNodeAsString() {
        return outputNodeAsString;
    }

    public String getPath() {
        return path;
    }

    /**
     * @return the result
     */
    public FLASHResult getResult() {
        return result;
    }

    /**
     * Returns the currently selected attribute
     * 
     * @return
     */
    public String getSelectedAttribute() {
        return selectedAttribute;
    }

    public FLASHNode getSelectedNode() {
        return selectedNode;
    }

    public char getSeparator() {
        return separator;
    }

    /**
     * @return the snapshotSizeDataset
     */
    public double getSnapshotSizeDataset() {
        return snapshotSizeDataset;
    }

    public double getSnapshotSizeSnapshot() {
        return snapshotSizeSnapshot;
    }

    /**
     * @return the suppressionString
     */
    public String getSuppressionString() {
        return suppressionString;
    }

    public long getTime() {
        return time;
    }

    public boolean isModified() {
        if (inputConfig.isModified()) { return true; }
        if ((outputConfig != null) && outputConfig.isModified()) { return true; }
        return modified;
    }

    public boolean isQuasiIdentifierSelected() {
        return (getInputConfig().getInput()
                                .getDefinition()
                                .getAttributeType(getSelectedAttribute()) instanceof Hierarchy);
    }

    public boolean isSensitiveAttributeSelected() {
        return (getInputConfig().getInput()
                                .getDefinition()
                                .getAttributeType(getSelectedAttribute()) == AttributeType.SENSITIVE_ATTRIBUTE);
    }

    public void reset() {
        // TODO: Need to reset more fields
        inputConfig = new Configuration();
        outputConfig = null;
        output = null;
        result = null;
    }

    public void resetAttributePair() {
        if (pair == null) pair = new String[] { null, null };
        pair[0] = null;
        pair[1] = null;
    }

    public void setAnonymizer(final FLASHAnonymizer anonymizer) {
        setModified();
        this.anonymizer = anonymizer;
    }

    public void setClipboard(final HashSet<FLASHNode> set) {
        setModified();
        clipboard = set;
    }

    public void setDescription(final String description) {
        this.description = description;
        setModified();
    }

    /**
     * @param historySize
     *            the historySize to set
     */
    public void setHistorySize(final int historySize) {
        this.historySize = historySize;
        setModified();
    }

    public void setInitialNodesInViewer(final int val) {
        initialNodesInViewer = val;
        setModified();
    }

    public void setInputBytes(final long inputBytes) {
        setModified();
        this.inputBytes = inputBytes;
    }

    public void setInputConfig(final Configuration config) {
        inputConfig = config;
    }

    public void setMaxNodesInLattice(final int maxNodesInLattice) {
        this.maxNodesInLattice = maxNodesInLattice;
        setModified();
    }

    public void setMaxNodesInViewer(final int maxNodesInViewer) {
        this.maxNodesInViewer = maxNodesInViewer;
        setModified();
    }

    private void setModified() {
        modified = true;
    }

    public void setName(final String name) {
        this.name = name;
        setModified();
    }

    public void setNodeFilter(final NodeFilter filter) {
        nodeFilter = filter;
        setModified();
    }

    /**
     * @param output
     *            the output to set
     */
    public void setOutput(final DataHandle output, final FLASHNode node) {
        this.output = output;
        outputNode = node;
        if (node != null) {
            outputNodeAsString = Arrays.toString(node.getTransformation());
        }
        setModified();
    }

    public void setOutputConfig(final Configuration config) {
        outputConfig = config;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    /**
     * @param result
     *            the result to set
     */
    public void setResult(final FLASHResult result) {
        this.result = result;
        if ((result != null) && (result.getGlobalOptimum() != null)) {
            optimalNodeAsString = Arrays.toString(result.getGlobalOptimum()
                                                        .getTransformation());
        }
        setModified();
    }

    public void setSaved() {
        modified = false;
    }

    public void setSelectedAttribute(final String attribute) {
        selectedAttribute = attribute;

        if (pair == null) pair = new String[] { null, null };
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

    public void setSelectedNode(final FLASHNode node) {
        selectedNode = node;
        setModified();
    }

    public void setSeparator(final char separator) {
        this.separator = separator;
    }

    /**
     * @param snapshotSizeDataset
     *            the snapshotSizeDataset to set
     */
    public void setSnapshotSizeDataset(final double snapshotSize) {
        snapshotSizeDataset = snapshotSize;
        setModified();
    }

    public void setSnapshotSizeSnapshot(final double snapshotSize) {
        setModified();
        snapshotSizeSnapshot = snapshotSize;
    }

    /**
     * @param suppressionString
     *            the suppressionString to set
     */
    public void setSuppressionString(final String suppressionString) {
        this.suppressionString = suppressionString;
        setModified();
    }

    public void setTime(final long time) {
        this.time = time;
    }

    public void setUnmodified() {
        modified = false;
        inputConfig.setUnmodified();
        if (outputConfig != null) {
            outputConfig.setUnmodified();
        }
    }

    public boolean validLatticeSize() {
        return getInputConfig().validLatticeSize(maxNodesInLattice);
    }

    public void setColors(int[] colors) {
        this.colors = colors;
    }
    
    public void setGroups(int[] groups) {
        this.groups = groups;
    }

    public int[] getColors() {
        // TODO: Refactor to colors[groups[row]]
        return this.colors;
    }

    public int[] getGroups() {
        // TODO: Refactor to colors[groups[row]]
        return this.groups;
    }
}
