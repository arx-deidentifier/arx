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
package org.deidentifier.arx.aggregates.classification;

import java.util.ArrayList;
import java.util.List;

import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.OrderedIntDoubleMapping;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.function.DoubleDoubleFunction;
import org.apache.mahout.math.function.DoubleFunction;
import org.apache.mahout.vectorizer.encoders.ConstantValueEncoder;
import org.apache.mahout.vectorizer.encoders.StaticWordValueEncoder;
import org.deidentifier.arx.DataHandleInternal;
import org.deidentifier.arx.aggregates.ClassificationConfigurationSVM;

import smile.classification.SVM;
import smile.classification.SVM.Multiclass;
import smile.math.kernel.GaussianKernel;
import smile.math.kernel.HellingerKernel;
import smile.math.kernel.HyperbolicTangentKernel;
import smile.math.kernel.LaplacianKernel;
import smile.math.kernel.LinearKernel;
import smile.math.kernel.MercerKernel;
import smile.math.kernel.PearsonKernel;
import smile.math.kernel.PolynomialKernel;
import smile.math.kernel.ThinPlateSplineKernel;

import com.carrotsearch.hppc.IntArrayList;

/**
 * Implements a classifier
 * 
 * @author Fabian Prasser
 */
public class MultiClassSVM implements ClassificationMethod {
    
    /**
     * Wrapper around vector to expose the underlying array for compatibility between Mahout and Smile
     * 
     * @author Fabian Prasser
     */
    private static class NBVector implements Vector {

        private final double[] array;
        private NBVector(int size) { array = new double[size]; }
        @Override public double aggregate(DoubleDoubleFunction arg0, DoubleFunction arg1) { throw new UnsupportedOperationException(); }
        @Override public double aggregate(Vector arg0, DoubleDoubleFunction arg1, DoubleDoubleFunction arg2) { throw new UnsupportedOperationException(); }
        @Override public Iterable<Element> all() { throw new UnsupportedOperationException(); }
        @Override public String asFormatString() { throw new UnsupportedOperationException(); }
        @Override public Vector assign(double arg0) { throw new UnsupportedOperationException(); }
        @Override public Vector assign(double[] arg0) { throw new UnsupportedOperationException(); }
        @Override public Vector assign(Vector arg0) { throw new UnsupportedOperationException(); }
        @Override public Vector assign(DoubleFunction arg0) { throw new UnsupportedOperationException(); }
        @Override public Vector assign(Vector arg0, DoubleDoubleFunction arg1) { throw new UnsupportedOperationException(); }
        @Override public Vector assign(DoubleDoubleFunction arg0, double arg1) { throw new UnsupportedOperationException(); }
        @Override public Matrix cross(Vector arg0) { throw new UnsupportedOperationException(); }
        @Override public Vector divide(double arg0) { throw new UnsupportedOperationException(); }
        @Override public double dot(Vector arg0) { throw new UnsupportedOperationException(); }
        @Override public double get(int arg0) { return array[arg0]; }
        @Override public double getDistanceSquared(Vector arg0) { throw new UnsupportedOperationException(); }
        @Override public Element getElement(int arg0) { throw new UnsupportedOperationException(); }
        @Override public double getIteratorAdvanceCost() { throw new UnsupportedOperationException(); }
        @Override public double getLengthSquared() { throw new UnsupportedOperationException(); }
        @Override public double getLookupCost() { throw new UnsupportedOperationException(); }
        @Override public int getNumNonZeroElements() { throw new UnsupportedOperationException(); }
        @Override public int getNumNondefaultElements() { throw new UnsupportedOperationException(); }
        @Override public double getQuick(int arg0) { throw new UnsupportedOperationException(); }
        @Override public void incrementQuick(int arg0, double arg1) { throw new UnsupportedOperationException(); }
        @Override public boolean isAddConstantTime() { throw new UnsupportedOperationException(); }
        @Override public boolean isDense() { throw new UnsupportedOperationException(); }
        @Override public boolean isSequentialAccess() { throw new UnsupportedOperationException(); }
        @Override public Vector like() { throw new UnsupportedOperationException(); }
        @Override public Vector like(int arg0) { throw new UnsupportedOperationException(); }
        @Override public Vector logNormalize() { throw new UnsupportedOperationException(); }
        @Override public Vector logNormalize(double arg0) { throw new UnsupportedOperationException(); }
        @Override public double maxValue() { throw new UnsupportedOperationException(); }
        @Override public int maxValueIndex() { throw new UnsupportedOperationException(); }
        @Override public void mergeUpdates(OrderedIntDoubleMapping arg0) { throw new UnsupportedOperationException(); }
        @Override public double minValue() { throw new UnsupportedOperationException(); }
        @Override public int minValueIndex() { throw new UnsupportedOperationException(); }
        @Override public Vector minus(Vector arg0) { throw new UnsupportedOperationException(); }
        @Override public Iterable<Element> nonZeroes() { throw new UnsupportedOperationException(); }
        @Override public double norm(double arg0) { throw new UnsupportedOperationException(); }
        @Override public Vector normalize() { throw new UnsupportedOperationException(); }
        @Override public Vector normalize(double arg0) { throw new UnsupportedOperationException(); }
        @Override public Vector plus(double arg0) { throw new UnsupportedOperationException(); }
        @Override public Vector plus(Vector arg0) { throw new UnsupportedOperationException(); }
        @Override public void set(int position, double value) { array[position] = value; }
        @Override public void setQuick(int arg0, double arg1) { throw new UnsupportedOperationException(); }
        @Override public int size() { return array.length; }
        @Override public Vector times(double arg0) { throw new UnsupportedOperationException(); }
        @Override public Vector times(Vector arg0) { throw new UnsupportedOperationException(); }
        @Override public Vector viewPart(int arg0, int arg1) { throw new UnsupportedOperationException(); }
        @Override public double zSum() { throw new UnsupportedOperationException(); }
        @Override public NBVector clone() {throw new UnsupportedOperationException(); }
    }

