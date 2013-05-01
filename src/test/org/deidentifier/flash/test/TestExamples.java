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

package org.deidentifier.flash.test;

import java.io.IOException;

import junit.framework.Assert;

import org.deidentifier.flash.examples.Example1;
import org.deidentifier.flash.examples.Example2;
import org.deidentifier.flash.examples.Example3;
import org.deidentifier.flash.examples.Example4;
import org.deidentifier.flash.examples.Example5;
import org.deidentifier.flash.examples.Example6;
import org.deidentifier.flash.examples.Example7;
import org.deidentifier.flash.examples.Example8;
import org.junit.Test;

public class TestExamples {

    @Test
    public void testExample1() {
        try {
            Example1.main(null);
        } catch (final Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testExample2() throws IOException {
        try {
            Example2.main(null);
        } catch (final Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testExample3() {
        try {
            Example3.main(null);
        } catch (final Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testExample4() {
        try {
            Example4.main(null);
        } catch (final Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testExample5() {
        try {
            Example5.main(null);
        } catch (final Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testExample6() {
        try {
            Example6.main(null);
        } catch (final Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testExample7() {
        try {
            Example7.main(null);
        } catch (final Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testExample8() {
        try {
            Example8.main(null);
        } catch (final Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

}
