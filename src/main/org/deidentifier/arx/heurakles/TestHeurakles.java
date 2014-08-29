package org.deidentifier.arx.heurakles;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXLattice;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXListener;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.algorithm.AbstractAlgorithm;
import org.deidentifier.arx.algorithm.AbstractAlgorithmFactory;
import org.deidentifier.arx.algorithm.FLASHAlgorithm;
import org.deidentifier.arx.algorithm.HeuraklesAlgorithm;
import org.deidentifier.arx.criteria.EntropyLDiversity;
import org.deidentifier.arx.framework.check.INodeChecker;
import org.deidentifier.arx.framework.lattice.Lattice;
import org.deidentifier.arx.io.CSVHierarchyInput;
import org.deidentifier.arx.metric.Metric;

public class TestHeurakles {

	public static void main(String[] args) throws IOException {
		
		FLASHAlgorithm.setReplacement(new AbstractAlgorithmFactory() {
			@Override
			public AbstractAlgorithm create(Lattice lattice, INodeChecker checker) {
				return new HeuraklesAlgorithm(lattice, checker);
			}
		});
		
		
//		ARXConfiguration config = ARXConfiguration.create();
//		config.setMaxOutliers(0.04d);
//		config.setMetric(Metric.createNMPrecisionMetric());
//		config.addCriterion(new KAnonymity(5));
//		
//		Data data = getDataObject("../arx-data/data-junit/adult.csv");

		ARXConfiguration config = ARXConfiguration.create();
		config.setMaxOutliers(0.04d);
		config.setMetric(Metric.createNMPrecisionMetric());
		config.addCriterion(new EntropyLDiversity("occupation", 5));
		
		Data data = getDataObject("../arx-data/data-junit/adult.csv", "occupation");
		
		
		ARXAnonymizer anonymizer = new ARXAnonymizer();
		anonymizer.setListener(new ARXListener(){
			int count = 0;
			public void nodeTagged(int searchSpaceSize) {
				if (count++ % 100 == 0) System.out.println("Progress "+count+"/"+searchSpaceSize);
			} 
		});
		
		
		ARXResult result = anonymizer.anonymize(data, config);
		
		if (result.isResultAvailable()) {
			ARXNode optimum = result.getGlobalOptimum();
			System.out.println(Arrays.toString(optimum.getTransformation()));
			
			DataHandle handle = result.getOutput(optimum);
			System.out.println(handle.getValue(3, 3));
			
			double loss = (Double)optimum.getMaximumInformationLoss().getValue();
			System.out.println(loss);
			
			ARXLattice searchSpace = result.getLattice();
			
			int checked = 0;
			int total = 0;
			for (ARXNode[] level : searchSpace.getLevels()) {
				for (ARXNode node : level) {
					total++;
					checked += node.isChecked() ? 1 : 0;
				}
			}
			System.out.println(checked+"/"+total);
			
			for (ARXNode node : searchSpace.getBottom().getSuccessors()) {
				System.out.println(Arrays.toString(node.getTransformation()));
			}
			
		} else {
			System.out.println("No result");
		}
		
	}

	/**
	 * Returns the data object
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private static Data getDataObject(final String dataset) throws IOException {
		return getDataObject(dataset, null);
	}
	/**
	 * Returns the data object
	 * 
	 * @param file
	 * @param sensitiveAttribute
	 * @return
	 * @throws IOException
	 */
	private static Data getDataObject(final String dataset, 
			final String sensitiveAttribute) throws IOException {

		final Data data = Data.create(dataset, ';');

		// Read generalization hierachies
		final FilenameFilter hierarchyFilter = new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String name) {
				if (name.matches(dataset.substring(
						dataset.lastIndexOf("/") + 1,
						dataset.length() - 4)
						+ "_hierarchy_(.)+.csv")) {
					return true;
				} else {
					return false;
				}
			}
		};

		final File testDir = new File(dataset.substring(0,
				dataset.lastIndexOf("/")));
		final File[] genHierFiles = testDir.listFiles(hierarchyFilter);
		final Pattern pattern = Pattern.compile("_hierarchy_(.*?).csv");

		for (final File file : genHierFiles) {
			final Matcher matcher = pattern.matcher(file.getName());
			if (matcher.find()) {

				final CSVHierarchyInput hier = new CSVHierarchyInput(file, ';');
				final String attributeName = matcher.group(1);

				if (!attributeName.equalsIgnoreCase(sensitiveAttribute)) {
					data.getDefinition().setAttributeType(attributeName,
							Hierarchy.create(hier.getHierarchy()));
				} else { // sensitive attribute
					data.getDefinition().setAttributeType(attributeName, AttributeType.SENSITIVE_ATTRIBUTE);
				}

			}
		}

		return data;
	}

}
