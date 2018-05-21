/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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
package org.deidentifier.arx.gui.view.impl.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.Accessible;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleControlAdapter;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * A label that can display animated GIFs. 
 * Adapted from https://stackoverflow.com/questions/6896632/is-there-a-custom-label-widget-which-supports-animated-gif
 */
public class ComponentStatusLabel extends Canvas {

    /** Gap between icon and text. */
    private static final int                     GAP            = 5;

    /** Left and right margins. */
    private static final int                     DEFAULT_MARGIN = 3;

    /** A string inserted in the middle of text that has been shortened. */
    private static final String                  ELLIPSIS       = "..."; //$NON-NLS-1$

    /** The alignment. Either CENTER, RIGHT, LEFT. Default is LEFT */
    private int                                  align          = SWT.LEFT;

    /** Field */
    private int                                  leftMargin     = DEFAULT_MARGIN;

    /** Field */
    private int                                  topMargin      = DEFAULT_MARGIN;

    /** Field */
    private int                                  rightMargin    = DEFAULT_MARGIN;

    /** Field */
    private int                                  bottomMargin   = DEFAULT_MARGIN;

    /** Field */
    private String                               text;

    /** Field */
    private Image                                image;

    /** Field */
    private String                               appToolTipText;

    /** Field */
    private ComponentStatusLabelProgressProvider progressProvider;

    /** Field */
    private boolean                              ignoreDispose;

    /** Field */
    private Image                                backgroundImage;

    /** Field */
    private Color[]                              gradientColors;

    /** Field */
    private int[]                                gradientPercents;

    /** Field */
    private boolean                              gradientVertical;

    /** Field */
    private Color                                background;

    /** Field */
    private ComponentStatusLabelGIFHandler       thread         = null;

    /** Field */
    private static int                           DRAW_FLAGS     = SWT.DRAW_MNEMONIC | SWT.DRAW_TAB | SWT.DRAW_TRANSPARENT |
                                                                  SWT.DRAW_DELIMITER;

    /**
     * Checkstyle method.
     *
     * @param style
     * @return
     */
    private static int checkStyle(int style) {
        if ((style & SWT.BORDER) != 0) style |= SWT.SHADOW_IN;
        int mask = SWT.SHADOW_IN | SWT.SHADOW_OUT | SWT.SHADOW_NONE | SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT;
        style = style & mask;
        return style |= SWT.NO_FOCUS | SWT.DOUBLE_BUFFERED;
    }
    
