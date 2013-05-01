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

package org.deidentifier.flash.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class SWTUtil {

    public static void center(final Shell shell, final Shell parent) {

        final Rectangle bounds = parent.getBounds();
        final Point p = shell.getSize();
        final int left = (bounds.width - p.x) / 2;
        final int top = (bounds.height - p.y) / 2;
        shell.setBounds(left + bounds.x, top + bounds.y, p.x, p.y);
    }

    public static GridData createFillGridData() {
        final GridData data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.verticalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        return data;
    }

    public static GridData createFillHorizontallyGridData() {
        final GridData data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.verticalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = false;
        return data;
    }

    public static GridData createFillVerticallyGridData() {
        final GridData data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.verticalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = false;
        data.grabExcessVerticalSpace = true;
        return data;
    }

    public static GridData createGridData() {
        final GridData data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.verticalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = false;
        data.grabExcessVerticalSpace = false;
        return data;
    }

    public static GridData createNoFillGridData() {
        final GridData d = new GridData();
        d.horizontalAlignment = SWT.LEFT;
        d.verticalAlignment = SWT.TOP;
        d.grabExcessHorizontalSpace = false;
        d.grabExcessVerticalSpace = false;
        return d;
    }

    public static Object createSpanColumnsAndFillGridData(final int i) {
        final GridData d = new GridData();
        d.grabExcessHorizontalSpace = true;
        d.grabExcessVerticalSpace = true;
        d.horizontalSpan = i;
        return d;
    }

    public static Object createSpanColumnsGridData(final int i) {
        final GridData d = new GridData();
        d.grabExcessHorizontalSpace = false;
        d.grabExcessVerticalSpace = false;
        d.horizontalSpan = i;
        return d;
    }

    public static void disable(final Composite elem) {
        setEnabled(elem, false);
    }

    public static void enable(final Composite elem) {
        setEnabled(elem, true);
    }

    private static void setEnabled(final Composite elem, final boolean val) {
        elem.setEnabled(val);
        for (final Control c : elem.getChildren()) {
            if (c instanceof Composite) {
                setEnabled((Composite) c, val);
            } else {
                c.setEnabled(val);
            }
        }
    }
}
