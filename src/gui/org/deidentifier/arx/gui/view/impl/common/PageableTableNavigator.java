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

import java.text.MessageFormat;
import java.util.Locale;

import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.nebula.widgets.pagination.AbstractPageControllerComposite;
import org.eclipse.nebula.widgets.pagination.PageableController;
import org.eclipse.nebula.widgets.pagination.PaginationHelper;
import org.eclipse.nebula.widgets.pagination.renderers.navigation.graphics.BlueNavigationPageGraphicsConfigurator;
import org.eclipse.nebula.widgets.pagination.renderers.navigation.graphics.INavigationPageGraphicsConfigurator;
import org.eclipse.nebula.widgets.pagination.renderers.navigation.graphics.NavigationPageGraphics;
import org.eclipse.nebula.widgets.pagination.renderers.navigation.graphics.NavigationPageGraphicsItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Adapted from the Nebula source. 
 * 
 * Contributors:
 * Angelo ZERR - initial API and implementation
 * Pascal Leclercq - initial API and implementation
 */
public class PageableTableNavigator extends AbstractPageControllerComposite {

    /** Enable pagination */
    public static final int                           PAGE_SIZE = 100;

    /** the result label **/
    private Label                                     resultLabel;
    /** the navigation page graphics **/
    private NavigationPageGraphics                    navigationPage;
    /** Configurator */
    private final INavigationPageGraphicsConfigurator configurator;

    /**
     * Creates a new instance
     * @param parent
     * @param style
     * @param controller
     */
    public PageableTableNavigator(Composite parent, int style, PageableController controller) {
        super(parent, style, controller, PageableController.DEFAULT_PAGE_SIZE, null, false);
        this.configurator = BlueNavigationPageGraphicsConfigurator.getInstance();
        createUI(this);
        refreshEnabled(controller);
    }

    /**
     * Returns the {@link GC} navigation page.
     * 
     * @return
     */
    public NavigationPageGraphics getNavigationPage() {
        return navigationPage;
    }

    public void pageIndexChanged(int oldPageNumber, int newPageNumber,
            PageableController controller) {
        // 1) Compute page indexes
        int[] indexes = PaginationHelper.getPageIndexes(
                controller.getCurrentPage(), controller.getTotalPages(), 10);
        // Update the GC navigation page with page indexes and selected page.
        navigationPage.update(indexes, newPageNumber, getLocale());
        refreshEnabled(controller);
    }

    public void pageSizeChanged(int oldPageSize, int newPageSize,
            PageableController paginationController) {
        // Do nothing
    }

    /**
     * Configure navigation page.
     * 
     * @param configurator
     */
    public void setConfigurator(INavigationPageGraphicsConfigurator configurator) {
        getNavigationPage().setConfigurator(configurator);
    }

    @Override
    public void setLocale(Locale locale) {
        super.setLocale(locale);
        navigationPage.setText(Resources.getMessage("PaginationTable.0"), Resources.getMessage("PaginationTable.1"));
        resultLabel.setText(getResultsText(getController(), getLocale()));
    }

    @Override
    public void sortChanged(String oldPopertyName, String propertyName,
            int oldSortDirection, int sortDirection,
            PageableController paginationController) {
        // Do nothing
    }

    @Override
    public void totalElementsChanged(long oldTotalElements, long newTotalElements, PageableController controller) {
        
        if (newTotalElements <= PAGE_SIZE) {
            ((GridData)this.getLayoutData()).heightHint = 0;
            this.getParent().layout();
        } else {
            ((GridData)this.getLayoutData()).heightHint = -1;
            this.getParent().layout();
        }
        
        // 1) Compute page indexes
        int[] indexes = PaginationHelper.getPageIndexes(controller.getCurrentPage(), controller.getTotalPages(), 10);
        // Update the GC navigation page with page indexes and selected page.
        navigationPage.update(indexes, 0, getLocale());
                
        refreshEnabled(controller);
    }

