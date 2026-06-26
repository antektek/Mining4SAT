package datastructure;

public class Clause {
	private int id;
	private int[] clause;

	public Clause(int id, int[] clause) {
		this.id = id;
		this.clause = clause;
	}

	public int getId() {
		return id;
	}

	public int[] getClause() {
		return clause;
	}
}