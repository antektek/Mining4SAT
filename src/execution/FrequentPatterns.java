package execution;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.DecimalFormat;
import datastructure.PatternsClassesSet;
import parsers.CNFParser;
import parsers.CNFParserBinariesClauses;
import parsers.FileSizeFormater;
import parsers.LCMResultParser;

public class FrequentPatterns {
	private String tdbFilePath;
	private int lambda;
	String ResultOutput;
	private final static String EXECUTABLE_PATH = "lcm/lcm";
	// voir la documentatio+ " & "n de lcm pour les options
	private final static String OPTIONS1 = "C_fI";
	private final static String OPTIONS2 = "-l";
	private final static String OPTIONS2Value = "2";
	private final static String OPTIONS3 = "-#";
	private final static String OPTIONS3Value = "50000";
	private final static boolean PRINT_CLASSES = false;
	// remove intermediate file
	protected static final boolean RIV = true;

	public FrequentPatterns(String tdbFilePath, int lambda) {
		this.lambda = lambda;
		this.tdbFilePath = tdbFilePath;
		// ResultOutput = "-";
		ResultOutput = "Result_tmp_" + tdbFilePath;
	}

	private String[] buildLcmCMD() {
		String[] lcmCMD = new String[] { EXECUTABLE_PATH, OPTIONS1, OPTIONS2,
				OPTIONS2Value, /* OPTIONS3, OPTIONS3Value, */tdbFilePath,
				Integer.toString(lambda), ResultOutput };
		return lcmCMD;
	}

	static private String launchNonBinary(CNFParser cnfParser, String[] args,
			int lambda) throws IOException, InterruptedException {

		System.out.println();
		System.out.print("##### Build transactions database #####.....");
		String tdbFileName = cnfParser.buildtdatabase();

		System.out.println("[OK]");
		if (cnfParser.getNbTransaction() == 0) {
			System.out.println("no binary clause for the instance "
					+ cnfParser.getInstancePath());
			if (RIV) {
				new File(tdbFileName).delete();
			}
			return cnfParser.getInstancePath();
		}
		System.out.println("nbTransactions = " + cnfParser.getNbTransaction());
		System.out.println("AVGTransactionsLenght = "
				+ cnfParser.getAVGTransactionsLenght());
		FrequentPatterns frequentPatterns = new FrequentPatterns(tdbFileName,
				lambda);
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
				tmpResultFileName, cnfParser.getCnfInstance());

		PatternsClassesSet patternsClassesSet = lcmResultParser
				.getFrequentClauses();
		System.out.println("[OK]");
		System.out.println();
		System.out.println("nbValidPatterns= "
				+ lcmResultParser.getNbValidPatterns());
		System.out.println("nbClasses= " + patternsClassesSet.getNbClasses());
		System.out.println();
		// System.out.println(patternsClassesSet);
		if (patternsClassesSet.getNbClasses() == 0) {
			System.out.println("no non-binary reduction for the instance "
					+ cnfParser.getInstancePath());
			if (RIV) {
				new File(tmpResultFileName).delete();
				new File(tdbFileName).delete();
			}
			return cnfParser.getInstancePath();
		}
		if (PRINT_CLASSES) {
			PrintWriter out = new PrintWriter(new FileWriter("Patterns_Classe_"
					+ tdbFileName.substring(4)), true);
			out.println(patternsClassesSet);
		}
		// System.out.println("[OK]");
		// parsers.Toolkit.memoryStat();
		System.out.print("##### Reduction of non binaries clauses #####.....");
		SizeReduction sizeReduction = new SizeReduction(
				patternsClassesSet.getPatternClassesSet(), cnfParser);
		sizeReduction.launch();
		System.out.println("[OK]");
		System.out.print("##### Writing of output #####.....");
		String flieName = sizeReduction.buildCnfFile();
		System.out.println("[OK]");

