package org.deidentifier.arx.gui.view.impl.wizard.sharingwizard.checklist;

/**
 * represents a single question from the checklist
 *
 */
public class Question extends Item {
	private Section section;
	public enum Answer { YES, NO, N_A }

	public Answer answer;

	public Question(Section section, String line) {
		super(line);
		this.section = section;
		this.answer = Answer.N_A;
	}

	public static Question itemFromLine(Section section, String line) {
		line = line.trim();
		Question item = new Question(section, line);
		return item;
	}
	
	public String getIdentifier() {
		return section.getIdentifier()+"."+identifier;
	}

	public String getAnswerString() {
		switch(answer) {
		case YES:
			return "YES";
		case NO:
			return "NO";
		default:
			return "N/A";
		}
	}
	
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
