package io.chandler.gap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import io.chandler.gap.GroupExplorer.Generator;
import io.chandler.gap.GroupExplorer.MemorySettings;

public class J2Search {
	public static int[][] reorder5_5(int[][] src) {
		ArrayList<int[]> out = new ArrayList<>();
		for (int[] x : src) {
			if (x.length == 5) out.add(0, x);
			else out.add(x);
		}
		
		return out.toArray(new int[out.size()][]);
	}

	public static void main(String[] args) {
		// Start with 2p


		GroupExplorer ge = new GroupExplorer(Generators.j2, MemorySettings.FASTEST);

		ArrayList<int[]> states0 = new ArrayList<>();

		ArrayList<int[]> statesSplitMove = new ArrayList<>();


		IcosahedralGenerators.exploreGroup(ge, (state, disc) -> {
			if (disc.equals("dual 5-cycles, 6p 15-cycles")) states0.add(state);
			if (disc.equals("40p 2-cycles")) statesSplitMove.add(state);
		});

		// State to cycles 0
		
		int[][] cycles = GroupExplorer.stateToCycles(states0.get(0));
		cycles = reorder5_5(cycles);

		int[][] cyclesRen = null;

		int[][] piggyback = null;

		ArrayList<int[][]> found = new ArrayList<>();
		for (int[] state : states0) {
			if (state == states0.get(0)) continue;
			int[][][] gen = new int[][][] {
				cycles, reorder5_5(GroupExplorer.stateToCycles(state)),
				GroupExplorer.stateToCycles(statesSplitMove.get(0)) // Piggyback
			};
			gen = GroupExplorer.renumberGenerators(gen);
			piggyback = gen[2]; // Pull this out to get a full generator w/ new numbering
			gen = new int[][][]{
				gen[0], gen[1]
			};
			
			cyclesRen = gen[0];

			TreeSet<Integer> cycle0Numbers = new TreeSet<>();
			TreeSet<Integer> cycle1Numbers = new TreeSet<>();
			for (int i : gen[1][0]) cycle0Numbers.add(i);
			for (int i : gen[1][1]) cycle1Numbers.add(i);
			if (cycle0Numbers.last() - cycle0Numbers.first() > 25-11
					 || cycle0Numbers.first() <= 10) continue;
			if (cycle1Numbers.last() - cycle1Numbers.first() > 25-11
					|| cycle1Numbers.first() <= 10) continue;

			int order ;

			//if (found == 0) {
				System.out.println(GroupExplorer.generatorsToString(gen));
				Generator gg = new Generator(gen);
				System.out.println(order = getOrder(gg));
			//}
			//break;

			if (order == 300 && found.size() == 0) {
				found.add(gen[1]);
				break;
			}
		}
		System.out.println(found.size());

		System.out.println("Ico Chop with gearing");

		int[][] gen0Ren = cyclesRen, gen1Ren = found.get(0);


		int[][][] combinedOriginal = new int[][][]{gen0Ren, gen1Ren};
		System.out.println(GroupExplorer.generatorsToString(GroupExplorer.renumberGenerators(combinedOriginal)));
		System.out.println();
		// Manually renumber
		int[][][] helper = new int[][][]{new int[][] {
			 gen0Ren[0], gen1Ren[0], // Front faces
			 gen1Ren[6], gen0Ren[5], // Entanglement of front faces


			 gen0Ren[1], gen1Ren[1], // Back faces
			 gen1Ren[7], gen0Ren[2], // Entanglement of back faces
		}};
		System.out.println(GroupExplorer.generatorsToString(helper));

		int[][] remapRef = helper[0];
		int[][] remap = new int[][] {
			{1,2,3,4,5},{6,7,8,9,10},
			{25,1,11,21,2,12,22,3,13,23,4,14,24,5,15},
			{19,8,28,18,7,27,17,6,26,16,10,30,20,9,29}, // TODO might be wrong
			{31,32,33,34,35},{36,37,38,39,40},
			{31,41,51,32,42,52,33,43,53,34,44,54,35,45,55},
			{57,47,36,56,46,40,60,50,39,59,49,38,58,48,37}
		};
		TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>();
		for (int i = 0; i < remap.length; i++) {
			for (int j = 0; j < remap[i].length; j++) {
				Integer old = map.put(remapRef[i][j], remap[i][j]);
				if (old != null && old != remapRef[i][j]) 
					throw new IllegalArgumentException("Collision: " + remapRef[i][j] + " -> " + remap[i][j] + " overwrites old value " + old);
			}
		}

		// 40 faces should be expressed

		int[][][] fullRenumbered = GroupExplorer.renumberGenerators(
			Generator.combine(
					Generator.combine(
						new Generator(helper),
						new Generator(combinedOriginal)),
				new Generator(piggyback))
			.generator());

		int[][][] fullRenumberedRenumbered = new int[fullRenumbered.length][][];
		
		TreeSet<Integer> unusedRemaps = new TreeSet<>();
		for (int i = 1; i <= 100; i++) unusedRemaps.add(i);
		unusedRemaps.removeAll(map.keySet());

		for (int i = 0; i < fullRenumbered.length; i++) {
			fullRenumberedRenumbered[i] = new int[fullRenumbered[i].length][];
			for (int j = 0; j < fullRenumbered[i].length; j++) {
				fullRenumberedRenumbered[i][j] = new int[fullRenumbered[i][j].length];
				for (int k = 0; k < fullRenumbered[i][j].length; k++) {
					int old = fullRenumbered[i][j][k];
					Integer n = map.get(old);
					if (n == null) {
						n = unusedRemaps.first();
						unusedRemaps.remove(n);
						map.put(fullRenumbered[i][j][k], n);
					}
					System.out.println(fullRenumbered[i][j][k] + " - " + n);
					fullRenumberedRenumbered[i][j][k] = n;
				}
			}
		}

		System.out.println(GroupExplorer.generatorsToString(new int[][][]{fullRenumberedRenumbered[0]}));
		System.out.println();
		System.out.println(GroupExplorer.generatorsToString(fullRenumberedRenumbered));
		
		ge = new GroupExplorer(new Generator(fullRenumberedRenumbered), 100, MemorySettings.FASTEST, new HashSet<>());
		IcosahedralGenerators.exploreGroup(ge, null);



		/*


		System.out.println(GroupExplorer.generatorsToString(combined.generator()));
		GroupExplorer vc = new GroupExplorer(combined, 100, MemorySettings.FASTEST, new HashSet<>());
		IcosahedralGenerators.exploreGroup(vc, null);



		ArrayList<int[][]> statesSplitMoveRenumbered = new ArrayList<>();
		for (int[] splitState : statesSplitMove) {
			int[][][] src = new int[][][] {
				cycles,
				GroupExplorer.stateToCycles(splitState)
			};
			src = GroupExplorer.renumberGenerators_fast(src);
			
			System.out.println(getOrder(new Generator(src)));
			//if (getOrder(new Generator(src))) statesSplitMoveRenumbered.add(src[1]);
		}
			*/

	}

