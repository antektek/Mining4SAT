package datastructure;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import parsers.Toolkit;

public class Literal {

	private int id;
	private String name;
	private List<int[]> clauseContainingLiteral;

	public Literal(int id) {
		this.id = id;
		name = Integer.toString(id);
		clauseContainingLiteral = new LinkedList<int[]>();
	}

	public int getId() {
		return id;
	}

	public List<int[]> getClauseContainingLiteral() {
		return clauseContainingLiteral;
	}

	// A utiliser à la fin de la collection de tous les clauses contenant le
	// littéral en question
	public int getNbOfOccurrences() {
		return clauseContainingLiteral.size();
	}

	public int[] getTransaction() {
		Set<Integer> transaction = new HashSet<Integer>();
		List<int[]> usedClause = new LinkedList<int[]>();
		// System.out.println("clauses voisines : ");
		for (int[] clause : clauseContainingLiteral) {
			// System.out.println(Arrays.toString(clause));
			if (clause[clause.length - 1] == 0) {
				int neighboorLiteral = (id == clause[0]) ? clause[1]
						: clause[0];
				transaction.add(neighboorLiteral*-1);
				usedClause.add(clause);
			}
		}
		int[] t = Toolkit.setToArray(transaction);
		if (t.length > 2) {
			for (int[] clause : usedClause)
				clause[clause.length - 1] = -1;
			return t;
		} else
			return null;
	}

	public List<int[]> getNonUsedClauses() {
		List<int[]> nonUsedClauses = new LinkedList<int[]>();
		for (int[] clause : clauseContainingLiteral) {
			if (clause[clause.length - 1] == 0) {
				nonUsedClauses.add(clause);
				clause[clause.length - 1]=-1;
			}
		}
		return nonUsedClauses;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		String st = "Literal id : " + id + "\n";
		for (int[] clause : clauseContainingLiteral)
			st += Toolkit.printNeighboorClause(clause) + "\n";
		return st;
	}

	public void addNeighborClause(int[] clause) {
		clauseContainingLiteral.add(clause);
	}

}
