package parsers;

import java.util.List;
import java.util.Set;

import datastructure.Clause;

public class Toolkit {
	public static boolean found(int[] tab, int key) {
		for (int e : tab)
			if (e == key)
				return true;
		return false;
	}

	public static int[] setToArray(Set<Integer> set) {
		int[] t = new int[set.size()];
		int i = 0;
		for (Integer integer : set)
			t[i++] = integer.intValue();
		return t;

	}

	public static int[][] buildNonBinariesclausesSet(List<Clause> clauses) {
		int[][] clausesSet = new int[clauses.size()][];
		for (Clause clause : clauses) {
			clausesSet[clause.getId()] = clause.getClause();
		}
		return clausesSet;
	}

	public static int[][] toArray(List<int[]> clauses) {
		int[][] clausesSet = new int[clauses.size()][];
		int index = 0;
		for (int[] clause : clauses) {
			clausesSet[index++] = clause;
		}
		return clausesSet;
	}

	public static String printClause(int[] clause) {
		String st = "";
		for (int l : clause)
			st += l + " ";
		return st + "0";
	}

	public static String printNeighboorClause(int[] clause) {
		String st = "";
		for (int l : clause)
			st += l + " ";
		return st;
	}

	public static String printItemsSet(int[] itemsSet) {
		String st = "";
		for (int l : itemsSet)
			st += l + " ";
		return st;
	}

	public static boolean notExist(int idLiteral, int[] clause) {
		for (int l : clause)
			if (l == idLiteral)
				return false;
		return true;
	}

	public static void memoryStat() {
		int mb = 1024 * 1024;

		// Getting the runtime reference from system
		Runtime runtime = Runtime.getRuntime();

		System.out.println("##### memory stat [MB] #####");

		// Print used memory
		System.out.println("Used Memory:"
				+ (runtime.totalMemory() - runtime.freeMemory()) / mb);

		// Print free memory
		System.out.println("Free Memory:" + runtime.freeMemory() / mb);

		// Print total available memory
		System.out.println("Total Memory:" + runtime.totalMemory() / mb);

		// Print Maximum available memory
		System.out.println("Max Memory:" + runtime.maxMemory() / mb);
	}

	public static boolean subSet(int[] itemSet, int[] itemSet2) {
		if (itemSet.length < itemSet2.length) {
			for (int a : itemSet)
				if (!found(itemSet2, a))
					return false;
		} else
			return false;
		return true;
	}

	public static String printNonUsedClause(int[] clause) {
		String st = "";
		for (int i = 0; i < clause.length - 1; i++)
			st += clause[i] + " ";
		return st + "0";
	}

	public static String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1)
				+ (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

}
