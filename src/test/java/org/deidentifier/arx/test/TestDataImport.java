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

package org.deidentifier.arx.test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deidentifier.arx.Data;
import org.junit.Test;

import cern.colt.Arrays;

/**
 * Tests for importing complex csv files
 * 
 * @author Fabian Prasser
 */
public class TestDataImport extends AbstractTest {
    
    /**
     * Test
     *
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void test1() throws IllegalArgumentException, IOException {
        Data data = Data.create(new File("data/test-import.csv"), StandardCharsets.UTF_8, ';', '\"');
        Iterator<String[]> iter = data.getHandle().iterator();
        List<String[]> result = new ArrayList<String[]>();
        while (iter.hasNext()) {
            result.add(iter.next());
            System.out.println(Arrays.toString(result.get(result.size() - 1)));
        }
    }
}
