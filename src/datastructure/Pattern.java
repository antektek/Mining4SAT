package datastructure;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import parsers.Toolkit;

public class Pattern implements Comparable<Pattern> {
	private int[] itemSet;
	private int[] OriginalItemSet;
	private int frequency;
	private int weight;
	// la transaction id=0 est la première clause non binaire dans le
	// fichier cnf
	private int[] idTransactionsIncludingPattern;
	private List<Pattern> neighborhoodPatterns = new LinkedList<Pattern>();
	private PatternsClasse patternClasse;

	public Pattern(int[] itemSet, int frequency, int weight) {
		this.itemSet = itemSet;
		OriginalItemSet=Arrays.copyOf(itemSet, itemSet.length);
		this.frequency = frequency;
		this.weight = weight;

	}

	public boolean isBelongsToPatternClasse() {
		return patternClasse != null ? true : false;
	}

	public boolean hasNePatternsWithoutClasse() {
		assert neighborhoodPatterns != null : "il faut d'abord affecter les patternes du voisinage ";
		for (Pattern nePattern : neighborhoodPatterns)
			if (!nePattern.isBelongsToPatternClasse())
				return true;
		return false;
	}

	public void setIdTransactionsIncludingPattern(
			int[] idTransactionsIncludingPattern) {
		this.idTransactionsIncludingPattern = idTransactionsIncludingPattern;
	}

	public void setPatternClasse(PatternsClasse patternClasse) {
		this.patternClasse = patternClasse;
	}

	public void setNeighborhoodPatterns(List<Pattern> neighborhoodPatterns) {
		this.neighborhoodPatterns = neighborhoodPatterns;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public void setNewItemsSet(int[] itemSet) {
		this.itemSet = itemSet;

	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public int getWeight() {
		return weight;
	}

	public int[] getIdTransactionsIncludingPattern() {
		return idTransactionsIncludingPattern;
	}

	public PatternsClasse getPatternClasse() {
		if (patternClasse != null)
			return patternClasse;
		else
			throw new RuntimeException("the pattern : " + this
					+ " does not belong to any class");
	}

	public List<Pattern> getNeighborhoodPatterns() {
		return neighborhoodPatterns;
	}

	public int getFrequency() {
		return frequency;
	}
	
	public int[] getOriginalItemSet() {
		return OriginalItemSet;
	}

	public int[] getItemSet() {
		return itemSet;
	}

	public String printNeighborhoodPatterns() {
		assert neighborhoodPatterns != null : "if faut d'abord affecter les patternes du voisinage ";
		String st = "liste des patterns voisins de " + this + ":\n";
		for (Pattern oPattern : neighborhoodPatterns)
			st += oPattern + "\n";
		return st;
	}

	@Override
	public String toString() {
		return Arrays.toString(itemSet) + " (frequency :" + frequency + ")"
				+ " (weight :" + weight + ")\n" + "idTransactions : "
				+ Arrays.toString(idTransactionsIncludingPattern) + "\n";

	}

	@Override
	public int compareTo(Pattern pattern) {
		return pattern.getWeight() - weight;
	}

	public void addNeighborhoodPatterns(Pattern pattern) {
		neighborhoodPatterns.add(pattern);

	}

	public boolean isNeighbor(Pattern pattern) {
		if (pattern.getOriginalItemSet() == OriginalItemSet)
			return false;
		for (int l : OriginalItemSet) {
			if (Toolkit.found(pattern.getOriginalItemSet(), l))
				return true;
		}
		return false;
	}
}
