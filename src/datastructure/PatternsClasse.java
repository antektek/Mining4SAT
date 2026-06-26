package datastructure;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class PatternsClasse {
	private static int nbClasses;
	private int idClasse;
	private String name;
	private List<Pattern> collectedPatterns = new LinkedList<Pattern>();

	public PatternsClasse() {
		idClasse = nbClasses++;
		name = "C" + idClasse;
	}

	public PatternsClasse(String name) {
		idClasse = nbClasses++;
		this.name = name;
	}

	public List<Pattern> getPatternsList() {
		return collectedPatterns;
	}

	public List<Pattern> getCollectedPatterns() {
		return collectedPatterns;
	}

	public String getName() {
		return name;
	}

	public void setPatternsList() {

		Collections.sort(collectedPatterns);
	}

	public boolean canContainsPattern(Pattern pattern) {
		int cpt = 0;
		for (Pattern pat : collectedPatterns) {
			if (pat.isNeighbor(pattern)) {
				cpt++;
//				pat.addNeighborhoodPatterns(pattern);
//				pattern.addNeighborhoodPatterns(pat);
			}
		}
		if (cpt == 0)
			return false;
		else
			return true;
	}

	public void addPattern(Pattern pattern) {
		collectedPatterns.add(pattern);

	}

	public void addPatterns(List<Pattern> patterns) {
		collectedPatterns.addAll(patterns);

	}

	@Override
	public String toString() {
		String st = "=======================================\n";
		st += "\t Nom de la classe : " + name + "\n";
		st += "=======================================\n\n";

		for (Pattern pattern : collectedPatterns)
			st += pattern + "\n";

		return st;
	}

}
