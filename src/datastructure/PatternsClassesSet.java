package datastructure;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class PatternsClassesSet {
	private List<PatternsClasse> patternClassesSet;

	// private Pattern[] allPatterns;

	public PatternsClassesSet() throws IOException {
		// this.allPatterns = patterns;
		patternClassesSet = new LinkedList<PatternsClasse>();
	}

	public void addPattern(Pattern pattern) {
		// l'ensmeble des classes ou le pattern peut etre ajouter
		List<PatternsClasse> patternsClasseListWichCanPattern = new LinkedList<PatternsClasse>();
		for (PatternsClasse patternsClasse : patternClassesSet) {
			if (patternsClasse.canContainsPattern(pattern)) {
				patternsClasseListWichCanPattern.add(patternsClasse);
			}
		}

		if (patternsClasseListWichCanPattern.size() == 0) {
			PatternsClasse patternsClasse = new PatternsClasse();
			patternsClasse.addPattern(pattern);
			patternClassesSet.add(patternsClasse);
			pattern.setPatternClasse(patternsClasse);

		} else if (patternsClasseListWichCanPattern.size() == 1) {
			patternsClasseListWichCanPattern.get(0).addPattern(pattern);
			pattern.setPatternClasse(patternsClasseListWichCanPattern.get(0));
		} else {
			PatternsClasse newPatternsClasse = new PatternsClasse();
			for (PatternsClasse patternsClasse : patternsClasseListWichCanPattern) {
				// A vérifier par rapport au référence java
				newPatternsClasse.addPatterns(patternsClasse
						.getCollectedPatterns());
				for(Pattern pat:patternsClasse.getCollectedPatterns())
					pat.setPatternClasse(newPatternsClasse);
			}
			for(Pattern pat:newPatternsClasse.getCollectedPatterns())
				pat.setPatternClasse(newPatternsClasse);
			patternClassesSet.add(newPatternsClasse);
			patternClassesSet.removeAll(patternsClasseListWichCanPattern);

		}
	}

	public int getNbClasses() {
		return patternClassesSet.size();
	}

	public List<PatternsClasse> getPatternClassesSet() {
		return patternClassesSet;
	}

	@Override
	public String toString() {
		String st = "Nombre total de classes :" + patternClassesSet.size()
				+ "\n\n";
		for (PatternsClasse patternsClasse : patternClassesSet)
			st += patternsClasse + "\n";
		return st;
	}

}
