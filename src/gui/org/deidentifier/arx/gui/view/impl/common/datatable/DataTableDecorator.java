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
package org.deidentifier.arx.gui.view.impl.common.datatable;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.CellStyleUtil;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * A table decorator. Based on code from Dirk Fauth.
 * @author Dirk Fauth
 * @author Fabian Prasser
 */
public class DataTableDecorator extends CellPainterWrapper {

    /**
     * Label for adding a border at the top of a cell.
     */
    public static final String TOP_LINE_BORDER_LABEL    = "topLineBorderLabel";   //$NON-NLS-1$
    /**
     * Label for adding a border at the bottom of a cell.
     */
    public static final String BOTTOM_LINE_BORDER_LABEL = "bottomLineBorderLabel"; //$NON-NLS-1$
    /**
     * Label for adding a border at the left of a cell.
     */
    public static final String LEFT_LINE_BORDER_LABEL   = "leftLineBorderLabel";  //$NON-NLS-1$
    /**
     * Label for adding a border at the right of a cell.
     */
    public static final String RIGHT_LINE_BORDER_LABEL  = "rightLineBorderLabel"; //$NON-NLS-1$

    /**
     * The default border style which will be used if no border style is
     * configured via cell style configuration. Can be <code>null</code> if
     * there should be no border rendered by default.
     */
    private final BorderStyle  defaultBorderStyle;

    /**
     * Creates a new LabelLineBorderDecorator wrapping the given interior
     * painter and no default border style.
     * 
     * @param interiorPainter
     *            The painter to be wrapped by this decorator.
     */
    public DataTableDecorator(ICellPainter interiorPainter) {
        this(interiorPainter, null);
    }

    /**
     * Creates a new LabelLineBorderDecorator wrapping the given interior
     * painter using the given BorderStyle as default.
     * 
     * @param interiorPainter
     *            The painter to be wrapped by this decorator.
     * @param defaultBorderStyle
     *            The BorderStyle to use as default if there is no BorderStyle
     *            configured via cell styles. Can be <code>null</code>.
     */
    public DataTableDecorator(ICellPainter interiorPainter, BorderStyle defaultBorderStyle) {
        super(interiorPainter);
        this.defaultBorderStyle = defaultBorderStyle;
    }

