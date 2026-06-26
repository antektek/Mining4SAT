package datastructure.literalscomparators;

import java.util.Comparator;
import datastructure.Literal;

public class NbOfOccurencesComparator implements Comparator<Literal> {

	@Override
	public int compare(Literal literal1, Literal literal2) {
		return literal1.getNbOfOccurrences() - literal2.getNbOfOccurrences();
	}

}
