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

package org.deidentifier.arx.gui.view.impl.explore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deidentifier.arx.ARXLattice;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelNodeFilter;
import org.deidentifier.arx.gui.resources.Resources;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
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

/**
 * This class implements a view of a lattice.
 *
 * @author Fabian Prasser
 */
public class ViewLattice extends ViewSolutionSpace {

    /**
     * This class is here for backwards compatibility only.
     */
    @SuppressWarnings("unused")
    private static class Bounds implements Serializable {
        
        /**  TODO */
        private static final long serialVersionUID = -7472570696920782588L;
    }
    
    /**
     * The current drag type.
     */
    private static enum DragType {
        
        /**  TODO */
        MOVE,
        
        /**  TODO */
        ZOOM,
        
        /**  TODO */
        NONE
    }

    /**
     * This class is here for serializability, only.
     */
    private static class SerializablePath implements Serializable {
        
        /**  TODO */
        private static final long serialVersionUID = -4572722688452678425L;
        
        /**  TODO */
        private final transient Path path;
        
        /**
         * 
         *
         * @param path
         */
        public SerializablePath(Path path){
            this.path = path;
        }
        
        /**
         * 
         */
        public void dispose(){
            if (!this.path.isDisposed()) {
                this.path.dispose();
            }
        }
        
        /**
         * 
         *
         * @return
         */
        public Path getPath(){
            return this.path;
        }
    }

    /** Color. */
    private static final Color         COLOR_WHITE             = GUIHelper.getColor(255, 255, 255);
    
    /** Color. */
    private static final Color         COLOR_BLACK             = GUIHelper.getColor(0, 0, 0);
    
    /** Color. */
    private static final Color         COLOR_LIGHT_GRAY        = GUIHelper.getColor(211, 211, 211);

    /** Attribute constant. */
    private static final int          ATTRIBUTE_CENTER        = 4;
    
    /** Attribute constant. */
    private static final int          ATTRIBUTE_LABEL         = 5;
    
    /** Attribute constant. */
    private static final int          ATTRIBUTE_VISIBLE       = 6;
    
    /** Attribute constant. */
    private static final int          ATTRIBUTE_PATH          = 7;
    
    /** Attribute constant. */
    private static final int          ATTRIBUTE_EXTENT        = 8;

    /** Time to wait for a tool tip to show. */
    private static final int          TOOLTIP_WAIT            = 200;
    
    /** Global settings. */
    private static final double       NODE_INITIAL_SIZE       = 200d;
    
    /** Global settings. */
    private static final double       NODE_FRAME_RATIO        = 0.7d;
    
    /** Global settings. */
    private static final double       NODE_SIZE_RATIO         = 0.3d;
    
    /** Global settings. */
    private static final double       ZOOM_SPEED              = 10d;
    
    /** Global settings. */
    private static final int          MSG_WIDTH               = 300;
    
    /** Global settings. */
    private static final int          MSG_HEIGHT              = 100;
    
    /** Global settings. */
    private static final int          MIN_WIDTH               = 2;
    
    /** Global settings. */
    private static final int          MIN_HEIGHT              = 1;
    
    /** For the current view. */
    private static final int          STROKE_WIDTH_NODE       = 1;
    
    /** For the current view. */
    private static final int          STROKE_WIDTH_CONNECTION = 1;
    
    /** The font. */
    private Font                      font                    = null;

    /** For the current view. */
    private double                    nodeWidth               = 0f;
    
    /** For the current view. */
    private double                    nodeHeight              = 0f;
    
    /** The lattice to display. */
    private final List<List<ARXNode>> lattice                 = new ArrayList<List<ARXNode>>();
    
    /** The according ARX lattice. */
    private ARXLattice                arxLattice              = null;
    
    /** The lattice to display. */
    private int                       latticeWidth            = 0;
    
    /** The screen size. */
    private Point                     screen                  = null;

    /** The number of nodes. */
    private int                       numNodes                = 0;
    
    /** Drag parameters. */
    private int                       dragX                   = 0;
    
    /** Drag parameters. */
    private int                       dragY                   = 0;
    
    /** Drag parameters. */
    private int                       dragStartX              = 0;
    
    /** Drag parameters. */
    private int                       dragStartY              = 0;
    
