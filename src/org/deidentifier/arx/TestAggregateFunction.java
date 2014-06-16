package org.deidentifier.arx;

import java.text.DecimalFormat;

public class TestAggregateFunction {
    
    private static final DecimalFormat format = new DecimalFormat("0.000");

    public static void main(String[] args) {
        
        System.out.println("----");
        test2d();
        System.out.println("----");
        test3d();
        System.out.println("----");
        
        analyze(0.3, 0.3, 0.3);
        analyze(0.6, 0.3, 0.0);
        analyze(0.9, 0.0, 0.0);
        System.out.println("----");
        analyze(1.0, 0.8, 0.2);
        analyze(1.0, 0.5, 0.5);
        analyze(0.7, 0.7, 0.6);
        System.out.println("----");
        analyze(2.0, 0.8, 0.2);
        analyze(1.0, 0.5, 0.5);
        analyze(0.7, 0.7, 0.6);
        analyze(0.5, 0.5, 0.5);
        analyze(0.3, 0.3, 0.3);
        System.out.println("----");
        analyze(2.0, 0.8);
        analyze(1.0, 0.5);
        analyze(0.7, 0.7);
        analyze(0.5, 0.5);
        analyze(0.3, 0.3);
        System.out.println("----");
        analyze(2.0, 0.2);
        analyze(1.0, 0.5);
        analyze(0.7, 0.6);
        analyze(0.5, 0.5);
        analyze(0.3, 0.3);
        System.out.println("----");
        analyze(2.0, 0.8, 0.2,1.0,1.0);
        analyze(1.0, 0.5, 0.5,1.0,1.0);
        analyze(0.7, 0.7, 0.6,1.0,1.0);
        analyze(0.5, 0.5, 0.5,1.0,1.0);
        analyze(0.3, 0.3, 0.3,1.0,1.0);
        System.out.println("----");
        // MONOTON?
        System.out.println("----");
        analyze(0.2, 0.2, 0.2, 0.2, 0.2);
        analyze(1.0, 0.2, 0.2, 0.2, 0.2);
        analyze(1.0, 1.0, 0.2, 0.2, 0.2);
        analyze(1.0, 1.0, 1.0, 0.2, 0.2);
        analyze(1.0, 1.0, 1.0, 1.0, 0.2);
    }
    private static void test2d() {
        for (double x = 0d; x<=1.0d; x+=0.3d) {
            for (double y = 0d; y<=1.0d; y+=0.3d) {
                analyze(x,y);
            }
        }
    }

    private static void test3d() {
        for (double x = 0d; x<=1.0d; x+=0.3d) {
            for (double y = 0d; y<=1.0d; y+=0.3d) {
                for (double z = 0d; z<=1.0d; z+=0.3d) {
                    analyze(x,y,z);
                }
            }
        }
    }
    
    private static final double getMean(double... values) {

        double mean = 0d;
        for (int i=0; i<values.length; i++){
            mean += values[i];
        }
        mean /= (double)values.length;
        return mean;
    }
    

    private static final double getStandardDeviation(double... values) {

        double mean = 0d;
        for (int i=0; i<values.length; i++){
            mean += values[i];
        }
        mean /= (double)values.length;
        
        double dev = 0;
        for (int i=0; i<values.length; i++){
            dev += Math.pow(values[i] - mean, 2.0d);
        }
        
        // TODO: OK?
        return Math.sqrt(dev / (double)values.length);
    }
    
    private static void analyze(double... values) {

        double d1 = getMean(values);
        double d3 = 1d;
        for (double val : values) d3 *= val;
        double d4 = (getMean(values) + 1d - getStandardDeviation(values) ) / 2d;
        
        for (double val : values){
            System.out.print(format.format(val));
            System.out.print("/");
        }
        System.out.print("   ");
        System.out.println(format.format(d1)+"/"+format.format(d3)+"/"+format.format(d4));
    }
}
