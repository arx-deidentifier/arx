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

package org.deidentifier.flash.gui.worker;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.StringEscapeUtils;
import org.deidentifier.flash.AttributeType;
import org.deidentifier.flash.AttributeType.Hierarchy;
import org.deidentifier.flash.DataDefinition;
import org.deidentifier.flash.DataHandle;
import org.deidentifier.flash.DataType;
import org.deidentifier.flash.FLASHConfiguration.Criterion;
import org.deidentifier.flash.FLASHConfiguration.LDiversityCriterion;
import org.deidentifier.flash.FLASHConfiguration.TClosenessCriterion;
import org.deidentifier.flash.FLASHLattice;
import org.deidentifier.flash.FLASHLattice.FLASHNode;
import org.deidentifier.flash.gui.Configuration;
import org.deidentifier.flash.gui.Controller;
import org.deidentifier.flash.gui.Model;
import org.deidentifier.flash.gui.resources.Resources;
import org.deidentifier.flash.io.CSVDataOutput;
import org.deidentifier.flash.metric.InformationLoss;
import org.eclipse.core.runtime.IProgressMonitor;

public class WorkerSave extends Worker<Model> {

    /**
     * Wraps a writer
     * 
     * @author Prasser, Kohlmayer
     */
    private class FileBuffer {
        private final OutputStreamWriter w;

        public FileBuffer(final OutputStreamWriter w) {
            this.w = w;
        }

        public FileBuffer append(final boolean val) throws IOException {
            return append(String.valueOf(val));
        }

        public FileBuffer append(final double val) throws IOException {
            return append(String.valueOf(val));
        }

        public FileBuffer append(final int val) throws IOException {
            return append(String.valueOf(val));
        }

        public FileBuffer append(final Object val) throws IOException {
            return append(String.valueOf(val));
        }

        public FileBuffer append(final String s) throws IOException {
            w.write(s);
            return this;
        }

        public void flush() throws IOException {
            w.flush();
        }
    }

    private final String     path;
    private final Model      model;
    private final Controller controller;

    public WorkerSave(final String path,
                      final Controller controller,
                      final Model model) {
        this.path = path;
        this.model = model;
        this.controller = controller;
    }

    @Override
    public void
            run(final IProgressMonitor arg0) throws InvocationTargetException,
                                            InterruptedException {

        arg0.beginTask(Resources.getMessage("WorkerSave.0"), 8); //$NON-NLS-1$

        try {
            final FileOutputStream f = new FileOutputStream(path);
            final ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(f));
            writeMetadata(model, zip);
            arg0.worked(1);
            writeModel(model, zip);
            arg0.worked(2);
            writeInput(model, zip);
            arg0.worked(3);
            writeOutput(model, zip);
            arg0.worked(4);
            writeConfiguration(model, zip);
            arg0.worked(5);
            final Map<String, Integer> map = writeLattice(model, zip);
            arg0.worked(6);
            writeClipboard(model, map, zip);
            arg0.worked(7);
            writeFilter(model, zip);
            zip.close();
            arg0.worked(8);
        } catch (final Exception e) {
            error = e;
            arg0.done();
            return;
        }

