package execution;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import datastructure.PatternsClassesSet;
import datastructure.literalscomparators.NbOfOccurencesComparator;
import parsers.CNFParserBinariesClauses;
import parsers.LCMResultParser;

public class FrequentPatternForBinary {
	private String tdbFilePath;
	private int lambda;
	String ResultOutput;
	private final static String EXECUTABLE_PATH = "lcm/lcm";
	// voir la documentation de lcm pour les options
	private final static String OPTIONS1 = "C_fI";
	private final static String OPTIONS2 = "-l";
	private final static String OPTIONS2Value = "2";
	private final static String OPTIONS3 = "-#";
	private final static String OPTIONS3Value = "50000";
	private final static boolean PRINT_CLASSES = false;
	private static int NBCV;
	// remove intermediate file
	protected static final boolean RIV = true;

	public FrequentPatternForBinary(String tdbFilePath, int lambda) {
		this.lambda = lambda;
		this.tdbFilePath = tdbFilePath;
		// ResultOutput = "-";
		ResultOutput = "Result_tmp_" + tdbFilePath;
	}

	private String[] buildLcmCMD() {
		String[] lcmCMD = new String[] { EXECUTABLE_PATH, OPTIONS1, OPTIONS2,
				OPTIONS2Value, /*OPTIONS3, OPTIONS3Value,*/ tdbFilePath,
				Integer.toString(lambda), ResultOutput };
		return lcmCMD;
	}

	static String launchBinary(CNFParserBinariesClauses cnfParserBinariesClauses, int lambda) throws IOException,
			InterruptedException {

		System.out.println();
		System.out.print("##### Build transactions database #####.....");
		String tdbFileName = cnfParserBinariesClauses
				.buildTransactionsDatabase();
		System.out.println("[OK]");
		if(cnfParserBinariesClauses.getNbTransaction()==0){
			System.out.println("no binary clause for the instance "
					+ cnfParserBinariesClauses.getFilePathe());
			if (RIV) {
				new File(tdbFileName).delete();
			}
			return cnfParserBinariesClauses.getCnfInstance().getName();
		}
//		System.out.println("nbTransactions = "
//				+ cnfParserBinariesClauses.getNbTransaction());
//		
//		System.out.println("AVGTransactionsLenght = "
//				+ cnfParserBinariesClauses.getAVGTransactionsLenght());
		FrequentPatternForBinary frequentPatterns = new FrequentPatternForBinary(
				tdbFileName, lambda);
		System.out.print("##### Execute lcm #####.....");
		ExecuteLcm executeLCM = new ExecuteLcm(frequentPatterns.buildLcmCMD());
		// recupérer le fichier tdatabase qui correspond a la cnf donnée
		// en paramètre.
		String tmpResultFileName = executeLCM.launchCMD(tdbFileName);
		System.out.println("[OK]");
		executeLCM = null;
		System.out
				.print("##### Parse lcm result & Build classes patterns #####.....");
		LCMResultParser lcmResultParser = new LCMResultParser(
				tmpResultFileName, cnfParserBinariesClauses.getCnfInstance());

		PatternsClassesSet patternsClassesSet = lcmResultParser
				.getFrequentClauses();
		System.out.println("[OK]");
		System.out.println();
		System.out.println("nbValidPatterns = "
				+ lcmResultParser.getNbValidPatterns());
		System.out.println("nbClasses = " + patternsClassesSet.getNbClasses());
		if (patternsClassesSet.getNbClasses() == 0) {
			System.out.println("no reduction for the instance "
					+ cnfParserBinariesClauses.getFilePathe().substring(0, cnfParserBinariesClauses.getFilePathe().lastIndexOf(".")));
			if (RIV) {
				new File(tmpResultFileName).delete();
				new File(tdbFileName).delete();
			}
			return cnfParserBinariesClauses.getFilePathe();
		}
		System.out.println();
		if (PRINT_CLASSES) {
			PrintWriter out = new PrintWriter(new FileWriter("Patterns_Classe_"
					+ tdbFileName.substring(4)), true);
			out.println(patternsClassesSet);
		}
		System.out.print("##### Reduction of binaries clauses #####.....");
		SizeReductionForBinary sizeReduction = new SizeReductionForBinary(
				patternsClassesSet.getPatternClassesSet(),
				cnfParserBinariesClauses);
		sizeReduction.launch();
		
		System.out.println("[OK]");
		System.out.print("##### Writing of output #####.....");
		String file = sizeReduction.buildCnfFile();
		frequentPatterns.NBCV=sizeReduction.getNbCV();
		System.out.println("[OK]");

		if (RIV) {
			new File(tmpResultFileName).delete();
			new File(tdbFileName).delete();
		}
		return file;
	}
	
	public static int getNbCV() {
		return NBCV;
	};
	// public static void main(String[] args) throws IOException,
	// InterruptedException {
	// if (args.length != 2)
	// System.out.println("usage: : <cnfFile> <lambda>");
	// else {
	// if (!args[0].substring(args[0].lastIndexOf(".")).equals(".cnf")) {
	// System.out.println(args[0] + " is not cnf file");
	// } else {
	// int lambda = 0;
	// try {
	// lambda = Integer.parseInt(args[1]);
	// } catch (IllegalArgumentException e) {
	// System.err
	// .println("The seconde argument must be an integer");
	// System.exit(1);
	//
	// }
	//
	// }
	// }
	// }

}