    /**
     * Create result label "Results 1-5 of 10"
     * 
     * @param parent
     */
    private void createLeftContainer(Composite parent) {
        Composite left = new Composite(parent, SWT.NONE);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        left.setLayoutData(data);

        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        left.setLayout(layout);

        resultLabel = new Label(left, SWT.NONE);
        resultLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    /**
     * Create page links "Previous 1 2 ...10 Next" with {@link GC}.
     * 
     * @param parent
     */
    private void createRightContainer(Composite parent) {
        Composite right = new Composite(parent, SWT.NONE);
        right.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        right.setLayout(layout);

        navigationPage = new NavigationPageGraphics(right, SWT.NONE, configurator) {
            @Override
            protected void handleSelection(NavigationPageGraphicsItem pageItem) {
                // Page item was clicked, update the page controller according to the selected page item.
                Integer newCurrentPage = null;
                if (!pageItem.isEnabled()) {
                    return;
                }
                if (pageItem.isNext()) {
                    newCurrentPage = getController().getCurrentPage() + 1;
                } else if (pageItem.isPrevious()) {
                    newCurrentPage = getController().getCurrentPage() - 1;
                } else {
                    newCurrentPage = pageItem.getIndex();
                }
                if (newCurrentPage != null) {
                    getController().setCurrentPage(newCurrentPage);
                }
            }
        };
        navigationPage.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    /**
     * Returns the results text (ex: "Results 1-5 of 10") for the given
     * pagination information start, end and total. The resultsMessage (ex:
     * "Results {0}-{1} of {2}") is used to compute the text.
     * 
     * @param start
     *            first page offset.
     * @param end
     *            last page offset
     * @param total
     *            total elements.
     * @param controller
     * @param resultsMessage
     * @return
     */
    private String getResultsText(int start, int end, long total,
            PageableController controller, String resultsMessage) {
        return MessageFormat.format(resultsMessage, start, end, total);
    }

    /**
     * Returns the results text (ex: "Results 1-5 of 10") for the given
     * pagination controller. The given locale is used to translate the results
     * text.
     * 
     * @param controller
     *            the pagination controller.
     * @param locale
     *            the locale.
     * @return
     */
    private String getResultsText(PageableController controller, Locale locale) {
        return getResultsText(controller, Resources.getMessage("PaginationTable.2")); // "Items {0}-{1} of {2}";
    }

    /**
     * Returns the results text (ex: "Results 1-5 of 10") for the given
     * pagination controller. The resultsMessage (ex: "Results {0}-{1} of {2}")
     * is used to compute the text.
     * 
     * @param controller
     *            the pagination controller.
     * @param resultsMessage
     *            the results message.
     * @return
     */
    private String getResultsText(PageableController controller,
            String resultsMessage) {
        int start = controller.getPageOffset() + 1;
        int end = start + controller.getPageSize() - 1;
        long total = controller.getTotalElements();
        if (end > total) {
            end = (int) total;
        }
        return getResultsText(start, end, total, controller, resultsMessage);
    }

    /**
     * Enable refresh
     * @param controller
     */
    private void refreshEnabled(PageableController controller) {
        resultLabel.setText(getResultsText(controller, getLocale()));
        navigationPage.setEnabled(controller.hasPreviousPage(), controller.hasNextPage());
    }

    @Override
    protected void createUI(Composite parent) {
        
        // Parent == this
        GridLayout layout = SWTUtil.createGridLayout(2, true);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.makeColumnsEqualWidth = false;
        parent.setLayout(layout);

        createLeftContainer(parent);
        createRightContainer(parent);
//
//        // Parent
//        GridLayout layout = SWTUtil.createGridLayout(1, true);
//        layout.marginWidth = 0;
//        layout.marginHeight = 0;
//        this.setLayout(layout);
//        GridData data = SWTUtil.createNoFillGridData();
//        data.heightHint=0;
//        data.widthHint=0;
//        this.setLayoutData(data);
//        
//        // Empty child
//        Composite empty = new Composite(parent, SWT.NONE);
//        data = SWTUtil.createNoFillGridData();
//        data.heightHint=0;
//        data.widthHint=0;
//        empty.setLayoutData(data);
    }
}