package org.deidentifier.arx.algorithm;

import java.util.Arrays;

import org.deidentifier.arx.framework.check.INodeChecker;
import org.deidentifier.arx.framework.lattice.Lattice;
import org.deidentifier.arx.framework.lattice.Node;

public class HeuraklesAlgorithm extends AbstractAlgorithm{
	
	private static final int HEURAKLES_FLAG_1 = 1 << 20;
	private static final int HEURAKLES_FLAG_2 = 1 << 21;

	public HeuraklesAlgorithm(Lattice lattice, INodeChecker checker) {
		super(lattice, checker);
	}

	@Override
	public void traverse() {		
		
		lattice.setChecked(lattice.getBottom(), checker.check(lattice.getBottom(), true));
		trackOptimum(lattice.getBottom());
		traverse(lattice.getBottom());
		
		System.out.println(getGlobalOptimum());
		System.out.println(Arrays.toString(getGlobalOptimum().getTransformation()));
		System.out.println(getGlobalOptimum().getInformationLoss());
		
	}
	
	private void traverse(Node node) {
		
		Node localOptimum = null;
		for (Node successor : node.getSuccessors()) {
			lattice.setChecked(successor, checker.check(successor, true));
			trackOptimum(successor);
			if (localOptimum == null || successor.getInformationLoss().compareTo(localOptimum.getInformationLoss())<0) {
				localOptimum = successor;
			}
		}
		
		if (localOptimum != null) traverse(localOptimum);
	}
}
