/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.deidentifier.arx.ARXLattice;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.model.ModelNodeFilter;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import cern.colt.Arrays;
import de.linearbits.swt.tiles.DecoratorColor;
import de.linearbits.swt.tiles.DecoratorInteger;
import de.linearbits.swt.tiles.DecoratorString;
import de.linearbits.swt.tiles.Filter;
import de.linearbits.swt.tiles.TileLayoutDynamic;
import de.linearbits.swt.tiles.Tiles;

/**
 * This class implements a tiles view on selected nodes.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class ViewTiles extends ViewSolutionSpace {

    /** The tiles. */
    private final Tiles<ARXNode> tiles;
    /** Config */
    private static final int     NUM_COLUMNS = 10;
    /** Config */
    private static final int     NUM_ROWS    = 20;
    /** Config */
    private static final int     MARGIN      = 5;

    /**
     * Constructor
     *
     * @param parent
     * @param controller
     */
    public ViewTiles(final Composite parent, final Controller controller) {
        
        // Super class
        super(parent, controller);

        tiles = new Tiles<ARXNode>(super.getPrimaryComposite(), SWT.BORDER);
        tiles.setLayoutData(SWTUtil.createFillGridData());
        
        // Selection listener
        tiles.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent arg0) {
                ViewTiles.this.actionSelectNode(tiles.getSelectedItem());
            }
        });
        
        // Show menu
        tiles.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseUp(MouseEvent arg0) {
                if (arg0.button == 3) {
                    if (getSelectedNode() != null) {
                        Point display = tiles.toDisplay(arg0.x, arg0.y);
                        getModel().setSelectedNode(getSelectedNode());
                        controller.update(new ModelEvent(ViewTiles.this, 
                                                         ModelPart.SELECTED_NODE, getSelectedNode()));
                        actionShowMenu(display.x, display.y);
                    }
                }
            }       
            
        });

        // Set layout
        tiles.setTileLayout(new TileLayoutDynamic(NUM_COLUMNS, NUM_ROWS, MARGIN, MARGIN));
        tiles.setComparator(new Comparator<ARXNode>() {
            public int compare(ARXNode o1, ARXNode o2) {
                
                boolean unknown2 = o2.getLowestScore().compareTo(o2.getHighestScore())!=0 &&
                                   asRelativeValue(o2.getLowestScore())==0d;
                
                boolean unknown1 = o1.getLowestScore().compareTo(o1.getHighestScore())!=0 &&
                                   asRelativeValue(o1.getLowestScore())==0d;
                
                if (unknown1 && unknown2) return 0;
                else if (unknown1 && !unknown2) return +1;
                else if (!unknown1 && unknown2) return -1;
                else {
                    try {
                        int c1 = o1.getLowestScore().compareTo(o2.getLowestScore());
                        return c1 != 0 ? c1 : o1.getHighestScore().compareTo(o2.getHighestScore());
                    } catch (Exception e) {
                        return 0;
                    }
                }
            }
        });
        tiles.setFilter(new Filter<ARXNode>() {
            public boolean accepts(ARXNode arg0) {
                return true;
            }
        });
        tiles.setDecoratorLabel(new DecoratorString<ARXNode>() {
            @Override
            public String decorate(ARXNode node) {
                String text = Arrays.toString(node.getTransformation());
                text = text.substring(1, text.length() - 1);
                return trimLabel(text);
            }
        });
        tiles.setDecoratorBackgroundColor(createDecoratorBackgroundColor());
        tiles.setDecoratorTooltip(super.getTooltipDecorator());
        tiles.setDecoratorLineColor(createDecoratorLineColor());
        tiles.setDecoratorLineWidth(createDecoratorLineWidth());
        tiles.setBackground(tiles.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        tiles.update();
    }

    /**
     * Resets the view.
     */
    @Override
    public void reset() {
        super.reset();
        tiles.setRedraw(false);
        tiles.setItems(new ArrayList<ARXNode>());
        tiles.setRedraw(true);
        tiles.setFilter(new Filter<ARXNode>() {
            public boolean accepts(ARXNode arg0) {
                return true;
            }
        });
        tiles.update();
        SWTUtil.disable(tiles);
    }

    /**
     * Creates a background decorator
     * @return
     */
    private DecoratorColor<ARXNode> createDecoratorBackgroundColor() {

        DecoratorColor<ARXNode> decorator = new DecoratorColor<ARXNode>() {
            @Override
            public Color decorate(ARXNode element) {
                return ViewTiles.this.getUtilityColor(element);
            }
        };
        return decorator;
    }

    /**
     * Creates a decorator
     * @return
     */
    private DecoratorColor<ARXNode> createDecoratorLineColor() {
        return new DecoratorColor<ARXNode>() {
            @Override
            public Color decorate(ARXNode node) {
                return getInnerColor(node);
            }
        };
    }

    /**
     * Creates a decorator
     * @return
     */
    private DecoratorInteger<ARXNode> createDecoratorLineWidth() {
        return new DecoratorInteger<ARXNode>() {
            @Override
            public Integer decorate(ARXNode node) {
                return getOuterStrokeWidth(node, (tiles.getSize().x - NUM_COLUMNS * MARGIN) / NUM_COLUMNS);
            }
        };
    }

    /**
     * Updates the filter
     *
     * @param lattice
     * @param filter
     */
    private void updateFilter(final ARXLattice lattice, final ModelNodeFilter filter) {

        if (filter == null) return;

        final ModelNodeFilter filterClone = filter.clone();
        getController().getResources().getDisplay().asyncExec(new Runnable() {
            public void run() {
                tiles.setFilter(new Filter<ARXNode>() {
                    public boolean accepts(ARXNode node) {
                        return filterClone.isAllowed(lattice, node);
                    }
                });
                tiles.update();
            }
        });
    }

    /**
     * Updates the lattice
     *
     * @param lattice
     */
    private void updateLattice(final ARXLattice lattice) {

        if (lattice == null) {
            reset();
            return;
        }

        getController().getResources().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                SWTUtil.enable(tiles);
                tiles.setRedraw(true);
                
                final ARXLattice l = getModel().getProcessStatistics().isLocalTransformation() ? 
                                     getModel().getProcessStatistics().getLattice() : lattice;

                List<ARXNode> list = new ArrayList<ARXNode>();
                for (final ARXNode[] level : l.getLevels()) {
                    for (final ARXNode node : level) {
                        list.add(node);
                    }
                }

                // Set
                tiles.setItems(list);

                // Draw
                tiles.update();
            }
        });
    }
    
    @Override
    protected void actionRedraw() {
        this.tiles.redraw();
    }

    @Override
    protected void eventFilterChanged(ARXResult result, ModelNodeFilter filter) {
        if (getModel() != null && result != null) {
            ARXLattice lattice = getModel().getProcessStatistics().isLocalTransformation() ?
                                 getModel().getProcessStatistics().getLattice() : result.getLattice();
            updateFilter(lattice, filter);
        } else {
            reset();
        }
    }

    @Override
    protected void eventModelChanged() {
        if (getModel() != null && getModel().getResult() != null) {
            updateLattice(getModel().getResult().getLattice());
            updateFilter(getModel().getResult().getLattice(), getModel().getNodeFilter());
        }
    }

    @Override
    protected void eventNodeSelected() {
        tiles.setSelectedItem(super.getSelectedNode());
    }

    @Override
    protected void eventResultChanged(ARXResult result) {
        if (result == null) {
            reset();
        } else {
            updateLattice(result.getLattice());
        }
    }
}