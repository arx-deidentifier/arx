/*
 * ARX: Powerful Data Anonymization
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

package org.deidentifier.arx.gui.view.impl.explore;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deidentifier.arx.ARXLattice;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.model.ModelNodeFilter;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.metric.InformationLoss;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * This class implements a view of a lattice
 * 
 * @author Fabian Prasser
 */
public class ViewLattice implements IView {

    /**
     * Bounds
     * @author Fabian Prasser
     */
    private static class Bounds implements Serializable {
        private static final long serialVersionUID = -7472570696920782588L;
        public double             centerX;
        public double             centerY;
        public double             x;
        public double             y;
    }

    /** The current drag type */
    private static enum DragType {
        MOVE,
        ZOOM,
        NONE
    }

    /** Color*/
    public static final Color   COLOR_GREEN             = GUIHelper.getColor(50, 205, 50);
    /** Color*/
    public static final Color   COLOR_LIGHT_GREEN       = GUIHelper.getColor(50, 128, 50);
    /** Color*/
    public static final Color   COLOR_ORANGE            = GUIHelper.getColor(255, 145, 0);
    /** Color*/
    public static final Color   COLOR_RED               = GUIHelper.getColor(255, 99, 71);
    /** Color*/
    public static final Color   COLOR_LIGHT_RED         = GUIHelper.getColor(128, 99, 71);
    /** Color*/
    public static final Color   COLOR_BLUE              = GUIHelper.getColor(0, 0, 255);
    /** Color*/
    public static final Color   COLOR_YELLOW            = GUIHelper.getColor(255, 215, 0);
    /** Color*/
    public static final Color   COLOR_WHITE             = GUIHelper.getColor(255, 255, 255);
    /** Color*/
    public static final Color   COLOR_BLACK             = GUIHelper.getColor(0, 0, 0);
    /** Color*/
    public static final Color   COLOR_LIGHT_GRAY        = GUIHelper.getColor(211, 211, 211);

    /** Attribute constant */
    private static final int    ATTRIBUTE_POSITION      = 1;
    /** Attribute constant */
    private static final int    ATTRIBUTE_LEVEL         = 2;
    /** Attribute constant */
    private static final int    ATTRIBUTE_LEVELSIZE     = 3;
    /** Attribute constant */
    private static final int    ATTRIBUTE_BOUNDS        = 4;
    /** Attribute constant */
    private static final int    ATTRIBUTE_LABEL         = 5;
    /** Attribute constant */
    private static final int    ATTRIBUTE_VISIBLE       = 6;
    /** Attribute constant */
    private static final int    ATTRIBUTE_PATH          = 7;
    /** Attribute constant */
    private static final int    ATTRIBUTE_EXTENT        = 8;
    
    
    /** Time to wait for a tool tip to show */
    private static final int    TOOLTIP_WAIT            = 200;
    /** Global settings */
    private static final double NODE_INITIAL_SIZE       = 200d;
    /** Global settings */
    private static final double NODE_FRAME_RATIO        = 0.7d;
    /** Global settings */
    private static final double NODE_SIZE_RATIO         = 0.3d;
    /** Global settings */
    private static final double ZOOM_SPEED              = 10d;
    /** Global settings */
    private static final int    MSG_WIDTH               = 300;
    /** Global settings */
    private static final int    MSG_HEIGHT              = 100;
    /** Global settings */
    private static final int    MIN_WIDTH               = 2;
    /** Global settings */
    private static final int    MIN_HEIGHT              = 1;
    /** For the current view */
    private static final int    STROKE_WIDTH_NODE       = 1;
    /** For the current view */
    private static final int    STROKE_WIDTH_CONNECTION = 1;

    /** The model */
    private Model               model;
    /** The font */
    private final Font          font;

    /** For the current view */
    private double              nodeWidth               = 0f;
    /** For the current view */
    private double              nodeHeight              = 0f;
    /** The lattice to display */
    private final List<ARXNode> lattice                 = new ArrayList<ARXNode>();
    /** The lattice to display */
    private int                 latticeWidth            = 0;
    /** The lattice to display */
    private int                 latticeHeight           = 0;
    /** The screen size */
    private Point               screen                  = null;

