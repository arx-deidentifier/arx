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
import de.linearbits.tiles.DecoratorColor;
import de.linearbits.tiles.DecoratorInteger;
import de.linearbits.tiles.DecoratorListener;
import de.linearbits.tiles.DecoratorString;
import de.linearbits.tiles.Filter;
import de.linearbits.tiles.Gradient;
import de.linearbits.tiles.GradientHeatscale;
import de.linearbits.tiles.TileLayoutDynamic;
import de.linearbits.tiles.Tiles;

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

        tiles = new Tiles<ARXNode>(parent, SWT.NONE);
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
                
                boolean unknown2 = o2.getMinimumInformationLoss().compareTo(o2.getMaximumInformationLoss())!=0 &&
                                   asRelativeValue(o2.getMinimumInformationLoss())==0d;
                
                boolean unknown1 = o1.getMinimumInformationLoss().compareTo(o1.getMaximumInformationLoss())!=0 &&
                        asRelativeValue(o1.getMinimumInformationLoss())==0d;
                
                if (unknown1 && unknown2) return 0;
                else if (unknown1 && !unknown2) return +1;
                else if (!unknown1 && unknown2) return -1;
                else {
                  int c1 = o1.getMinimumInformationLoss().compareTo(o2.getMinimumInformationLoss());
                  return c1 != 0 ? c1 : o1.getMaximumInformationLoss().compareTo(o2.getMaximumInformationLoss());
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
                return Arrays.toString(node.getTransformation());
            }
        });
        tiles.setDecoratorBackgroundColor(createDecoratorBackgroundColor());
        tiles.setDecoratorTooltip(super.getTooltipDecorator());
        tiles.setDecoratorLineColor(createDecoratorLineColor());
        tiles.setDecoratorLineWidth(createDecoratorLineWidth());
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

        final Gradient gradient = new GradientHeatscale(tiles);
        final Color gray = new Color(tiles.getDisplay(), 160, 160, 160);
        
        DecoratorColor<ARXNode> decorator = new DecoratorColor<ARXNode>() {

            @Override
            public Color decorate(ARXNode element) {
                if (element.getMinimumInformationLoss().compareTo(element.getMaximumInformationLoss())!=0 &&
                    asRelativeValue(element.getMinimumInformationLoss())==0d){
                    return gray;
                } else {
                    return gradient.getColor(asRelativeValue(element.getMinimumInformationLoss()) / 100d);
                }
            }
        };

        decorator.addDecoratorListener(new DecoratorListener() {
            @Override
            public void disposed() {
                gradient.dispose();
                gray.dispose();
            }
        });

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

                List<ARXNode> list = new ArrayList<ARXNode>();
                for (final ARXNode[] level : lattice.getLevels()) {
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
            updateFilter(result.getLattice(), filter);
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
