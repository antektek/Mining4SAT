package datastructure.literalscomparators;

import java.util.Comparator;

import datastructure.Literal;

public class AbsComparator implements Comparator<Literal> {

	@Override
	public int compare(Literal literal1, Literal literal2) {
		return   Math.abs(literal2.getId()) - Math.abs(literal1.getId());
	}

}
