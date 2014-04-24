package org.deidentifier.arx.masking;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A masker that splits up the input strings into groups by matching delimiters in it. A
 * specified group is then replaced by the given string replacement and the new, modified
 * string is returned.
 * <p>
 * The group to be replaced must be given by an index >= 0. If the group index exceeds the
 * number of groups found by splitting the input, the input is returned unmodified.
 * <p>
 * If no match is found, the input is split into a single group - the whole input string. Note
 * that if the group index is 0, there will always occur masking - if no match is in the input,
 * the whole input string is masked. 
 * <p>
 * The user can specify whether a group shall be replaced as a whole or each single character
 * should be replaced with the same replacement string.
 * <p>
 * <b>Examples:</b><pre>
 * // A masker replacing the user name in an email address with "[REDACTED]":
 * 
 * SplitAndReplaceStringMasker emailMasker = new SplitAndReplaceStringMasker(
 * 		"@",		// Split at occurrences of "@".
 * 		"[REDACTED]",	// Replacement string.
 * 		0,		// Replace group with index 0.
 * 		false);		// Replace the whole group.
 * 
 * String maskedEmail =
 * 	emailMasker.mask("john.doe@email.com"); // Returns "[REDACTED]@email.com".
 * 
 * 
 * // A masker starring out the year in a date string with DD.MM.YYYY format:
 *  
 * SplitAndReplaceStringMasker yearMasker = new SplitAndReplaceStringMasker(
 * 		"\\.",	// Split at occurrences of ".". (Regex operator '.' must be escaped with a backslash.)
 * 		"*",	// Replacement string.
 * 		2,	// Replace group with index 2.
 * 		true);	// Replace each character in group.
 * 
 * String maskedDate =
 * 	yearMasker.mask("02.07.1980"); // Returns "02.07.****".
 * </pre>
 * @author Wesper
 */
public class SplitAndReplaceStringMasker extends AbstractInstBasedDictMasker<String> {

	private Pattern	regEx;
	private String	replacementString;
	private int		replaceGroup;	// Must be >= 0.
	private boolean	replacingEachCharacter;
	
	/**
	 * Compiles the given regex string and constructs a new masker, using the compiled pattern
	 * to match delimiters. The masker will use the pattern to split the input string into
	 * several groups. The group at the specified index is then replaced by a given string,
	 * either as a whole, or character for character.
	 * @param regEx The string to be compiled into a regex pattern, which is then used to find
	 * the delimiters splitting the input string.
	 * @param replacementString The string used to replace the specified group.
	 * @param replaceGroup A zero-based index addressing one of the split groups. If a
	 * non-existent group is addressed because the index is too high, the original string will
	 * be returned unmodified.
	 * @param replaceEachCharacter If true, each single character in the specified group is
	 * replaced repeatedly by the replacement string.
	 * @throws PatternSyntaxException if compilation of the regex string fails.
	 */
	public SplitAndReplaceStringMasker(String regEx, String replacementString,
										int replaceGroup, boolean replaceEachCharacter)
											throws PatternSyntaxException {
		this(Pattern.compile(regEx), replacementString, replaceGroup, replaceEachCharacter);
	}
	
	/**
	 * Creates a new masker, using the given regex pattern to match delimiters. The matched
	 * delimiters split the input string into several groups. The group at the specified index
	 * is then replaced by a given string, either as a whole, or character for character.
	 * @param regEx The regex pattern used to find the delimiters splitting the input string.
	 * @param replacementString The string used to replace the specified group.
	 * @param replaceGroup A zero-based index addressing one of the split groups. If a
	 * non-existent group is addressed because the index is too high, the original string will
	 * be returned unmodified.
	 * @param replaceEachCharacter If true, each single character in the specified group is
	 * replaced repeatedly by the replacement string.
	 * @throws PatternSyntaxException if compilation of the regex string fails.
	 */
	public SplitAndReplaceStringMasker(Pattern regEx, String replacementString,
										int replaceGroup, boolean replaceEachCharacter) {
		if (replaceGroup < 0)
			throw new IllegalArgumentException("replaceGroup parameter must be >= 0!");
		this.regEx					= regEx;
		this.replacementString		= replacementString;
		this.replaceGroup			= replaceGroup;
		this.replacingEachCharacter	= replaceEachCharacter;
	}
	
	/**
	 * Splits the input string into groups and then replaces the specified group with the
	 * replacement string. If the specified group does not exist because too few groups were
	 * found, the input is returned unchanged.
	 */
	@Override
	public String mask(String input) {
		Matcher matcher = regEx.matcher(input);
		
		// This list will contain the substrings of the input string.  
		ArrayList<String> matchList = new ArrayList<String>();
		
		// This variable will contain the current position on the input string.
		int currentIndex = 0;
		
		while (matcher.find()) {			// Find the next regex match, then:
			
			matchList.add(input.substring(			// Add an even numbered position in the
				currentIndex, matcher.start()));	// matchList with the substring before
													// the new match.
			matchList.add(input.substring(			// Now add an odd numbered position in
				matcher.start(), matcher.end()));	// matchList with the matched substring,
													// i.e. the delimiter.
			currentIndex = matcher.end();	// Continue with the next match.
		}
		matchList.add(	// Finally, add the substring after the final matching.
			input.substring(currentIndex));
		
		// Return original input if there have not been found enough groups.
		if (replaceGroup * 2 >= matchList.size()) return input;
		
		if(replacingEachCharacter) {
			
			StringBuilder newGroup = new StringBuilder(matchList.get(replaceGroup * 2));
			int startingSize = newGroup.length();
			for (int i = startingSize; i > 0; --i)	// Go backwards through the characters and
				newGroup.replace(i - 1, i, replacementString);	// replace each of them.
			
			matchList.set(replaceGroup * 2, newGroup.toString());
			
		}
		else {
			matchList.set(replaceGroup * 2, replacementString);
		}
		
		StringBuilder maskedString = new StringBuilder();
		for (String s : matchList)
			maskedString.append(s);
		
		return maskedString.toString();
	}

	public Pattern getRegEx() {
		return regEx;
	}

	public void setRegEx(Pattern regEx) {
		this.regEx = regEx;
	}
	
	public void setRegEx(String regEx) throws PatternSyntaxException {
		this.regEx = Pattern.compile(regEx);
	}

	public String getReplacementString() {
		return replacementString;
	}

	public void setReplacementString(String replacementString) {
		this.replacementString = replacementString;
	}

	public int getReplaceGroup() {
		return replaceGroup;
	}

	public void setReplaceGroup(int replaceGroup) {
		this.replaceGroup = replaceGroup;
	}

	public boolean isReplacingEachCharacter() {
		return replacingEachCharacter;
	}

	public void setReplacingEachCharacter(boolean replacingEachCharacter) {
		this.replacingEachCharacter = replacingEachCharacter;
	}

}
