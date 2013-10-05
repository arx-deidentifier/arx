/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.arx.gui.view.def;

import java.util.Collection;
import java.util.List;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.gui.model.ModelExplicitCriterion;
import org.deidentifier.arx.gui.view.impl.MainPopUp;
import org.deidentifier.arx.gui.view.impl.MainToolTip;
import org.deidentifier.arx.gui.view.impl.menu.QueryDialogResult;
import org.deidentifier.arx.gui.worker.Worker;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Shell;

public interface IMainWindow {

    public static final Font FONT = GUIHelper.getFont(new FontData("Verdana", 8, SWT.NORMAL)); //$NON-NLS-1$

    public MainPopUp getPopUp();

    public Shell getShell();

    public MainToolTip getToolTip();

    public void show();

    public String showDateFormatInputDialog(String header, String text, Collection<String> dates);

    public void showErrorDialog(String header, String text);

    public void showInfoDialog(String header, String text);

    public String showInputDialog(String header, String text, String initial);

    public String showOpenFileDialog(String filter);

    public void showProgressDialog(String text, Worker<?> worker);

    public boolean showQuestionDialog(String header, String text);

    public String showSaveFileDialog(String filter);

    public ModelExplicitCriterion showSelectCriterionDialog(List<ModelExplicitCriterion> others);

    public QueryDialogResult showQueryDialog(String query, Data data);
}
