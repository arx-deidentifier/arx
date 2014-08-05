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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

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
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.metric.InformationLoss;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * This class implements a view of a lattice
 * 
 * @author Fabian Prasser
 */
public class ViewLattice extends Panel implements IView {

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

    public static final org.eclipse.swt.graphics.Color GREEN           = GUIHelper.getColor(50, 205, 50);
    public static final org.eclipse.swt.graphics.Color LIGHT_GREEN     = GUIHelper.getColor(50, 128, 50);
    public static final org.eclipse.swt.graphics.Color ORANGE          = GUIHelper.getColor(255, 145, 0);
    public static final org.eclipse.swt.graphics.Color RED             = GUIHelper.getColor(255, 99, 71);
    public static final org.eclipse.swt.graphics.Color LIGHT_RED       = GUIHelper.getColor(128, 99, 71);
    public static final org.eclipse.swt.graphics.Color BLUE            = GUIHelper.getColor(0, 0, 255);
    public static final org.eclipse.swt.graphics.Color YELLOW          = GUIHelper.getColor(255, 215, 0);

    public static final Color                          AWT_GREEN       = asAWTColor(GREEN);
    public static final Color                          AWT_LIGHT_GREEN = asAWTColor(LIGHT_GREEN);
    public static final Color                          AWT_ORANGE      = asAWTColor(ORANGE);
    public static final Color                          AWT_RED         = asAWTColor(RED);
    public static final Color                          AWT_LIGHT_RED   = asAWTColor(LIGHT_RED);
    public static final Color                          AWT_BLUE        = asAWTColor(BLUE);
    public static final Color                          AWT_YELLOW      = asAWTColor(YELLOW);

    /**
     * Converts between different implementations of the color class
     * @param in
     * @return
     */
    private static Color asAWTColor(final org.eclipse.swt.graphics.Color in) {
        return new Color(in.getRed(), in.getGreen(), in.getBlue());
    }

    private Model               model;

    /** SVUID */
    private static final long   serialVersionUID      = 158477251360929026L;
    /** Attribute constant */
    private static final int    ATTRIBUTE_POSITION    = 1;
    /** Attribute constant */
    private static final int    ATTRIBUTE_LEVEL       = 2;
    /** Attribute constant */
    private static final int    ATTRIBUTE_LEVELSIZE   = 3;
    /** Attribute constant */
    private static final int    ATTRIBUTE_BOUNDS      = 4;
    /** Attribute constant */
    private static final int    ATTRIBUTE_LABEL       = 5;

    /** Attribute constant */
    private static final int    ATTRIBUTE_VISIBLE     = 6;
    /** Time to wait for a tooltip to show */
    private static final int    TOOLTIP_WAIT          = 100;
    /** Global settings */
    private static final double NODE_INITIAL_SIZE     = 200d;
    /** Global settings */
    private static final double NODE_FRAME_RATIO      = 0.7d;
    /** Global settings */
    private static final double NODE_SIZE_RATIO       = 0.3d;
    /** Global settings */
    private static final double ZOOM_SPEED            = 10d;
    /** Global settings */
    private static final int    MSG_WIDTH             = 300;
    /** Global settings */
    private static final int    MSG_HEIGHT            = 100;
    /** Global settings */
    private static final int    MIN_WIDTH             = 2;

    /** Global settings */
    private static final int    MIN_HEIGHT            = 1;
    /** For the current view */
    private final float         strokeWidthNode       = 0.1f;
    /** For the current view */
    private final float         strokeWidthConnection = 0.1f;
    /** For the current view */
    private double              nodeWidth             = 0f;

    /** For the current view */
    private double              nodeHeight            = 0f;
    /** The lattice to display */
    private final List<ARXNode> lattice               = new ArrayList<ARXNode>();
    /** The lattice to display */
    private int                 latticeWidth          = 0;
    /** The lattice to display */
    private int                 latticeHeight         = 0;
    /** The screen size */
    private Dimension           screen                = null;

    /** The number of nodes */
    private int                 numNodes              = 0;
    /** Drag parameters */
    private int                 dragX                 = 0;
    /** Drag parameters */
    private int                 dragY                 = 0;
    /** Drag parameters */
    private int                 dragStartX            = 0;
    /** Drag parameters */
    private int                 dragStartY            = 0;

    /** Drag parameters */
    private DragType            dragType              = DragType.NONE;

    /** The backbuffer for implementing double buffering */
    private BufferedImage       buffer                = null;

    /** The optimum */
    private ARXNode             optimum;

    /** The selected node */
    private ARXNode             selectedNode;

