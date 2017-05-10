package org.deidentifier.arx.gui.view.impl.common;

/*******************************************************************************
 * Copyright (c) 2011 Laurent CARON.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Laurent CARON (laurent.caron@gmail.com) - initial API and implementation
 *     Fabian Prasser (fabian.prasser@gmail.com) - Adapted for ARX
 *******************************************************************************/

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;

/**
 * Instances of this class provide a separator with a title and/or an image.
 * 
 */
public class ComponentTitledSeparator extends Composite {

    /** Image */
    private Image  image;
    /** Text */
    private String text;

    /**
     * Creates a new separator
     * @param parent
     * @param text
     */
    public ComponentTitledSeparator(final Composite parent, final String text) {
        this(parent, text, null);
    }

    /**
     * Creates a new separator
     * @param parent
     * @param text
     * @param image
     */
    public ComponentTitledSeparator(final Composite parent, final String text, final Image image) {
        super(parent, SWT.NONE);

        this.image = image;
        this.text = text;
        
        final Color originalColor = new Color(getDisplay(), 0, 88, 150);
        setForeground(originalColor);

        final Font originalFont;
        final FontData[] fontData = getFont().getFontData();
        if (fontData != null && fontData.length > 0) {
            final FontData fd = fontData[0];
            fd.setStyle(SWT.BOLD);
            originalFont = new Font(getDisplay(), fd);
            setFont(originalFont);
        } else {
            originalFont = null;
        }

        this.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent arg0) {
                if (originalColor != null && !originalColor.isDisposed()) {
                    originalColor.dispose();
                }
                if (originalFont != null && !originalFont.isDisposed()) {
                    originalFont.dispose();
                }
            }

        });
        
        int numberOfColumns = 1;

        if (this.text != null) {
            numberOfColumns++;
        }

        if (this.image != null) {
            numberOfColumns++;
        }

        super.setLayout(new GridLayout(numberOfColumns, false));
        createTitle();
        createSeparator();
    }

    @Override
    public void setLayout(final Layout layout) {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * @return a SWT label
     */
    private Label createLabel() {
        final Label label = new Label(this, SWT.NONE);
        label.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
        label.setFont(getFont());
        label.setForeground(getForeground());
        label.setBackground(getBackground());
        return label;
    }

    /**
     * Create a separator
     */
    private void createSeparator() {
        final Label separator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        separator.setBackground(getBackground());
    }

    /**
     * Create the title
     */
    private void createTitle() {
        if (this.image != null) {
            final Label imageLabel = createLabel();
            imageLabel.setImage(this.image);
        }

        if (this.text != null && !this.text.trim().equals("")) {
            final Label textLabel = createLabel();
            textLabel.setText(this.text);
        }
    }
}
