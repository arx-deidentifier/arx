package org.deidentifier.arx.algorithm;

import org.deidentifier.arx.framework.check.INodeChecker;
import org.deidentifier.arx.framework.lattice.Lattice;

public interface AbstractAlgorithmFactory {

	public AbstractAlgorithm create(final Lattice lattice,
            final INodeChecker checker);
}