    /** Drag parameters. */
    private DragType                  dragType                = DragType.NONE;

    /** The tool tip. */
    private int                       tooltipX                = -1;
    
    /** The tool tip. */
    private int                       tooltipY                = -1;
    
    /** The tool tip. */
    private int                       oldTooltipX             = -1;
    
    /** The tool tip. */
    private int                       oldTooltipY             = -1;
    
    /** The canvas. */
    private final Canvas              canvas;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     */
    public ViewLattice(final Composite parent, final Controller controller) {

        super(parent, controller);
                
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
        this.canvas.addDisposeListener(new DisposeListener(){
            public void widgetDisposed(DisposeEvent arg0) {
                clearLatticeAndDisposePaths(); // Free resources
            }
        });
        
        // Initialize
        this.initializeToolTipTimer();
        this.initializeListeners();
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        font.dispose();
    }

    /**
     * Resets the view.
     */
    @Override
    public void reset() {
        super.reset();
        this.numNodes = 0;
        this.arxLattice = null;
        this.clearLatticeAndDisposePaths();
        this.latticeWidth = 0;
        this.screen = null;
        this.canvas.redraw();
    }

    /**
     * Called when button 1 is clicked on a node.
     *
     * @param node
     */
    private void actionButtonClicked1(ARXNode node) {
        actionSelectNode(node);
        canvas.redraw();
    }

    /**
     * Called when button 3 is clicked on a node.
     *
     * @param node
     * @param x
     * @param y
     */
    private void actionButtonClicked3(ARXNode node, final int x, final int y) {
        actionSelectNode(node);
        canvas.redraw();
        actionShowMenu(x, y);
        dragType = DragType.NONE;
    }

    /**
     * Clears the lattice.
     */
    private void clearLatticeAndDisposePaths() {
        for (List<ARXNode> level : lattice) {
            for (ARXNode node : level) {
                SerializablePath path = (SerializablePath)node.getAttributes().get(ATTRIBUTE_PATH);
                if (path!=null && path.getPath()!=null) {
                    node.getAttributes().put(ATTRIBUTE_PATH, null);
                    path.dispose();
                }
            }
        }
        this.lattice.clear();
    }

    /**
     * Draws the lattice.
     *
     * @param g
     */
    private void draw(final GC g) {

        // Fill background
        Point size = canvas.getSize();
        g.setBackground(COLOR_WHITE);
        g.fillRectangle(0, 0, size.x, size.y);
                
        // Return, if nothing to show
        if (getModel() == null) {
            return;
        }

        // If too many nodes
        if (numNodes > getModel().getMaxNodesInViewer()) {
            int x = (size.x / 2) - (MSG_WIDTH / 2);
            int y = (size.y / 2) - (MSG_HEIGHT / 2);
            if ((x < 0) || (y < 0)) { return; }
            g.setBackground(COLOR_LIGHT_GRAY);
            g.fillRectangle(x, y, MSG_WIDTH, MSG_HEIGHT);
            g.setForeground(COLOR_BLACK);
            g.drawRectangle(x, y, MSG_WIDTH, MSG_HEIGHT);
            drawText(g, Resources.getMessage("LatticeView.7"), x, y, MSG_WIDTH, MSG_HEIGHT); //$NON-NLS-1$
            return;
        }

        // Return, if nothing to show
        if (lattice.isEmpty() || (screen == null)) { return; }

        // Draw connections
        drawConnections(g);
        
        // Draw nodes
        drawNodes(g);
    }