    /** The number of nodes */
    private int                 numNodes                = 0;
    /** Drag parameters */
    private int                 dragX                   = 0;
    /** Drag parameters */
    private int                 dragY                   = 0;
    /** Drag parameters */
    private int                 dragStartX              = 0;
    /** Drag parameters */
    private int                 dragStartY              = 0;
    /** Drag parameters */
    private DragType            dragType                = DragType.NONE;

    /** The optimum */
    private ARXNode             optimum;

    /** The selected node */
    private ARXNode             selectedNode;

    /** The controller */
    private final Controller    controller;

    /** The canvas */
    private final Canvas        canvas;

    /** The tool tip */
    private int                 tooltipX                = -1;
    /** The tool tip */
    private int                 tooltipY                = -1;
    /** The tool tip */
    private int                 oldTooltipX             = -1;
    /** The tool tip */
    private int                 oldTooltipY             = -1;
    /** Context menu */
    private Menu                menu                    = null;

    /** Number format */
    private final NumberFormat  format                  = new DecimalFormat("##0.000");     //$NON-NLS-1$

    /**
     * Creates a new instance
     * 
     * @param parent
     * @param controller
     */
    public ViewLattice(final Composite parent, final Controller controller) {

        // Listen
        controller.addListener(ModelPart.SELECTED_NODE, this);
        controller.addListener(ModelPart.FILTER, this);
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.RESULT, this);

        this.controller = controller;
        
        // Compute font
        FontData[] fd = parent.getFont().getFontData();
        fd[0].setHeight(8);
        this.font = new Font(parent.getDisplay(), fd[0]);

