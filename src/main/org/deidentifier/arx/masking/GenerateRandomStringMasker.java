package org.deidentifier.arx.masking;

import org.apache.commons.lang.RandomStringUtils;

/**
 * A masker that generates random strings according to a set of user-specified rules.
 * <p>
 * The user can specify the length of the generated strings or whether to use the length of the
 * input strings, he can choose to generate alphabetic, numeric or alphanumeric strings or he
 * can specify a set of characters from which the strings are constructed.
 * 
 * @author Wesper
 */
public class GenerateRandomStringMasker extends AbstractInstBasedDictMasker<String> {

	/** The number of characters the replacement shall consist of. If length < 0, the length of 
	 *	the input is used. */
	private int length = -1;
	
	/** If true, alphabetic characters may be generated. */
	private boolean	letters = true;
	
	/** If true, numeric characters may be generated. */ 
	private boolean numbers = false;

	/** An optional set of characters to choose from. If null, all characters are used. */
	private char[] charSet = null;
	
	
	/**
	 * Creates a masker generating strings with the same length as the input, using only
	 * alphabetic characters.
	 */
	public GenerateRandomStringMasker() {
		this(-1);
	}
	
	/**
	 * Creates a masker generating strings with the same length as the input data, using
	 * alphabetic and numeric characters, depending on the parameters. If both parameters are
	 * false, the masker samples from the whole range of possible characters.
	 * @param letters If true, generated strings contain alphabetic characters.
	 * @param numbers If true, generated strings contain numeric characters.
	 */
	public GenerateRandomStringMasker(boolean letters, boolean numbers) {
		this(-1, letters, numbers);
	}
	
	/**
	 * Creates a masker generating strings with the same length as the input, using random
	 * characters from the supplied character set.
	 * @param charSet The array from which characters are sampled. If null, all alphabetic
	 * characters are used.
	 */
	public GenerateRandomStringMasker(char[] charSet) {
		this(-1, charSet);
	}
	
	/**
	 * Creates a masker generating alphabetic strings with the specified length.
	 * @param length The length of the strings to be generated. If negative, the length of the
	 * masked string is used.
	 */
	public GenerateRandomStringMasker(int length) {
		this(length, true, false);
	}
	
	/**
	 * Creates a masker generating strings with the specified length, using alphabetic and
	 * numeric characters, depending on the parameters. If both parameters are false, the masker
	 * samples from the whole range of possible characters.
	 * @param length The length of the strings to be generated. If negative, the length of the
	 * masked string is used.
	 * @param letters If true, generated strings contain alphabetic characters.
	 * @param numbers If true, generated strings contain numeric characters.
	 */
	public GenerateRandomStringMasker(int length, boolean letters, boolean numbers) {
		this(length, letters, numbers, null);
	}
	
	/**
	 * Creates a masker generating strings with the specified length, using random characters
	 * from the supplied character set.
	 * @param length The length of the strings to be generated. If negative, the length of the
	 * masked string is used.
	 * @param charSet The array from which characters are sampled. If null, all alphabetic
	 * characters are used.
	 */
	public GenerateRandomStringMasker(int length, char[] charSet) {
		this(length, true, false, charSet);
	}
	
	/**
	 * Creates a masker generating strings with the specified length, using random characters
	 * from the supplied character set. The characters used can be alphabetic, numeric, or
	 * both, depending on the parameters. If these parameters are both set to false, the masker
	 * samples from the whole range of possible characters.
	 * @param length The length of the strings to be generated. If negative, the length of the
	 * masked string is used.
	 * @param letters If true, generated strings contain alphabetic characters.
	 * @param numbers If true, generated strings contain numeric characters.
	 * @param charSet The array from which characters are sampled. If null, all alphabetic
	 * characters are used.
	 */
	public GenerateRandomStringMasker(int length, boolean letters, boolean numbers,
										char[] charSet) {
		this.length		= length;
		this.letters	= letters;
		this.numbers	= numbers;		
		this.charSet	= charSet;	
	}
	
	/**
	 * Generates a new random string according to the rules specified in the constructor.
	 */
	@Override
	public String mask(String input) {
		
		int stringLength;
		if (length < 0)	stringLength = input.length();
		else			stringLength = length;
		
		return RandomStringUtils.
				random(stringLength, 0, 0, letters, numbers, charSet, Random.staticInstance);
	}
	
}
