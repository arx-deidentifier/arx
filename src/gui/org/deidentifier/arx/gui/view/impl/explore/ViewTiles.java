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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.deidentifier.arx.ARXLattice;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.model.ModelNodeFilter;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.metric.InformationLoss;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

import cern.colt.Arrays;
import de.linearbits.tiles.DecoratorColor;
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
 * @author prasser
 */
public class ViewTiles implements IView {

    /** The controller. */
    private final Controller     controller;

    /** The tiles. */
    private final Tiles<ARXNode> tiles;

    /** The model. */
    private Model                model;

    /** The format. */
    private final NumberFormat   format = new DecimalFormat("##0.000"); //$NON-NLS-1$

    /**
     * Init.
     *
     * @param parent
     * @param controller
     */
    public ViewTiles(final Composite parent, final Controller controller) {

        // Listen
        controller.addListener(ModelPart.SELECTED_NODE, this);
        controller.addListener(ModelPart.FILTER, this);
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.RESULT, this);

        this.controller = controller;

        tiles = new Tiles<ARXNode>(parent, SWT.NONE);
        tiles.setLayoutData(SWTUtil.createFillGridData());

        // Set layout
        tiles.setTileLayout(new TileLayoutDynamic(10, 20, 5, 5));
        tiles.setComparator(new Comparator<ARXNode>() {
            public int compare(ARXNode o1, ARXNode o2) {
                int c1 = o1.getMinimumInformationLoss().compareTo(o2.getMinimumInformationLoss());
                return c1 != 0 ? c1 : o1.getMaximumInformationLoss().compareTo(o2.getMaximumInformationLoss());
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
        tiles.setDecoratorTooltip(new DecoratorString<ARXNode>() {
            @Override
            public String decorate(ARXNode node) {
                final StringBuffer b = new StringBuffer();
                b.append(Resources.getMessage("LatticeView.1")); //$NON-NLS-1$
                b.append(format.format(asRelativeValue(node.getMinimumInformationLoss())));
                b.append(" - "); //$NON-NLS-1$
                b.append(format.format(asRelativeValue(node.getMaximumInformationLoss())));
                b.append(" [%]\n"); //$NON-NLS-1$
                if (model.getOutputDefinition() != null) {
                    for (final String qi : node.getQuasiIdentifyingAttributes()) {

                        // Determine height of hierarchy
                        int height = model.getOutputDefinition().isHierarchyAvailable(qi) ?
                                model.getOutputDefinition().getHierarchy(qi)[0].length : 0;
                        b.append(" * "); //$NON-NLS-1$
                        b.append(qi);
                        b.append(": "); //$NON-NLS-1$
                        b.append(format.format(asRelativeValue(node.getGeneralization(qi), height - 1)));
                        b.append(" [%]\n"); //$NON-NLS-1$
                    }
                }
                b.setLength(b.length() - 1);
                return b.toString();

            }
        });
        tiles.update();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.arx.gui.view.def.IView#dispose()
     */
    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    /**
     * Resets the view.
     */
    @Override
    public void reset() {
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

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.arx.gui.view.def.IView#update(org.deidentifier.arx.gui.model.ModelEvent)
     */
    @Override
    public void update(final ModelEvent event) {

        if (event.part == ModelPart.RESULT) {
            if (model.getResult() == null) {
                reset();
            } else {
                updateLattice(model.getResult().getLattice());
            }
        } else if (event.part == ModelPart.SELECTED_NODE) {
            // selectedNode = (ARXNode) event.data;
        } else if (event.part == ModelPart.MODEL) {
            reset();
            model = (Model) event.data;
            if (model != null && model.getResult() != null) {
                updateLattice(model.getResult().getLattice());
                updateFilter(model.getResult().getLattice(), model.getNodeFilter());
            }
        } else if (event.part == ModelPart.FILTER) {
            if (model != null && model.getResult() != null) {
                updateFilter(model.getResult().getLattice(), (ModelNodeFilter) event.data);
            } else {
                reset();
            }
        }
    }

    /**
     * Converts an information loss into a relative value in percent.
     *
     * @param infoLoss
     * @return
     */
    private double asRelativeValue(final InformationLoss<?> infoLoss) {
        if (model != null && model.getResult() != null && model.getResult().getLattice() != null &&
            model.getResult().getLattice().getBottom() != null && model.getResult().getLattice().getTop() != null) {
            return infoLoss.relativeTo(model.getResult().getLattice().getMinimumInformationLoss(),
                                       model.getResult().getLattice().getMaximumInformationLoss()) * 100d;
        } else {
            return 0;
        }
    }

    /**
     * Converts a generalization to a relative value.
     *
     * @param generalization
     * @param max
     * @return
     */
    private double asRelativeValue(final int generalization, final int max) {
        if (model != null && model.getResult() != null && model.getResult().getLattice() != null &&
            model.getResult().getLattice().getBottom() != null && model.getResult().getLattice().getTop() != null) {
            return ((double) generalization / (double) max) * 100d;
        } else {
            return 0;
        }

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
                switch (element.getAnonymity()) {
                case NOT_ANONYMOUS:
                case UNKNOWN:
                case PROBABLY_NOT_ANONYMOUS:
                    return gray;
                default:
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
     * Updates the filter
     *
     * @param lattice
     * @param filter
     */
    private void updateFilter(final ARXLattice lattice, final ModelNodeFilter filter) {

        if (filter == null) return;

        final ModelNodeFilter filterClone = filter.clone();
        controller.getResources().getDisplay().asyncExec(new Runnable() {
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

        controller.getResources().getDisplay().asyncExec(new Runnable() {

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
}