    /** The controller */
    private final Controller    controller;

    /** The bridge */
    private final Composite     bridge;

    /** The bridge */
    private final Frame         frame;

    /** The tooltip */
    private Timer               tooltipTimer          = null;
    /** The tooltip */
    private int                 tooltipX              = -1;
    /** The tooltip */
    private int                 tooltipY              = -1;

    /** Number format */
    private final NumberFormat  format                = new DecimalFormat("##0.000"); //$NON-NLS-1$

    /** Context menu*/
    private final Menu          menu;

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
        
        // Build bridge
        parent.setLayout(new GridLayout());
        bridge = new Composite(parent, SWT.BORDER | SWT.NO_BACKGROUND | SWT.EMBEDDED);
        bridge.setLayoutData(SWTUtil.createFillGridData());
        
        // Build frame
        frame = SWT_AWT.new_Frame(bridge);
        frame.setLayout(new BorderLayout());
        frame.add(this, BorderLayout.CENTER);
        frame.setBackground(Color.WHITE);
        
        // Build menu
        menu = new Menu(parent.getShell());
        MenuItem item1 = new MenuItem(menu, SWT.NONE);
        item1.setText(Resources.getMessage("LatticeView.9")); //$NON-NLS-1$
        item1.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.getClipboard().add(selectedNode);
                controller.update(new ModelEvent(ViewLattice.this, ModelPart.CLIPBOARD, selectedNode));
                model.setSelectedNode(selectedNode);
                controller.update(new ModelEvent(ViewLattice.this, ModelPart.SELECTED_NODE, selectedNode));
                repaint();
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
                repaint();
            }
        });
        
        // Build timer for tooltip
        this.tooltipTimer = new Timer(TOOLTIP_WAIT, new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (tooltipX != -1 && tooltipY != -1) {
                    final ARXNode node = getNode(tooltipX, tooltipY);
                    if (node != null) {
                        controller.getToolTip().show(createTooltipText(node));
                    } else {
                        controller.getToolTip().unshow();
                    }
                } else {
                    controller.getToolTip().unshow();
                }
            }
        });
        this.tooltipTimer.setRepeats(false);
        
        resetBuffer();
        initializeListeners();
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    @Override
    public void paint(final Graphics g) {
        if (buffer != null) {
            final Graphics bg = buffer.getGraphics();
            if (model != null) {
                draw(bg);
            } else {
                bg.setColor(Color.WHITE);
                bg.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
            }
            bg.dispose();
            g.drawImage(buffer, 0, 0, this);
        }
    }

    /**
     * Resets the view
     */
    @Override
    public void reset() {
        numNodes = 0;
        optimum = null;
        selectedNode = null;
        lattice.clear();
        latticeWidth = 0;
        latticeHeight = 0;
        screen = null;
        this.repaint();
    }

    @Override
    public void update(final Graphics g) {
        paint(g);
    }

    @Override
    public void update(final ModelEvent event) {

        if (event.part == ModelPart.SELECTED_NODE) {
            selectedNode = (ARXNode) event.data;
            this.repaint();
        } else if (event.part == ModelPart.RESULT) {
            if (model.getResult() == null) reset();
        } else if (event.part == ModelPart.MODEL) {
            model = (Model) event.data;
        } else if (event.part == ModelPart.FILTER) {
            if (model != null) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        initialize(model.getResult(), (ModelNodeFilter) event.data);
                        repaint();
                    }
                });
            }
        }
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
     * Utility method which centers a text in a rectangle
     * 
     * @param s1
     * @param g
     * @param x
     * @param y
     * @param w
     * @param h
     */
    private void centerText(final String s1, final Graphics g, final int x, final int y, final int w, final int h) {

        // Obtain metrics and data
        final Font f = g.getFont();
        final FontMetrics fm = g.getFontMetrics(f);
        final int ascent = fm.getAscent();
        final int height = fm.getHeight();

        // Compute position
        int width1 = 0, x0 = 0, y0 = 0;
        width1 = fm.stringWidth(s1);
        x0 = x + ((w - width1) / 2);
        y0 = y + ((h - height) / 2) + ascent;

        // Draw
        g.drawString(s1, x0, y0);
    }

    /**
     * Creates a tooltip text
     * 
     * @param node
     */
    private String createTooltipText(final ARXNode node) {
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
     * Draws the lattice
     * 
     * @param gr
     */
    private void draw(final Graphics gr) {

        // Transform
        final Graphics2D g = (Graphics2D) gr;
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (numNodes > model.getMaxNodesInViewer()) {

            final int x = (this.getSize().width / 2) - (MSG_WIDTH / 2);
            final int y = (this.getSize().height / 2) - (MSG_HEIGHT / 2);

            if ((x < 0) || (y < 0)) { return; }

            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(x, y, MSG_WIDTH, MSG_HEIGHT);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, MSG_WIDTH, MSG_HEIGHT);
            centerText(Resources.getMessage("LatticeView.7"), gr, x, y, MSG_WIDTH, MSG_HEIGHT); //$NON-NLS-1$

            return;
        }

        if (lattice.isEmpty() || (screen == null)) { return; }

        // Set style
        g.setStroke(new BasicStroke(strokeWidthConnection));
        g.setColor(Color.black);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw connections
        for (final ARXNode node : lattice) {
            drawConnections(node, g);
        }

        // Set style
        g.setStroke(new BasicStroke(strokeWidthNode));

        // Draw nodes
        for (final ARXNode node : lattice) {
            drawNode(node, g);
        }
    }

    /**
     * Draws the connections
     * 
     * @param node
     * @param g
     */
    private void drawConnections(final ARXNode node, final Graphics2D g) {

        // Obtain coordinates
        final Bounds center = (Bounds) node.getAttributes().get(ATTRIBUTE_BOUNDS);

        // Draw
        for (final ARXNode n : node.getSuccessors()) {
            if ((Boolean) n.getAttributes().get(ATTRIBUTE_VISIBLE)) {
                final Bounds centerN = (Bounds) n.getAttributes().get(ATTRIBUTE_BOUNDS);
                g.drawLine((int) center.centerX, (int) center.centerY, (int) centerN.centerX, (int) centerN.centerY);
            }
        }
    }

    /**
     * Draws a node
     * 
     * @param node
     * @param g
     */
    private boolean drawNode(final ARXNode node, final Graphics2D g) {

        // Obtain coordinates
        final Bounds dbounds = (Bounds) node.getAttributes().get(ATTRIBUTE_BOUNDS);
        final Rectangle bounds = new Rectangle((int) dbounds.x, (int) dbounds.y, (int) nodeWidth, (int) nodeHeight);

        // Clipping
        if (!bounds.intersects(new Rectangle(0, 0, screen.width, screen.height))) { return false; }

        // Degrade if too far away
        if (bounds.width <= 4) {
            g.setColor(getInnerColor(node));
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

            // Draw real node
        } else {
            final Color c = g.getColor();
            g.setColor(getInnerColor(node));
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            if (node != selectedNode) {
                g.fillOval(bounds.x, bounds.y, bounds.width, bounds.height);
            } else {
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
            final Stroke s = g.getStroke();
            g.setStroke(new BasicStroke(getOuterStrokeWidth(node, bounds.width)));
            g.setColor(getOuterColor(node));
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (node != selectedNode) {
                g.drawOval(bounds.x, bounds.y, bounds.width, bounds.height);
            } else {
                g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
            g.setStroke(s);
            g.setColor(c);

            if (bounds.width >= 20) {
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                final String text = (String) node.getAttributes().get(ATTRIBUTE_LABEL);
                g.setFont(new Font("Arial", Font.PLAIN, 8)); //$NON-NLS-1$
                final float factor1 = (bounds.width * 0.7f) / g.getFontMetrics().stringWidth(text);
                final float factor2 = (bounds.height * 0.7f) / g.getFontMetrics().getHeight();
                final float factor = Math.min(factor1, factor2);
                g.setFont(g.getFont().deriveFont(AffineTransform.getScaleInstance(factor, factor)));

                // Scale text to fit and center in r
                centerText(text, g, bounds.x, bounds.y, bounds.width, bounds.height);
            }
        }
        return true;
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
                return AWT_YELLOW;
            } else {
                return AWT_GREEN;
            }
        } else if (node.isAnonymous() == Anonymity.PROBABLY_ANONYMOUS) {
            return AWT_LIGHT_GREEN;
        } else if (node.isAnonymous() == Anonymity.PROBABLY_NOT_ANONYMOUS) {
            return AWT_LIGHT_RED;
        } else {
            return AWT_RED;
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
            return AWT_BLUE;
        } else {
            return Color.BLACK;
        }
    }

    /**
     * Returns the outer stroke width
     * 
     * @param node
     * @param width
     * @return
     */
    private float getOuterStrokeWidth(final ARXNode node, final int width) {
        if (node.isChecked()) {
            if (width < 20) {
                return 1.2f;
            } else if (width < 60) {
                return 1.5f;
            } else {
                return 3f;
            }
        } else {
            return 1f;
        }
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

        // Now initialize the data structures
        this.lattice.clear();
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
        screen = getSize();

        // Obtain optimal width and height per node
        double width = NODE_INITIAL_SIZE;
        double height = width * NODE_SIZE_RATIO;
        if ((height * latticeHeight) > screen.height) {
            final double factor = screen.height / (height * latticeHeight);
            height *= factor;
            width *= factor;
        }
        if ((width * latticeWidth) > screen.width) {
            final double factor = screen.width / (width * latticeWidth);
            height *= factor;
            width *= factor;
        }
        nodeWidth = width * NODE_FRAME_RATIO;
        nodeHeight = height * NODE_FRAME_RATIO;

        // Compute deltas to center the lattice
        final double deltaY = (screen.height - (height * latticeHeight)) / 2d;
        final double deltaX = (screen.width - (width * latticeWidth)) / 2d;

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

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(final MouseEvent arg0) {
                if (arg0.getButton() == MouseEvent.BUTTON1) {
                    final ARXNode node = getNode(arg0.getX(), arg0.getY());
                    if (node != null) {
                        actionButtonClicked1(node);
                    }
                } else if (arg0.getButton() == MouseEvent.BUTTON3) {
                    final ARXNode node = getNode(arg0.getX(), arg0.getY());
                    if (node != null) {
                        actionButtonClicked3(node, arg0.getXOnScreen(), arg0.getYOnScreen());
                    }
                }
            }

            @Override
            public void mousePressed(final MouseEvent arg0) {
                dragX = arg0.getX();
                dragY = arg0.getY();
                dragStartX = arg0.getX();
                dragStartY = arg0.getY();
                if (dragType == DragType.NONE) {
                    if (arg0.getButton() == MouseEvent.BUTTON1) {
                        dragType = DragType.MOVE;
                    } else if (arg0.getButton() == MouseEvent.BUTTON3) {
                        dragType = DragType.ZOOM;
                    }
                }
            }

            @Override
            public void mouseReleased(final MouseEvent arg0) {
                dragType = DragType.NONE;
            }

            @Override
            public void mouseExited(MouseEvent arg0) {
                tooltipX = -1;
                tooltipY = -1;
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseDragged(final MouseEvent arg0) {
                final int deltaX = arg0.getX() - dragX;
                final int deltaY = arg0.getY() - dragY;
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
                    double zoom = -((double) deltaY / (double) screen.height) * ZOOM_SPEED;
                    final double newWidth = nodeWidth + (zoom * nodeWidth);
                    if (newWidth > screen.width) {
                        zoom = (screen.width - nodeWidth) / nodeWidth;
                    }
                    if (newWidth < MIN_WIDTH) {
                        zoom = (MIN_WIDTH - nodeWidth) / nodeWidth;
                    }
                    final double newHeight = nodeHeight + (zoom * nodeHeight);
                    if (newHeight > screen.height) {
                        zoom = (screen.height - nodeHeight) / nodeHeight;
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
                repaint();
            }

            @Override
            public void mouseMoved(final MouseEvent e) {
                tooltipX = e.getX();
                tooltipY = e.getY();
                tooltipTimer.restart();
            }
        });
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent arg0) {
                resetBuffer();
                screen = getSize();
                initializeCanvas();
                repaint();
            }

            @Override
            public void componentShown(final ComponentEvent arg0) {
                resetBuffer();
                screen = getSize();
                repaint();
            }
        });
    }

    /**
     * Called when button 3 is clicked on a node
     * @param node
     * @param x
     * @param y
     */
    private void actionButtonClicked3(ARXNode node, final int x, final int y) {
        selectedNode = node;
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                model.setSelectedNode(selectedNode);
                controller.update(new ModelEvent(ViewLattice.this, ModelPart.SELECTED_NODE, selectedNode));
                repaint();
                controller.getContextMenu().show(menu, x, y);
            }
        });
    }

    /**
     * Called when button 1 is clicked on a node
     * @param node
     */
    private void actionButtonClicked1(ARXNode node) {
        selectedNode = node;
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                model.setSelectedNode(selectedNode);
                controller.update(new ModelEvent(ViewLattice.this, ModelPart.SELECTED_NODE, selectedNode));
            }
        });
        repaint();
    }

    /**
     * Resets the buffer
     */
    private void resetBuffer() {
        buffer = new BufferedImage(Math.max(1, getWidth()), Math.max(1, getHeight()), BufferedImage.TYPE_INT_RGB);
    }
}