        arg0.worked(100);
        arg0.done();
    }

    /**
     * Converts an attribute name to a file name
     * 
     * @param a
     * @return
     */
    private String toFileName(final String a) {
        return a;
    }

    /**
     * Escape XML
     * 
     * @param a
     * @return
     */
    private String toXML(final boolean a) {
        return StringEscapeUtils.escapeXml(String.valueOf(a));
    }

    /**
     * Escape XML
     * 
     * @param a
     * @return
     */
    private String toXML(final char a) {
        return StringEscapeUtils.escapeXml(String.valueOf(a));
    }

    /**
     * Converts a configuration to XML
     * 
     * @param model
     * @return
     */
    private String toXML(final Configuration config) {
        final StringBuffer b = new StringBuffer();
        b.append("<config>\n"); //$NON-NLS-1$
        b.append("\t").append("<removeOutliers>").append(toXML(config.isRemoveOutliers())).append("</removeOutliers>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        b.append("\t").append("<k>").append(toXML(config.getK())).append("</k>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        b.append("\t").append("<relativeMaxOutliers>").append(toXML(config.getRelativeMaxOutliers())).append("</relativeMaxOutliers>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        b.append("\t").append("<criterion>").append(toXML(config.getCriterion())).append("</criterion>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        b.append("\t").append("<metric>").append(toXML(config.getMetric().getClass().getSimpleName())).append("</metric>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        b.append("\t").append("<tClosenessCriterion>").append(toXML(config.getTClosenessCriterion())).append("</tClosenessCriterion>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        b.append("\t").append("<lDiversityCriterion>").append(toXML(config.getLDiversityCriterion())).append("</lDiversityCriterion>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        b.append("\t").append("<l>").append(toXML(config.getL())).append("</l>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        b.append("\t").append("<c>").append(toXML(config.getC())).append("</c>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        b.append("\t").append("<practicalMonotonicity>").append(toXML(config.getPracticalMonotonicity())).append("</practicalMonotonicity>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        b.append("\t").append("<t>").append(toXML(config.getT())).append("</t>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        b.append("</config>\n"); //$NON-NLS-1$
        return b.toString();
    }

    /**
     * Escape XML
     * 
     * @param a
     * @return
     */
    private String toXML(final Criterion a) {
        return StringEscapeUtils.escapeXml(String.valueOf(a));
    }

    /**
     * Returns an XML representation of the data definition
     * 
     * @param handle
     * @param definition
     * @return
     */
    private String toXML(final DataHandle handle,
                         final DataDefinition definition) {
        final StringBuffer b = new StringBuffer();
        b.append("<definition>\n"); //$NON-NLS-1$
        for (int i = 0; i < handle.getNumColumns(); i++) {
            final String attr = handle.getAttributeName(i);
            AttributeType t = definition.getAttributeType(attr);
            DataType dt = definition.getDataType(attr);
            if (t == null) {
                t = AttributeType.IDENTIFYING_ATTRIBUTE;
            }
            if (dt == null) {
                dt = DataType.STRING;
            }
            b.append("\t").append("<assigment>\n"); //$NON-NLS-1$ //$NON-NLS-2$
            b.append("\t\t").append("<name>").append(toXML(attr)).append("</name>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            b.append("\t\t").append("<type>").append(toXML(t.toString())).append("</type>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            b.append("\t\t").append("<datatype>").append(toXML(dt.toString())).append("</datatype>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if (t instanceof Hierarchy) {
                b.append("\t\t").append("<ref>").append("hierarchies/" + toFileName(attr) + ".csv").append("</ref>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                b.append("\t\t").append("<min>").append(toXML(definition.getMinimumGeneralization(attr))).append("</min>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                b.append("\t\t").append("<max>").append(toXML(definition.getMaximumGeneralization(attr))).append("</max>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            b.append("\t").append("</assigment>\n"); //$NON-NLS-1$ //$NON-NLS-2$

        }
        b.append("</definition>\n"); //$NON-NLS-1$
        return b.toString();
    }

    /**
     * Escape XML
     * 
     * @param a
     * @return
     */
    private String toXML(final double a) {
        return StringEscapeUtils.escapeXml(String.valueOf(a));
    }

    private String toXML(final LDiversityCriterion a) {
        if (a == null) {
            return ""; //$NON-NLS-1$
        } else {
            return toXML(a.toString());
        }
    }

    /**
     * Escape XML
     * 
     * @param a
     * @return
     */
    private String toXML(final long a) {
        return StringEscapeUtils.escapeXml(String.valueOf(a));
    }

    /**
     * Returns an XML representation of the lattice
     * 
     * @param map
     * @param l
     * @param zip
     * @return
     * @throws IOException
     */
    private void toXML(final Map<String, Integer> map,
                       final FLASHLattice l,
                       final ZipOutputStream zip) throws IOException {

        // Build mapping
        int id = 0;
        for (final FLASHNode[] level : l.getLevels()) {
            for (final FLASHNode n : level) {
                final String key = Arrays.toString(n.getTransformation());
                if (!map.containsKey(key)) {
                    map.put(key, id++);
                }
            }
        }

        // Write directly because of size
        final FileBuffer b = new FileBuffer(new OutputStreamWriter(zip));

        // Build xml
        b.append("<lattice>\n"); //$NON-NLS-1$
        for (int i = 0; i < l.getLevels().length; i++) {
            b.append("\t").append("<level depth=\"").append(i).append("\">\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            for (final FLASHNode n : l.getLevels()[i]) {
                final String key = Arrays.toString(n.getTransformation());
                final int currentId = map.get(key);
                b.append("\t\t").append("<node id=\"").append(currentId).append("\">\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                b.append("\t\t\t").append("<transformation>").append(Arrays.toString(n.getTransformation())).append("</transformation>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                b.append("\t\t\t").append("<anonymity>").append(n.isAnonymous()).append("</anonymity>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                b.append("\t\t\t").append("<checked>").append(n.isChecked()).append("</checked>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                if (n.getPredecessors().length > 0) {
                    b.append("\t\t\t").append("<predecessors>"); //$NON-NLS-1$ //$NON-NLS-2$
                    for (int j = 0; j < n.getPredecessors().length; j++) {
                        b.append(map.get(Arrays.toString(n.getPredecessors()[j].getTransformation())));
                        if (j < (n.getPredecessors().length - 1)) {
                            b.append(","); //$NON-NLS-1$
                        }
                    }
                    b.append("</predecessors>\n"); //$NON-NLS-1$
                }
                if (n.getSuccessors().length > 0) {
                    b.append("\t\t\t").append("<successors>"); //$NON-NLS-1$ //$NON-NLS-2$
                    for (int j = 0; j < n.getSuccessors().length; j++) {
                        b.append(map.get(Arrays.toString(n.getSuccessors()[j].getTransformation())));
                        if (j < (n.getSuccessors().length - 1)) {
                            b.append(","); //$NON-NLS-1$
                        }
                    }
                    b.append("</successors>\n"); //$NON-NLS-1$
                }
                b.append("\t\t\t").append("<infoloss>\n"); //$NON-NLS-1$ //$NON-NLS-2$
                b.append("\t\t\t\t").append("<max>").append(n.getMaximumInformationLoss().getValue()).append("</max>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                b.append("\t\t\t\t").append("<min>").append(n.getMinimumInformationLoss().getValue()).append("</min>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                b.append("\t\t\t").append("</infoloss>\n"); //$NON-NLS-1$ //$NON-NLS-2$
                b.append("\t\t").append("</node>\n"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            b.append("\t").append("</level>\n"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        b.append("</lattice>\n"); //$NON-NLS-1$
        b.flush();
    }

    /**
     * Returns an XML representation of the clipboard
     * 
     * @param map
     * @param clipboard
     * @return
     */
    private String toXML(final Map<String, Integer> map,
                         final Set<FLASHNode> clipboard) {

        // Build xml
        final StringBuffer b = new StringBuffer();
        b.append("<clipboard>\n"); //$NON-NLS-1$
        for (final FLASHNode n : clipboard) {
            b.append("\t").append("<node>").append(Arrays.toString(n.getTransformation())).append("</node>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        b.append("</clipboard>\n"); //$NON-NLS-1$
        return b.toString();
    }

    /**
     * Converts a model to XML
     * 
     * @param model
     * @return
     */
    private String toXML(final Model model) {
        final StringBuffer b = new StringBuffer();
        b.append("<project>\n"); //$NON-NLS-1$
        b.append("\t").append("<name>").append(toXML(model.getName())).append("</name>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        b.append("\t").append("<separator>").append(toXML(model.getSeparator())).append("</separator>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        b.append("\t").append("<description>").append(toXML(model.getDescription())).append("</description>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        b.append("\t").append("<suppressionString>").append(toXML(model.getSuppressionString())).append("</suppressionString>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        b.append("\t").append("<historySize>").append(toXML(model.getHistorySize())).append("</historySize>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        b.append("\t").append("<snapshotSizeDataset>").append(toXML(model.getSnapshotSizeDataset())).append("</snapshotSizeDataset>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        b.append("\t").append("<snapshotSizeSnapshot>").append(toXML(model.getSnapshotSizeSnapshot())).append("</snapshotSizeSnapshot>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        b.append("\t").append("<initialNodesInViewer>").append(toXML(model.getInitialNodesInViewer())).append("</initialNodesInViewer>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        b.append("\t").append("<maxNodesInLattice>").append(toXML(model.getMaxNodesInLattice())).append("</maxNodesInLattice>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        b.append("\t").append("<maxNodesInViewer>").append(toXML(model.getMaxNodesInViewer())).append("</maxNodesInViewer>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        b.append("\t").append("<selectedAttribute>").append(toXML(model.getSelectedAttribute())).append("</selectedAttribute>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        b.append("\t").append("<inputBytes>").append(toXML(model.getInputBytes())).append("</inputBytes>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        b.append("</project>\n"); //$NON-NLS-1$
        return b.toString();
    }

    /**
     * Escape XML
     * 
     * @param a
     * @return
     */
    private String toXML(final String a) {
        return StringEscapeUtils.escapeXml(a);
    }

    /**
     * Escape XML
     * 
     * @param a
     * @return
     */
    private String toXML(final TClosenessCriterion a) {
        if (a == null) {
            return ""; //$NON-NLS-1$
        } else {
            return toXML(a.toString());
        }
    }

    /**
     * Writes the clipboard to the file
     * 
     * @param map
     * @param zip
     * @throws IOException
     */
    private void writeClipboard(final Model model,
                                final Map<String, Integer> map,
                                final ZipOutputStream zip) throws IOException {
        if ((model.getClipboard() == null) || model.getClipboard().isEmpty()) { return; }

        // Write clipboard
        zip.putNextEntry(new ZipEntry("clipboard.xml")); //$NON-NLS-1$
        final Writer w = new OutputStreamWriter(zip);
        w.write(toXML(map, model.getClipboard()));
        w.flush();

    }

    /**
     * Writes the configuration to the file
     * 
     * @param zip
     * @throws IOException
     */
    private void
            writeConfiguration(final Configuration config,
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
     * Writes the configuration to the file
     * 
     * @param zip
     * @throws IOException
     */
    private void
            writeConfiguration(final Model model, final ZipOutputStream zip) throws IOException {

        if (model.getInputConfig() != null) {
            writeConfiguration(model.getInputConfig(), "input/", zip); //$NON-NLS-1$
        }
        if (model.getOutputConfig() != null) {
            writeConfiguration(model.getOutputConfig(), "output/", zip); //$NON-NLS-1$
        }
    }

    /**
     * Writes the data definition to the file
     * 
     * @param zip
     * @throws IOException
     */
    private void writeDefinition(final Configuration config,
                                 final String prefix,
                                 final ZipOutputStream zip) throws IOException {
        if (config.getInput() != null) {
            if (config.getInput().getDefinition() != null) {
                zip.putNextEntry(new ZipEntry(prefix + "definition.xml")); //$NON-NLS-1$
                final Writer w = new OutputStreamWriter(zip);
                w.write(toXML(config.getInput().getHandle(),
                              config.getInput().getDefinition()));
                w.flush();
            }
        }
    }

    /**
     * Writes the current filter to the file
     * 
     * @param zip
     * @throws IOException
     */
    private void
            writeFilter(final Model model, final ZipOutputStream zip) throws IOException {
        if ((model.getAnonymizer() == null) || (model.getResult() == null)) { return; }
        zip.putNextEntry(new ZipEntry("filter.dat")); //$NON-NLS-1$
        final ObjectOutputStream oos = new ObjectOutputStream(zip);
        oos.writeObject(model.getNodeFilter());
        oos.flush();
    }

    /**
     * Writes the hierarchies to the file
     * 
     * @param zip
     * @throws IOException
     */
    private void writeHierarchies(final Configuration config,
                                  final String prefix,
                                  final ZipOutputStream zip) throws IOException {
        if (config.getInput() != null) {
            if (config.getInput().getDefinition() != null) {
                for (final String a : config.getInput()
                                            .getDefinition()
                                            .getQuasiIdentifyingAttributes()) {
                    final String[][] h = config.getInput()
                                               .getDefinition()
                                               .getHierarchy(a);
                    if (h != null) {
                        zip.putNextEntry(new ZipEntry(prefix +
                                                      "hierarchies/" + toFileName(a) + ".csv")); //$NON-NLS-1$ //$NON-NLS-2$
                        final CSVDataOutput out = new CSVDataOutput(zip,
                                                                    model.getSeparator());
                        out.write(h);
                    }
                }
            }
        }

        if (config.getSensitiveHierarchy() != null) {
            zip.putNextEntry(new ZipEntry(prefix + "hierarchies/sensitive.csv")); //$NON-NLS-1$
            final CSVDataOutput out = new CSVDataOutput(zip,
                                                        model.getSeparator());
            out.write(config.getSensitiveHierarchy().getHierarchy());
        }
    }

    /**
     * Writes the input to the file
     * 
     * @param zip
     * @throws IOException
     */
    private void
            writeInput(final Model model, final ZipOutputStream zip) throws IOException {
        if (model.getInputConfig().getInput() != null) {
            if (model.getInputConfig().getInput().getHandle() != null) {
                zip.putNextEntry(new ZipEntry("data/input.csv")); //$NON-NLS-1$
                final CSVDataOutput out = new CSVDataOutput(zip,
                                                            model.getSeparator());
                out.write(model.getInputConfig()
                               .getInput()
                               .getHandle()
                               .iterator());
            }
        }
    }

    /**
     * Writes the lattice to the file
     * 
     * @param zip
     * @return
     * @throws IOException
     */
    private Map<String, Integer>
            writeLattice(final Model model, final ZipOutputStream zip) throws IOException {

        // Mapping
        final Map<String, Integer> map = new HashMap<String, Integer>();
        if ((model.getResult() == null) ||
            (model.getResult().getLattice() == null)) { return map; }

        // Write lattice
        final FLASHLattice l = model.getResult().getLattice();
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

        // Write infoloss
        zip.putNextEntry(new ZipEntry("infoloss.dat")); //$NON-NLS-1$
        final Map<Integer, InformationLoss> max = new HashMap<Integer, InformationLoss>();
        final Map<Integer, InformationLoss> min = new HashMap<Integer, InformationLoss>();
        for (final FLASHNode[] level : l.getLevels()) {
            for (final FLASHNode n : level) {
                final String key = Arrays.toString(n.getTransformation());
                min.put(map.get(key), n.getMinimumInformationLoss());
                max.put(map.get(key), n.getMaximumInformationLoss());
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
        for (final FLASHNode[] level : l.getLevels()) {
            for (final FLASHNode n : level) {
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
     * Writes the metadata to the file
     * 
     * @param map
     * @param zip
     * @throws IOException
     */
    private void
            writeMetadata(final Model model, final ZipOutputStream zip) throws IOException {

        // Write metadata
        zip.putNextEntry(new ZipEntry("metadata.xml")); //$NON-NLS-1$
        final Writer w = new OutputStreamWriter(zip);
        w.write("<metadata>\n"); //$NON-NLS-1$
        w.write("\t<version>" + toXML(controller.getResources().getVersion()) + "</version>\n"); //$NON-NLS-1$ //$NON-NLS-2$
        w.write("</metadata>\n"); //$NON-NLS-1$
        w.flush();

    }

    /**
     * Writes the project to the file
     * 
     * @param zip
     * @throws IOException
     */
    private void
            writeModel(final Model model, final ZipOutputStream zip) throws IOException {
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
     * Writes the output to the file
     * 
     * @param zip
     * @throws IOException
     */
    private void
            writeOutput(final Model model, final ZipOutputStream zip) throws IOException {
        if (model.getOutput() != null) {
            zip.putNextEntry(new ZipEntry("data/output.csv")); //$NON-NLS-1$
            final CSVDataOutput out = new CSVDataOutput(zip,
                                                        model.getSeparator());
            out.write(model.getOutput().iterator());
        }
    }
}
