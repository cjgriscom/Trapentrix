package io.chandler.gap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import io.chandler.gap.GroupExplorer.Generator;
import io.chandler.gap.GroupExplorer.MemorySettings;

public class CubicGenerators {
	public static void main(String[] args) {

		HashSet<Generator> validVertexCombinations = new HashSet<>();
		int[] iteration = new int[] {0};

		long combinations = 57_996_288;
		long startTime = System.currentTimeMillis();


		String cubicPISymmetries = "[" +
			"(1,4,7,10)(2,5,8,11)(3,6,9,12)" + // Face 1 CW
			"(17,20,23,15)(13,18,21,24)(16,19,22,14)" + // Face 6 CCW
			"," +
			"(2,12,13,16)(3,10,14,17)(1,11,15,18)" + // Face 2 CW
			"(6,8,22,21)(4,9,23,19)(5,7,24,20)" + // Face 4 CCW
			"," +
			"(3,18,19,5)(1,16,20,6)(2,17,21,4)" + // Face 3 CW
			"(9,11,14,24)(7,12,15,22)(8,10,13,23)" + // Face 5 CCW
			"]";
		int[][][] symm = GroupExplorer.parseOperationsArr(cubicPISymmetries);
		System.out.println(GroupExplorer.generatorsToString(GroupExplorer.renumberGenerators_fast(symm)));

		GroupExplorer cube = new GroupExplorer(cubicPISymmetries, MemorySettings.FASTEST);
		
		IcosahedralGenerators.exploreGroup(cube, null);
		
		System.exit(0);

		HashSet<Integer> uniqueFaces = new HashSet<>();

		// Generate combinations of 1 octahedral vertex and 2 weird vertices
		// Generate combinations of 2 octahedral vertices and 1 weird vertex
		// Generate combinations of the weird vertices
		PermuCallback.generateCombinations(32, 6, (b) -> {
	

			int[][] cyclesA = new int[][] {
				PentagonalIcositrahedron.getFacesFromVertex(b[0] + 1),
				PentagonalIcositrahedron.getFacesFromVertex(b[1] + 1),
				PentagonalIcositrahedron.getFacesFromVertex(b[2] + 1),
				PentagonalIcositrahedron.getFacesFromVertex(b[3] + 1),
				PentagonalIcositrahedron.getFacesFromVertex(b[4] + 1),
				PentagonalIcositrahedron.getFacesFromVertex(b[5] + 1),
			};


			uniqueFaces.clear();
			for (int[] cycle : cyclesA) {
				for (int face : cycle) {
					uniqueFaces.add(face);
				}
			}
			
			if (uniqueFaces.size() == 6*3) {

				Generator g;
				//validVertexCombinations.add(new Generator(new int[][][] {cyclesA, cyclesB}));
				//TODO
				
			}

			
			if (iteration[0] % 10000 == 0) {
				long currentTime = System.currentTimeMillis();
				long elapsedTime = currentTime - startTime;
				long estimatedTotalTime = (long) ((double) elapsedTime / iteration[0] * combinations);
				long remainingTime = estimatedTotalTime - elapsedTime;
				
				String remainingTimeStr = String.format("%d hours, %d minutes, %d seconds",
					remainingTime / 3600000,
					(remainingTime % 3600000) / 60000,
					(remainingTime % 60000) / 1000);
				
				System.out.println(iteration[0] + " / " + combinations + " -> " + validVertexCombinations.size() +
					" | Estimated time remaining: " + remainingTimeStr);
			}
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
