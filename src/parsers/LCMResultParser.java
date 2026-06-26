/**
 * parsing the result file of lcm output 
 * 
 */

package parsers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import datastructure.CNFInstance;
import datastructure.Pattern;
import datastructure.PatternsClassesSet;

public class LCMResultParser {
	private BufferedReader in;
	private CNFInstance cnfInstance;
	private String lcmResultPahtFile;
	private int nbValidPatterns;
	private final static boolean WRITE_RESULTAT_IN_FILE = false;

	public LCMResultParser(String lcmResultPahtFile, CNFInstance cnfInstance) {
		try {
			this.cnfInstance = cnfInstance;
			cnfInstance.getNbVariables();
			this.lcmResultPahtFile = lcmResultPahtFile;
			in = new BufferedReader(new FileReader(lcmResultPahtFile));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param line
	 *            represent the string which contains the pattern and frequency
	 * @return new obejct of the pattern
	 */
	private Pattern parsLinePattern(String line) {
		StringTokenizer st = new StringTokenizer(line);
		String token = "";
		int[] itemSet = new int[st.countTokens() - 1];
		int index = 0;
		while (!(token = st.nextToken()).startsWith("(")) {
			if (Integer.parseInt(token) > cnfInstance.getNbVariables()) {
				itemSet[index++] = Integer.parseInt(token) * -1
						+ cnfInstance.getNbVariables();
			} else {
				itemSet[index++] = Integer.parseInt(token);
			}
		}
		int frequency = Integer
				.parseInt(token.substring(1, token.length() - 1));
		int weight = frequency * (itemSet.length - 1) - itemSet.length - 1;
		Pattern pattern = new Pattern(itemSet, frequency, weight);
		return pattern;
	}

	/**
	 * get set of transactions id which including the pattern
	 * 
	 * @param idTransactions
	 * @param pattern
	 * @return
	 */
	private int[] parsIdTransactionsIncludingPattern(String idTransactions,
			Pattern pattern) {
		StringTokenizer st = new StringTokenizer(idTransactions);
		int[] idTransactionsIncludingPattern = new int[st.countTokens()];
		int index = 0;
		while (st.hasMoreTokens())
			idTransactionsIncludingPattern[index++] = Integer.parseInt(st
					.nextToken());

		return idTransactionsIncludingPattern;
	}

	/**
	 * 
	 * @param lcmResultPahtFile
	 *            represent the name of file which contains patterns list of cnf
	 * @return patterns list of cnf
	 * @throws IOException
	 */
	public PatternsClassesSet getFrequentClauses() throws IOException {
		// List<Pattern> patternsList = new LinkedList<Pattern>();
		PatternsClassesSet patternsClassesSet = new PatternsClassesSet();
		PrintWriter out;

		if (WRITE_RESULTAT_IN_FILE) {
			String outputName = "Result_" + lcmResultPahtFile.substring(11);
			out = new PrintWriter(new FileWriter(outputName));
		}
		String line = "";
		while ((line = in.readLine()) != null) {
			Pattern pattern = parsLinePattern(line.trim());
			String idTransactionsList = in.readLine().trim();
			int[] idTransactionsIncludingPattern = parsIdTransactionsIncludingPattern(
					idTransactionsList, pattern);
			pattern.setIdTransactionsIncludingPattern(idTransactionsIncludingPattern);
			// ajouter les patterns avec un poids sup à 0
			if (pattern.getWeight() > 0) {
				patternsClassesSet.addPattern(pattern);
				nbValidPatterns++;
			}
			// patternsList.add(pattern);
			if (WRITE_RESULTAT_IN_FILE)
				out.println(pattern);
		}

		in.close();
		if (WRITE_RESULTAT_IN_FILE)
			out.close();
		return patternsClassesSet;

	}

	public int getNbValidPatterns() {
		return nbValidPatterns;
	}

}
