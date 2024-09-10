package io.chandler.gap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import io.chandler.gap.GroupExplorer.Generator;
import io.chandler.gap.GroupExplorer.MemorySettings;

public class CubicGenerators {
	public static void main(String[] args) {

		System.out.println(Arrays.toString(PentagonalIcositrahedron.getFacesFromVertex(1)));
		
		HashSet<Generator> validVertexCombinations = new HashSet<>();
		int[] iteration = new int[] {0};

		long combinations = 208632584160l;
		PermuCallback.generateCombinations(32, 12, (b) -> {
		
			List<int[]> partitions = Permu.generateCombinations(12, 6);
			for (int[] p : partitions) {
				HashSet<Integer> notSelected = new HashSet<>();
				for (int i = 0 ; i < 12; i++) notSelected.add(i);
				for (int i : p) notSelected.remove(i);
				int[] q = new int[notSelected.size()];
				int index = 0;
				for (int i : notSelected) q[index++] = i;

				int[][] cyclesA = new int[][] {
					PentagonalIcositrahedron.getFacesFromVertex(b[p[0]] + 1),
					PentagonalIcositrahedron.getFacesFromVertex(b[p[1]] + 1),
					PentagonalIcositrahedron.getFacesFromVertex(b[p[2]] + 1),
					PentagonalIcositrahedron.getFacesFromVertex(b[p[3]] + 1),
					PentagonalIcositrahedron.getFacesFromVertex(b[p[4]] + 1),
					PentagonalIcositrahedron.getFacesFromVertex(b[p[5]] + 1),
				};

				int[][] cyclesB = new int[][] {
					PentagonalIcositrahedron.getFacesFromVertex(b[q[0]] + 1),
					PentagonalIcositrahedron.getFacesFromVertex(b[q[1]] + 1),
					PentagonalIcositrahedron.getFacesFromVertex(b[q[2]] + 1),
					PentagonalIcositrahedron.getFacesFromVertex(b[q[3]] + 1),
					PentagonalIcositrahedron.getFacesFromVertex(b[q[4]] + 1),
					PentagonalIcositrahedron.getFacesFromVertex(b[q[5]] + 1),
				};

				HashSet<Integer> uniqueFaces = new HashSet<>();
				for (int[] cycle : cyclesA) {
					for (int face : cycle) {
						uniqueFaces.add(face);
					}
				}
				
				if (uniqueFaces.size() != 6*3) continue;

				uniqueFaces.clear();
				for (int[] cycle : cyclesB) {
					for (int face : cycle) {
						uniqueFaces.add(face);
					}
				}
				if (uniqueFaces.size() != 6*3) continue;
				
				validVertexCombinations.add(new Generator(new int[][][] {cyclesA, cyclesB}));

			}
			if (iteration[0] % 10000 == 0) System.out.println(iteration[0] + " / " + combinations + " -> " + validVertexCombinations.size());
			iteration[0]++;
		});

		System.out.println(validVertexCombinations.size());
		System.exit(0);


		List<int[][]> generatorCandidates = new ArrayList<>();
		List<int[][]> generatorCandidates2 = new ArrayList<>();

        GroupExplorer group = new GroupExplorer(Generators.m22, MemorySettings.FASTEST);
        IcosahedralGenerators.exploreGroup(group, (state, cycleDescription) -> {
            if (cycleDescription.equals("6p 3-cycles")) {
                if (Math.random() > 0.99) generatorCandidates.add(GroupExplorer.stateToCycles(state));
            }
            if (cycleDescription.equals("6p 3-cycles")) {
                if (Math.random() > 0.99) generatorCandidates2.add(GroupExplorer.stateToCycles(state));
            }
        });

		ArrayList<Generator> generators = GeneratorPairSearch.findGeneratorPairs_NoCache(
			group, 30, 443518, generatorCandidates, generatorCandidates2, true);
		
		for (Generator g : generators) {
			System.out.println(GroupExplorer.generatorsToString(g.generator()));
		}
	}
}
