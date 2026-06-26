package execution;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import parsers.CNFParser;
import parsers.Toolkit;
import datastructure.Pattern;
import datastructure.PatternsClasse;

public class SizeReduction {
	private List<PatternsClasse> patternClassesSet;
	private int[][] originalNonBinariesClausesSet;
	private List<int[]> addNonBinariesClausesSet = new LinkedList<int[]>();
	private CNFParser cnfParser;
	private int nbTotalVariables;

	public SizeReduction(List<PatternsClasse> patternClassesSet,
			CNFParser cnfParser) {
		this.patternClassesSet = patternClassesSet;
		this.originalNonBinariesClausesSet = cnfParser
				.getClausesNonBinariesSet();
		this.cnfParser = cnfParser;
	}

	public void launch() throws FileNotFoundException {
		int nbVariables = cnfParser.getCnfInstance().getNbVariables();
		for (PatternsClasse patternsClasse : patternClassesSet) {
			for (Pattern pattern : patternsClasse.getPatternsList()) {
				if (pattern.getWeight() > 0) {
					int[] itemsSet = pattern.getItemSet();
					boolean addVariable = false;
					for (int i = 0; i < pattern
							.getIdTransactionsIncludingPattern().length; i++) {

						int idTransaction = pattern
								.getIdTransactionsIncludingPattern()[i];
						if (idTransaction != -1) {
							addVariable = true;
							originalNonBinariesClausesSet[idTransaction] = buildNewClause(
									originalNonBinariesClausesSet[idTransaction],
									itemsSet, nbVariables + 1);

							// update transactions

							updatePatternsByTransactions(pattern,
									idTransaction, nbVariables + 1);
							pattern.getIdTransactionsIncludingPattern()[i] = -1;
						}
					}

					if (addVariable) {
						int[] addClause = Arrays.copyOf(itemsSet,
								itemsSet.length + 1);
						assert addClause.length == itemsSet.length + 1 : "anomalie dans la méthode Arrays.copyOf";
						addClause[addClause.length - 1] = -1 * (++nbVariables);
						addNonBinariesClausesSet.add(addClause);
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
		
//		for(Pattern p:pattern.getNeighborhoodPatterns())
//			System.out.println(p);
		for (Pattern pat : patternsClasse.getPatternsList()) {
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
//		System.out.println("=====================================");
//		System.out.println(pattern);
//		System.out.println(pattern.getNeighborhoodPatterns().size());
//		for(Pattern p:pattern.getNeighborhoodPatterns())
//			System.out.println(p);
//		System.out.println("+++++++++++++++++++++++++++++++++++++++++++");
		for (Pattern pat : patternsClasse.getPatternsList()) {
			if (pattern.isNeighbor(pat)) {
//				System.out.println(pat);
//				cpt++;
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
//		System.out.println(cpt);
//		System.out.println("=====================================");

	}

	public String buildCnfFile() throws IOException {
		String fileName = "NonBinary_Compressed_"
				+ cnfParser.getInstanceCNFName() + ".cnf";
		PrintWriter out = new PrintWriter(new FileWriter(fileName), true);
		try {
			int[][] binariesClauses = cnfParser.getClausesBinariesSet();
			int nbTotalClauses = +binariesClauses.length
					+ originalNonBinariesClausesSet.length
					+ addNonBinariesClausesSet.size();
			out.println("p cnf " + nbTotalVariables + " " + nbTotalClauses);
			for (int[] addNonBinaryClause : addNonBinariesClausesSet)
				out.println(Toolkit.printClause(addNonBinaryClause));
			for (int[] binaryClause : binariesClauses)
				out.println(Toolkit.printClause(binaryClause));
			for (int[] nonBinaryClause : originalNonBinariesClausesSet)
				out.println(Toolkit.printClause(nonBinaryClause));

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