    /**
     * Draws the connections.
     *
     * @param g
     */
    private void drawConnections(GC g) {
        
        // Prepare
        Color color = getLineColor(nodeWidth);
        Set<ARXNode> done = new HashSet<ARXNode>();
        int[] clip = new int[4];

        // Set style
        g.setLineWidth(STROKE_WIDTH_CONNECTION);
        g.setForeground(color);

        // For each node
        for (List<ARXNode> level : lattice) {
            for (ARXNode node1 : level) {

                // Obtain coordinates
                double[] center1 = (double[]) node1.getAttributes().get(ATTRIBUTE_CENTER);
                
                // Draw
                for (final ARXNode node2 : node1.getSuccessors()) {
                    
                    boolean visible = (Boolean)node2.getAttributes().get(ATTRIBUTE_VISIBLE);
                    if (visible && !done.contains(node2)) {

                       // Obtain coordinates
                       double[] center2 = (double[]) node2.getAttributes().get(ATTRIBUTE_CENTER);
                           
                       // Perform clipping
                       if (liangBarsky(0, screen.x, 0, screen.y, 
                                       center1[0], center1[1],
                                       center2[0], center2[1],
                                       clip)) {
                           
                           // Draw
                           g.drawLine(clip[0], clip[1], clip[2], clip[3]);
                       }
                    }
                }

                // Add to set of already processed nodes
                done.add(node1);
            }
        }
        
        // Dispose color
        if (color != null && !color.isDisposed()) {
            color.dispose();
        }
    }

    /**
     * Draws a node.
     *
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
        for (List<ARXNode> level : lattice) {
            for (ARXNode node : level) {
                
                // Obtain coordinates
                double[] center = (double[]) node.getAttributes().get(ATTRIBUTE_CENTER);
                bounds.x = (int)(center[0] - nodeWidth / 2d);
                bounds.y = (int)(center[1] - nodeHeight / 2d);
                
                // Clipping
                if (bounds.intersects(new Rectangle(0, 0, screen.x, screen.y))) { 
                    
                    // Retrieve/compute text rendering data
                    SerializablePath path = (SerializablePath) node.getAttributes().get(ATTRIBUTE_PATH);
                    Point extent = (Point) node.getAttributes().get(ATTRIBUTE_EXTENT);
                    if (path == null || path.getPath() == null) {
                        String text = (String) node.getAttributes().get(ATTRIBUTE_LABEL);
                        path = new SerializablePath(new Path(canvas.getDisplay()));
                        path.getPath().addString(text, 0, 0, font);
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
                        if (node != getSelectedNode()) {
                            g.fillOval(bounds.x, bounds.y, bounds.width, bounds.height);
                        } else {
                            g.fillRectangle(bounds.x, bounds.y, bounds.width, bounds.height);
                        }
                        
                        // Draw line
                        g.setLineWidth(getOuterStrokeWidth(node, bounds.width));
                        g.setForeground(getOuterColor(node));
                        g.setAntialias(SWT.ON);
                        if (node != getSelectedNode()) {
                            g.drawOval(bounds.x, bounds.y, bounds.width, bounds.height);
                        } else {
                            g.drawRectangle(bounds.x, bounds.y, bounds.width, bounds.height);
                        }
                        
                        // Draw text
                        if (bounds.width >= 20) {
                            
                            // Enable anti-aliasing
                            g.setTextAntialias(SWT.ON);
                            
                            // Compute position and factor
                            float factor1 = (bounds.width * 0.7f) / (float)extent.x;
                            float factor2 = (bounds.height * 0.7f) / (float)extent.y;
                            float factor = Math.min(factor1, factor2);
                            int positionX = bounds.x + (int)(((float)bounds.width - (float)extent.x * factor) / 2f); 
                            int positionY = bounds.y + (int)(((float)bounds.height - (float)extent.y * factor) / 2f);
                            
                            // Initialize transformation
                            transform.identity();
                            transform.translate(positionX, positionY);
                            transform.scale(factor, factor);
                            g.setTransform(transform);
                            
                            // Draw and reset
                            g.setBackground(COLOR_BLACK);
                            g.fillPath(path.getPath());
                            g.setTransform(null);
                            g.setTextAntialias(SWT.OFF);
                        }
                    }
                }
            }
        }
        
        // Clean up
        transform.dispose();
    }

    /**
     * Utility method which centers a text in a rectangle.
     *
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
     * Returns a line color for drawing the connections.
     *
     * @param nodeWidth
     * @return
     */
    private Color getLineColor(double nodeWidth) {
        return new Color(canvas.getDisplay(), 200, 200, 200);
    }