    /** Config */
    private final ClassificationConfigurationSVM             config;
    /** Encoder */
    private final ConstantValueEncoder            interceptEncoder;
    /** Instance */
    private final SVM<double[]>                   svm;
    /** Specification */
    private final ClassificationDataSpecification specification;
    /** Encoder */
    private final StaticWordValueEncoder          wordEncoder;

    /** Data */
    private List<double[]>                        features = new ArrayList<double[]>();
    /** Data */
    private IntArrayList                          classes  = new IntArrayList();

    /**
     * Creates a new instance
     * @param specification
     * @param config
     */
    public MultiClassSVM(ClassificationDataSpecification specification,
                         ClassificationConfigurationSVM config) {

        // Store
        this.config = config;
        this.specification = specification;
        
        // Prepare classifier
        if (specification.classMap.size() > 2) {
            // Multiclass
            this.svm = new SVM<double[]>(getKernel(config), config.getC(), specification.classMap.size(), getMulticlass(config));
        } else {
            // Binary
            this.svm = new SVM<double[]>(getKernel(config), config.getC());
        }
                
        // Prepare encoders
        this.interceptEncoder = new ConstantValueEncoder("intercept");
        this.wordEncoder = new StaticWordValueEncoder("feature");
    }

    /**
     * Returns multi class handling strategy
     * @param config
     * @return
     */
    private Multiclass getMulticlass(ClassificationConfigurationSVM config) {

        switch(config.getMulticlassType()) {
        case ONE_VS_ALL:
            return Multiclass.ONE_VS_ALL;
        case ONE_VS_ONE:
            return Multiclass.ONE_VS_ONE;
        default:
            throw new IllegalArgumentException("Unknown multiclass parameter");
        }
    }