        // Build canvas
        parent.setLayout(new FillLayout());
        this.canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);
        this.canvas.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                screen = canvas.getSize();
                e.gc.setAdvanced(true);
                e.gc.setAntialias(SWT.ON);
                draw(e.gc);
            }
        });
        
        // Initialize
        this.initializeToolTipTimer();
        this.initializeMenu();
        this.initializeListeners();
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
        font.dispose();
    }

    /**
     * Resets the view
     */
    @Override
    public void reset() {
        this.numNodes = 0;
        this.optimum = null;
        this.selectedNode = null;
        for (ARXNode node : lattice) {
            Path path = (Path)node.getAttributes().get(ATTRIBUTE_PATH);
            if (path!=null) {
                node.getAttributes().put(ATTRIBUTE_PATH, null);
                path.dispose();
            }
        }
        this.lattice.clear();
        this.latticeWidth = 0;
        this.latticeHeight = 0;
        this.screen = null;
        this.canvas.redraw();
    }

    @Override
    public void update(final ModelEvent event) {

        if (event.part == ModelPart.SELECTED_NODE) {
            selectedNode = (ARXNode) event.data;
            canvas.redraw();
        } else if (event.part == ModelPart.RESULT) {
            if (model.getResult() == null) reset();
        } else if (event.part == ModelPart.MODEL) {
            model = (Model) event.data;
        } else if (event.part == ModelPart.FILTER) {
            if (model != null) {
                initialize(model.getResult(), (ModelNodeFilter) event.data);
                canvas.redraw();
            }
        }
    }

    /**
     * Called when button 1 is clicked on a node
     * @param node
     */
    private void actionButtonClicked1(ARXNode node) {
        selectedNode = node;
        model.setSelectedNode(selectedNode);
        controller.update(new ModelEvent(ViewLattice.this, ModelPart.SELECTED_NODE, selectedNode));
        canvas.redraw();
    }

    /**
     * Called when button 3 is clicked on a node
     * @param node
     * @param x
     * @param y
     */
    private void actionButtonClicked3(ARXNode node, final int x, final int y) {
        selectedNode = node;
        model.setSelectedNode(selectedNode);
        controller.update(new ModelEvent(ViewLattice.this, ModelPart.SELECTED_NODE, selectedNode));
        canvas.redraw();
        menu.setLocation(x, y);
        menu.setVisible(true);
        dragType = DragType.NONE;
    }

    /**
     * Converts an information loss into a relative value in percent
     * 
     * @param infoLoss
     * @return
     */
    private double asRelativeValue(final InformationLoss<?> infoLoss) {
        return infoLoss.relativeTo(model.getResult().getLattice().getMinimumInformationLoss(), 
                                   model.getResult().getLattice().getMaximumInformationLoss()) * 100d;
    }

    /**
     * Converts a generalization to a relative value
     * 
     * @param generalization
     * @param max
     * @return
     */
    private double asRelativeValue(final int generalization, final int max) {
        return ((double) generalization / (double) max) * 100d;
    }

    /**
     * Draws the lattice
     * 
     * @param gr
     */
    private void draw(final GC g) {
        
        if (model == null) {
            Point size = canvas.getSize();
            g.setBackground(COLOR_WHITE);
            g.fillRectangle(0, 0, size.x, size.y);
            return;
        }

        // Transform
        Point size = canvas.getSize();
        g.setBackground(COLOR_WHITE);
        g.fillRectangle(0, 0, size.x, size.y);

        if (numNodes > model.getMaxNodesInViewer()) {

            final int x = (size.x / 2) - (MSG_WIDTH / 2);
            final int y = (size.y / 2) - (MSG_HEIGHT / 2);

            if ((x < 0) || (y < 0)) { return; }

            g.setBackground(COLOR_LIGHT_GRAY);
            g.fillRectangle(x, y, MSG_WIDTH, MSG_HEIGHT);
            g.setForeground(COLOR_BLACK);
            g.drawRectangle(x, y, MSG_WIDTH, MSG_HEIGHT);
            drawText(g, Resources.getMessage("LatticeView.7"), x, y, MSG_WIDTH, MSG_HEIGHT); //$NON-NLS-1$

            return;
        }

        if (lattice.isEmpty() || (screen == null)) { return; }

        // Draw connections
        drawConnections(g);
        
        // Draw nodes
        drawNodes(g);
    }

    /**
     * Draws the connections
     * 
     * @param g
     */
    private void drawConnections(GC g) {
        
        // Prepare
        Color color = null;
        Set<ARXNode> done = new HashSet<ARXNode>();
        SWTLineDrawer drawer = new SWTLineDrawer(g, screen);

        // Set style
        g.setLineWidth(STROKE_WIDTH_CONNECTION);
        g.setForeground(COLOR_BLACK);

        // For each node
        for (final ARXNode node : lattice) {
            
            // If visible
            if ((Boolean) node.getAttributes().get(ATTRIBUTE_VISIBLE)) {
                
                // Obtain coordinates
                Bounds center1 = (Bounds) node.getAttributes().get(ATTRIBUTE_BOUNDS);
                if (color == null) {
                    color = getLineColor(nodeWidth);
                    g.setForeground(color);
                } 
                
                // Draw
                for (final ARXNode n : node.getSuccessors()) {
                    
                    // If other visible and not already processed
                    if (!done.contains(n) && (Boolean) n.getAttributes().get(ATTRIBUTE_VISIBLE)) {

                       // Obtain coordinates
                       Bounds center2 = (Bounds) n.getAttributes().get(ATTRIBUTE_BOUNDS);
                       
                       // Draw
                       drawer.drawLine((int)center1.centerX, (int)center1.centerY,
                                       (int)center2.centerX, (int)center2.centerY);
                    }
                }
            }
            
            // Add to set of already processed nodes
            done.add(node);
        }
        
        // Dispose color
        if (color != null && !color.isDisposed()) {
            color.dispose();
        }
    }

    /**
     * Returns a line color for drawing the connections
     * @param node
     * @return
     */
    private Color getLineColor(double nodeWidth) {

        int value = (int) (nodeWidth / 50d * 128d);
        value = value < 0 ? 0 : value;
        value = value > 255 ? 255 : value;
        value = 255 - value;
        value = value < 64 ? 64 : value;
        value = value > 200 ? 200 : value;
        return new Color(canvas.getDisplay(), value, value, value);
    }

    /**
     * Draws a node
     * 
     * @param node
     * @param g
     */
    private void drawNodes(final GC g) {

        // Prepare
        Rectangle bounds = new Rectangle(0, 0, (int)nodeWidth, (int)nodeHeight);
        Transform transform = new Transform(g.getDevice());
        
        // Set style
        g.setLineWidth(STROKE_WIDTH_NODE);
        g.setFont(font);

        // Draw nodes
        for (final ARXNode node : lattice) {
            
            // Obtain coordinates
            Bounds _bounds = (Bounds) node.getAttributes().get(ATTRIBUTE_BOUNDS);
            bounds.x = (int) _bounds.x;
            bounds.y = (int) _bounds.y;
            
            // Clipping
            if (bounds.intersects(new Rectangle(0, 0, screen.x, screen.y))) { 
                
                // Retrieve/compute some data
                Path path = (Path) node.getAttributes().get(ATTRIBUTE_PATH);
                Point extent = (Point) node.getAttributes().get(ATTRIBUTE_EXTENT);
                if (path == null) {
                    String text = (String) node.getAttributes().get(ATTRIBUTE_LABEL);
                    path = new Path(canvas.getDisplay());
                    path.addString(text, 0, 0, font);
                    node.getAttributes().put(ATTRIBUTE_PATH, path);
                    extent = g.textExtent(text);
                    node.getAttributes().put(ATTRIBUTE_EXTENT, extent);
                }
        
                // Degrade if too far away
                if (bounds.width <= 4) {
                    g.setBackground(getInnerColor(node));
                    g.setAntialias(SWT.OFF);
                    g.fillRectangle(bounds.x, bounds.y, bounds.width, bounds.height);
        
                    // Draw real node
                } else {
                    
                    // Fill background
                    g.setBackground(getInnerColor(node));
                    g.setAntialias(SWT.OFF);
                    if (node != selectedNode) {
                        g.fillOval(bounds.x, bounds.y, bounds.width, bounds.height);
                    } else {
                        g.fillRectangle(bounds.x, bounds.y, bounds.width, bounds.height);
                    }
                    
                    // Draw line
                    g.setLineWidth(getOuterStrokeWidth(node, bounds.width));
                    g.setForeground(getOuterColor(node));
                    g.setAntialias(SWT.ON);
                    if (node != selectedNode) {
                        g.drawOval(bounds.x, bounds.y, bounds.width, bounds.height);
                    } else {
                        g.drawRectangle(bounds.x, bounds.y, bounds.width, bounds.height);
                    }
                    
                    // Draw text
                    if (bounds.width >= 20) {
                        
                        g.setTextAntialias(SWT.ON);
                        
                        float factor1 = (bounds.width * 0.7f) / (float)extent.x;
                        float factor2 = (bounds.height * 0.7f) / (float)extent.y;
                        float factor = Math.min(factor1, factor2);
                        
                        int positionX = bounds.x + (int)(((float)bounds.width - (float)extent.x * factor) / 2f); 
                        int positionY = bounds.y + (int)(((float)bounds.height - (float)extent.y * factor) / 2f);
                        
                        transform.identity();
                        transform.translate(positionX, positionY);
                        transform.scale(factor, factor);
                        g.setTransform(transform);
                        
                        g.setBackground(COLOR_BLACK);
                        g.fillPath(path);
                        g.setTransform(null);
                    }
                }
            }
        }
        
        // Clean up
        transform.dispose();
    }

    /**
     * Utility method which centers a text in a rectangle
     * @param gc
     * @param text
     * @param x
     * @param y
     * @param width
     * @param height
     */
    private void drawText(final GC gc, final String text, final int x, final int y, final int width, final int height) {

        Point size = canvas.getSize();
        Point extent = gc.textExtent(text);
        gc.setClipping(x, y, width, height);
        int xx = x + (width - extent.x) / 2;
        int yy = y + height / 2 - extent.y / 2;
        gc.drawText(text, xx, yy, true);
        gc.setClipping(0, 0, size.x, size.y);
    }

    /**
     * Returns the inner color
     * 
     * @param node
     * @return
     */
    private Color getInnerColor(final ARXNode node) {
        if (node.isAnonymous() == Anonymity.ANONYMOUS) {
            if (node.equals(optimum)) {
                return COLOR_YELLOW;
            } else {
                return COLOR_GREEN;
            }
        } else if (node.isAnonymous() == Anonymity.PROBABLY_ANONYMOUS) {
            return COLOR_LIGHT_GREEN;
        } else if (node.isAnonymous() == Anonymity.PROBABLY_NOT_ANONYMOUS) {
            return COLOR_LIGHT_RED;
        } else {
            return COLOR_RED;
        }
    }

    /**
     * Returns the node at the given location
     * 
     * @param x
     * @param y
     * @return
     */
    private ARXNode getNode(final int x, final int y) {
        for (final ARXNode node : lattice) {
            final Bounds bounds = (Bounds) node.getAttributes().get(ATTRIBUTE_BOUNDS);
            if (bounds == null) { return null; }
            if ((x >= bounds.x) && (y >= bounds.y) && (x <= (bounds.x + nodeWidth)) && (y <= (bounds.y + nodeHeight))) { return node; }
        }
        return null;
    }

    /**
     * Returns the outer color
     * 
     * @param node
     * @return
     */
    private Color getOuterColor(final ARXNode node) {
        if (node.isChecked()) {
            return COLOR_BLUE;
        } else {
            return COLOR_BLACK;
        }
    }

    /**
     * Returns the outer stroke width
     * 
     * @param node
     * @param width
     * @return
     */
    private int getOuterStrokeWidth(final ARXNode node, final int width) {
        int result = node.isChecked() ? width / 100 : 1;
        result = node.isChecked() ? result + 1 : result;
        return result >=1 ? result < 1 ? 1 : result : 1;
    }

    /**
     * Creates a tooltip text
     * 
     * @param node
     */
    private String getTooltipText(final ARXNode node) {
        final StringBuffer b = new StringBuffer();
        b.append(Resources.getMessage("LatticeView.1")); //$NON-NLS-1$
        b.append(format.format(asRelativeValue(node.getMinimumInformationLoss())));
        b.append(" - "); //$NON-NLS-1$
        b.append(format.format(asRelativeValue(node.getMaximumInformationLoss())));
        b.append(" [%]\n"); //$NON-NLS-1$
        for (final String qi : node.getQuasiIdentifyingAttributes()) {
            int height = model.getOutputDefinition().getHierarchyHeight(qi);
            b.append(" * "); //$NON-NLS-1$
            b.append(qi);
            b.append(": "); //$NON-NLS-1$
            b.append(format.format(asRelativeValue(node.getGeneralization(qi), height - 1)));
            b.append(" [%]\n"); //$NON-NLS-1$
        }
        b.setLength(b.length() - 1);
        return b.toString();
    }

    /**
     * Initializes the data structures for displaying a new lattice
     * 
     * @param lattice
     */
    private void initialize(final ARXResult r, final ModelNodeFilter filter) {

        if ((r == null) || (r.getLattice() == null) || (filter == null)) {
            reset();
            return;
        }

        // Build the sublattice
        final ARXLattice l = r.getLattice();
        latticeWidth = 0;
        numNodes = 0;
        optimum = r.getGlobalOptimum();
        final List<List<ARXNode>> lattice = new ArrayList<List<ARXNode>>();
        for (final ARXNode[] level : l.getLevels()) {
            final List<ARXNode> lvl = new ArrayList<ARXNode>();
            for (final ARXNode node : level) {
                if (filter.isAllowed(r.getLattice(), node)) {
                    lvl.add(node);
                    numNodes++;
                    node.getAttributes().put(ATTRIBUTE_VISIBLE, true);
                } else {
                    node.getAttributes().put(ATTRIBUTE_VISIBLE, false);
                }
            }
            if (!lvl.isEmpty()) {
                lattice.add(lvl);
            }
            latticeWidth = Math.max(latticeWidth, lvl.size());
        }
        latticeHeight = lattice.size();

        // Check
        if (numNodes > model.getMaxNodesInViewer()) { return; }

        // Cleanup
        for (ARXNode node : this.lattice) {
            Path path = (Path)node.getAttributes().get(ATTRIBUTE_PATH);
            if (path!=null) {
                path.dispose();
                node.getAttributes().put(ATTRIBUTE_PATH, null);
            }
        }
        this.lattice.clear();
        
        // Now initialize the data structures
        int y = latticeHeight - 1;
        for (final List<ARXNode> level : lattice) {
            for (int i = 0; i < level.size(); i++) {
                final ARXNode node = level.get(i);
                this.lattice.add(node);
                node.getAttributes().put(ATTRIBUTE_POSITION, i);
                node.getAttributes().put(ATTRIBUTE_LEVEL, y);
                node.getAttributes().put(ATTRIBUTE_LEVELSIZE, level.size());
                String text = Arrays.toString(node.getTransformation());
                text = text.substring(1, text.length() - 1);
                node.getAttributes().put(ATTRIBUTE_LABEL, text);
            }
            y--;
        }

        // Reset the parameters
        initializeCanvas();
    }

    /**
     * Recomputes the initial positions of all nodes
     */
    private void initializeCanvas() {

        // Obtain screen size
        screen = canvas.getSize();

        // Obtain optimal width and height per node
        double width = NODE_INITIAL_SIZE;
        double height = width * NODE_SIZE_RATIO;
        if ((height * latticeHeight) > screen.y) {
            final double factor = screen.y / (height * latticeHeight);
            height *= factor;
            width *= factor;
        }
        if ((width * latticeWidth) > screen.x) {
            final double factor = screen.x / (width * latticeWidth);
            height *= factor;
            width *= factor;
        }
        nodeWidth = width * NODE_FRAME_RATIO;
        nodeHeight = height * NODE_FRAME_RATIO;

        // Compute deltas to center the lattice
        final double deltaY = (screen.y - (height * latticeHeight)) / 2d;
        final double deltaX = (screen.x - (width * latticeWidth)) / 2d;

        // Attach initial boundaries to each node
        for (final ARXNode node : lattice) {

            // Node properties
            final double position = (Integer) node.getAttributes().get(ATTRIBUTE_POSITION);
            final double level = (Integer) node.getAttributes().get(ATTRIBUTE_LEVEL);
            final double levelsize = (Integer) node.getAttributes().get(ATTRIBUTE_LEVELSIZE);

            // Level offset
            final double offset = (latticeWidth * width) - (levelsize * width);

            // Node boundaries
            final double centerX = deltaX + (position * width) + (width / 2d) + (offset / 2d);
            final double centerY = deltaY + (level * height) + (height / 2d);
            final double x = centerX - (nodeWidth / 2);
            final double y = centerY - (nodeHeight / 2);

            // Pack
            final Bounds b = new Bounds();
            b.centerX = centerX;
            b.centerY = centerY;
            b.x = x;
            b.y = y;

            // Store
            node.getAttributes().put(ATTRIBUTE_BOUNDS, b);
        }
    }

    /**
     * Creates all required listeners
     */
    private void initializeListeners() {

        canvas.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseDown(MouseEvent arg0) {
                dragX = arg0.x;
                dragY = arg0.y;
                dragStartX = arg0.x;
                dragStartY = arg0.y;
                if (dragType == DragType.NONE) {
                    if (arg0.button == 1) {
                        dragType = DragType.MOVE;
                    } else  if (arg0.button == 3) {
                        dragType = DragType.ZOOM;
                    }
                }
            }

            @Override
            public void mouseUp(MouseEvent arg0) {
                dragType = DragType.NONE;
            }
        });

        canvas.addMouseListener(new MouseAdapter(){
            /** Drag parameters */
            private int clickX = 0;
            /** Drag parameters */
            private int clickY = 0;
            @Override
            public void mouseDown(MouseEvent arg0) {
                
                clickX = arg0.x;
                clickY = arg0.y;
                
                if (arg0.button == 1) {
                    final ARXNode node = getNode(arg0.x, arg0.y);
                    if (node != null) {
                        actionButtonClicked1(node);
                    }
                } 
            }
            @Override
            public void mouseUp(MouseEvent arg0) {
                if (arg0.button == 3 && arg0.x == clickX && arg0.y == clickY) {
                    final ARXNode node = getNode(arg0.x, arg0.y);
                    if (node != null) {
                        Point display = canvas.toDisplay(arg0.x, arg0.y);
                        actionButtonClicked3(node, display.x, display.y);
                    }
                }
                clickX = arg0.x;
                clickY = arg0.y;
            }       
            
        });

        canvas.addMouseMoveListener(new MouseMoveListener(){

            @Override
            public void mouseMove(MouseEvent arg0) {
                
                if (dragType != DragType.NONE) {
                    final int deltaX = arg0.x - dragX;
                    final int deltaY = arg0.y - dragY;
                    if (dragType == DragType.MOVE) {

                        // Just move the nodes around
                        for (final ARXNode node : lattice) {
                            final Bounds dbounds = (Bounds) node.getAttributes().get(ATTRIBUTE_BOUNDS);
                            dbounds.centerX += deltaX;
                            dbounds.centerY += deltaY;
                            dbounds.x += deltaX;
                            dbounds.y += deltaY;
                        }
                    } else if (dragType == DragType.ZOOM) {

                        // Ensure min & max zoom
                        double zoom = -((double) deltaY / (double) screen.y) * ZOOM_SPEED;
                        final double newWidth = nodeWidth + (zoom * nodeWidth);
                        if (newWidth > screen.x) {
                            zoom = (screen.x - nodeWidth) / nodeWidth;
                        }
                        if (newWidth < MIN_WIDTH) {
                            zoom = (MIN_WIDTH - nodeWidth) / nodeWidth;
                        }
                        final double newHeight = nodeHeight + (zoom * nodeHeight);
                        if (newHeight > screen.y) {
                            zoom = (screen.y - nodeHeight) / nodeHeight;
                        }
                        if (newHeight < MIN_HEIGHT) {
                            zoom = (MIN_HEIGHT - nodeHeight) / nodeHeight;
                        }

                        // Zoom the node sizes
                        nodeWidth += zoom * nodeWidth;
                        nodeHeight += zoom * nodeHeight;

                        // Zoom the node positions
                        for (final ARXNode node : lattice) {
                            final Bounds dbounds = (Bounds) node.getAttributes().get(ATTRIBUTE_BOUNDS);
                            dbounds.centerX -= dragStartX;
                            dbounds.centerX += zoom * dbounds.centerX;
                            dbounds.centerX += dragStartX;

                            dbounds.centerY -= dragStartY;
                            dbounds.centerY += zoom * dbounds.centerY;
                            dbounds.centerY += dragStartY;

                            dbounds.x -= dragStartX;
                            dbounds.x += zoom * dbounds.x;
                            dbounds.x += dragStartX;

                            dbounds.y -= dragStartY;
                            dbounds.y += zoom * dbounds.y;
                            dbounds.y += dragStartY;
                        }
                    }
                    dragX += deltaX;
                    dragY += deltaY;
                    canvas.redraw();
                }
            }
        });

        canvas.addMouseMoveListener(new MouseMoveListener(){
            public void mouseMove(MouseEvent arg0) {
                tooltipX = arg0.x;
                tooltipY = arg0.y;
            }
        });
        
        canvas.addListener(SWT.MouseExit, new Listener() {
            public void handleEvent(Event e) {
                tooltipX = -1;
                tooltipY = -1;
            }
          });
        
        canvas.addControlListener(new ControlAdapter() {

            @Override
            public void controlResized(ControlEvent arg0) {
                screen = canvas.getSize();
                initializeCanvas();
                canvas.redraw();
            }
        });
    }

    /**
     * Creates the context menu
     */
    private void initializeMenu() {
        menu = new Menu(canvas.getShell());
        MenuItem item1 = new MenuItem(menu, SWT.NONE);
        item1.setText(Resources.getMessage("LatticeView.9")); //$NON-NLS-1$
        item1.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.getClipboard().add(selectedNode);
                controller.update(new ModelEvent(ViewLattice.this, ModelPart.CLIPBOARD, selectedNode));
                model.setSelectedNode(selectedNode);
                controller.update(new ModelEvent(ViewLattice.this, ModelPart.SELECTED_NODE, selectedNode));
                canvas.redraw();
            }
        });
        
        MenuItem item2 = new MenuItem(menu, SWT.NONE);
        item2.setText(Resources.getMessage("LatticeView.10")); //$NON-NLS-1$
        item2.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                controller.actionApplySelectedTransformation();
                model.setSelectedNode(selectedNode);
                controller.update(new ModelEvent(ViewLattice.this, ModelPart.SELECTED_NODE, selectedNode));
                canvas.redraw();
            }
        });
    }

    /**
     * For performance reasons, we check for tool tips only at certain times
     */
    private void initializeToolTipTimer() {
        canvas.getDisplay().timerExec(TOOLTIP_WAIT, new Runnable(){
            public void run(){
                
                if (tooltipX != oldTooltipX || tooltipY != oldTooltipY) {

                    String text = null;
                    if (tooltipX != -1 && tooltipY != -1) {
                        ARXNode node = getNode(tooltipX, tooltipY);
                        text = node == null ? null : getTooltipText(node);
                    } 
                    canvas.setToolTipText(text);
                }
                oldTooltipX = tooltipX;
                oldTooltipY = tooltipY;
                if (!canvas.isDisposed()) {
                    canvas.getDisplay().timerExec(TOOLTIP_WAIT, this);
                }
            }
        });
    }
}
