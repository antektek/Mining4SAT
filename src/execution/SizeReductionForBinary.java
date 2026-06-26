package execution;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import parsers.CNFParserBinariesClauses;
import parsers.Toolkit;
import datastructure.Pattern;
import datastructure.PatternsClasse;

public class SizeReductionForBinary {
	private List<PatternsClasse> patternClassesSet;
	// private int[][] originalBinariesClausesSet;
	private List<int[]> addBinariesClausesSet = new LinkedList<int[]>();
	private List<int[]> addNonBinariesClausesSet = new LinkedList<int[]>();
	private int[][] originalNonBinariesClausesSet;
	private int nbCV;
	private CNFParserBinariesClauses cnfParserBinariesClauses;
	private int nbTotalVariables;

	public SizeReductionForBinary(List<PatternsClasse> patternClassesSet,
			CNFParserBinariesClauses cnfParserBinariesClauses) {
		this.patternClassesSet = patternClassesSet;
		this.originalNonBinariesClausesSet = cnfParserBinariesClauses
				.getClausesNonBinaries();
		this.cnfParserBinariesClauses = cnfParserBinariesClauses;
	}

	public void launch() throws FileNotFoundException {
		int nbVariables = cnfParserBinariesClauses.getCnfInstance()
				.getNbVariables();
		for (PatternsClasse patternsClasse : patternClassesSet) {
			for (Pattern pattern : patternsClasse.getPatternsList()) {
				if (pattern.getWeight() > 0) {
					int[] itemsSet = pattern.getItemSet();
					boolean addVariable = false;
					boolean addVariableForBinary = false;
					boolean addVariableForNonBinary = false;
					for (int i = 0; i < pattern
							.getIdTransactionsIncludingPattern().length; i++) {

						int idTransaction = pattern
								.getIdTransactionsIncludingPattern()[i];
						if (idTransaction != -1) {
							addVariable = true;
							if (idTransaction >= originalNonBinariesClausesSet.length) {
								addVariableForBinary = true;
								cnfParserBinariesClauses
										.setTransactionsAt(
												idTransaction,
												buildNewClause(
														cnfParserBinariesClauses
																.getTransactionsAt(idTransaction),
														itemsSet,
														nbVariables + 1));

								// update transactions

								updatePatternsByTransactions(pattern,
										idTransaction, nbVariables + 1);
								pattern.getIdTransactionsIncludingPattern()[i] = -1;
							} else if (idTransaction >= 0
									&& idTransaction < originalNonBinariesClausesSet.length) {
								addVariableForNonBinary = true;
								originalNonBinariesClausesSet[idTransaction] = buildNewClause(
										originalNonBinariesClausesSet[idTransaction],
										itemsSet, nbVariables + 1);

								// update transactions

								updatePatternsByTransactions(pattern,
										idTransaction, nbVariables + 1);
								pattern.getIdTransactionsIncludingPattern()[i] = -1;
							} else
								throw new RuntimeException(
										"idTransaction out of boudns !");
						}
					}
					if (addVariable) {
						int[] addClause = Arrays.copyOf(itemsSet,
								itemsSet.length + 1);
						assert addClause.length == itemsSet.length + 1 : "anomalie dans la méthode Arrays.copyOf";
						addClause[addClause.length - 1] = -1 * (++nbVariables);
						if (addVariableForNonBinary)
							addNonBinariesClausesSet.add(addClause);
						if (addVariableForBinary)
							addBinariesClausesSet.add(addClause);
						if (addVariableForNonBinary && addVariableForBinary)
							nbCV++;
					}
					updatePatternsByInclusion(pattern, nbVariables);
					Collections.sort(patternsClasse.getPatternsList());
				}
			}
		}
		nbTotalVariables = nbVariables;
	}

