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

package org.deidentifier.flash.gui.view.impl.analyze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.csstudio.swt.widgets.datadefinition.ColorMap;
import org.csstudio.swt.widgets.datadefinition.ColorMap.PredefinedColorMap;
import org.csstudio.swt.widgets.figures.IntensityGraphFigure;
import org.csstudio.swt.xygraph.linearscale.Range;
import org.deidentifier.flash.AttributeType;
import org.deidentifier.flash.AttributeType.Hierarchy;
import org.deidentifier.flash.DataHandle;
import org.deidentifier.flash.DataType;
import org.deidentifier.flash.gui.Configuration;
import org.deidentifier.flash.gui.Controller;
import org.deidentifier.flash.gui.Model;
import org.deidentifier.flash.gui.view.def.IMainWindow;
import org.deidentifier.flash.gui.view.def.IView;
import org.deidentifier.flash.gui.view.def.IView.ModelEvent.EventTarget;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class DensityView implements IView {

    private static final int           MAX_DIMENSION = 500;

    private Canvas                     canvas        = null;
    private final LightweightSystem    lws;
    private final IntensityGraphFigure intensityGraph;
    private final EventTarget          target;
    private final EventTarget          reset;
    private final Controller           controller;
    private Model                      model;

    public DensityView(final Composite parent,
                       final Controller controller,
                       final EventTarget target,
                       final EventTarget reset) {

        // Register
        controller.addListener(EventTarget.SELECTED_ATTRIBUTE, this);
        controller.addListener(EventTarget.MODEL, this);
        controller.addListener(target, this);
        this.controller = controller;
        if (reset != null) {
            controller.addListener(reset, this);
        }
        this.reset = reset;
        this.target = target;

        // Build
        canvas = new Canvas(parent, SWT.NONE);
        lws = new LightweightSystem(canvas);
        intensityGraph = new IntensityGraphFigure();
        lws.setContents(intensityGraph);
        intensityGraph.getXAxis().setTitleFont(IMainWindow.FONT);
        intensityGraph.getYAxis().setTitleFont(IMainWindow.FONT);
        intensityGraph.getXAxis().setFont(IMainWindow.FONT);
        intensityGraph.getYAxis().setFont(IMainWindow.FONT);
        intensityGraph.setFont(IMainWindow.FONT);

        canvas.setBackground(parent.getBackground());
        
        // TODO: OSX workaround
        if (System.getProperty("os.name").toLowerCase().contains("mac")){
        	int r = canvas.getBackground().getRed()-13;
        	int g = canvas.getBackground().getGreen()-13;
        	int b = canvas.getBackground().getBlue()-13;
        	r = r>0 ? r : 0;
        	r = g>0 ? g : 0;
        	r = b>0 ? b : 0;
        	org.eclipse.swt.graphics.Color c2 = new org.eclipse.swt.graphics.Color(controller.getResources().getDisplay(), r, g, b);
        	canvas.setBackground(c2);
        }
        
        // Reset
        reset();
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    private DataHandle getData() {
        // Obtain the right config
        Configuration config = model.getOutputConfig();
        if (config == null) {
            config = model.getInputConfig();
        }

        // Obtain the right handle
        final DataHandle data;
        if (target == EventTarget.INPUT) {
            data = config.getInput().getHandle();
        } else {
            data = model.getOutput();
        }

        // Clear if nothing to draw
        if ((config == null) || (data == null)) {
            return null;
        } else {
            return data;
        }
    }

    /**
     * Returns the labels sorted per hierarchy or per data type
     * 
     * @param attribute
     * @return
     */
    private String[] getLabels(final String attribute) {

        // Obtain the right config
        Configuration config = model.getOutputConfig();
        if (config == null) {
            config = model.getInputConfig();
        }

        // Obtain the right handle
        final DataHandle data;
        if (target == EventTarget.INPUT) {
            data = config.getInput().getHandle();
        } else {
            data = model.getOutput();
        }

        // Check if there is a hierarchy
        final AttributeType type = config.getInput()
                                         .getDefinition()
                                         .getAttributeType(attribute);

        Hierarchy hierarchy = null;
        if (type instanceof Hierarchy) {
            hierarchy = (Hierarchy) type;
        } else if (type == AttributeType.SENSITIVE_ATTRIBUTE) {
            hierarchy = config.getSensitiveHierarchy();
        }

        // Count
        boolean suppressed = false;
        final int index = data.getColumnIndexOf(attribute);
        final Set<String> elems = new HashSet<String>();
        for (int i = 0; i < data.getNumRows(); i++) {
            if (!suppressed) {
                suppressed |= data.isOutlier(i);
            }
            elems.add(data.getValue(i, index));
        }

        // Init distribution
        final String[] dvals;

        // Sort by hierarchy if possible
        if (hierarchy != null) {

            final int level = data.getGeneralization(attribute);
            final List<String> list = new ArrayList<String>();
            final Set<String> done = new HashSet<String>();
            final String[][] h = hierarchy.getHierarchy();
            for (int i = 0; i < h.length; i++) {
                final String val = h[i][level];
                if (elems.contains(val) && !done.contains(val)) {
                    list.add(val);
                    done.add(val);
                }
            }
            if (suppressed) {
                if (!done.contains(model.getAnonymizer().getSuppressionString())) {
                    list.add(model.getAnonymizer().getSuppressionString());
                }
            }

            dvals = list.toArray(new String[] {});

            // Else sort per data type
        } else {
            final DataType dtype = data.getDataType(attribute);
            final String[] v = new String[elems.size()];
            int i = 0;
            for (final String s : elems) {
                v[i++] = s;
            }
            Arrays.sort(v, new Comparator<String>() {
                @Override
                public int compare(final String arg0, final String arg1) {
                    try {
                        return dtype.compare(arg0, arg1);
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            dvals = v;
        }

        return dvals;
    }

    private void redraw() {

        if (model == null) { return; }

        if ((model.getAttributePair()[0] != null) &&
            (model.getAttributePair()[1] != null)) {

            final long time = System.currentTimeMillis();

            DataHandle data = getData();
            if (data == null) {
                reset();
                return;
            }

            // Draw
            canvas.setRedraw(true);

            final int index1 = data.getColumnIndexOf(model.getAttributePair()[0]);
            final int index2 = data.getColumnIndexOf(model.getAttributePair()[1]);

            if (index1 < 0 || index2 < 0) return;

            final String[] vals1 = getLabels(model.getAttributePair()[0]);
            final String[] vals2 = getLabels(model.getAttributePair()[1]);

            final Map<String, Integer> map1 = new HashMap<String, Integer>();
            final Map<String, Integer> map2 = new HashMap<String, Integer>();

            int step1 = vals1.length / MAX_DIMENSION; // Round down
            int step2 = vals2.length / MAX_DIMENSION; // Round down
            step1 = Math.max(step1, 1);
            step2 = Math.max(step2, 1);

            int index = 0;
            for (int i = 0; i < vals1.length; i += step1) {
                for (int j = 0; j < step1; j++) {
                    if ((i + j) < vals1.length) {
                        map1.put(vals1[i + j], index);
                    }
                }
                index++;
            }
            final int size1 = index;

            index = 0;
            for (int i = 0; i < vals2.length; i += step2) {
                for (int j = 0; j < step2; j++) {
                    if ((i + j) < vals2.length) {
                        map2.put(vals2[i + j], index);
                    }
                }
                index++;
            }
            final int size2 = index;

            final short[] heat = new short[size1 * size2];

            int max = 0;
            int ignored = 0;
            for (int row = 0; row < data.getNumRows(); row++) {
                final String v1 = data.getValue(row, index1);
                final String v2 = data.getValue(row, index2);
                final Integer i1 = map1.get(v1);
                final Integer i2 = map2.get(v2);
                if ((i1 == null) || (i2 == null)) {
                    ignored++;
                } else {
                    index = (i2 * size1) + i1;
                    heat[index]++;
                    max = (heat[index] > max ? heat[index] : max);
                }
            }

            map1.clear();
            map2.clear();
            if (ignored != 0) {
                controller.getResources()
                          .getLogger()
                          .warn("Ignored " + ignored + " tuples"); //$NON-NLS-1$ //$NON-NLS-2$
            }

            controller.getResources()
                      .getLogger()
                      .info("Density computed in " + (System.currentTimeMillis() - time)); //$NON-NLS-1$

            final int fMax = max;
            // controller.getResources().getDisplay().asyncExec(new Runnable() {
            // @Override
            // public void run() {

            // Dont run this asynchronously, because it seems to cause problems
            // on MS Windows
            // Configure
            intensityGraph.setMax(fMax);
            intensityGraph.setMin(0);
            intensityGraph.setDataHeight(size2);
            intensityGraph.setDataWidth(size1);
            intensityGraph.setColorMap(new ColorMap(PredefinedColorMap.JET,
                                                    true,
                                                    true));
            intensityGraph.getXAxis().setTitle(model.getAttributePair()[0]);
            intensityGraph.getYAxis().setTitle(model.getAttributePair()[1]);
            intensityGraph.getXAxis().setRange(new Range(0, size1 - 1));
            intensityGraph.getYAxis().setRange(new Range(0, size2 - 1));

            intensityGraph.setDataArray(heat);
            canvas.setRedraw(true);
            canvas.redraw();
            // }
            // });
        }
    }

    @Override
    public void reset() {

        intensityGraph.setDataArray(new short[0]);
        if (model != null) model.resetAttributePair();
        canvas.setEnabled(false);
    }

    @Override
    public void update(final ModelEvent event) {

        if (event.target == EventTarget.OUTPUT) {
            canvas.setEnabled(true);
            redraw();
        }

        // Handle reset target, i.e., e.g. input has changed
        if (event.target == reset) {
            reset();
            // Handle new project
        } else if (event.target == EventTarget.MODEL) {
            reset();
            model = (Model) event.data;
            // Handle new data
        } else if (event.target == target) {
            canvas.setEnabled(true);
            redraw();
            // Handle selected attribute
        } else if (event.target == EventTarget.SELECTED_ATTRIBUTE) {
            if (model.getAttributePair()[0] != null &&
                model.getAttributePair()[1] != null) {
                canvas.setEnabled(true);
                redraw();
            }
        }
    }
}