    /**
     * Creates a new instance
     *
     * @param parent
     * @param style
     */
    public ComponentStatusLabel(Composite parent, int style) {
        super(parent, checkStyle(style));
        if ((style & (SWT.CENTER | SWT.RIGHT)) == 0) style |= SWT.LEFT;
        if ((style & SWT.CENTER) != 0) align = SWT.CENTER;
        if ((style & SWT.RIGHT) != 0) align = SWT.RIGHT;
        if ((style & SWT.LEFT) != 0) align = SWT.LEFT;

        addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent event) {
                onPaint(event);
            }
        });

        addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent event) {
                if (event.detail == SWT.TRAVERSE_MNEMONIC) {
                    onMnemonic(event);
                }
            }
        });

        addListener(SWT.Dispose, new Listener() {
            public void handleEvent(Event event) {
                onDispose(event);
            }
        });

        initAccessible();

    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        checkWidget();
        Point e = getTotalSize(image, text);
        if (wHint == SWT.DEFAULT) {
            e.x += leftMargin + rightMargin;
        } else {
            e.x = wHint;
        }
        if (hHint == SWT.DEFAULT) {
            e.y += topMargin + bottomMargin;
        } else {
            e.y = hHint;
        }
        return e;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    /**
     * Returns the alignment
     * @return
     */
    public int getAlignment() {
        return align;
    }

    /**
     * Returns a margin
     *
     * @return
     */
    public int getBottomMargin() {
        return bottomMargin;
    }

    /**
     * Returns the image
     *
     * @return
     */
    public Image getImage() {
        return image;
    }

    /**
     * Returns a margin
     *
     * @return
     */
    public int getLeftMargin() {
        return leftMargin;
    }

    /**
     * Returns a margin
     *
     * @return
     */
    public int getRightMargin() {
        return rightMargin;
    }

    @Override
    public int getStyle() {
        int style = super.getStyle();
        switch (align) {
        case SWT.RIGHT:
            style |= SWT.RIGHT;
            break;
        case SWT.CENTER:
            style |= SWT.CENTER;
            break;
        case SWT.LEFT:
            style |= SWT.LEFT;
            break;
        }
        return style;
    }

    /**
     * Returns the text
     *
     * @return
     */
    public String getText() {
        return text;
    }

    @Override
    public String getToolTipText() {
        checkWidget();
        return appToolTipText;
    }

    /**
     * Returns a margin
     *
     * @return
     */
    public int getTopMargin() {
        return topMargin;
    }

    /**
     * Sets the alignment
     *
     * @param align
     */
    public void setAlignment(int align) {
        checkWidget();
        if (align != SWT.LEFT && align != SWT.RIGHT && align != SWT.CENTER) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        if (this.align != align) {
            this.align = align;
            redraw();
        }
    }

    @Override
    public void setBackground(Color color) {
        super.setBackground(color);
        // Are these settings the same as before?
        if (backgroundImage == null && gradientColors == null && gradientPercents == null) {
            if (color == null) {
                if (background == null) return;
            } else {
                if (color.equals(background)) return;
            }
        }
        background = color;
        backgroundImage = null;
        gradientColors = null;
        gradientPercents = null;
        redraw();
    }
    
    /**
     * Sets an image 
     *
     * @param image
     */
    public void setBackground(Image image) {
        checkWidget();
        if (image == backgroundImage) return;
        if (image != null) {
            gradientColors = null;
            gradientPercents = null;
        }
        backgroundImage = image;
        redraw();

    }

    /**
     * Sets a margin
     *
     * @param bottomMargin
     */
    public void setBottomMargin(int bottomMargin) {
        checkWidget();
        if (this.bottomMargin == bottomMargin || bottomMargin < 0) return;
        this.bottomMargin = bottomMargin;
        redraw();
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        redraw();
    }

    /**
     * Sets the GIF
     *
     * @param inputStream
     */
    public void setGIF(InputStream inputStream) {
        checkWidget();
        if (thread != null) {
            thread.stop();
            this.getDisplay().timerExec(-1, thread);
        }

        ImageLoader loader = new ImageLoader();

        try {
            loader.load(inputStream);
        } catch (Exception e) {
            this.image = null;
            return;
        }

        if (loader.data[0] != null) this.image = new Image(this.getDisplay(), loader.data[0]);

        if (loader.data.length > 1) {
            thread = new ComponentStatusLabelGIFHandler(this, loader);
            thread.run();
        }

        redraw();
    }

    /**
     * Sets the GIF
     *
     * @param path
     */
    public void setGIF(String path) {
        try {
            this.setGIF(new FileInputStream(new File(path)));
        } catch (FileNotFoundException e) {
            this.image = null;
            return;
        }
    }
    
    /**
     * Sets the image
     *
     * @param image
     */
    public void setImage(Image image) {
        checkWidget();
        if (thread != null) {
            thread.stop();
            getDisplay().timerExec(-1, thread);
        }

        if (image != this.image) {
            this.image = image;
            redraw();
        }
    }

    /**
     * Sets a margin
     *
     * @param leftMargin
     */
    public void setLeftMargin(int leftMargin) {
        checkWidget();
        if (this.leftMargin == leftMargin || leftMargin < 0) return;
        this.leftMargin = leftMargin;
        redraw();
    }

    /**
     * Sets a progress provider, if any
     * @param provider
     */
    public void setProgressProvider(ComponentStatusLabelProgressProvider provider) {
        this.progressProvider = provider;
    }

    /**
     * Sets a margin
     *
     * @param rightMargin
     */
    public void setRightMargin(int rightMargin) {
        checkWidget();
        if (this.rightMargin == rightMargin || rightMargin < 0) return;
        this.rightMargin = rightMargin;
        redraw();
    }

    /**
     * Sets the text
     *
     * @param text
     */
    public void setText(String text) {
        checkWidget();
        if (text == null) text = ""; //$NON-NLS-1$
        if (!text.equals(this.text)) {
            this.text = text;
            redraw();
        }
    }

    @Override
    public void setToolTipText(String string) {
        super.setToolTipText(string);
        appToolTipText = super.getToolTipText();
    }

    /**
     * Sets a margin
     *
     * @param topMargin
     */
    public void setTopMargin(int topMargin) {
        checkWidget();
        if (this.topMargin == topMargin || topMargin < 0) return;
        this.topMargin = topMargin;
        redraw();
    }

    /**
     * Updates the image
     * @param image
     */
    public void updateImage(Image image) {
        this.image = image;
        this.redraw();
    }

    /**
     * Drawing method 
     *
     * @param gc
     * @param x
     * @param y
     * @param w
     * @param h
     * @param topleft
     * @param bottomright
     */
    private void drawBevelRect(GC gc, int x, int y, int w, int h, Color topleft, Color bottomright) {
        gc.setForeground(bottomright);
        gc.drawLine(x + w, y, x + w, y + h);
        gc.drawLine(x, y + h, x + w, y + h);

        gc.setForeground(topleft);
        gc.drawLine(x, y, x + w - 1, y);
        gc.drawLine(x, y, x, y + h - 1);
    }

    /**
     * Calculate size
     *
     * @param image
     * @param text
     * @return
     */
    private Point getTotalSize(Image image, String text) {
        Point size = new Point(0, 0);

        if (image != null) {
            Rectangle r = image.getBounds();
            size.x += r.width;
            size.y += r.height;
        }

        GC gc = new GC(this);
        if (text != null && text.length() > 0) {
            Point e = gc.textExtent(text, DRAW_FLAGS);
            size.x += e.x;
            size.y = Math.max(size.y, e.y);
            if (image != null) size.x += GAP;
        } else {
            size.y = Math.max(size.y, gc.getFontMetrics().getHeight());
        }
        gc.dispose();

        return size;
    }

    /**
     * Initialize accessibility
     */
    private void initAccessible() {
        Accessible accessible = getAccessible();
        accessible.addAccessibleListener(new AccessibleAdapter() {
            public void getHelp(AccessibleEvent e) {
                e.result = getToolTipText();
            }

            public void getKeyboardShortcut(AccessibleEvent e) {
                char mnemonic = _findMnemonic(ComponentStatusLabel.this.text);
                if (mnemonic != '\0') {
                    e.result = "Alt+" + mnemonic; //$NON-NLS-1$
                }
            }

            public void getName(AccessibleEvent e) {
                e.result = getText();
            }
        });

        accessible.addAccessibleControlListener(new AccessibleControlAdapter() {
            public void getChildAtPoint(AccessibleControlEvent e) {
                e.childID = ACC.CHILDID_SELF;
            }

            public void getChildCount(AccessibleControlEvent e) {
                e.detail = 0;
            }

            public void getLocation(AccessibleControlEvent e) {
                Rectangle rect = getDisplay().map(getParent(), null, getBounds());
                e.x = rect.x;
                e.y = rect.y;
                e.width = rect.width;
                e.height = rect.height;
            }

            public void getRole(AccessibleControlEvent e) {
                e.detail = ACC.ROLE_LABEL;
            }

            public void getState(AccessibleControlEvent e) {
                e.detail = ACC.STATE_READONLY;
            }
        });
    }

    /**
     * Drawing method
     *
     * @param gc
     * @param r
     */
    private void paintBorder(GC gc, Rectangle r) {
        Display disp = getDisplay();

        Color c1 = null;
        Color c2 = null;

        int style = getStyle();
        if ((style & SWT.SHADOW_IN) != 0) {
            c1 = disp.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
            c2 = disp.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);
        }
        if ((style & SWT.SHADOW_OUT) != 0) {
            c1 = disp.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
            c2 = disp.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
        }

        if (c1 != null && c2 != null) {
            gc.setLineWidth(1);
            drawBevelRect(gc, r.x, r.y, r.width - 1, r.height - 1, c1, c2);
        }
    }

    /**
     * Split string
     *
     * @param text
     * @return
     */
    private String[] splitString(String text) {
        String[] lines = new String[1];
        int start = 0, pos;
        do {
            pos = text.indexOf('\n', start);
            if (pos == -1) {
                lines[lines.length - 1] = text.substring(start);
            } else {
                boolean crlf = (pos > 0) && (text.charAt(pos - 1) == '\r');
                lines[lines.length - 1] = text.substring(start, pos - (crlf ? 1 : 0));
                start = pos + 1;
                String[] newLines = new String[lines.length + 1];
                System.arraycopy(lines, 0, newLines, 0, lines.length);
                lines = newLines;
            }
        } while (pos != -1);
        return lines;
    }

    /**
     * Shorten text
     *
     * @param gc
     * @param t
     * @param width
     * @return
     */
    protected String shortenText(GC gc, String t, int width) {
        if (t == null) return null;
        int w = gc.textExtent(ELLIPSIS, DRAW_FLAGS).x;
        if (width <= w) return t;
        int l = t.length();
        int max = l / 2;
        int min = 0;
        int mid = (max + min) / 2 - 1;
        if (mid <= 0) return t;
        TextLayout layout = new TextLayout(getDisplay());
        layout.setText(t);
        mid = validateOffset(layout, mid);
        while (min < mid && mid < max) {
            String s1 = t.substring(0, mid);
            String s2 = t.substring(validateOffset(layout, l - mid), l);
            int l1 = gc.textExtent(s1, DRAW_FLAGS).x;
            int l2 = gc.textExtent(s2, DRAW_FLAGS).x;
            if (l1 + w + l2 > width) {
                max = mid;
                mid = validateOffset(layout, (max + min) / 2);
            } else if (l1 + w + l2 < width) {
                min = mid;
                mid = validateOffset(layout, (max + min) / 2);
            } else {
                min = max;
            }
        }
        String result = mid == 0 ? t : t.substring(0, mid) + ELLIPSIS + t.substring(validateOffset(layout, l - mid), l);
        layout.dispose();
        return result;
    }

    /**
     * Find mnemonic
     *
     * @param string
     * @return
     */
    char _findMnemonic(String string) {
        if (string == null) return '\0';
        int index = 0;
        int length = string.length();
        do {
            while (index < length && string.charAt(index) != '&')
                index++;
            if (++index >= length) return '\0';
            if (string.charAt(index) != '&') return Character.toLowerCase(string.charAt(index));
            index++;
        } while (index < length);
        return '\0';
    }

    /**
     * On dispose handler
     *
     * @param event
     */
    void onDispose(Event event) {
        /* make this handler run after other dispose listeners */
        if (ignoreDispose) {
            ignoreDispose = false;
            return;
        }
        ignoreDispose = true;
        notifyListeners(event.type, event);
        event.type = SWT.NONE;

        gradientColors = null;
        gradientPercents = null;
        backgroundImage = null;
        text = null;
        image = null;
        appToolTipText = null;
    }

    /**
     * On mnemonic handler 
     *
     * @param event
     */
    void onMnemonic(TraverseEvent event) {
        char mnemonic = _findMnemonic(text);
        if (mnemonic == '\0') return;
        if (Character.toLowerCase(event.character) != mnemonic) return;
        Composite control = this.getParent();
        while (control != null) {
            Control[] children = control.getChildren();
            int index = 0;
            while (index < children.length) {
                if (children[index] == this) break;
                index++;
            }
            index++;
            if (index < children.length) {
                if (children[index].setFocus()) {
                    event.doit = true;
                    event.detail = SWT.TRAVERSE_NONE;
                }
            }
            control = control.getParent();
        }
    }

    /**
     * On paint handler
     *
     * @param event
     */
    void onPaint(PaintEvent event) {
        Rectangle rect = getClientArea();
        if (rect.width == 0 || rect.height == 0) return;

        boolean shortenText = false;
        String t = text;
        Image img = image;
        int availableWidth = Math.max(0, rect.width - (leftMargin + rightMargin));
        Point extent = getTotalSize(img, t);
        if (extent.x > availableWidth) {
            img = null;
            extent = getTotalSize(img, t);
            if (extent.x > availableWidth) {
                shortenText = true;
            }
        }

        GC gc = event.gc;
        String[] lines = text == null ? null : splitString(text);

        // shorten the text
        if (shortenText) {
            extent.x = 0;
            for (int i = 0; i < lines.length; i++) {
                Point e = gc.textExtent(lines[i], DRAW_FLAGS);
                if (e.x > availableWidth) {
                    lines[i] = shortenText(gc, lines[i], availableWidth);
                    extent.x = Math.max(extent.x, getTotalSize(null, lines[i]).x);
                } else {
                    extent.x = Math.max(extent.x, e.x);
                }
            }
            if (appToolTipText == null) {
                super.setToolTipText(text);
            }
        } else {
            super.setToolTipText(appToolTipText);
        }

        // determine horizontal position
        int x = rect.x + leftMargin;
        if (align == SWT.CENTER) {
            x = (rect.width - extent.x) / 2;
        }
        if (align == SWT.RIGHT) {
            x = rect.width - rightMargin - extent.x;
        }

        // draw a background image behind the text
        try {
            if (backgroundImage != null) {
                // draw a background image behind the text
                Rectangle imageRect = backgroundImage.getBounds();
                // tile image to fill space
                gc.setBackground(getBackground());
                gc.fillRectangle(rect);
                int xPos = 0;
                while (xPos < rect.width) {
                    int yPos = 0;
                    while (yPos < rect.height) {
                        gc.drawImage(backgroundImage, xPos, yPos);
                        yPos += imageRect.height;
                    }
                    xPos += imageRect.width;
                }
            } else if (gradientColors != null) {
                // draw a gradient behind the text
                final Color oldBackground = gc.getBackground();
                if (gradientColors.length == 1) {
                    if (gradientColors[0] != null) gc.setBackground(gradientColors[0]);
                    gc.fillRectangle(0, 0, rect.width, rect.height);
                } else {
                    final Color oldForeground = gc.getForeground();
                    Color lastColor = gradientColors[0];
                    if (lastColor == null) lastColor = oldBackground;
                    int pos = 0;
                    for (int i = 0; i < gradientPercents.length; ++i) {
                        gc.setForeground(lastColor);
                        lastColor = gradientColors[i + 1];
                        if (lastColor == null) lastColor = oldBackground;
                        gc.setBackground(lastColor);
                        if (gradientVertical) {
                            final int gradientHeight = (gradientPercents[i] * rect.height / 100) - pos;
                            gc.fillGradientRectangle(0, pos, rect.width, gradientHeight, true);
                            pos += gradientHeight;
                        } else {
                            final int gradientWidth = (gradientPercents[i] * rect.width / 100) - pos;
                            gc.fillGradientRectangle(pos, 0, gradientWidth, rect.height, false);
                            pos += gradientWidth;
                        }
                    }
                    if (gradientVertical && pos < rect.height) {
                        gc.setBackground(getBackground());
                        gc.fillRectangle(0, pos, rect.width, rect.height - pos);
                    }
                    if (!gradientVertical && pos < rect.width) {
                        gc.setBackground(getBackground());
                        gc.fillRectangle(pos, 0, rect.width - pos, rect.height);
                    }
                    gc.setForeground(oldForeground);
                }
                gc.setBackground(oldBackground);
            } else {
                if (background != null || (getStyle() & SWT.DOUBLE_BUFFERED) == 0) {
                    gc.setBackground(getBackground());
                    gc.fillRectangle(rect);
                }
            }
        } catch (SWTException e) {
            if ((getStyle() & SWT.DOUBLE_BUFFERED) == 0) {
                gc.setBackground(getBackground());
                gc.fillRectangle(rect);
            }
        }

        // draw border
        int style = getStyle();
        if ((style & SWT.SHADOW_IN) != 0 || (style & SWT.SHADOW_OUT) != 0) {
            paintBorder(gc, rect);
        }

        Rectangle imageRect = null;
        int lineHeight = 0, textHeight = 0, imageHeight = 0;

        if (img != null) {
            imageRect = img.getBounds();
            imageHeight = imageRect.height;
        }
        if (lines != null) {
            lineHeight = gc.getFontMetrics().getHeight();
            textHeight = lines.length * lineHeight;
        }

        int imageY = 0, midPoint = 0, lineY = 0;
        if (imageHeight > textHeight) {
            if (topMargin == DEFAULT_MARGIN && bottomMargin == DEFAULT_MARGIN) imageY = rect.y +
                                                                                        (rect.height - imageHeight) / 2;
            else imageY = topMargin;
            midPoint = imageY + imageHeight / 2;
            lineY = midPoint - textHeight / 2;
        } else {
            if (topMargin == DEFAULT_MARGIN && bottomMargin == DEFAULT_MARGIN) lineY = rect.y +
                                                                                       (rect.height - textHeight) / 2;
            else lineY = topMargin;
            midPoint = lineY + textHeight / 2;
            imageY = midPoint - imageHeight / 2;
        }

        // draw the image
        if (img != null) {
            gc.drawImage(img, 0, 0, imageRect.width, imageHeight, x, imageY, imageRect.width, imageHeight);
            x += imageRect.width + GAP;
            extent.x -= imageRect.width + GAP;
        }

        // draw the text
        if (lines != null) {
            gc.setForeground(getForeground());
            for (int i = 0; i < lines.length; i++) {
                int lineX = x;
                if (lines.length > 1) {
                    if (align == SWT.CENTER) {
                        int lineWidth = gc.textExtent(lines[i], DRAW_FLAGS).x;
                        lineX = x + Math.max(0, (extent.x - lineWidth) / 2);
                    }
                    if (align == SWT.RIGHT) {
                        int lineWidth = gc.textExtent(lines[i], DRAW_FLAGS).x;
                        lineX = Math.max(x, rect.x + rect.width - rightMargin - lineWidth);
                    }
                }
                if (i == lines.length-1 && progressProvider != null && progressProvider.getProgress() != 0) {
                    gc.drawText(lines[i] + " (" + progressProvider.getProgress()+"%)", lineX, lineY, DRAW_FLAGS); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    gc.drawText(lines[i], lineX, lineY, DRAW_FLAGS);
                }
                lineY += lineHeight;
            }
        }
    }

    /**
     * Validate offset
     * 
     * @param layout
     * @param offset
     * @return
     */
    int validateOffset(TextLayout layout, int offset) {
        int nextOffset = layout.getNextOffset(offset, SWT.MOVEMENT_CLUSTER);
        if (nextOffset != offset) return layout.getPreviousOffset(nextOffset, SWT.MOVEMENT_CLUSTER);
        return offset;
    }
}
