/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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
package org.deidentifier.arx;

import java.io.Serializable;

/**
 * Configuration for SVM classifiers
 * @author Fabian Prasser
 */
public class ARXSVMConfiguration extends ARXClassificationConfiguration implements Serializable {

    /** 
     * Type of multiclass SVM
     */
    public static enum MulticlassType {
        ONE_VS_ALL,
        ONE_VS_ONE
    }

    /** SVUID */
    private static final long serialVersionUID = -4043281971961479819L;

    /**
     * Returns a new instance
     * @return
     */
    public static ARXSVMConfiguration create() {
        return new ARXSVMConfiguration();
    }

    /** Type */
    private MulticlassType multiclassType         = MulticlassType.ONE_VS_ALL;
    /** Soft margin penalty */
    private double         c            = 1.0d;
    /** The smooth/width parameter of the kernel. */
    private double         kernelSigma  = 1.0d;
    /** The degree of the kernel. */
    private int            kernelDegree = 2;
    /** Kernel */
    private Kernel         kernelType       = Kernel.GAUSSIAN;

    /**
     * Kernel for the SVM
     * 
     * @author Fabian Prasser
     */
    public static enum Kernel {
        /** Kernel*/
        LAPLACIAN,
        /** Kernel*/
        GAUSSIAN,
        /** Kernel*/
        HELLINGER,
        /** Kernel*/
        HYPERBOLIC_TANGENT,
        /** Kernel*/
        LINEAR,
        /** Kernel*/
        PEARSON,
        /** Kernel*/
        POLYNOMIAL,
        /** Kernel*/
        THIN_PLATE_SPLINE,
//        /** Kernel*/
//        BINARY_SPARSE_HYPERBOLIC_TANGENT,
//        /** Kernel*/
//        BINARY_SPARSE_GAUSSIAN,
//        /** Kernel*/
//        BINARY_SPARSE_LAPLACIAN,
//        /** Kernel*/
//        BINARY_SPARSE_LINEAR,
//        /** Kernel*/
//        BINARY_SPARSE_POLYNOMIAL,
//        /** Kernel*/
//        BINARY_SPARSE_THIN_PLATE_SPLINE,
//        /** Kernel*/
//        SPARSE_GAUSSIAN,
//        /** Kernel*/
//        SPARSE_HYPERBOLIC_TANGENT,
//        /** Kernel*/
//        SPARSE_LAPLACIAN,
//        /** Kernel*/
//        SPARSE_LINEAR,
//        /** Kernel*/
//        SPARSE_POLYNOMIAL,
//        /** Kernel*/
//        SPARSE_THIN_PLATE_SPLINE,
    }
    
    /** Configuration */
    private int     vectorLength  = 1000;
    /** Max records */
    private int     maxRecords    = 100000;
    /** Seed */
    private long    seed          = Integer.MAX_VALUE;
    /** Folds */
    private int     numberOfFolds = 10;
    /** Deterministic */
    private boolean deterministic = true;

    /**
     * Constructor
     */
    private ARXSVMConfiguration(){
        // Empty by design
    }

    @Override
    public int getMaxRecords() {
        return maxRecords;
    }
    /**
     * @return the numberOfFolds
     */
    public int getNumFolds() {
        return numberOfFolds;
    }

    /**
     * @return the seed
     */
    public long getSeed() {
        return seed;
    }

    /**
     * @return the vectorLength
     */
    public int getVectorLength() {
        return vectorLength;
    }

    /**
     * Returns whether the process should be deterministic
     * @return
     */
    public boolean isDeterministic() {
        return deterministic;
    }
    
    /**
     * Sets whether the process should be deterministic
     * @param deterministic
     * @return
     */
    public ARXSVMConfiguration setDeterministic(boolean deterministic) {
        this.deterministic = deterministic;
        return this;
    }
    
    /**
     * @param maxRecords the maxRecords to set
     */
    public ARXSVMConfiguration setMaxRecords(int maxRecords) {
        if (maxRecords <= 0) {
            throw new IllegalArgumentException("Must be >0");
        }
        this.maxRecords = maxRecords;
        return this;
    }

    /**
     * @param numberOfFolds the numberOfFolds to set
     */
    public ARXSVMConfiguration setNumFolds(int numberOfFolds) {
        if (numberOfFolds <= 0) {
            throw new IllegalArgumentException("Must be >0");
        }
        this.numberOfFolds = numberOfFolds;
        return this;
    }

    /**
     * Seed for randomization. Set to Integer.MAX_VALUE for randomization.
     * @param seed the seed to set
     */
    public ARXSVMConfiguration setSeed(int seed) {
        this.seed = seed;
        return this;
    }

    /**
     * @param vectorLength the vectorLength to set
     */
    public ARXSVMConfiguration setVectorLength(int vectorLength) {
        if (vectorLength <= 0) {
            throw new IllegalArgumentException("Must be >0");
        }
        this.vectorLength = vectorLength;
        return this;
    }

    /**
     * @return the c
     */
    public double getC() {
        return c;
    }

    /**
     * @param c the c to set
     */
    public ARXSVMConfiguration setC(double c) {
        this.c = c;
        return this;
    }

    /**
     * @return the kernelSigma
     */
    public double getKernelSigma() {
        return kernelSigma;
    }

    /**
     * @param kernelSigma the kernelSigma to set
     */
    public ARXSVMConfiguration setKernelSigma(double kernelSigma) {
        this.kernelSigma = kernelSigma;
        return this;
    }

    /**
     * @return the kernelDegree
     */
    public int getKernelDegree() {
        return kernelDegree;
    }

    /**
     * @param kernelDegree the kernelDegree to set
     */
    public ARXSVMConfiguration setKernelDegree(int kernelDegree) {
        this.kernelDegree = kernelDegree;
        return this;
    }

    /**
     * @return the kernel
     */
    public Kernel getKernelType() {
        return kernelType;
    }

    /**
     * @param kernel the kernel to set
     */
    public ARXSVMConfiguration setKernelType(Kernel kernel) {
        this.kernelType = kernel;
        return this;
    }

    /**
     * @return the multiclassType
     */
    public MulticlassType getMulticlassType() {
        return multiclassType;
    }

    /**
     * @param multiclassType the multiclassType to set
     */
    public ARXSVMConfiguration setMulticlassType(MulticlassType multiclassType) {
        this.multiclassType = multiclassType;
        return this;
    }
}
