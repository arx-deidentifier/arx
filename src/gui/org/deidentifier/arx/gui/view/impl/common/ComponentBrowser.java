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
package org.deidentifier.arx.gui.view.impl.common;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ImageView;

import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Simple browser based on JEditorPane
 * 
 * @author Fabian Prasser
 *
 */
public class ComponentBrowser {

    /** The pane */
    private final JEditorPane pane;

    /** Root composite */
    private final Composite   root;

    /** Root composite */
    private final Label       label;

    /** History */
    private List<String>      history = new ArrayList<String>();

    /** Offset */
    private int               offset  = -1;
    
    /** View*/
    private ScrolledComposite scroller; 

    static class MyViewFactory extends HTMLEditorKit.HTMLFactory {
        @Override
        public View create(Element elem) {
            View view = super.create(elem);
            if (view instanceof ImageView) ((ImageView) view).setLoadsSynchronously(true);
            return view;
        }
    }

    static class MyHTMLEditorKit extends HTMLEditorKit {
        @Override
        public ViewFactory getViewFactory() {
            return new MyViewFactory();
        }
    }

    /**
     * Creates a new instance
     * @param parent
     */
    public ComponentBrowser(Composite parent) {
        
        root = new Composite(parent, SWT.NONE);
        root.addControlListener(new ControlAdapter(){
            public void controlResized(ControlEvent arg0) {
                render();
            }
        });

        root.setLayout(new FillLayout());
        scroller = new ScrolledComposite(root, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        scroller.setBackground(GUIHelper.COLOR_WHITE);
        label = new Label(scroller, SWT.NONE);
        label.setBackground(GUIHelper.COLOR_WHITE);
        label.addDisposeListener(new DisposeListener(){
            public void widgetDisposed(DisposeEvent arg0) {
                if (label.getImage() != null && !label.getImage().isDisposed()) {
                    label.getImage().dispose();
                }
            }
        });
        scroller.setContent(label);

        // Create pane
        pane = new JEditorPane() {
            /** SVUID*/
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D graphics2d = (Graphics2D) g;
                graphics2d.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
                super.paintComponent(graphics2d);
            }
        };
        
        HTMLEditorKit kit = new MyHTMLEditorKit();
        pane.setEditorKit(kit);
        pane.setDocument(kit.createDefaultDocument());
        
        pane.addPropertyChangeListener (
                                              new PropertyChangeListener ()
                                              {
                                                  public void propertyChange (PropertyChangeEvent e)
                                                  {
                                                      if (e.getPropertyName ().equals ("page"))
                                                      {
                                                          EventQueue.invokeLater (
                                                             new Runnable ()
                                                             {
                                                                 public void run ()
                                                                 {
                                                                     render();
                                                                 }
                                                              });
                                                      }
                                                  }
                                              });
        
        pane.setOpaque(false);
        pane.setBorder(null);
        pane.setEditable(false);
        pane.setDoubleBuffered(true);
        pane.setVisible(true);
    }
    
    private void render() {
        
        Point p = root.getSize();
        
        if (p.x <= 0 || p.y <= 0) {
            return;
        }
        
        pane.setSize(p.x, Integer.MAX_VALUE);
        final int width = p.x;
        final int height = (int)pane.getPreferredSize().getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        pane.paint(image.getGraphics());
        
        final ImageData swtData = convertToSWT(image);
        final org.eclipse.swt.graphics.Image swtImage = new org.eclipse.swt.graphics.Image(root.getDisplay(), swtData);
        
        root.getDisplay().asyncExec(new Runnable(){
            @Override
            public void run() {
                if (label.getImage() != null && !label.getImage().isDisposed()) {
                    label.getImage().dispose();
                }
                label.setImage(swtImage);
                label.setSize(width, height);
                scroller.layout();
            }
        });
    }

