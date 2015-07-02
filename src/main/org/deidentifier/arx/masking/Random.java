package org.deidentifier.arx.masking;

import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.Well44497b;

/**
 * Provides quick access to an alternative random number generator used instead of the standard
 * RNG {@link java.util.Random}, because of its mediocre statistical qualities. A superior RNG
 * from the Apache Commons Math library is used in its place.
 * <p>
 * The class inherits from {@link org.apache.commons.math3.random.RandomAdaptor RandomAdaptor},
 * a wrapper for {@link org.apache.commons.math3.random.RandomGenerator RandomGenerator}s so
 * they can be used instead of a {@link java.util.Random}. 
 * 
 * @author Wesper
 */
public final class Random extends RandomAdaptor {
	
	private static final long serialVersionUID = -8827826017115532703L;
	
	/**
	 * A static instance of this RNG. 
	 */
	public static final Random staticInstance = new Random();
	
	/**
	 * Creates a new Random object using the {@link org.apache.commons.math3.random.Well44497b
	 * Well44497b} RNG. Seeded using system time.
	 */
	public Random() {
		super(new Well44497b());
	}
	
}