    /**
     * Returns the node at the given location.
     *
     * @param x
     * @param y
     * @return
     */
    private ARXNode getNode(final int x, final int y) {
        for (List<ARXNode> level : lattice) {
            for (ARXNode node : level) {
                double[] bounds = (double[]) node.getAttributes().get(ATTRIBUTE_CENTER);
                if (bounds == null) { return null; }
                if ((x >= bounds[0] - nodeHeight/2) && 
                    (y >= bounds[1] - nodeHeight/2) && 
                    (x <= (bounds[0] + nodeWidth/2)) && 
                    (y <= (bounds[1] + nodeHeight/2))) {
                    
                    return node; 
                }
            }
        }
        return null;
    }

    /**
     * Initializes the data structures for displaying a new lattice.
     *
     * @param result
     * @param filter
     */
    private void initialize(final ARXResult result, final ModelNodeFilter filter) {

        // Return if nothing to do
        if ((result == null) || (result.getLattice() == null) || (filter == null)) {
            reset();
            return;
        }
        
        // Clear the lattice
        if (!result.getLattice().equals(this.arxLattice)) {
            this.clearLatticeAndDisposePaths();
            this.arxLattice = result.getLattice();
        } else {
            this.lattice.clear();
        }

        // Build the visible sub-lattice
        ARXLattice originalLattice = result.getLattice();
        this.latticeWidth = 0;
        this.numNodes = 0;
        for (ARXNode[] originalLevel : originalLattice.getLevels()) {
            List<ARXNode> level = new ArrayList<ARXNode>();
            for (ARXNode node : originalLevel) {
                boolean visible = filter.isAllowed(result.getLattice(), node);
                node.getAttributes().put(ATTRIBUTE_VISIBLE, visible);
                if (visible) {
                    level.add(node);
                    numNodes++;
                } 
            }
            if (!level.isEmpty()) {
                this.lattice.add(level);
            }
            this.latticeWidth = Math.max(this.latticeWidth, level.size());
        }

        // Check
        if (numNodes > getModel().getMaxNodesInViewer()) { return; }

        // Now initialize the text attribute
        for (List<ARXNode> level : this.lattice) {
            for (ARXNode node : level) {
                if (!node.getAttributes().containsKey(ATTRIBUTE_LABEL)) {
                    String text = Arrays.toString(node.getTransformation());
                    text = text.substring(1, text.length() - 1);
                    node.getAttributes().put(ATTRIBUTE_LABEL, text);
                }
            }
        }
        
        // Reset the parameters
        initializeCanvas();
    }

    /**
     * Recomputes the initial positions of all nodes.
     */
    private void initializeCanvas() {

        // Obtain screen size
        screen = canvas.getSize();

        // Obtain optimal width and height per node
        double width = NODE_INITIAL_SIZE;
        double height = width * NODE_SIZE_RATIO;
        if ((height * lattice.size()) > screen.y) {
            double factor = screen.y / (height * lattice.size());
            height *= factor; width *= factor;
        }
        if ((width * latticeWidth) > screen.x) {
            double factor = screen.x / (width * latticeWidth);
            height *= factor; width *= factor;
        }
        nodeWidth = width * NODE_FRAME_RATIO;
        nodeHeight = height * NODE_FRAME_RATIO;

        // Compute deltas to center the lattice
        final double deltaY = (screen.y - (height * lattice.size())) / 2d;
        final double deltaX = (screen.x - (width * latticeWidth)) / 2d;

        // For each level
        double positionY = lattice.size() -1;
        for (List<ARXNode> level : lattice) {

            // For each node on this level
            double centerY = deltaY + (positionY * height) + (height / 2d);
            double positionX = 0;
            for (ARXNode node : level) {
    
                // Create and store node properties
                double offset = (latticeWidth * width) - (level.size() * width);
                double centerX = deltaX + (positionX * width) + (width / 2d) + (offset / 2d);
                node.getAttributes().put(ATTRIBUTE_CENTER, new double[]{centerX, centerY});
                
                // Next node
                positionX++;
            }
            // Next level
            positionY--;
        }
    }