    public static ImageData convertToSWT(BufferedImage bufferedImage) {
        if (bufferedImage.getColorModel() instanceof DirectColorModel) {
            DirectColorModel colorModel = (DirectColorModel) bufferedImage.getColorModel();
            PaletteData palette = new PaletteData(
                colorModel.getRedMask(),
                colorModel.getGreenMask(),
                colorModel.getBlueMask()
            );
            ImageData data = new ImageData(
                bufferedImage.getWidth(),
                bufferedImage.getHeight(), colorModel.getPixelSize(),
                palette
            );
            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[3];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    raster.getPixel(x, y, pixelArray);
                    int pixel = palette.getPixel(
                        new RGB(pixelArray[0], pixelArray[1], pixelArray[2])
                    );
                    data.setPixel(x, y, pixel);
                }
            }
            return data;
        } else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
            IndexColorModel colorModel = (IndexColorModel) bufferedImage.getColorModel();
            int size = colorModel.getMapSize();
            byte[] reds = new byte[size];
            byte[] greens = new byte[size];
            byte[] blues = new byte[size];
            colorModel.getReds(reds);
            colorModel.getGreens(greens);
            colorModel.getBlues(blues);
            RGB[] rgbs = new RGB[size];
            for (int i = 0; i < rgbs.length; i++) {
                rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF, blues[i] & 0xFF);
            }
            PaletteData palette = new PaletteData(rgbs);
            ImageData data = new ImageData(
                bufferedImage.getWidth(),
                bufferedImage.getHeight(),
                colorModel.getPixelSize(),
                palette
            );
            data.transparentPixel = colorModel.getTransparentPixel();
            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[1];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    raster.getPixel(x, y, pixelArray);
                    data.setPixel(x, y, pixelArray[0]);
                }
            }
            return data;
        }
        return null;
    }
    
    /**
     * @param arg0
     * @see org.eclipse.swt.widgets.Control#setLayoutData(java.lang.Object)
     */
    public void setLayoutData(Object arg0) {
        root.setLayoutData(arg0);
    }

    /**
     * Sets to the given url
     * @param url
     * @throws IOException
     */
    public void setUrl(String url) throws IOException {
        String html = getHtml(url);
        history.add(html);
        offset = history.size()-1;
        pane.setText(history.get(offset));
        render();
    }
    
    /**
     * Fetch html from website
     * @param url
     * @return
     * @throws IOException
     */
    private String getHtml(String url) throws IOException {
        BufferedReader in = null;
        StringBuilder builder = null;
        try {
            in = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
            builder = new StringBuilder();
            String line;
            boolean body = false;
            while ((line = in.readLine()) != null) {
                
                if (!body && !line.startsWith("</head>")) {
                    continue;
                } else if (!body) {
                    body = true;
                    builder.append("<html>\n");
                    builder.append("<head>\n");
                    builder.append(getStyles());
                }
                
                builder.append(line).append("\n");
            }
            in.close();
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return builder.toString();
    }

    /**
     * Returns CSS styles
     * @return
     */
    private String getStyles() {
        
        Font font = pane.getFont();
        String fontFamily = font.getFamily();
        int fontSize = font.getSize();
        fontFamily = "Arial";
        fontSize = 12;
        
        StringBuilder styles = new StringBuilder();
        styles.append("<style type=\"text/css\">\n");
        styles.append("   body { background-color: #FFFFFF; font-family: " + fontFamily + "; " + "font-size: " + fontSize + "px; }\n");
        styles.append("   h1 { font-family: " + fontFamily + "; " + "font-size: " + (fontSize + 8) + "px; }\n");
        styles.append("   h2 { font-family: " + fontFamily + "; " + "font-size: " + (fontSize + 6) + "px; }\n");
        styles.append("   h3 { font-family: " + fontFamily + "; " + "font-size: " + (fontSize + 4) + "px; }\n");
        styles.append("   h4 { font-family: " + fontFamily + "; " + "font-size: " + (fontSize + 2) + "px; }\n");
        styles.append("</style>\n");
        
        return styles.toString();
    }
    
    public void forward() {
        if (offset < 0 || offset >= history.size() - 1) { 
            return; 
        }
        offset++;
        pane.setText(history.get(offset));
    }

    public void back() {
        if (offset <= 0 || offset > history.size() - 1) { 
            return; 
        }
        offset--;
        pane.setText(history.get(offset));
    }

    public boolean isBackEnabled() {
        return !(offset <= 0 || offset > history.size() - 1);
    }

    public boolean isForwardEnabled() {
        return !(offset < 0 || offset >= history.size() - 1);
    }
}
