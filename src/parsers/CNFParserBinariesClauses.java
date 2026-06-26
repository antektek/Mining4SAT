package parsers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import datastructure.CNFInstance;
import datastructure.Literal;
import datastructure.literalscomparators.LexicographiqueComparator;

public class CNFParserBinariesClauses {
	private static final boolean DISPLAY_PARSING = false;
	private BufferedReader in;
	private String instanceCNFName;
	private Comparator<Literal> comparator = new LexicographiqueComparator();
	private Map<Integer, Literal> collectedLiterals = new HashMap<Integer, Literal>();
	private Map<Integer, int[]> transactions = new HashMap<Integer, int[]>();
	private List<int[]> collectedNonBinariesClauses = new LinkedList<int[]>();
	private int[][] clausesNonBinaries;
	private int lambdaMinMax;
	private int lambdaMoy;
	private List<int[]> collectedUnariesClauses = new LinkedList<int[]>();
	private String filePathe;
	private List<int[]> deadBinariesClauses = new LinkedList<int[]>();
	private Map<Integer, Integer> collectedBinariesClauses = new HashMap<Integer, Integer>();
	int nbBinariesClauses;
	int[] nbOccurenceLiteral;
	// private int[][] clausesBinaries;
	private CNFInstance cnfInstance;
	private int nbTransaction;
	private int transactionLenght;

	private Literal getLiteral(int idLiteral) {
		Literal literal = collectedLiterals.get(idLiteral);
		if (literal == null) {
			literal = new Literal(idLiteral);
			collectedLiterals.put(idLiteral, literal);
		}
		return literal;
	}

