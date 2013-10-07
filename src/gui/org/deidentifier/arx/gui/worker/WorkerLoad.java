/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.arx.gui.worker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXLattice;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandleOutput;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelConfiguration;
import org.deidentifier.arx.gui.model.ModelNodeFilter;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.metric.InformationLoss;
import org.deidentifier.arx.metric.Metric;
import org.eclipse.core.runtime.IProgressMonitor;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class WorkerLoad extends Worker<Model> {

	private final ZipFile    zipfile;
	private final Controller controller;
	private ARXLattice       lattice;
	private Model            model;

	/**
	 * Constructor
	 * 
	 * @param file
	 * @param controller
	 * @throws ZipException
	 * @throws IOException
	 */
    public WorkerLoad(final File file, final Controller controller) throws ZipException,
                                                                   IOException {
        zipfile = new ZipFile(file);
        this.controller = controller;
    }

    /**
     * Constructor
     * 
     * @param path
     * @param controller
     * @throws IOException
     */
    public WorkerLoad(final String path, final Controller controller) throws IOException {
        zipfile = new ZipFile(path);
        this.controller = controller;
    }

    /**
     * Reads the clipboard from the file
     * 
     * @param map
     * @param zip
     * @throws SAXException
     * @throws IOException
     */
    private void readClipboard(final Map<String, ARXNode> map,
                               final ZipFile zip) throws SAXException,
                                                 IOException {

        // Check
        final ZipEntry entry = zip.getEntry("clipboard.xml"); //$NON-NLS-1$
        if (entry == null) { return; }

        // Clear
        model.setClipboard(new HashSet<ARXNode>());
        model.getClipboard().clear();

        // Parse
        final XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        final InputSource inputSource = new InputSource(zip.getInputStream(entry));
        xmlReader.setContentHandler(new WorkerLoadXMLHandler() {
            @Override
            protected boolean end(final String uri,
                                  final String localName,
                                  final String qName) throws SAXException {
                if (localName.equals("clipboard")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("node")) { //$NON-NLS-1$
                    final ARXNode node = map.get(payload.trim());
                    model.getClipboard().add(node);
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            protected boolean
                    start(final String uri,
                          final String localName,
                          final String qName,
                          final Attributes attributes) throws SAXException {
                if (localName.equals("clipboard")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("node")) { //$NON-NLS-1$
                    return true;
                } else {
                    return false;
                }
            }
        });
        xmlReader.parse(inputSource);
    }

    /**
     * Reads the configuration from the file
     * 
     * @param map
     * @param zip
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws SAXException
     */
    private void readConfiguration(final Map<String, ARXNode> map,
                                   final ZipFile zip) throws IOException,
                                                     ClassNotFoundException,
                                                     SAXException {

        readConfiguration("input/", false, map, zip); //$NON-NLS-1$
        readConfiguration("output/", true, map, zip); //$NON-NLS-1$

    }

    /**
     * Reads the configuration from the file
     * 
     * @param prefix
     * @param output
     * @param map
     * @param zip
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws SAXException
     */
    private void readConfiguration(final String prefix,
                                   final boolean output,
                                   final Map<String, ARXNode> map,
                                   final ZipFile zip) throws IOException,
                                                     ClassNotFoundException,
                                                     SAXException {

        // Check
        final ZipEntry entry = zip.getEntry(prefix + "config.dat"); //$NON-NLS-1$
        if (entry == null) { return; }

        // Read config
        final ObjectInputStream oos = new ObjectInputStream(zip.getInputStream(entry));
        final ModelConfiguration config = (ModelConfiguration) oos.readObject();
        oos.close();

        // Attach data
        if (!output) {
            readInput(config, zip);
            model.setInputConfig(config);
        } else {
            config.setInput(model.getInputConfig().getInput().clone(false));
            model.setOutputConfig(config);
        }

        // Attach definition
        readDefinition(config, prefix, zip);

        if (output) {
        	
            // Parse
            final String suppressionString = model.getSuppressionString();
            final int historySize = model.getHistorySize();
            final double snapshotSizeSnapshot = model.getSnapshotSizeSnapshot();
            final double snapshotSizeDataset = model.getSnapshotSizeDataset();
            final boolean removeOutliers = model.getOutputConfig().isRemoveOutliers();
            final Metric<?> metric = config.getMetric();
            final long time = model.getTime();
            final ARXNode optimalNode;
            final ARXNode outputNode;

            if (model.getOptimalNodeAsString() != null) {
                optimalNode = map.get(model.getOptimalNodeAsString());
            } else {
            	optimalNode = null;
            }
            if (model.getOutputNodeAsString() != null) {
                outputNode = map.get(model.getOutputNodeAsString());
            } else {
            	outputNode = null;
            }
            model.setSelectedNode(outputNode);
            
            // Update model
            model.setResult(new DataHandleOutput(config.getInput().getHandle(),
                                                 config.getInput().getDefinition(),
                                                 lattice,
                                                 removeOutliers,
                                                 suppressionString,
                                                 historySize,
                                                 snapshotSizeSnapshot,
                                                 snapshotSizeDataset,
                                                 metric,
                                                 model.getOutputConfig().getConfig(),
                                                 optimalNode,
                                                 time));

            // Create anonymizer
            final ARXAnonymizer f = new ARXAnonymizer();
            model.setAnonymizer(f);
            f.setRemoveOutliers(removeOutliers);
            f.setSuppressionString(suppressionString);
            f.setHistorySize(historySize);
            f.setMaximumSnapshotSizeSnapshot(snapshotSizeSnapshot);
            f.setMaximumSnapshotSizeDataset(snapshotSizeDataset);
        }
    }

    /**
     * Reads the data definition from the file
     * 
     * @param config
     * @param prefix
     * @param zip
     * @throws IOException
     * @throws SAXException
     */
    private void readDefinition(final ModelConfiguration config,
                                final String prefix,
                                final ZipFile zip) throws IOException,
                                                          SAXException {
    	
    	// Obtain entry
        final ZipEntry entry = zip.getEntry(prefix + "definition.xml"); //$NON-NLS-1$
        if (entry == null) { return; }

        // Read xml
        final XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        final InputSource inputSource = new InputSource(zip.getInputStream(entry));
        xmlReader.setContentHandler(new WorkerLoadXMLHandler() {
        	
            String attr, dtype, atype, ref, min, max;

            @Override
            protected boolean end(final String uri,
                                  final String localName,
                                  final String qName) throws SAXException {
            	
                if (localName.equals("definition")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("assigment")) { //$NON-NLS-1$

                    // Attribute name
                    if (attr == null) { throw new SAXException(Resources.getMessage("WorkerLoad.3")); } //$NON-NLS-1$

                    // Data type
                    if (dtype.equals(DataType.STRING.toString())) {
                        config.getInput()
                              .getDefinition()
                              .setDataType(attr, DataType.STRING);
                    } else if (dtype.equals(DataType.DECIMAL.toString())) {
                        config.getInput()
                              .getDefinition()
                              .setDataType(attr, DataType.DECIMAL);
                    } else {
                        config.getInput()
                              .getDefinition()
                              .setDataType(attr, DataType.DATE(dtype));
                    }

                    // Attribute type
                    if (atype.equals(AttributeType.IDENTIFYING_ATTRIBUTE.toString())) {
                        config.getInput()
                              .getDefinition()
                              .setAttributeType(attr, AttributeType.IDENTIFYING_ATTRIBUTE);
                    } else if (atype.equals(AttributeType.SENSITIVE_ATTRIBUTE.toString())) {
                        config.getInput()
                              .getDefinition()
                              .setAttributeType(attr, AttributeType.SENSITIVE_ATTRIBUTE);
                    } else if (atype.equals(AttributeType.INSENSITIVE_ATTRIBUTE.toString())) {
                        config.getInput()
                              .getDefinition()
                              .setAttributeType(attr, AttributeType.INSENSITIVE_ATTRIBUTE);
                    } else if (atype.equals(Hierarchy.create().toString())) {
                        try {
                            config.getInput()
                                  .getDefinition()
                                  .setAttributeType(attr, readHierarchy(zip, prefix, ref));
                        } catch (final IOException e) {
                            throw new SAXException(e);
                        }
                        config.getInput()
                              .getDefinition()
                              .setMinimumGeneralization(attr,Double.valueOf(min).intValue());
                        config.getInput()
                              .getDefinition()
                              .setMaximumGeneralization(attr,Double.valueOf(max).intValue());
                    } else {
                        throw new SAXException(Resources.getMessage("WorkerLoad.4")); //$NON-NLS-1$
                    }

                    return true;

                } else if (localName.equals("name")) { //$NON-NLS-1$
                    attr = payload;
                    return true;
                } else if (localName.equals("type")) { //$NON-NLS-1$
                    atype = payload;
                    return true;
                } else if (localName.equals("datatype")) { //$NON-NLS-1$
                    dtype = payload;
                    return true;
                } else if (localName.equals("ref")) { //$NON-NLS-1$
                    ref = payload;
                    return true;
                } else if (localName.equals("min")) { //$NON-NLS-1$
                    min = payload;
                    return true;
                } else if (localName.equals("max")) { //$NON-NLS-1$
                    max = payload;
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            protected boolean start(final String uri,
                                    final String localName,
                                    final String qName,
                                    final Attributes attributes) throws SAXException {
            	
                if (localName.equals("definition")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("assigment")) { //$NON-NLS-1$
                    attr = null;
                    dtype = null;
                    atype = null;
                    ref = null;
                    min = null;
                    max = null;
                    return true;
                } else if (localName.equals("name")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("type")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("datatype")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("ref")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("min")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("max")) { //$NON-NLS-1$
                    return true;
                } else {
                    return false;
                }
            }
        });
        xmlReader.parse(inputSource);
    }

    /**
     * Reads the filter from the file
     * 
     * @param zip
     * @throws SAXException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readFilter(final ZipFile zip) throws SAXException,
                                              IOException,
                                              ClassNotFoundException {
        // Read filter
        final ZipEntry entry = zip.getEntry("filter.dat"); //$NON-NLS-1$
        if (entry == null) { return; }
        final ObjectInputStream oos = new ObjectInputStream(zip.getInputStream(entry));
        model.setNodeFilter((ModelNodeFilter) oos.readObject());
        oos.close();
    }

    /**
     * Reads the hierarchy from the given location
     * 
     * @param zip
     * @param ref
     * @return
     * @throws IOException
     */
    private Hierarchy readHierarchy(final ZipFile zip,
                                    final String prefix,
                                    final String ref) throws IOException {
    	
        final ZipEntry entry = zip.getEntry(prefix + ref);
        if (entry == null) { throw new IOException(Resources.getMessage("WorkerLoad.5")); } //$NON-NLS-1$
        final InputStream is = zip.getInputStream(entry);
        return Hierarchy.create(is, model.getSeparator());
    }

    /**
     * Reads the input from the file
     * 
     * @param zip
     * @throws IOException
     */
    private void readInput(final ModelConfiguration config, final ZipFile zip) throws IOException {

        final ZipEntry entry = zip.getEntry("data/input.csv"); //$NON-NLS-1$
        if (entry == null) { return; }

        // Read input
        config.setInput(Data.create(zip.getInputStream(entry),
                                    model.getSeparator()));

        // And encode
        config.getInput().getHandle();
    }

    /**
     * Reads the lattice from several files
     * 
     * @param zip
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws SAXException
     */
    @SuppressWarnings({ "unchecked" })
    private Map<String, ARXNode> readLattice(final ZipFile zip) throws IOException,
                                                                       ClassNotFoundException,
                                                                       SAXException {

        ZipEntry entry = zip.getEntry("infoloss.dat"); //$NON-NLS-1$
        if (entry == null) { return null; }

        // Read infoloss
        final Map<Integer, InformationLoss> max;
        final Map<Integer, InformationLoss> min;
        ObjectInputStream oos = new ObjectInputStream(zip.getInputStream(entry));
        min = (Map<Integer, InformationLoss>) oos.readObject();
        max = (Map<Integer, InformationLoss>) oos.readObject();
        oos.close();

        entry = zip.getEntry("attributes.dat"); //$NON-NLS-1$
        if (entry == null) { throw new IOException(Resources.getMessage("WorkerLoad.6")); } //$NON-NLS-1$

        // Read attributes
        final Map<Integer, Map<Integer, Object>> attrs;
        oos = new ObjectInputStream(zip.getInputStream(entry));
        attrs = (Map<Integer, Map<Integer, Object>>) oos.readObject();
        oos.close();

        // Read lattice skeleton
        entry = zip.getEntry("lattice.dat"); //$NON-NLS-1$
        if (entry == null) { throw new IOException(Resources.getMessage("WorkerLoad.8")); } //$NON-NLS-1$
        oos = new ObjectInputStream(zip.getInputStream(entry));
        lattice = (ARXLattice) oos.readObject();
        final Map<String, Integer> headermap = (Map<String, Integer>) oos.readObject();
        oos.close();

        final Map<Integer, List<ARXNode>> levels = new HashMap<Integer, List<ARXNode>>();

        // Read the lattice for the first time
        entry = zip.getEntry("lattice.xml"); //$NON-NLS-1$
        if (entry == null) { throw new IOException(Resources.getMessage("WorkerLoad.7")); } //$NON-NLS-1$

        final Map<Integer, ARXNode> map = new HashMap<Integer, ARXNode>();
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        InputSource inputSource = new InputSource(zip.getInputStream(entry));
        xmlReader.setContentHandler(new WorkerLoadXMLHandler() {

            private int       level = 0;
            private int       id    = 0;
            private int[]     transformation;
            private Anonymity anonymity;
            private boolean   checked;

            @Override
            protected boolean end(final String uri,
                                  final String localName,
                                  final String qName) throws SAXException {
            	
                if (localName.equals("lattice")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("level")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("predecessors")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("successors")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("infoloss")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("max")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("min")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("node")) { //$NON-NLS-1$
                    final ARXNode node = lattice.new ARXNode();
                    node.access().setAnonymity(anonymity);
                    node.access().setChecked(checked);
                    node.access().setTransformation(transformation);
                    node.access().setMaximumInformationLoss(max.get(id));
                    node.access().setMinimumInformationLoss(min.get(id));
                    node.access().setAttributes(attrs.get(id));
                    node.access().setHeadermap(headermap);
                    levels.get(level).add(node);
                    map.put(id, node);
                    return true;
                } else if (localName.equals("transformation")) { //$NON-NLS-1$
                    transformation = readTransformation(payload);
                    return true;
                } else if (localName.equals("anonymity")) { //$NON-NLS-1$
                    anonymity = Anonymity.valueOf(payload);
                    return true;
                } else if (localName.equals("checked")) { //$NON-NLS-1$
                    checked = Boolean.valueOf(payload);
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            protected boolean start(final String uri,
                                    final String localName,
                                    final String qName,
                                    final Attributes attributes) throws SAXException {

                if (localName.equals("lattice")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("level")) { //$NON-NLS-1$
                    level = Integer.valueOf(attributes.getValue("depth")); //$NON-NLS-1$
                    if (!levels.containsKey(level)) {
                        levels.put(level, new ArrayList<ARXNode>());
                    }
                    return true;
                } else if (localName.equals("node")) { //$NON-NLS-1$
                    id = Integer.valueOf(attributes.getValue("id")); //$NON-NLS-1$
                    return true;
                } else if (localName.equals("transformation")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("anonymity")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("checked")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("predecessors")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("successors")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("infoloss")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("max")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("min")) { //$NON-NLS-1$
                    return true;
                } else {
                    return false;
                }
            }
        });
        xmlReader.parse(inputSource);

        // Read the lattice for the second time
        entry = zip.getEntry("lattice.xml"); //$NON-NLS-1$
        xmlReader = XMLReaderFactory.createXMLReader();
        inputSource = new InputSource(zip.getInputStream(entry));
        xmlReader.setContentHandler(new WorkerLoadXMLHandler() {
        	
            private int                   id;
            private final List<ARXNode> predecessors = new ArrayList<ARXNode>();
            private final List<ARXNode> successors   = new ArrayList<ARXNode>();

            @Override
            protected boolean end(final String uri,
                                  final String localName,
                                  final String qName) throws SAXException {
                if (localName.equals("lattice")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("level")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("node")) { //$NON-NLS-1$
                    map.get(id)
                       .access()
                       .setPredecessors(predecessors.toArray(new ARXNode[predecessors.size()]));
                    map.get(id)
                       .access()
                       .setSuccessors(successors.toArray(new ARXNode[successors.size()]));
                    return true;
                } else if (localName.equals("transformation")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("anonymity")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("checked")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("infoloss")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("max")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("min")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("predecessors")) { //$NON-NLS-1$

                    final String[] a = payload.trim().split(","); //$NON-NLS-1$
                    for (final String s : a) {
                        final String b = s.trim();
                        if (!b.equals("")) { //$NON-NLS-1$
                            predecessors.add(map.get(Integer.valueOf(b)));
                        }
                    }
                    return true;
                } else if (localName.equals("successors")) { //$NON-NLS-1$
                    final String[] a = payload.trim().split(","); //$NON-NLS-1$
                    for (final String s : a) {
                        final String b = s.trim();
                        if (!b.equals("")) { //$NON-NLS-1$
                            successors.add(map.get(Integer.valueOf(b)));
                        }
                    }
                    return true;
                } else if (localName.equals("attribute")) { //$NON-NLS-1$
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            protected boolean start(final String uri,
                                    final String localName,
                                    final String qName,
                                    final Attributes attributes) throws SAXException {

                if (localName.equals("lattice")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("level")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("node")) { //$NON-NLS-1$
                    id = Integer.valueOf(attributes.getValue("id")); //$NON-NLS-1$
                    successors.clear();
                    predecessors.clear();
                    return true;
                } else if (localName.equals("transformation")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("anonymity")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("checked")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("predecessors")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("successors")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("attribute")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("infoloss")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("max")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("min")) { //$NON-NLS-1$
                    return true;
                } else {
                    return false;
                }
            }
        });
        xmlReader.parse(inputSource);

        // Set lattice
        final ARXNode[][] llevels = new ARXNode[levels.size()][];
        for (final Entry<Integer, List<ARXNode>> e : levels.entrySet()) {
            llevels[e.getKey()] = e.getValue().toArray(new ARXNode[] {});
        }
        lattice.access().setLevels(llevels);
        lattice.access().setBottom(llevels[0][0]);
        lattice.access().setTop(llevels[llevels.length - 1][0]);

        // Return the map
        final Map<String, ARXNode> result = new HashMap<String, ARXNode>();
        for (final List<ARXNode> e : levels.values()) {
            for (final ARXNode node : e) {
                result.put(Arrays.toString(node.getTransformation()), node);
            }
        }

        return result;
    }

    /**
     * Reads the metadata from the file
     * 
     * @param map
     * @param zip
     * @throws IOException
     * @throws SAXException
     */
    private void readMetadata(final ZipFile zip) throws IOException,
                                                SAXException {
        final ZipEntry entry = zip.getEntry("metadata.xml"); //$NON-NLS-1$
        if (entry == null) { throw new IOException(Resources.getMessage("WorkerLoad.9")); } //$NON-NLS-1$

        final XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        final InputSource inputSource = new InputSource(zip.getInputStream(entry));
        xmlReader.setContentHandler(new WorkerLoadXMLHandler() {
            @Override
            protected boolean end(final String uri,
                                  final String localName,
                                  final String qName) throws SAXException {
            	
                if (localName.equals("metadata")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("version")) { //$NON-NLS-1$
                    if (!payload.equals(controller.getResources().getVersion())) { throw new SAXException(Resources.getMessage("WorkerLoad.10") + payload); } //$NON-NLS-1$
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            protected boolean start(final String uri,
                                    final String localName,
                                    final String qName,
                                    final Attributes attributes) throws SAXException {
            	
                if (localName.equals("metadata")) { //$NON-NLS-1$
                    return true;
                } else if (localName.equals("version")) { //$NON-NLS-1$
                    return true;
                } else {
                    return false;
                }
            }
        });
        xmlReader.parse(inputSource);
    }

    /**
     * Reads the project from the file
     * 
     * @param zip
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readModel(final ZipFile zip) throws IOException,
                                             ClassNotFoundException {
    	
        final ZipEntry entry = zip.getEntry("project.dat"); //$NON-NLS-1$
        if (entry == null) { throw new IOException(Resources.getMessage("WorkerLoad.11")); } //$NON-NLS-1$

        // Read model
        final ObjectInputStream oos = new ObjectInputStream(zip.getInputStream(entry));
        model = (Model) oos.readObject();
        oos.close();
    }

    /**
     * Reads a transformation from the serialized array representation
     * 
     * @param payload
     * @return
     */
    private int[] readTransformation(final String payload) {
    	
        final String[] a = payload.split("\\[|,|\\]"); //$NON-NLS-1$
        final int[] r = new int[a.length - 1];
        for (int i = 1; i < a.length; i++) {
            r[i - 1] = Integer.valueOf(a[i].trim());
        }
        return r;
    }

    @Override
    public void run(final IProgressMonitor arg0) throws InvocationTargetException,
                                                        InterruptedException {

        arg0.beginTask(Resources.getMessage("WorkerLoad.2"), 8); //$NON-NLS-1$

        try {
            final ZipFile zip = zipfile;
            readMetadata(zip);
            arg0.worked(1);
            readModel(zip);
            arg0.worked(2);
            final Map<String, ARXNode> map = readLattice(zip);
            arg0.worked(3);
            readClipboard(map, zip);
            arg0.worked(4);
            readFilter(zip);
            arg0.worked(5);
            readConfiguration(map, zip);
            arg0.worked(6);
            zip.close();
            arg0.worked(7);
        } catch (final Exception e) {
            error = e;
            arg0.done();
            return;
        }
        result = model;
        arg0.worked(8);
        arg0.done();
    }
}
