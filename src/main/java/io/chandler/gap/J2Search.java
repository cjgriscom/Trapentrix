package io.chandler.gap;

import java.util.ArrayList;
import java.util.HashSet;

import io.chandler.gap.GroupExplorer.MemorySettings;

public class J2Search {

	public static void main(String[] args) {
		GroupExplorer ge = new GroupExplorer(Generators.j2, MemorySettings.FASTEST);

		ArrayList<int[]> states0 = new ArrayList<>();
		ArrayList<int[]> states1 = new ArrayList<>();
		IcosahedralGenerators.exploreGroup(ge, (state, disc) -> {
			if (disc.equals("10p 10-cycles")) states0.add(state);
			if (disc.equals("10p 10-cycles")) states1.add(state);
		});

		ArrayList<int[]> reducedStates0 = new ArrayList<>();
		ArrayList<int[]> reducedStates1 = new ArrayList<>();

		reducedStates0.add(states0.get(60480/8*1));

		reducedStates1.addAll(states1.subList(30000, 35000));

		HashSet<String> gens = new HashSet<>();

		int i = 0;
		// For each pair
		for (int[] state0 : reducedStates0) {
			for (int[] state1 : reducedStates1) {
				String gen = "[";
				gen += GroupExplorer.stateToNotation(state0);
				gen += ",";
				gen += GroupExplorer.stateToNotation(state1);
				gen += "]";
				GroupExplorer verify = new GroupExplorer(gen, MemorySettings.FASTEST);
				int iter = verify.exploreStates(false, 5000, null);
				//System.out.println(gen.hashCode() + ": " + iter + " " + verify.order());
				System.err.println(i + " " + ge.order());
				i++;
				if (verify.order() == ge.order() || iter == -1) gens.add(gen);
			}
		}

		for (String gen : gens) {
			System.out.println(GroupExplorer.renumberGeneratorNotation(gen));
		}
		System.out.println("Found " + gens.size() + " generators");

	}
}