	private void updatePatternsByInclusion(Pattern pattern, int idVariable) {
		PatternsClasse patternsClasse = pattern.getPatternClasse();
		for (Pattern pat : patternsClasse.getCollectedPatterns()) {
			if (pat.isNeighbor(pattern)
					&& Toolkit.subSet(pattern.getItemSet(), pat.getItemSet())) {
				int[] newItemSet = new int[pat.getItemSet().length
						- pattern.getItemSet().length + 1];
				int index = 0;
				for (int idLiteral : pat.getItemSet()) {
					if (Toolkit.notExist(idLiteral, pattern.getItemSet())) {
						newItemSet[index++] = idLiteral;
					}
				}
				newItemSet[index] = idVariable;
				pat.setNewItemsSet(newItemSet);
			}
		}
	}

	private void updatePatternsByTransactions(Pattern pattern,
			int idTransaction, int idVariable) {
		PatternsClasse patternsClasse = pattern.getPatternClasse();
		for (Pattern pat : patternsClasse.getCollectedPatterns()) {
			if (pat.isNeighbor(pattern)) {
				int[] transactions = pat.getIdTransactionsIncludingPattern();
				for (int i = 0; i < transactions.length; i++)
					if (transactions[i] == idTransaction
							&& !Toolkit.subSet(pattern.getItemSet(),
									pat.getItemSet())) {

						transactions[i] = -1;
						// nouvelle frequency
						int newFrequency = pat.getFrequency() - 1;
						pat.setFrequency(newFrequency);

						// nouveau poid
						int newWeight = newFrequency
								* (pat.getItemSet().length - 1)
								- pat.getItemSet().length - 1;
						pat.setWeight(newWeight);

						break;
					}
			}
		}
	}
	
	public int getNbCV() {
		return nbCV;
	}

	public String buildCnfFile() throws IOException {
		String fileName = "compressed_"
				+ cnfParserBinariesClauses.getInstanceCNFName() + ".cnf";
		PrintWriter out = new PrintWriter(new FileWriter(fileName), true);
		Map<Integer, int[]> mapOfBinariesTransactions = cnfParserBinariesClauses
				.getMapOfTransactions();
		int nbTotalClauses = 0;

		for (Entry<Integer, int[]> entry : mapOfBinariesTransactions.entrySet()) {
			nbTotalClauses += entry.getValue().length;
		}

		for (int[] clause : addBinariesClausesSet)
			nbTotalClauses += clause.length - 1;

		nbTotalClauses += cnfParserBinariesClauses.getDeadBinariesClauses()
				.size()
				+ cnfParserBinariesClauses.getCollectedUnariesClauses().size()
				+ originalNonBinariesClausesSet.length
				+ addNonBinariesClausesSet.size();
		try {

			out.println("p cnf " + nbTotalVariables + " " + nbTotalClauses);

			for (int[] unariesClauses : cnfParserBinariesClauses.getCollectedUnariesClauses())
				out.println(Toolkit.printClause(unariesClauses));
			
			for (Entry<Integer, int[]> entry : mapOfBinariesTransactions
					.entrySet()) {
				int idLiteral = entry.getKey();
				int[] clause = entry.getValue();
				for (int i = 0; i < clause.length; i++) {
					out.println(idLiteral + " " + clause[i]*-1 + " 0");
				}
			}

			for (int[] nonBinaryClause : originalNonBinariesClausesSet)
				out.println(Toolkit.printClause(nonBinaryClause));
			
			for (int[] addNonBinaryClause : addNonBinariesClausesSet)
				out.println(Toolkit.printClause(addNonBinaryClause));

			for (int[] clause : cnfParserBinariesClauses
					.getDeadBinariesClauses()) {
				out.println(Toolkit.printNonUsedClause(clause));
			}


			for (int[] clause : addBinariesClausesSet) {
				for (int i = 0; i < clause.length - 1; i++) {
					out.println(clause[clause.length - 1]*-1 + " " + clause[i]*-1 
							+ " 0");
				}
			}

		} finally {
			out.close();
		}
		return fileName;
	}

	private int[] buildNewClause(int[] originalClause, int[] itemsSet,
			int idNewVariable) {
		int[] newClause = new int[originalClause.length - itemsSet.length + 1];
		int i = 0;
		for (int idLiteral : originalClause) {
			if (Toolkit.notExist(idLiteral, itemsSet)) {
				newClause[i++] = idLiteral;
			}
		}
		newClause[i] = idNewVariable;
		return newClause;
	}

}