	private static int getOrder(Generator gen) {
		
		GroupExplorer verify = new GroupExplorer(gen, 100, MemorySettings.FASTEST, new HashSet<>());
		int iter = verify.exploreStates(false, 604801, null);
		if (iter < 0) return iter;
		return verify.order();
	}

	public static void old(String[] args) {
		GroupExplorer ge = new GroupExplorer(Generators.j2, MemorySettings.FASTEST);

		ArrayList<int[]> states0 = new ArrayList<>();
		ArrayList<int[]> states5 = new ArrayList<>();


		IcosahedralGenerators.exploreGroup(ge, (state, disc) -> {
			if (disc.equals("40p 2-cycles")) states0.add(state);
			if (disc.equals("30p 3-cycles")) states5.add(state);
		});


		HashSet<String> gens = new HashSet<>();
		List<int[]> combinations = Permu.generateCombinations(30, 2);

		System.out.println(combinations.size());

		int it = 0;
		for (int[] comb : combinations) {
			for (int[] cy5 : states5) {
				Generator combined = new Generator(new int[][][] {GroupExplorer.stateToCycles(states0.get(comb[0]))});
				for (int i = 1; i < 2; i++) {
					combined = Generator.combine(combined, new Generator(new int[][][] {GroupExplorer.stateToCycles(states0.get(comb[i]))}));

				}

				//combined = Generator.combine(combined, new Generator(new int[][][]{GroupExplorer.stateToCycles(cy5)}));
				String gen = GroupExplorer.generatorsToString(combined.generator());

				HashMap<String, Integer> stateDescsForVerify = new HashMap<>();

				GroupExplorer verify = new GroupExplorer(gen, MemorySettings.FASTEST);
				int iter = verify.exploreStates(false, 5000, (states, depth) -> {
					for (int[] state : states) {
						String desc = GroupExplorer.describeState(100, state);
						stateDescsForVerify.merge(desc, 1, Integer::sum);
					}
				});
				//System.out.println(stateDescsForVerify);
				//System.out.println(gen.hashCode() + ": " + iter + " " + verify.order());
				
				//if (verify.order() == 60) {
				System.err.println(it + " " + ge.order() + " " +  verify.order());
				//}
				it++;
				if (verify.order() == ge.order() || iter == -1) gens.add(gen);
			}
		}


		for (String gen : gens) {
			System.out.println(GroupExplorer.renumberGeneratorNotation(gen));
		}
		System.out.println("Found " + gens.size() + " generators");

	}
}
