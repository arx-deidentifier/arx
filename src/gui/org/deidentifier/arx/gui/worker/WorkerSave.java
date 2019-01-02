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

package org.deidentifier.arx.gui.worker;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.deidentifier.arx.ARXLattice;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataHandleOutput;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeWithFormat;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelConfiguration;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.worker.io.FileBuilder;
import org.deidentifier.arx.gui.worker.io.Vocabulary;
import org.deidentifier.arx.gui.worker.io.Vocabulary_V2;
import org.deidentifier.arx.gui.worker.io.XMLWriter;
import org.deidentifier.arx.io.CSVDataOutput;
import org.deidentifier.arx.io.CSVSyntax;
import org.deidentifier.arx.metric.InformationLoss;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This worker saves a project file to disk.
 *
 * @author Fabian Prasser
 */
public class WorkerSave extends Worker<Model> {

    /** The vocabulary to use. */
    private Vocabulary vocabulary = new Vocabulary_V2();
	
	/** The path. */
    private final String     path;
    
    /** The model. */
    private final Model      model;

    /**
     * Creates a new instance.
     *
     * @param path
     * @param controller
     * @param model
     */
    public WorkerSave(final String path,
                      final Controller controller,
                      final Model model) {
        this.path = path;
        this.model = model;
    }

    @Override
    public void run(final IProgressMonitor arg0) throws InvocationTargetException,
                                                        InterruptedException {

        arg0.beginTask(Resources.getMessage("WorkerSave.0"), 8); //$NON-NLS-1$
        File temp = null;
        try {
            temp = File.createTempFile("arx", "deid");
            final FileOutputStream f = new FileOutputStream(temp);
            final ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(f));
            zip.setLevel(Deflater.BEST_SPEED);
            model.createConfig(); 
            writeMetadata(model, zip);
            arg0.worked(1);
            writeModel(model, zip);
            arg0.worked(1);
            writeInput(model, zip);
            arg0.worked(1);
            writeOutput(model, zip);
            arg0.worked(1);
            writeConfiguration(model, zip);
            arg0.worked(1);
            final Map<String, Integer> map = writeLattice(model, zip);
            arg0.worked(1);
            writeClipboard(model, map, zip);
            arg0.worked(1);
            writeFilter(model, zip);
            zip.close();
            arg0.worked(1);
            FileUtils.copyFile(temp, new File(path));
            FileUtils.deleteQuietly(temp);
        } catch (final Exception e) {
            error = e;
            arg0.done();
            FileUtils.deleteQuietly(temp);
            return;
        }