    /**
     * Returns the associated kernel
     * @param config
     * @return
     */
    private MercerKernel<double[]> getKernel(ClassificationConfigurationSVM config) {

       switch(config.getKernelType()) {
       case LAPLACIAN:
           return new LaplacianKernel(config.getKernelSigma());
       case GAUSSIAN:
           return new GaussianKernel(config.getKernelSigma());
       case HELLINGER:
           return new HellingerKernel();
       case HYPERBOLIC_TANGENT:
           return new HyperbolicTangentKernel();
       case LINEAR:
           return new LinearKernel();
       case PEARSON:
           return new PearsonKernel();
       case POLYNOMIAL:
           return new PolynomialKernel(config.getKernelDegree());
       case THIN_PLATE_SPLINE:
           return new ThinPlateSplineKernel(config.getKernelSigma());
       /*case BINARY_SPARSE_HYPERBOLIC_TANGENT:
           return new BinarySparseHyperbolicTangentKernel();
       case BINARY_SPARSE_GAUSSIAN:
           return new BinarySparseGaussianKernel(config.getKernelSigma());
       case BINARY_SPARSE_LAPLACIAN:
           return new BinarySparseLaplacianKernel(config.getKernelSigma());
       case BINARY_SPARSE_LINEAR:
           return new BinarySparseLinearKernel();
       case BINARY_SPARSE_POLYNOMIAL:
           return new BinarySparsePolynomialKernel(config.getKernelDegree());
       case BINARY_SPARSE_THIN_PLATE_SPLINE:
           return new BinarySparseThinPlateSplineKernel(config.getKernelSigma());
       case SPARSE_GAUSSIAN:
           return new SparseGaussianKernel(config.getKernelSigma());
       case SPARSE_HYPERBOLIC_TANGENT:
           return new SparseHyperbolicTangentKernel();
       case SPARSE_LAPLACIAN:
           return new SparseLaplacianKernel(config.getKernelSigma());
       case SPARSE_LINEAR:
           return new SparseLinearKernel();
       case SPARSE_POLYNOMIAL:
           return new SparsePolynomialKernel(config.getKernelDegree());
       case SPARSE_THIN_PLATE_SPLINE:
           return new SparseThinPlateSplineKernel(config.getKernelSigma());*/
           default:
               throw new IllegalArgumentException("Unknown kernel configuration");
       }
    }

    @Override
    public ClassificationResult classify(DataHandleInternal features, int row) {
        double[] probabilities = new double[specification.classMap.size()];
        int result = svm.predict(encodeFeatures(features, row), probabilities);
        return new MultiClassSVMClassificationResult(result, probabilities, specification.classMap);
    }

    @Override
    public void close() {
        
        // Learn now
        svm.learn(features.toArray(new double[features.size()][]), classes.toArray());
        svm.finish();
        
        // Clear
        features.clear();
        classes.clear();
        features = new ArrayList<double[]>();
        classes = new IntArrayList();
    }

    @Override
    public void train(DataHandleInternal features, DataHandleInternal clazz, int row) {
        // The SVM does not support online learning, so we have to cache data
        this.features.add(encodeFeatures(features, row));
        this.classes.add(encodeClass(clazz, row));
    }

    /**
     * Encodes a class
     * @param handle
     * @param row
     * @return
     */
    private int encodeClass(DataHandleInternal handle, int row) {
        return specification.classMap.get(handle.getValue(row, specification.classIndex, true));
    }

    /**
     * Encodes a feature
     * @param handle
     * @param row
     * @return
     */
    private double[] encodeFeatures(DataHandleInternal handle, int row) {

        // Prepare
        NBVector vector = new NBVector(config.getVectorLength());
        interceptEncoder.addToVector("1", vector);
        
        // Special case where there are no features
        if (specification.featureIndices.length == 0) {
            wordEncoder.addToVector("Feature:1", 1, vector);
            return vector.array;
        }
        
        // For each attribute
        int count = 0;
        for (int index : specification.featureIndices) {
            
            // Obtain data
            ClassificationFeatureMetadata metadata = specification.featureMetadata[count];
            String value = handle.getValue(row, index, true);
            Double numeric = metadata.getNumericValue(value);
            if (Double.isNaN(numeric)) {    
                wordEncoder.addToVector("Attribute-" + index + ":" + value, 1, vector);
            } else {
                wordEncoder.addToVector("Attribute-" + index, numeric, vector);
            }
            count++;
        }
        
        // Return
        return vector.array;
    }
}