    /**
     * 
     *
     * @param cell
     * @param configRegistry
     * @return
     */
    private BorderStyle getBorderStyle(ILayerCell cell, IConfigRegistry configRegistry) {
        IStyle cellStyle = CellStyleUtil.getCellStyle(cell, configRegistry);
        BorderStyle borderStyle = cellStyle.getAttributeValue(CellStyleAttributes.BORDER_STYLE);
        if (borderStyle == null) {
            borderStyle = this.defaultBorderStyle;
        }
        return borderStyle;
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper#getPreferredHeight(org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell, org.eclipse.swt.graphics.GC, org.eclipse.nebula.widgets.nattable.config.IConfigRegistry)
     */
    @Override
    public int getPreferredHeight(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
        BorderStyle borderStyle = getBorderStyle(cell, configRegistry);
        int borderThickness = borderStyle != null ? borderStyle.getThickness() : 0;

        int borderLineCount = 0;
        // check how many border lines are configured for that cell
        List<String> labels = cell.getConfigLabels().getLabels();
        if (labels.contains(TOP_LINE_BORDER_LABEL)) borderLineCount++;
        if (labels.contains(BOTTOM_LINE_BORDER_LABEL)) borderLineCount++;

        return super.getPreferredHeight(cell, gc, configRegistry) + (borderThickness * borderLineCount);
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper#getPreferredWidth(org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell, org.eclipse.swt.graphics.GC, org.eclipse.nebula.widgets.nattable.config.IConfigRegistry)
     */
    @Override
    public int getPreferredWidth(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
        BorderStyle borderStyle = getBorderStyle(cell, configRegistry);
        int borderThickness = borderStyle != null ? borderStyle.getThickness() : 0;

        int borderLineCount = 0;
        // check how many border lines are configured for that cell
        List<String> labels = cell.getConfigLabels().getLabels();
        if (labels.contains(RIGHT_LINE_BORDER_LABEL)) borderLineCount++;
        if (labels.contains(LEFT_LINE_BORDER_LABEL)) borderLineCount++;

        return super.getPreferredWidth(cell, gc, configRegistry) + (borderThickness * borderLineCount);
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper#paintCell(org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell, org.eclipse.swt.graphics.GC, org.eclipse.swt.graphics.Rectangle, org.eclipse.nebula.widgets.nattable.config.IConfigRegistry)
     */
    @Override
    public void paintCell(ILayerCell cell, GC gc, Rectangle rectangle, IConfigRegistry configRegistry) {
        BorderStyle borderStyle = getBorderStyle(cell, configRegistry);
        int borderThickness = borderStyle != null ? borderStyle.getThickness() : 0;

        // check how many border lines are configured for that cell
        List<String> labels = cell.getConfigLabels().getLabels();

        int leftBorderThickness = 0;
        int rightBorderThickness = 0;
        int topBorderThickness = 0;
        int bottomBorderThickness = 0;

        if (labels.contains(LEFT_LINE_BORDER_LABEL)) leftBorderThickness = borderThickness;
        if (labels.contains(RIGHT_LINE_BORDER_LABEL)) rightBorderThickness = borderThickness;
        if (labels.contains(TOP_LINE_BORDER_LABEL)) topBorderThickness = borderThickness;
        if (labels.contains(BOTTOM_LINE_BORDER_LABEL)) bottomBorderThickness = borderThickness;

        Rectangle interiorBounds = new Rectangle(rectangle.x + leftBorderThickness,
                                                 rectangle.y + topBorderThickness,
                                                 (rectangle.width - leftBorderThickness - rightBorderThickness),
                                                 (rectangle.height - topBorderThickness - bottomBorderThickness));
        super.paintCell(cell, gc, interiorBounds, configRegistry);

        if (borderStyle == null ||
            borderThickness <= 0 ||
            (leftBorderThickness == 0 && rightBorderThickness == 0 && topBorderThickness == 0 && bottomBorderThickness == 0)) { return; }

        // Save GC settings
        Color originalForeground = gc.getForeground();
        int originalLineWidth = gc.getLineWidth();
        int originalLineStyle = gc.getLineStyle();

        gc.setLineWidth(borderThickness);

        Rectangle borderArea = new Rectangle(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        if (borderThickness >= 1) {
            int shift = 0;
            int correction = 0;

            if ((borderThickness % 2) == 0) {
                shift = borderThickness / 2;
            } else {
                shift = borderThickness / 2;
                correction = 1;
            }

            if (leftBorderThickness >= 1) {
                borderArea.x += shift;
                borderArea.width -= shift;
            }

            if (rightBorderThickness >= 1) {
                borderArea.width -= shift + correction;
            }

            if (topBorderThickness >= 1) {
                borderArea.y += shift;
                borderArea.height -= shift;
            }

            if (bottomBorderThickness >= 1) {
                borderArea.height -= shift + correction;
            }
        }

        gc.setLineStyle(LineStyleEnum.toSWT(borderStyle.getLineStyle()));
        gc.setForeground(borderStyle.getColor());

        // if all borders are set draw a rectangle
        if (leftBorderThickness > 0 && rightBorderThickness > 0 && topBorderThickness > 0 && bottomBorderThickness > 0) {
            gc.drawRectangle(borderArea);
        }
        // else draw a line for every set border
        else {
            Point topLeftPos = new Point(borderArea.x, borderArea.y);
            Point topRightPos = new Point(borderArea.x + borderArea.width, borderArea.y);
            Point bottomLeftPos = new Point(borderArea.x, borderArea.y + borderArea.height);
            Point bottomRightPos = new Point(borderArea.x + borderArea.width, borderArea.y + borderArea.height);

            if (leftBorderThickness > 0) {
                gc.drawLine(topLeftPos.x, topLeftPos.y, bottomLeftPos.x, bottomLeftPos.y);
            }
            if (rightBorderThickness > 0) {
                gc.drawLine(topRightPos.x, topRightPos.y, bottomRightPos.x, bottomRightPos.y);
            }
            if (topBorderThickness > 0) {
                gc.drawLine(topLeftPos.x, topLeftPos.y, topRightPos.x, topRightPos.y);
            }
            if (bottomBorderThickness > 0) {
                gc.drawLine(bottomLeftPos.x, bottomLeftPos.y, bottomRightPos.x, bottomRightPos.y);
            }
        }

        // Restore GC settings
        gc.setForeground(originalForeground);
        gc.setLineWidth(originalLineWidth);
        gc.setLineStyle(originalLineStyle);
    }

}
