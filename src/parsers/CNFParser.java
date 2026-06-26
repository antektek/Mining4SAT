/**
 * parsing of cnf instance and create the transactions database
 * of this cnf in the PrintWriter out 
 */
package parsers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import datastructure.CNFInstance;
import datastructure.Clause;

public class CNFParser {
	private BufferedReader in;
	private PrintWriter out;
	private final static boolean DISPLAY_PARSING = false;
	private final static boolean LOAD_CNF_INSTANCE = false;
	private String instancePath;
	int[] nbAppar;
	private final static boolean COLLECT_NON_BINARIESCLAUSE = true;
	private int[][] clausesSet = null;
	private int nbTransaction;
	private int transactionLenght;
	private List<Clause> collectedClausesNonBinariesSet = new LinkedList<Clause>();
	private int[][] nonBinariesClausesSet;
	private List<Clause> collectedClausesBinariesSet = new LinkedList<Clause>();
	private int[][] clausesBinariesSet;
	private String instanceCNFName;
	private CNFInstance cnfInstance;
	int idNonBinaryClause;
	int idBinaryClause;

	public CNFParser(String cnfFilePath,int lambda) {
		try {
			in = new BufferedReader(new FileReader(cnfFilePath));
			instancePath = cnfFilePath;
			instanceCNFName = cnfFilePath.substring(
					cnfFilePath.lastIndexOf('/') + 1,
					cnfFilePath.lastIndexOf('.'))+"L"+lambda;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getInstancePath() {
		return instancePath;
	}

	public CNFInstance getCnfInstance() {
		return cnfInstance;
	}

	public String getInstanceCNFName() {
		return instanceCNFName;
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
			int nbClauses = Integer.parseInt(st.nextToken());
			cnfInstance = new CNFInstance(nbVariables, nbClauses,
					instanceCNFName);
			if (LOAD_CNF_INSTANCE)
				clausesSet = new int[nbClauses][];
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
		String st = "", transaction = "";
		int[] clause = null;
		Set<Integer> collectedItems = new HashSet<Integer>();
		while (!(st = token.nextToken()).equals("0")) {
			int idLiteral = Integer.parseInt(st);
			if (LOAD_CNF_INSTANCE) {
				// clause[index++] = idLiteral;
				// collectedItems.add(idLiteral);
			}

			if (collectedItems.add(idLiteral)) {
				if (idLiteral > 0)
					transaction += st + " ";
				// gérer les nombres négratifs avec un décalage
				else if (Integer.parseInt(st) < 0)
					transaction += idLiteral * -1
							+ cnfInstance.getNbVariables() + " ";
				else
					throw new RuntimeException(
							"Impossible davoir un literal avec id=0");
			}
		}

		clause = Toolkit.setToArray(collectedItems);
		if (LOAD_CNF_INSTANCE)
			clausesSet[idClause] = clause;
		if (clause.length > 2) {
			collectedClausesNonBinariesSet.add(new Clause(idNonBinaryClause++,
					clause));
			nbTransaction++;
			transactionLenght += transaction.length();

			for (int i : clause) {
				int item = (i > 0) ? i : i * -1 + cnfInstance.getNbVariables();
				nbAppar[item] = nbAppar[item] + 1;
			}
			out
					.println(transaction.substring(0, transaction.length()));
		} else {
			if (COLLECT_NON_BINARIESCLAUSE) {
				collectedClausesBinariesSet.add(new Clause(idBinaryClause++,
						clause));
				// outBinary
				// .println(transaction.substring(0, transaction.length()));
			}
		}

	}

	public int getNbTransaction() {
		return nbTransaction;
	}

	public int getAVGTransactionsLenght() {
		return transactionLenght / nbTransaction;
	}

	/**
	 * 
	 * @return the file name of transactions database corresponding cnf instance
	 * @throws IOException
	 */
	public String buildtdatabase() throws IOException {
		try {

			String tDataBaseOut = "tdb_nonBinary_" + instanceCNFName
					+ ".txt";
			out = new PrintWriter(
					new FileWriter(tDataBaseOut), true);
			String line = "";

			if (DISPLAY_PARSING)
				System.out.println("Entete du fichier :");
			// ignore comments
			while ((line = in.readLine().trim()) != null
					&& (line.trim().startsWith("c")))
				if (DISPLAY_PARSING)
					System.out.println(line);

			// get nbvariables and nbClauses
			parsHeaderFileCNF(line);
			nbAppar = new int[cnfInstance.getNbVariables() * 2 + 1];
			if (DISPLAY_PARSING)
				System.out.println("Ensemble de clauses :");
			int idClause = 0;
			while ((line = in.readLine()) != null) {
				parsingClause(line.trim(), idClause++);
			}
			nonBinariesClausesSet = Toolkit
					.buildNonBinariesclausesSet(collectedClausesNonBinariesSet);
			clausesBinariesSet = Toolkit
					.buildNonBinariesclausesSet(collectedClausesBinariesSet);
			if (LOAD_CNF_INSTANCE)
				cnfInstance.setClausesSet(clausesSet);
			return tDataBaseOut;
		} finally {
			in.close();
			out.close();
		}
	}

	public int[][] getClausesNonBinariesSet() {
		return nonBinariesClausesSet;
	}
	
	public int[][] getClausesBinariesSet() {
		return clausesBinariesSet;
	}

	/**
	 * faire des testes concernant le parseur
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		CNFParser cnfParser = new CNFParser(args[0],Integer.parseInt(args[1]));
		cnfParser.buildtdatabase();
		for (int[] a : cnfParser.getClausesNonBinariesSet())
			System.out.println(Arrays.toString(a));
		for (int[] a : cnfParser.getClausesBinariesSet())
			System.out.println(Arrays.toString(a));
		// mettre la variable LOAD_CNF_INSTANCE à vraie avant d'imprimer
		// l'instance
		// System.out.print(cnfParser.getCnfInstance());
	}
}
