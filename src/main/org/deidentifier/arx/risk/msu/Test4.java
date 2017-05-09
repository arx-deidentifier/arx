/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
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
package org.deidentifier.arx.risk.msu;

import org.apache.mahout.math.Arrays;


public class Test4 {

    public static void main(String[] args) {
        
//        /*1*/ new int[] {Urban, Female, Secondary incomplete, Employed},
//        /*2*/ new int[] {Urban, Female, Secondary incomplete, Employed},
//        /*3*/ new int[] {Urban, Female, Primary incomplete, Non-LF},
//        /*4*/ new int[] {Urban, Male, Secondary complete, Employed},
//        /*5*/ new int[] {Rural, Female, Secondary complete, Unemployed},
//        /*6*/ new int[] {Urban, Male, Secondary complete, Employed},
//        /*7*/ new int[] {Urban, Female, Primary complete, Non-LF},
//        /*8*/ new int[] {Urban, Male, Post-secondary, Unemployed},
//        /*9*/ new int[] {Urban, Female, Secondary incomplete, Non-LF},
//       /*10*/ new int[] {Urban, Female, Secondary incomplete, Non-LF}
        
        // IHSN - STATISTICAL DISCLOSURE CONTROL FOR MICRODATA: A PRACTICE GUIDE
        int[][] data = new int[][] {
               /*1*/ new int[] {0, 0, 0, 0},
               /*2*/ new int[] {0, 0, 0, 0},
               /*3*/ new int[] {0, 0, 1, 1},
               /*4*/ new int[] {0, 1, 2, 0},
               /*5*/ new int[] {1, 0, 2, 2},
               /*6*/ new int[] {0, 1, 2, 0},
               /*7*/ new int[] {0, 0, 3, 1},
               /*8*/ new int[] {0, 1, 4, 2},
               /*9*/ new int[] {0, 0, 0, 1},
              /*10*/ new int[] {0, 0, 0, 1}
        };
        print(data);
        System.out.println("\n-----\n"); 
        SUDA2Result result1 = new SUDA2(data).suda2(0);
        System.out.println(result1.toString());
    }

    /**
     * Prints the data
     * @param data
     */
    private static void print(int[][] data) {
        for (int[] row : data) {
            System.out.println(" - " + Arrays.toString(row));
        }
    }
}
