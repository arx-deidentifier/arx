/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.flash.gui.view.impl;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.flash.gui.SWTUtil;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class MainPopUp {

    private class ColorAdapter extends MouseTrackAdapter {
        @Override
        public void mouseEnter(final MouseEvent arg0) {
            final Label l = (Label) arg0.widget;
            l.setForeground(GUIHelper.COLOR_WHITE);
            l.setBackground(GUIHelper.COLOR_BLACK);
        }

        @Override
        public void mouseExit(final MouseEvent arg0) {
            final Label l = (Label) arg0.widget;
            l.setForeground(GUIHelper.COLOR_BLACK);
            l.setBackground(GUIHelper.COLOR_WHITE);
        }
    }

    private class HideAdapter extends MouseTrackAdapter {
        @Override
        public void mouseExit(final MouseEvent arg0) {
            final Point p = shell.getDisplay().getCursorLocation();
            final Rectangle bounds = shell.getBounds();
            if (!bounds.contains(p)) {
                hide();
            }
        }
    }

    private class SelectionAdapter extends MouseAdapter {
        @Override
        public void mouseDown(final MouseEvent arg0) {
            final Label l = (Label) arg0.widget;
            final Event e = new Event();
            e.data = l.getText();
            e.widget = shell;
            listener.widgetSelected(new SelectionEvent(e));
            hide();
        }
    }

    private final Shell            shell;
    private final HideAdapter      hideAdapter      = new HideAdapter();
    private final ColorAdapter     colorAdapter     = new ColorAdapter();

    private final SelectionAdapter selectionAdapter = new SelectionAdapter();

    private SelectionListener      listener         = null;

    private final List<Label>      items            = new ArrayList<Label>();

    public MainPopUp(final Shell parent) {
        shell = new Shell(parent, SWT.POP_UP | SWT.ON_TOP);
        final GridLayout layout = new GridLayout();
        layout.marginBottom = 0;
        layout.marginTop = 0;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        shell.setLayout(layout);
        shell.addMouseTrackListener(hideAdapter);

        shell.pack();
        shell.setVisible(false);
    }

    public void hide() {
        shell.setVisible(false);
        for (final Label l : items) {
            l.dispose();
        }
        items.clear();
        listener = null;
    }

    public boolean isVisible() {
        return shell.isVisible();
    }

    public void
            setItems(final String[] items, final SelectionListener listener) {
        this.listener = listener;
        for (final String item : items) {
            final Label label1 = new Label(shell, SWT.NONE);
            label1.setText(item);
            label1.setLayoutData(SWTUtil.createFillHorizontallyGridData());
            label1.setForeground(GUIHelper.COLOR_BLACK);
            label1.setBackground(GUIHelper.COLOR_WHITE);
            label1.addMouseTrackListener(colorAdapter);
            label1.addMouseTrackListener(hideAdapter);
            label1.addMouseListener(selectionAdapter);
            this.items.add(label1);
        }
        shell.layout();
        shell.pack();
    }

    public void show(final int x, final int y) {
        shell.setLocation(x, y);
        shell.setVisible(true);
    }

    public void show(final Point p) {
        show(p.x, p.y);
    }
}
