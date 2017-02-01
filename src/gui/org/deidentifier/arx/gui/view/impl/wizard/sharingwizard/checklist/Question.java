package org.deidentifier.arx.gui.view.impl.wizard.sharingwizard.checklist;

import org.deidentifier.arx.gui.resources.Resources;

/**
 * represents a single question from the checklist
 *
 */
public class Question extends Item {
	/**
	 * the section this question is in
	 */
	private Section section;
	
	public enum Answer { YES, NO, N_A }

	/**
	 * the current answer
	 */
	public Answer answer;
	
	/**
	 * creates a new question in a section, from a line
	 * @param section the section this question is in
	 * @param line the line to parse
	 */
	public Question(Section section, String line) {
		super(line);
		this.section = section;
		this.answer = Answer.N_A;
	}

	/**
	 * creates a new question from a line, in a section
	 * @param section the section this question is in
	 * @param line the line to parse
	 * @return the question item
	 */
	public static Question itemFromLine(Section section, String line) {
		line = line.trim();
		Question item = new Question(section, line);
		return item;
	}
	
	public String getIdentifier() {
		return section.getIdentifier()+"."+identifier;
	}

	/**
	 * get the current answer as a string
	 * @return the string
	 */
	public String getAnswerString() {
		switch(answer) {
		case YES:
			return Resources.getMessage("RiskWizard.3");
		case NO:
			return Resources.getMessage("RiskWizard.4");
		default:
			return Resources.getMessage("RiskWizard.5");
		}
	}
	
	/**
	 * returns the question's score using the weight and taking the answer into account
	 * @return the score
	 */
	public double getScore() {
		switch(answer) {
		case YES:
			return this.getWeight();
		case NO:
			return -this.getWeight();
		default:
			return 0.0;
		}
	}

	@Override
	public String toString() {
		return "\n\t\tQuestion [id=" + this.getIdentifier() + ", answer=" + this.getAnswerString() + ", weight=" + this.getWeight() + ", title=" + title + "]";
	}
}