	public CNFParserBinariesClauses(String cnfFilePath, int lambda) {
		try {
			in = new BufferedReader(new FileReader(cnfFilePath));
			filePathe = cnfFilePath;
			instanceCNFName = cnfFilePath.substring(
					cnfFilePath.lastIndexOf('/') + 1,
					cnfFilePath.lastIndexOf('.'))
					+ "L" + lambda;
			;
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public String getFilePathe() {
		return filePathe;
	}

	/**
	 * parsing a header file CNF.
	 * 
	 * @param line
	 *            represent the first line of CNF file and it must start with
	 *            character p
	 */
	private void parsHeaderFileCNF(String line) {
		if (line.startsWith("p")) {
			if (DISPLAY_PARSING)
				System.out.println(line);
			StringTokenizer st = new StringTokenizer(line);
			// ignore tokens : p and cnf
			st.nextToken();
			st.nextToken();
			int nbVariables = Integer.parseInt(st.nextToken());
			nbOccurenceLiteral = new int[nbVariables * 2 + 1];
			int nbClauses = Integer.parseInt(st.nextToken());
			cnfInstance = new CNFInstance(nbVariables, nbClauses,
					instanceCNFName);
		} else
			throw new RuntimeException(
					"fichier ne commençant pas avec le caractère p");
	}

	/**
	 * parsing the clause set from the CNF file
	 * 
	 * @param line
	 *            represent the clause
	 * @param idClause
	 *            the clause identifier
	 */
	private void parsingClause(String line, int idClause) {
		assert !line.startsWith("c") : "Des commentaires entre les clauses !";
		if (DISPLAY_PARSING)
			System.out.println(line);
		assert line.lastIndexOf('0') != line.length() : "Une clause qui ne finit pas par 0";
		StringTokenizer token = new StringTokenizer(line);
		String st = null;
		Set<Integer> collectedLiteralsInclause = new HashSet<Integer>();
		while (!(st = token.nextToken()).equals("0")) {
			int idLiteral = Integer.parseInt(st);
			assert idLiteral != 0 : "Impossible davoir un literal avec id=0";
			if (collectedLiteralsInclause.add(idLiteral)) {
				getLiteral(idLiteral);
				if (idLiteral > 0)
					nbOccurenceLiteral[idLiteral]++;
				else
					nbOccurenceLiteral[idLiteral * -1
							+ cnfInstance.getNbVariables()]++;
			}
		}

		int[] clause = buildClause(collectedLiteralsInclause);
		if (DISPLAY_PARSING)
			Toolkit.printClause(clause);
		if (collectedLiteralsInclause.size() == 2)
			nbBinariesClauses++;
		else if (collectedLiteralsInclause.size() > 2)
			collectedNonBinariesClauses.add(clause);
		else if (collectedLiteralsInclause.size() == 1)
			collectedUnariesClauses.add(clause);
	}

	/**
	 * Build clause using an object set of <strong> Literal</strong> such that
	 * the last element of the table represent a flag. This flag is used after
	 * to create transactions database. It's equal to 1 if the clause is already
	 * used in a transaction. In the other case (when the flag is equal to 0),
	 * the clause can be used to formulate a new transaction. Firstly, all flags
	 * are initialized with 0. Note that, we don't need to the flag in the
	 * non-binaries clauses...
	 * 
	 * @return a integer table clause + flag
	 */
	private int[] buildClause(Set<Integer> literalsSet) {
		int[] clause = null;
		if (literalsSet.size() == 2) {
			clause = new int[3];
			int index = 0;
			for (int idLiteral : literalsSet) {
				clause[index++] = idLiteral;
				getLiteral(idLiteral).addNeighborClause(clause);
			}
		} else {
			clause = new int[literalsSet.size()];
			int index = 0;
			for (int idLiteral : literalsSet)
				clause[index++] = idLiteral;

		}
		return clause;
	}

	private List<Literal> parsCNFFile() throws IOException {

		String line = null;
		if (DISPLAY_PARSING)
			System.out.println("Entete du fichier :");
		// ignore comments
		while ((line = in.readLine().trim()) != null
				&& (line.trim().startsWith("c")))
			if (DISPLAY_PARSING)
				System.out.println(line);
		// get nbvariables and nbClauses
		parsHeaderFileCNF(line);
		if (DISPLAY_PARSING)
			System.out.println("Ensemble de clauses :");
		int idClause = 0;

		while ((line = in.readLine()) != null) {
			parsingClause(line.trim(), idClause++);
		}
		clausesNonBinaries = Toolkit.toArray(collectedNonBinariesClauses);
		List<Literal> literals = new LinkedList<Literal>(
				collectedLiterals.values());
		Collections.sort(literals, comparator);
		return literals;
	}

	public String buildTransactionsDatabase() throws IOException {
		String tDataBase = "tdb_" + instanceCNFName + ".txt";
		PrintWriter out = new PrintWriter(new FileWriter(tDataBase), true);
		List<Literal> literals = parsCNFFile();

		for (int[] clause : collectedNonBinariesClauses) {
			int[] transaction = createNonBinaryTransaction(clause);
			nbTransaction++;
			transactionLenght += clause.length;
			out.println(Toolkit.printItemsSet(transaction));
		}

		int i = collectedNonBinariesClauses.size();
		for (Literal literal : literals) {
			// System.out.println(literal.getId());
			int[] clause = literal.getTransaction();
			if (clause != null) {
				nbTransaction++;
				transactionLenght += clause.length;
				transactions.put(literal.getId(), clause);
				int[] transaction = createBinaryTransaction(clause);
				collectedBinariesClauses.put(i++, literal.getId());
				out.println(Toolkit.printItemsSet(transaction));
			}

		}
		// add non used clause
		for (Literal literal : literals) {
			List<int[]> nonUsedClause = literal.getNonUsedClauses();
			if (!nonUsedClause.isEmpty())
				deadBinariesClauses.addAll(nonUsedClause);
		}
		int sum = 0, total = 0, max = 0, min = Integer.MAX_VALUE;
		for (int a : nbOccurenceLiteral) {
			if (a != 0) {
				sum += a;
				total++;
				if (a < min)
					min = a;
				if (a > max)
					max = a;

			}
		}
		lambdaMinMax = (min + max / 2);
		lambdaMoy = (sum / total);
		out.close();
		return tDataBase;
	}

	public int getLambdaMinMax() {
		return lambdaMinMax;
	}

	public int getLambdaMoy() {
		return lambdaMoy;
	}

	public int getNbTransaction() {
		return nbTransaction;
	}

	public int[][] getClausesNonBinaries() {
		return clausesNonBinaries;
	}

	public List<int[]> getCollectedUnariesClauses() {
		return collectedUnariesClauses;
	}

	public int getAVGTransactionsLenght() {
		return transactionLenght / nbTransaction;
	}

	// public int[][] getClausesBinaries() {
	// return clausesBinaries;
	// }

	public List<int[]> getDeadBinariesClauses() {
		return deadBinariesClauses;
	}

	public List<int[]> getCollectedNonBinariesClauses() {
		return collectedNonBinariesClauses;
	}

	public int[] getTransactionsAt(int index) {
		return transactions.get(collectedBinariesClauses.get(index));
	}

	public void setTransactionsAt(int index, int[] transaction) {
		transactions.put(collectedBinariesClauses.get(index), transaction);
	}

	public CNFInstance getCnfInstance() {
		return cnfInstance;
	}

	public String getInstanceCNFName() {
		return instanceCNFName;
	}

	public Map<Integer, int[]> getMapOfTransactions() {
		return transactions;
	}

	private int[] createNonBinaryTransaction(int[] clause) {
		int[] transaction = new int[clause.length];
		int i = 0;
		for (int literal : clause) {
			if (literal < 0)
				transaction[i++] = literal * -1 + cnfInstance.getNbVariables();
			else if (literal > 0)
				transaction[i++] = literal;
			else
				throw new RuntimeException("literal avec id 0 !");
		}
		// System.out.println("transaction : " + Arrays.toString(transaction));

		return transaction;

	}

	private int[] createBinaryTransaction(int[] clause) {
		int[] transaction = new int[clause.length];
		int i = 0;
		for (int literal : clause) {
			if (literal < 0)
				transaction[i++] = literal * -1 + cnfInstance.getNbVariables();
			else if (literal > 0)
				transaction[i++] = literal;
			else
				throw new RuntimeException("literal avec id 0 !");
		}
		// System.out.println("transaction : " + Arrays.toString(transaction));

		return transaction;

	}

	public Map<Integer, Literal> getCollectedLiterals() {
		return collectedLiterals;
	}

	// testes unitaires
	public static void main(String[] args) throws IOException {
		CNFParserBinariesClauses cnfParserBinariesClauses = new CNFParserBinariesClauses(
				args[0], Integer.parseInt(args[1]));
		cnfParserBinariesClauses.buildTransactionsDatabase();
		System.out.println(cnfParserBinariesClauses.getNbTransaction());
		System.out.println(cnfParserBinariesClauses.getAVGTransactionsLenght());
		// System.out.println(cnfParserBinariesClauses.getDeadBinariesClauses()
		// .isEmpty());
		// for (Entry<Integer, int[]> entry :
		// cnfParserBinariesClauses.transactions
		// .entrySet()) {
		// int idLiteral = entry.getKey();
		// int[] clause = entry.getValue();
		// System.out.println(idLiteral);
		// System.out.println(Arrays.toString(clause));
		// }
		//
		//
		// System.out.println(Arrays.toString(cnfParserBinariesClauses.getTransactionsAt(0)));
		// System.out.println(Arrays.toString(cnfParserBinariesClauses.getTransactionsAt(1)));
		// System.out.println(Arrays.toString(cnfParserBinariesClauses.getTransactionsAt(2)));

		// List<Literal> literals = new LinkedList<Literal>(
		// cnfParserBinariesClauses.getCollectedLiterals().values());
		// Collections.sort(literals, cnfParserBinariesClauses.comparator);
		// for (Literal literal : literals) {
		// System.out.println(literal);
		// for (int[] c : literal.getNonUsedClauses())
		// System.out.println(Arrays.toString(c));
		// }

	}

}