    /**
     * Creates all required listeners.
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
                        for (List<ARXNode> level : lattice) {
                            for (ARXNode node : level) {
                                double[] center = (double[]) node.getAttributes().get(ATTRIBUTE_CENTER);
                                center[0] += deltaX;
                                center[1] += deltaY;
                            }
                        }
                        
                    } else if (dragType == DragType.ZOOM) {

                        // Compute zoom
                        double zoom = -((double) deltaY / (double) screen.y) * ZOOM_SPEED;
                        double newWidth = nodeWidth + (zoom * nodeWidth);
                        double newHeight = nodeHeight + (zoom * nodeHeight);

                        // Adjust
                        zoom = newWidth > screen.x ? (screen.x - nodeWidth) / nodeWidth : zoom;
                        zoom = newWidth < MIN_WIDTH ? (MIN_WIDTH - nodeWidth) / nodeWidth : zoom;                        
                        zoom = newHeight > screen.y ? (screen.y - nodeHeight) / nodeHeight : zoom;
                        zoom = newHeight < MIN_HEIGHT ? (MIN_HEIGHT - nodeHeight) / nodeHeight : zoom;

                        // Zoom the node size
                        nodeWidth += zoom * nodeWidth;
                        nodeHeight += zoom * nodeHeight;

                        // Zoom the node positions
                        for (List<ARXNode> level : lattice) {
                            for (ARXNode node : level) {
                                double[] center = (double[]) node.getAttributes().get(ATTRIBUTE_CENTER);
                                center[0] -= dragStartX;
                                center[0] += zoom * center[0];
                                center[0] += dragStartX;
    
                                center[1] -= dragStartY;
                                center[1] += zoom * center[1];
                                center[1] += dragStartY;
                            }
                        }
                    }
                    
                    // Store mouse data & redraw
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
     * For performance reasons, we check for tool tips only at certain times.
     */
    private void initializeToolTipTimer() {
        canvas.getDisplay().timerExec(TOOLTIP_WAIT, new Runnable(){
            public void run(){
                
                if (tooltipX != oldTooltipX || tooltipY != oldTooltipY) {
                    String text = null;
                    if (tooltipX != -1 && tooltipY != -1) {
                        ARXNode node = getNode(tooltipX, tooltipY);
                        text = node == null ? null : getTooltipDecorator().decorate(node);
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

    /**
     * Liang-Barsky line clipping function. Adapted from Daniel White
     *
     * @param edgeLeft
     * @param edgeRight
     * @param edgeTop
     * @param edgeBottom
     * @param x0src
     * @param y0src
     * @param x1src
     * @param y1src
     * @param clip
     * @return
     * @see http://www.skytopia.com/project/articles/compsci/clipping.html
     */
     private boolean liangBarsky (double edgeLeft, double edgeRight, double edgeTop, double edgeBottom,
                       double x0src, double y0src, double x1src, double y1src, 
                       int[] clip) {
    
         // Init
         double t0 = 0.0;    
         double t1 = 1.0;
         double xdelta = x1src-x0src;
         double ydelta = y1src-y0src;
         double p = 0, q = 0,r = 0;
    
         for(int edge=0; edge<4; edge++) {
             if (edge==0) {  p = -xdelta;    q = -(edgeLeft-x0src);  }
             if (edge==1) {  p = xdelta;     q =  (edgeRight-x0src); }
             if (edge==2) {  p = -ydelta;    q = -(edgeTop-y0src);}
             if (edge==3) {  p = ydelta;     q =  (edgeBottom-y0src);   }   
             r = q/p;
             if(p==0 && q<0) return false;
    
             if(p<0) {
                 if(r>t1) return false;
                 else if(r>t0) t0=r;
             } else if(p>0) {
                 if(r<t0) return false;
                 else if(r<t1) t1=r;
             }
         }
    
         // Clip
         clip[0] = (int)(x0src + t0*xdelta);
         clip[1] = (int)(y0src + t0*ydelta);
         clip[2] = (int)(x0src + t1*xdelta);
         clip[3] = (int)(y0src + t1*ydelta);
         return true;
     }

    @Override
    protected void actionRedraw() {
        this.canvas.redraw();
    }

    @Override
    protected void eventFilterChanged(ARXResult result, ModelNodeFilter filter) {
        initialize(result, filter);
        canvas.redraw();
    }

    @Override
    protected void eventModelChanged() {
        // Empty by design
    }

    @Override
    protected void eventNodeSelected() {
        canvas.redraw();
    }

    @Override
    protected void eventResultChanged(ARXResult result) {
        if (getModel().getResult() == null) reset();
    }
}
