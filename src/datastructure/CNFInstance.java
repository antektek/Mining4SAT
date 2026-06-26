package datastructure;

public class CNFInstance {
	private int nbVariables;
	private int nbClauses;
	private int[][] clausesSet;
	private String name;

	public CNFInstance(int nbVariables, int nbClauses, String name) {
		this.nbVariables = nbVariables;
		this.nbClauses = nbClauses;
		this.name = name;
	}

	public void setClausesSet(int[][] calusesSet) {
		this.clausesSet = calusesSet;
	}

	public int[][] getClausesSet() {
		return clausesSet;
	}

	public int[] getClauseAt(int index) {
		return clausesSet[index];
	}

	public String getName() {
		return name;
	}

	public int getNbClauses() {
		return nbClauses;
	}

	public int getNbVariables() {
		return nbVariables;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		System.out.println(nbClauses);
		String st = "c instance name : " + name + "\np" + " cnf " + nbVariables
				+ " " + nbClauses + "\n";
		for (int[] idClause : clausesSet)
			st += printClause(idClause);
		return st;
	}

	private String printClause(int[] clause) {
		String st = "";
		for (int idLiteral : clause)
			st += idLiteral + " ";
		return st + "0\n";
	}
}