        arg0.done();
    }

    /**
     * Converts an attribute name to a file name.
     *
     * @param a
     * @return
     */
    private String toFileName(final String a) {
        return a;
    }

    /**
     * Returns an XML representation of the lattice.
     *
     * @param map
     * @param l
     * @param zip
     * @throws IOException
     */
    private void toXML(final Map<String, Integer> map,
                       final ARXLattice l,
                       final ZipOutputStream zip) throws IOException {

        // Build mapping
        int id = 0;
        for (final ARXNode[] level : l.getLevels()) {
            for (final ARXNode n : level) {
                final String key = Arrays.toString(n.getTransformation());
                if (!map.containsKey(key)) {
                    map.put(key, id++);
                }
            }
        }

        // Write directly because of size
        final FileBuilder b = new FileBuilder(new OutputStreamWriter(zip));
        final XMLWriter writer = new XMLWriter(b, true);
        
        writer.write(vocabulary.getHeader());

        // Build xml
        writer.indent(vocabulary.getLattice());
        for (int i = 0; i < l.getLevels().length; i++) {
        	
        	writer.indent(vocabulary.getLevel(), vocabulary.getDepth(), i);
            for (final ARXNode n : l.getLevels()[i]) {
                
            	final String key = Arrays.toString(n.getTransformation());
                final int currentId = map.get(key);
                
                writer.indent(vocabulary.getNode2(), vocabulary.getId(), currentId);
                writer.write(vocabulary.getTransformation(), n.getTransformation());
                writer.write(vocabulary.getAnonymity(), n.getAnonymity());
                writer.write(vocabulary.getChecked(), n.isChecked());
                if (n.getPredecessors().length > 0) {
                	writer.write(vocabulary.getPredecessors(), n.getPredecessors(), map);
                }
                if (n.getSuccessors().length > 0) {
                	writer.write(vocabulary.getSuccessors(), n.getSuccessors(), map);
                }
                writer.indent(vocabulary.getInfoloss());
                writer.write(vocabulary.getMax2(), n.getHighestScore().toString());
                writer.write(vocabulary.getMin2(), n.getLowestScore().toString());
                writer.unindent();
                writer.unindent();
            }
            writer.unindent();
        }
        writer.unindent();
        b.flush();
    }

    /**
     * Returns an XML representation of the clipboard.
     *
     * @param map
     * @param clipboard
     * @return
     * @throws IOException
     */
    private String toXML(final Map<String, Integer> map,
                         final List<ARXNode> clipboard) throws IOException {

        XMLWriter writer = new XMLWriter();
        writer.indent(vocabulary.getClipboard()); //$NON-NLS-1$
        for (final ARXNode n : clipboard) {
        	writer.write(vocabulary.getNode(), Arrays.toString(n.getTransformation())); //$NON-NLS-1$
        }
        writer.unindent();
        return writer.toString();
    }
    
    /**
     * Converts a model to XML.
     *
     * @param model
     * @return
     * @throws IOException
     */
    private String toXML(final Model model) throws IOException {
    	
        XMLWriter writer = new XMLWriter();
        writer.indent(vocabulary.getProject());
        writer.write(vocabulary.getName(), model.getName());
        
        writer.write(vocabulary.getSeparator(), model.getCSVSyntax().getDelimiter());
        writer.write(vocabulary.getEscape(), model.getCSVSyntax().getEscape());
        writer.write(vocabulary.getQuote(), model.getCSVSyntax().getQuote());
        
        String linebreak = "UNIX"; //$NON-NLS-1$
        char[] _linebreak = model.getCSVSyntax().getLinebreak();
        if (_linebreak.length == 1 && _linebreak[0] == '\r') {
            linebreak = "MAC"; //$NON-NLS-1$
        } else if (_linebreak.length == 2){
            linebreak = "WINDOWS"; //$NON-NLS-1$
        }
        writer.write(vocabulary.getLinebreak(), linebreak);
        
        writer.write(vocabulary.getDescription(), model.getDescription());
        writer.write(vocabulary.getLocale(), model.getLocale().getLanguage().toUpperCase());
        writer.write(vocabulary.getHistorySize(), model.getHistorySize());
        writer.write(vocabulary.getSnapshotSizeDataset(), model.getSnapshotSizeDataset());
        writer.write(vocabulary.getSnapshotSizeSnapshot(), model.getSnapshotSizeSnapshot());
        writer.write(vocabulary.getInitialNodesInViewer(), model.getInitialNodesInViewer());
        writer.write(vocabulary.getMaxNodesInViewer(), model.getMaxNodesInViewer());
        writer.write(vocabulary.getSelectedAttribute(), model.getSelectedAttribute());
        writer.write(vocabulary.getInputBytes(), model.getInputBytes());
        writer.unindent();
        return writer.toString();
    }

    /**
     * Converts a configuration to XML.
     *
     * @param config
     * @return
     * @throws IOException
     */
    private String toXML(final ModelConfiguration config) throws IOException {
        
    	XMLWriter writer = new XMLWriter(); 
        writer.indent(vocabulary.getConfig());
        writer.write(vocabulary.getSuppressionAlwaysEnabled(), config.isSuppressionAlwaysEnabled());
        
        // Write suppressed attribute types
        writer.indent(vocabulary.getSuppressedAttributeTypes());
        for (AttributeType type : new AttributeType[]{AttributeType.QUASI_IDENTIFYING_ATTRIBUTE,
                                                      AttributeType.SENSITIVE_ATTRIBUTE,
                                                      AttributeType.INSENSITIVE_ATTRIBUTE}) {
            if (config.isAttributeTypeSuppressed(type)) {
                writer.write(vocabulary.getType(), type.toString());
            }
        }
        writer.unindent();
        
        writer.write(vocabulary.getPracticalMonotonicity(), config.isPracticalMonotonicity());
        writer.write(vocabulary.getRelativeMaxOutliers(), config.getSuppressionLimit());
        writer.write(vocabulary.getMetric(), config.getMetric().toString());

        // Write weights
        writer.indent(vocabulary.getAttributeWeights());
        for (Entry<String, Double> entry : config.getAttributeWeights().entrySet()) {
            writer.indent(vocabulary.getAttributeWeight());
            writer.write(vocabulary.getAttribute(), entry.getKey());
            writer.write(vocabulary.getWeight(), entry.getValue());
            writer.unindent();
        }
        writer.unindent();
        
        // Write criteria
        writer.indent(vocabulary.getCriteria());
        for (PrivacyCriterion c : config.getCriteria()) {
        	if (c != null) {
        		writer.write(vocabulary.getCriterion(), c.toString());
        	}
        }
        writer.unindent();
        writer.unindent();
        return writer.toString();
    }

    /**
     * Returns an XML representation of the data definition.
     *
     * @param config
     * @param handle
     * @param definition
     * @return
     * @throws IOException
     */
    private String toXML(final ModelConfiguration config, 
                         final DataHandle handle,
                         final DataDefinition definition) throws IOException {
        
    	XMLWriter writer = new XMLWriter();
    	writer.indent(vocabulary.getDefinition());
        for (int i = 0; i < handle.getNumColumns(); i++) {
            final String attr = handle.getAttributeName(i);
            AttributeType t = definition.getAttributeType(attr);
            DataType<?> dt = definition.getDataType(attr);
            if (t == null) t = AttributeType.IDENTIFYING_ATTRIBUTE;
            if (dt == null) dt = DataType.STRING;
            
            writer.indent(vocabulary.getAssigment());
            writer.write(vocabulary.getName(), attr);
            writer.write(vocabulary.getType(), t.toString());
            writer.write(vocabulary.getDatatype(), dt.getDescription().getLabel());
            if (dt.getDescription().hasFormat()){
                String format = ((DataTypeWithFormat)dt).getFormat();
                if (format != null){
                    writer.write(vocabulary.getFormat(), format);
                }
                Locale locale = ((DataTypeWithFormat)dt).getLocale();
                if (locale != null){
                    writer.write(vocabulary.getLocale(), locale.getLanguage().toUpperCase());
                }
            }
            
            // Response variables
            if (definition.isResponseVariable(attr)) {
                writer.write(vocabulary.getResponseVariable(), "true"); //$NON-NLS-1$
            }
            
            // Do we have a hierarchy
            if (definition.getHierarchy(attr) != null && definition.getHierarchy(attr).length != 0 &&
                definition.getHierarchy(attr)[0].length != 0) {
                writer.write(vocabulary.getRef(), "hierarchies/" + toFileName(attr) + ".csv"); //$NON-NLS-1$ //$NON-NLS-2$
                Integer min = config.getMinimumGeneralization(attr);
                Integer max = config.getMaximumGeneralization(attr);
                writer.write(vocabulary.getMin(), min == null ? "All" : String.valueOf(min)); //$NON-NLS-1$
                writer.write(vocabulary.getMax(), max == null ? "All" : String.valueOf(max)); //$NON-NLS-1$
            }
            
            // Do we have a microaggregate function
            if (definition.getMicroAggregationFunction(attr) != null) {
                writer.write(vocabulary.getMicroAggregationFunction(), config.getMicroAggregationFunction(attr).getLabel());
                writer.write(vocabulary.getMicroAggregationIgnoreMissingData(), config.getMicroAggregationIgnoreMissingData(attr));
            }

            writer.unindent();

        }
        writer.unindent();
        return writer.toString();
    }

    /**
     * Writes the clipboard to the file.
     *
     * @param model
     * @param map
     * @param zip
     * @throws IOException
     */
    private void writeClipboard(final Model model,
                                final Map<String, Integer> map,
                                final ZipOutputStream zip) throws IOException {
        if (model.getClipboard().getClipboardEntries().isEmpty()) { return; }

        // Write clipboard
        zip.putNextEntry(new ZipEntry("clipboard.xml")); //$NON-NLS-1$
        final Writer w = new OutputStreamWriter(zip);
        w.write(toXML(map, model.getClipboard().getClipboardEntries()));
        w.flush();

    }

    /**
     * Writes the configuration to the file.
     *
     * @param model
     * @param zip
     * @throws IOException
     */
    private void writeConfiguration(final Model model, final ZipOutputStream zip) throws IOException {

        if (model.getInputConfig() != null) {
            writeConfiguration(model.getInputConfig(), "input/", zip); //$NON-NLS-1$
        }
        if (model.getOutputConfig() != null) {
            writeConfiguration(model.getOutputConfig(), "output/", zip); //$NON-NLS-1$
        }
    }

    /**
     * Writes the configuration to the file.
     *
     * @param config
     * @param prefix
     * @param zip
     * @throws IOException
     */
    private void writeConfiguration(final ModelConfiguration config,
                                    final String prefix,
                                    final ZipOutputStream zip) throws IOException {
    	
        zip.putNextEntry(new ZipEntry(prefix + "config.dat")); //$NON-NLS-1$
        final ObjectOutputStream oos = new ObjectOutputStream(zip);
        oos.writeObject(config);
        oos.flush();

        zip.putNextEntry(new ZipEntry(prefix + "config.xml")); //$NON-NLS-1$
        final Writer w = new OutputStreamWriter(zip);
        w.write(toXML(config));
        w.flush();

        writeDefinition(config, prefix, zip);
        writeHierarchies(config, prefix, zip);
    }

    /**
     * Writes the data definition to the file.
     *
     * @param config
     * @param prefix
     * @param zip
     * @throws IOException
     */
    private void writeDefinition(final ModelConfiguration config,
                                 final String prefix,
                                 final ZipOutputStream zip) throws IOException {
    	
    	// Obtain definition
    	DataDefinition definition = null;
    	if (config == model.getInputConfig()) definition = model.getInputDefinition();
    	else definition = model.getOutputDefinition();
    	
    	// Store
		if (definition != null) {
			zip.putNextEntry(new ZipEntry(prefix + "definition.xml")); //$NON-NLS-1$
			final Writer w = new OutputStreamWriter(zip);
			w.write(toXML(config, config.getInput().getHandle(), definition));
			w.flush();
		}
    }

    /**
     * Writes the current filter to the file.
     *
     * @param model
     * @param zip
     * @throws IOException
     */
    private void writeFilter(final Model model, final ZipOutputStream zip) throws IOException {
        if ((model.getAnonymizer() == null) || (model.getResult() == null)) { return; }
        zip.putNextEntry(new ZipEntry("filter.dat")); //$NON-NLS-1$
        final ObjectOutputStream oos = new ObjectOutputStream(zip);
        oos.writeObject(model.getNodeFilter());
        oos.flush();
    }

    /**
     * Writes the hierarchies to the file.
     *
     * @param config
     * @param prefix
     * @param zip
     * @throws IOException
     */
    private void writeHierarchies(final ModelConfiguration config,
                                  final String prefix,
                                  final ZipOutputStream zip) throws IOException {

        // Store all from config
        Set<String> saved = new HashSet<>();
        for (Entry<String, Hierarchy> entry : config.getHierarchies().entrySet()) {

            // Store this hierarchy
            zip.putNextEntry(new ZipEntry(prefix + "hierarchies/" + toFileName(entry.getKey()) + ".csv")); //$NON-NLS-1$ //$NON-NLS-2$
            CSVDataOutput out = new CSVDataOutput(zip, model.getCSVSyntax().getDelimiter());
            out.write(entry.getValue().getHierarchy());
            saved.add(entry.getKey());
        }

        // This additional code implements a bugfix. ARX automatically creates hierarchies
        // implementing the identity function when the user does not specify one but defines the attribute
        // to be a quasi-identifier. These hierarchies were not serialized into project files in ARX 3.4.1,
        // leading to inconsistent files which could not be loaded any more. We now do our best to save
        // every relevant hierarchy:

        // Obtain definition
        DataDefinition definition = null;
        if (config == model.getInputConfig()) definition = model.getInputDefinition();
        else definition = model.getOutputDefinition();
                
        // Store all from definition that have not yet been stored
        if (config.getInput() != null) {
            DataHandle handle = config.getInput().getHandle();
            for (int i = 0; i < handle.getNumColumns(); i++) {
                final String attr = handle.getAttributeName(i);

                // Do we have a hierarchy
                if (!saved.contains(attr) && definition.getHierarchy(attr) != null && 
                    definition.getHierarchy(attr).length != 0 &&
                    definition.getHierarchy(attr)[0].length != 0) {

                    // Store this hierarchy
                    zip.putNextEntry(new ZipEntry(prefix + "hierarchies/" + toFileName(attr) + ".csv")); //$NON-NLS-1$ //$NON-NLS-2$
                    CSVDataOutput out = new CSVDataOutput(zip, model.getCSVSyntax().getDelimiter());
                    out.write(definition.getHierarchy(attr));
                    saved.add(attr);
                }
            }
        }
    }
    
    /**
     * Writes the input to the file.
     *
     * @param model
     * @param zip
     * @throws IOException
     */
    private void writeInput(final Model model, final ZipOutputStream zip) throws IOException {
        if (model.getInputConfig().getInput() != null) {
            if (model.getInputConfig().getInput().getHandle() != null) {
                zip.putNextEntry(new ZipEntry("data/input.csv")); //$NON-NLS-1$
                
                // Write UTF-8 only
                final CSVDataOutput out = new CSVDataOutput(zip,
                                                            model.getCSVSyntax().getDelimiter(),
                                                            CSVSyntax.DEFAULT_QUOTE,
                                                            CSVSyntax.DEFAULT_ESCAPE,
                                                            CSVSyntax.DEFAULT_LINEBREAK,
                                                            StandardCharsets.UTF_8);
                
                // Write
                out.write(model.getInputConfig()
                               .getInput()
                               .getHandle()
                               .iterator());
                
            }
        }
    }

    /**
     * Writes the lattice to the file.
     *
     * @param model
     * @param zip
     * @return
     * @throws IOException
     */
    private Map<String, Integer> writeLattice(final Model model, final ZipOutputStream zip) throws IOException {

        // Mapping
        final Map<String, Integer> map = new HashMap<String, Integer>();
        if ((model.getResult() == null) ||
            (model.getResult().getLattice() == null)) { return map; }

        // Write lattice
        final ARXLattice l = model.getResult().getLattice();
        zip.putNextEntry(new ZipEntry("lattice.xml")); //$NON-NLS-1$
        toXML(map, l, zip);

        zip.putNextEntry(new ZipEntry("lattice.dat")); //$NON-NLS-1$
        ObjectOutputStream oos = new ObjectOutputStream(zip);
        oos.writeObject(model.getResult().getLattice());
        oos.writeObject(model.getResult()
                             .getLattice()
                             .access()
                             .getAttributeMap());
        oos.flush();

        // Write score
        zip.putNextEntry(new ZipEntry("infoloss.dat")); //$NON-NLS-1$
        final Map<Integer, InformationLoss<?>> max = new HashMap<Integer, InformationLoss<?>>();
        final Map<Integer, InformationLoss<?>> min = new HashMap<Integer, InformationLoss<?>>();
        for (final ARXNode[] level : l.getLevels()) {
            for (final ARXNode n : level) {
                final String key = Arrays.toString(n.getTransformation());
                min.put(map.get(key), n.getLowestScore());
                max.put(map.get(key), n.getHighestScore());
            }
        }
        oos = new ObjectOutputStream(zip);
        oos.writeObject(min);
        oos.writeObject(max);
        oos.flush();
        min.clear();
        max.clear();

        // Write attributes
        zip.putNextEntry(new ZipEntry("attributes.dat")); //$NON-NLS-1$
        final Map<Integer, Map<Integer, Object>> attrs = new HashMap<Integer, Map<Integer, Object>>();
        for (final ARXNode[] level : l.getLevels()) {
            for (final ARXNode n : level) {
                final String key = Arrays.toString(n.getTransformation());
                attrs.put(map.get(key), n.getAttributes());
            }
        }
        oos = new ObjectOutputStream(zip);
        oos.writeObject(attrs);
        oos.flush();
        attrs.clear();

        // Return mapping
        return map;
    }

    /**
     * Writes the meta data to the file.
     *
     * @param model
     * @param zip
     * @throws IOException
     */
    private void writeMetadata(final Model model, final ZipOutputStream zip) throws IOException {
    	
        // Write metadata
        zip.putNextEntry(new ZipEntry("metadata.xml")); //$NON-NLS-1$
        final OutputStreamWriter w = new OutputStreamWriter(zip);
        XMLWriter writer = new XMLWriter(new FileBuilder(w));
        writer.indent(vocabulary.getMetadata());
        writer.write(vocabulary.getVersion(), Resources.getVersion());
        writer.write(vocabulary.getVocabulary(), vocabulary.getVocabularyVersion());
        writer.unindent();
        w.flush();

    }
    
    /**
     * Writes the project to the file.
     *
     * @param model
     * @param zip
     * @throws IOException
     */
    private void writeModel(final Model model, final ZipOutputStream zip) throws IOException {
        
        // Backwards compatibility
        model.setCharset("UTF-8");
        
        zip.putNextEntry(new ZipEntry("project.dat")); //$NON-NLS-1$
        final ObjectOutputStream oos = new ObjectOutputStream(zip);
        oos.writeObject(model);
        oos.flush();

        zip.putNextEntry(new ZipEntry("project.xml")); //$NON-NLS-1$
        final Writer w = new OutputStreamWriter(zip);
        w.write(toXML(model));
        w.flush();
    }

    /**
     * Writes the output to the file.
     *
     * @param model
     * @param zip
     * @throws IOException
     */
    private void writeOutput(final Model model, final ZipOutputStream zip) throws IOException {
        if (model.getOutput() != null) {
            zip.putNextEntry(new ZipEntry("data/output.dat")); //$NON-NLS-1$
            ((DataHandleOutput) model.getOutput()).write(zip);
        }
    }
}
