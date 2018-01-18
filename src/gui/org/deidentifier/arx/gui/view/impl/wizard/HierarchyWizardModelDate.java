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

package org.deidentifier.arx.gui.view.impl.wizard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.aggregates.HierarchyBuilderDate;
import org.deidentifier.arx.aggregates.HierarchyBuilderDate.Format;
import org.deidentifier.arx.aggregates.HierarchyBuilderDate.Granularity;
import org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizard.HierarchyWizardView;

/**
 * A model for date-based builders.
 *
 * @author Fabian Prasser
 */
public class HierarchyWizardModelDate extends HierarchyWizardModelAbstract<Date> {
    
    /** Granularities */
    private List<Granularity>    granularities     = new ArrayList<Granularity>();
    /** Format */
    private Format               format            = new Format();
    /** Time zone */
    private TimeZone             timeZone          = TimeZone.getDefault();
    /** Top/bottom coding */
    private Date                 bottomCodingBound = null;
    /** Top/bottom coding */
    private Date                 topCodingBound    = null;
    /** Data type */
    private final DataType<Date> dataType;

    /**
     * Creates a new instance.
     *
     * @param dataType
     * @param data
     */
    public HierarchyWizardModelDate(DataType<Date> dataType, String[] data) {
        
        // Super
        super(data);
        
        // Store
        this.dataType = dataType;
        
        // Update
        this.update();
    }

    /**
     * Returns the bottom coding bound
     * @return
     */
    public Date getBottomCodingBound() {
        return this.bottomCodingBound;
    }

    @Override
    public HierarchyBuilderDate getBuilder(boolean serializable) {

        // Create
        HierarchyBuilder<Date> builder = HierarchyBuilderDate.create(this.dataType,
                                                                     this.timeZone,
                                                                     this.format,
                                                                     this.bottomCodingBound,
                                                                     this.topCodingBound,
                                                                     this.granularities.toArray(new Granularity[0]));

        // Return
        return (HierarchyBuilderDate) builder;
    }

    /**
     * @return the dataType
     */
    public DataType<Date> getDataType() {
        return dataType;
    }

    /**
     * @return the format
     */
    public Format getFormat() {
        return format;
    }

    /**
     * @return the granularities
     */
    public List<Granularity> getGranularities() {
        return granularities;
    }

    /**
     * @return the timeZone
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * Returns the top-coding bound
     * @return
     */
    public Date getTopCodingBound() {
        return this.topCodingBound;
    }

    @Override
    public void parse(HierarchyBuilder<Date> _builder) {
        
        if (!(_builder instanceof HierarchyBuilderDate)) {
            return;
        }
        HierarchyBuilderDate builder = (HierarchyBuilderDate)_builder; 
        this.granularities = new ArrayList<>(Arrays.asList(builder.getGranularities()));
        this.format = builder.getFormat();
        this.timeZone = builder.getTimeZone();
        this.topCodingBound = builder.getTopCodingBound();
        this.bottomCodingBound = builder.getBottomCodingBound();
        this.update();
    }

    /**
     * @param bottomCodingBound the bottomCodingBound to set
     */
    public void setBottomCodingBound(Date bottomCodingBound) {
        this.bottomCodingBound = bottomCodingBound;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(Format format) {
        this.format = format;
        this.update();
    }

    /**
     * @param granularities the granularities to set
     */
    public void setGranularities(List<Granularity> granularities) {
        this.granularities = granularities;
        this.update();
    }

    /**
     * @param timeZone the timeZone to set
     */
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
        this.update();
    }

    /**
     * @param topCodingBound the topCodingBound to set
     */
    public void setTopCodingBound(Date topCodingBound) {
        this.topCodingBound = topCodingBound;
    }

    @Override
    public void updateUI(HierarchyWizardView sender) {
        // Empty by design
    }

    @Override
    protected void build() {
        super.hierarchy = null;
        super.error = null;
        super.groupsizes = null;

        if (data==null) return;
        
        HierarchyBuilderDate builder = getBuilder(false);
        try {
            super.groupsizes = builder.prepare(data);
        } catch(Exception e){
            super.error = e.getMessage();
            return;
        }
        
        try {
            super.hierarchy = builder.build();
        } catch(Exception e){
            super.error = e.getMessage();
            return;
        }
    }
}
