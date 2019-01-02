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
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;

/**
 * Instances of this class provide a separator with a title and/or an image.
 * <p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>BORDER</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * </p>
 */
public class ComponentTitledSeparator extends Composite {

        private int alignment;
        private Image image;
        private String text;

        /**
         * Constructs a new instance of this class given its parent and a style
         * value describing its behavior and appearance.
         * <p>
         * The style value is either one of the style constants defined in class
         * <code>SWT</code> which is applicable to instances of this class, or must
         * be built by <em>bitwise OR</em>'ing together (that is, using the
         * <code>int</code> "|" operator) two or more of those <code>SWT</code>
         * style constants. The class description lists the style constants that are
         * applicable to the class. Style bits are also inherited from superclasses.
         * </p>
         * 
         * @param parent a composite control which will be the parent of the new
         *            instance (cannot be null)
         * @param style the style of control to construct
         * 
         * @exception IllegalArgumentException <ul>
         *                <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
         *                </ul>
         * @exception SWTException <ul>
         *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
         *                thread that created the parent</li>
         *                </ul>
         * 
         */
        public ComponentTitledSeparator(final Composite parent, final int style) {
                super(parent, style);
                this.alignment = SWT.LEFT;

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

                this.addListener(SWT.Resize, new Listener() {
                        @Override
                        public void handleEvent(final Event event) {
                                redrawComposite();
                        }
                });

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
        }

        /**
         * Returns a value which describes the position of the text or image in the
         * receiver. The value will be one of <code>LEFT</code>, <code>RIGHT</code>
         * or <code>CENTER</code>.
         * 
         * @return the alignment
         * 
         * @exception SWTException <ul>
         *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
         *                disposed</li>
         *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
         *                thread that created the receiver</li>
         *                </ul>
         */

        public int getAlignment() {
                checkWidget();
                return this.alignment;
        }

        /**
         * Returns the receiver's image if it has one, or null if it does not.
         * 
         * @return the receiver's image
         * 
         * @exception SWTException <ul>
         *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
         *                disposed</li>
         *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
         *                thread that created the receiver</li>
         *                </ul>
         */
        public Image getImage() {
                checkWidget();
                return this.image;
        }

        /**
         * Returns the receiver's text.
         * 
         * @return the receiver's text
         * 
         * @exception SWTException <ul>
         *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
         *                disposed</li>
         *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
         *                thread that created the receiver</li>
         *                </ul>
         */
        public String getText() {
                checkWidget();
                return this.text;
        }

        /**
         * Controls how text will be displayed in the receiver. The argument should
         * be one of <code>LEFT</code>, <code>RIGHT</code> or <code>CENTER</code>.
         * 
         * @param alignment the new alignment
         * 
         * @exception SWTException <ul>
         *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
         *                disposed</li>
         *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
         *                thread that created the receiver</li>
         *                </ul>
         */
        public void setAlignment(final int alignment) {
                checkWidget();
                this.alignment = alignment;
        }

        /**
         * Sets the receiver's image to the argument, which may be null indicating
         * that no image should be displayed.
         * 
         * @param image the image to display on the receiver (may be null)
         * 
         * @exception IllegalArgumentException <ul>
         *                <li>ERROR_INVALID_ARGUMENT - if the image has been
         *                disposed</li>
         *                </ul>
         * @exception SWTException <ul>
         *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
         *                disposed</li>
         *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
         *                thread that created the receiver</li>
         *                </ul>
         */
        public void setImage(final Image image) {
                checkWidget();
                this.image = image;
        }

        /**
         * @see org.eclipse.swt.widgets.Composite#setLayout(org.eclipse.swt.widgets.Layout)
         */
        @Override
        public void setLayout(final Layout layout) {
                throw new UnsupportedOperationException("Not supported");
        }

        /**
         * Sets the receiver's text.
         * 
         * @param string the new text
         * 
         * @exception IllegalArgumentException <ul>
         *                <li>ERROR_NULL_ARGUMENT - if the text is null</li>
         *                </ul>
         * @exception SWTException <ul>
         *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
         *                disposed</li>
         *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
         *                thread that created the receiver</li>
         *                </ul>
         */
        public void setText(final String text) {
                checkWidget();
                this.text = text;
        }

        /**
         * Create the content
         */
        private void createContent() {
                switch (this.alignment) {
                        case SWT.CENTER:
                                createSeparator();
                                createTitle();
                                createSeparator();
                                break;
                        case SWT.LEFT:
                                createTitle();
                                createSeparator();
                                break;
                        default:
                                createSeparator();
                                createTitle();
                                break;
                }
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

        /**
         * Redraw the composite
         */
        private void redrawComposite() {
                // Dispose previous content
                for (final Control c : this.getChildren()) {
                        c.dispose();
                }

                int numberOfColumns = 1;

                if (this.text != null) {
                        numberOfColumns++;
                }

                if (this.image != null) {
                        numberOfColumns++;
                }

                if (this.alignment == SWT.CENTER) {
                        numberOfColumns++;
                }

                super.setLayout(new GridLayout(numberOfColumns, false));
                createContent();
        }

}