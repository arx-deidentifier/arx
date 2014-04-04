/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.arx.gui.view.impl;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import org.deidentifier.arx.gui.view.impl.common.Popup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * This class implements a global tool tip
 * @author Fabian Prasser
 */
public class MainToolTip extends Popup{

    private static final int TIME   = 1000;

    private int              oldX;
    private int              oldY;
    private long             oldTime;

    private String           string = null;
    private MainContextMenu  popup  = null;
    
    private Text             label;

    /**
     * Creates a new instance
     * @param parent
     */
    public MainToolTip(final Shell parent) {      
        super(parent);
        new Timer(WAIT, new ActionListener(){
            @Override public void actionPerformed(ActionEvent e) {
                if (string != null && !isVisible() && !popup.isVisible()){
                    Point p = MouseInfo.getPointerInfo().getLocation();
                    if (p.x != oldX || p.y != oldY) {
                        oldX = p.x;
                        oldY = p.y;
                        oldTime = System.currentTimeMillis();
                    } else if (System.currentTimeMillis() - oldTime > TIME){
                         show(oldX, oldY);
                    }
                }
            }
        }).start();
    }

    /**
     * Sets the popup
     * @param popup
     */
    public void setPopUp(MainContextMenu popup){
        this.popup = popup;
    }

    @Override
    protected void prepareVisible(Shell shell) {
        label.setText(string);
        shell.pack();
    }

    @Override
    protected void prepare(Shell shell) {
        
        shell.setLayout(new FillLayout());
        shell.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        shell.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
        label = new Text(shell, SWT.MULTI);
        label.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        label.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
        shell.pack();
    }

    /**
     * Sets the options displayed by the popup
     * 
     * @param items
     * @param listener
     */
    public void show(final String text) {
        if (super.isVisible()) return;
        this.string = text;
    }
    
    /**
     * Hides the tooltip
     */
    public void unshow(){
        if (super.isVisible()) return;
        this.string = null;
    }
    
    @Override
    public boolean isVisible() {
        return super.isVisible();
    }
    
    @Override
    public void hide() {
        super.hide();
    }
}
