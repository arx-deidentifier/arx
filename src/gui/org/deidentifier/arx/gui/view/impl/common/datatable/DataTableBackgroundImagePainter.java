package org.deidentifier.arx.gui.view.impl.common.datatable;

/*******************************************************************************
 * Copyright (c) 2012, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Fabian Prasser - Image scaling
 ******************************************************************************/

import static org.eclipse.nebula.widgets.nattable.util.ObjectUtils.isNotNull;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * Paints the cell background using an image. Image is repeated to cover the
 * background. Similar to HTML table painting.
 * 
 * @author Fabian Prasser
 */
public class DataTableBackgroundImagePainter extends CellPainterWrapper {

    /** Color */
    public final Color separatorColor;
    /** Image */
    private Image      bgImage;
    /** Image */
    private Image      scaledBgImage;

    /**
     * @param interiorPainter
     *            used for painting the cell contents
     * @param bgImage
     *            to be used for painting the background
     * @param separatorColor
     *            to be used for drawing left and right borders for the cell.
     *            Set to null if the borders are not required.
     */
    public DataTableBackgroundImagePainter(ICellPainter interiorPainter, Image bgImage, Color separatorColor) {
        super(interiorPainter);
        this.bgImage = bgImage;
        this.scaledBgImage = bgImage;
        this.separatorColor = separatorColor;
    }

    @Override
    public int getPreferredHeight(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
        return super.getPreferredHeight(cell, gc, configRegistry) + 4;
    }

    @Override
    public int getPreferredWidth(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
        return super.getPreferredWidth(cell, gc, configRegistry) + 4;
    }

    @Override
    public void paintCell(ILayerCell cell, GC gc, Rectangle rectangle, IConfigRegistry configRegistry) {

        if (this.bgImage != null) {
            
            // Save GC settings
            Color originalBackground = gc.getBackground();
            Color originalForeground = gc.getForeground();

            // Ugly hack
            Pattern pattern = new Pattern(Display.getCurrent(), getImage(rectangle.height));
            gc.setBackgroundPattern(pattern);

            gc.fillRectangle(rectangle);

            gc.setBackgroundPattern(null);
            pattern.dispose();

            if (isNotNull(this.separatorColor)) {
                gc.setForeground(this.separatorColor);
                gc.drawLine(
                        rectangle.x - 1,
                        rectangle.y,
                        rectangle.x - 1,
                        rectangle.y + rectangle.height);
                gc.drawLine(
                        rectangle.x - 1 + rectangle.width,
                        rectangle.y,
                        rectangle.x - 1 + rectangle.width,
                        rectangle.y + rectangle.height);
            }

            // Restore original GC settings
            gc.setBackground(originalBackground);
            gc.setForeground(originalForeground);
        }

        // Draw interior
        Rectangle interiorBounds = new Rectangle(
                rectangle.x + 2,
                rectangle.y + 2,
                rectangle.width - 4,
                rectangle.height - 4);
        super.paintCell(cell, gc, interiorBounds, configRegistry);
    }

    /**
     * Returns a potentially scaled version of the background image
     * @param height
     */
    private Image getImage(int height) {
        if (this.scaledBgImage.getBounds().height != height) {
            int width = this.bgImage.getBounds().width;
            this.scaledBgImage = new Image(this.bgImage.getDevice(), this.bgImage.getImageData()
                                                                                 .scaledTo(width,
                                                                                           height));
        }
        return this.scaledBgImage;
    }
}