		if (RIV) {
			new File(tmpResultFileName).delete();
			new File(tdbFileName).delete();
		}
		return flieName;
	}

	public static void main(String[] args) throws IOException,
			InterruptedException {
		if (args.length < 2
				|| (args.length == 3 && (!args[2].equals("-n")
						&& !args[2].equals("-N") && !args[2].equals("-b") && !args[2]
							.equals("-B")))) {
			System.out.println("usage: : <cnfFile> <lambda> <option>");
			System.out
					.println("option : no option launch non binary and binary reductions");
			System.out
					.println("option : -n or -N  launch only non binary reduction");
			System.out
					.println("option : -b or -B  launch only binary reduction");
		} else {
			ThreadMXBean mx = ManagementFactory.getThreadMXBean();
			long beginTraitement1 = mx.getCurrentThreadCpuTime();
			if (!args[0].substring(args[0].lastIndexOf(".")).equals(".cnf")) {
				System.out.println(args[0] + " is not cnf file");
			} else {
				int lambda = 0;
				if (args[1] != null) {
					try {
						lambda = Integer.parseInt(args[1]);
					} catch (IllegalArgumentException e) {
						System.err
								.println("The seconde argument must be an integer");
						System.exit(1);

					}
				}
				CNFParser cnfParser = new CNFParser(args[0], lambda);
				CNFParserBinariesClauses cnfParserBinariesClauses = new CNFParserBinariesClauses(
						args[0], lambda);

				String outPut = args[0];

				// revoir les execptios quand item non fréquent
				DecimalFormat df = new DecimalFormat("#.##");
				if (args.length == 3
						&& (args[2].equals("-n") || args[2].equals("-N"))) {
					try {
						outPut = launchNonBinary(cnfParser, args, lambda);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (!outPut.equals("")) {
						System.out
								.print(cnfParser
										.getInstanceCNFName().substring(
												0,
												cnfParser
														.getInstanceCNFName()
														.lastIndexOf("L"))
										+ " & "
										+ lambda
										+ " & "
										+ FileSizeFormater.format(new File(
												args[0]).length())
										+ " & "
										+ FileSizeFormater.format(new File(
												outPut).length())
										+ " & "
										+ df.format(((float) (new File(args[0])
												.length() - new File(outPut)
												.length()) * 100)
												/ (float) (new File(args[0])
														.length())));
					}
					float endTraitement2 = (((float) (mx
							.getCurrentThreadCpuTime() - beginTraitement1)) / 1000000000f);
					System.out.println(" & "
							+ df.format(endTraitement2)
							+ " & "
							+ cnfParser.getNbTransaction()
							+ " & "
							+ cnfParser.getAVGTransactionsLenght() + " & "
							+ " & " + FrequentPatternForBinary.getNbCV()
							+ " \\\\");
					System.out.println();
					System.out.println("TotalCPUTime = "
							+ df.format(endTraitement2));
					if (((float) (new File(args[0]).length() - new File(outPut)
							.length()) * 100)
							/ (float) (new File(args[0]).length()) > 0) {
						System.out.println(args[0]);
						System.out.println(outPut);
					}

					System.out.println();
					System.out.println();
				}
				if (args.length == 3
						&& (args[2].equals("-b") || args[2].equals("-B")))
					outPut = args[0];
				if (args.length == 2 || args[2].equals("-b")
						|| args[2].equals("-B")) {
					// long beginTraitement2 = mx.getCurrentThreadCpuTime();
					System.gc();

					try {
						outPut = FrequentPatternForBinary.launchBinary(
								cnfParserBinariesClauses, lambda);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (!outPut.equals(""))
						System.out
								.print(cnfParserBinariesClauses
										.getInstanceCNFName().substring(
												0,
												cnfParserBinariesClauses
														.getInstanceCNFName()
														.lastIndexOf("L"))
										+ " & "
										+ lambda
										+ " & "
										+ FileSizeFormater.format(new File(
												args[0]).length())
										+ " & "
										+ FileSizeFormater.format(new File(
												outPut).length())
										+ " & "
										+ df.format(((float) (new File(args[0])
												.length() - new File(outPut)
												.length()) * 100)
												/ (float) (new File(args[0])
														.length())));
					float endTraitement2 = (((float) (mx
							.getCurrentThreadCpuTime() - beginTraitement1)) / 1000000000f);
					System.out.println(" & "
							+ df.format(endTraitement2)
							+ " & "
							+ cnfParserBinariesClauses.getNbTransaction()
							+ " & "
							+ cnfParserBinariesClauses
									.getAVGTransactionsLenght() + " & "
							+ cnfParserBinariesClauses.getLambdaMinMax()
							+ " & " + cnfParserBinariesClauses.getLambdaMoy()
							+ " & " + FrequentPatternForBinary.getNbCV()
							+ " \\\\");
					System.out.println();
					System.out.println("TotalCPUTime = "
							+ df.format(endTraitement2));
					if (((float) (new File(args[0]).length() - new File(outPut)
							.length()) * 100)
							/ (float) (new File(args[0]).length()) > 0) {
						System.out.println(args[0]);
						System.out.println(outPut);
					}

					System.out.println();
					System.out.println();
				}
			}
		}
	}
}
