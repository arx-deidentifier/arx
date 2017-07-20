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

import java.util.Random;

import org.apache.mahout.math.Arrays;


public class Test {

    public static void main(String[] args) {
        
        // Rank (0,0) => Support 4
        // Rank (2,0) => Support 4
        // Rank (4,2) => Support 3
        
        // Itemset: (2,0), (0, 0)
        // Reference: (4,2)

        // (4,2) (2,0) (0,0) eigentlich abgedeckt durch (4,2) (0,0)
        int[][] data = new int[][] {
                new int[] { 0, 3, 0, 1, 1 }, // 0 
                new int[] { 0, 3, 0, 0, 1 }, // 1 
                new int[] { 0, 3, 1, 1, 1 }, // 2
                new int[] { 1, 3, 0, 1, 2 }, // 3 
                new int[] { 0, 2, 0, 1, 2 }, // 4 (0, -, 0, -, 2) -> (0, -, -, -,2)
                new int[] { 1, 2, 1, 0, 2 }  // 5
        };
        
//        for (int i = 0; i < 100000; i++) {
//            SUDA2Result result1 = new SUDA2(data).suda2(0);
//            SUDA2Result result2 = new ExhaustiveSearch(data).exhaustive();
//            if (!result1.equals(result2)) {
//                System.out.println("Mismatch:");
//                print(data);
//                System.out.println("SUDA2");
//                System.out.println(result1);
//                System.out.println("Exhaustive");
//                System.out.println(result2);
//                System.exit(0);
//            }
//            permute(data, 100);
//        }
//        
        SUDA2Statistics result1 = new SUDA2(data).getStatistics(0);
        System.out.println(result1.toString());
        result1.getColumnKeyContributions(); // Attribute / variable contribution
        
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

    /**
     * Permutes the data
     * @param data
     * @param numPermutations
     */
    private static void permute(int[][] data, int numPermutations) {

        Random random = new Random();
        for (int i=0; i < numPermutations; i++) {
            
            // Select
            int x1 = random.nextInt(data.length);
            int x2 = random.nextInt(data.length);
            int y1 = random.nextInt(data[0].length);
            int y2 = random.nextInt(data[0].length);
            
            // Swap
            int temp = data[x1][y1];
            data[x1][y1] = data[x2][y2];
            data[x2][y2] = temp;
        }
    }
}
