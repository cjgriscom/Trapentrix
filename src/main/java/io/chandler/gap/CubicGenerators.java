package io.chandler.gap;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import io.chandler.gap.GroupExplorer.Generator;
import io.chandler.gap.GroupExplorer.MemorySettings;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class CubicGenerators {

	public static final String cubicPISymmetries3 = "[" +
		"(1,4,7,10)(2,5,8,11)(3,6,9,12)" + // Face 1 CW
		"(17,20,23,15)(13,18,21,24)(16,19,22,14)" + // Face 6 CCW
		"," +
		"(2,12,13,16)(3,10,14,17)(1,11,15,18)" + // Face 2 CW
		"(6,8,22,21)(4,9,23,19)(5,7,24,20)" + // Face 4 CCW
		"," +
		"(3,18,19,5)(1,16,20,6)(2,17,21,4)" + // Face 3 CW
		"(9,11,14,24)(7,12,15,22)(8,10,13,23)" + // Face 5 CCW
		"]";
	public static final String cubicPISymmetries_2 = "[" +
		"(1,4,7,10)(2,5,8,11)(3,6,9,12)" + // Face 1 CW
		"(17,20,23,15)(13,18,21,24)(16,19,22,14)" + // Face 6 CCW
		"," +
		"(2,12,13,16)(3,10,14,17)(1,11,15,18)" + // Face 2 CW
		"(6,8,22,21)(4,9,23,19)(5,7,24,20)" + // Face 4 CCW
		"]";
	public static final String cubicPISymmetries = "[" +
		"(1,4,7,10)(2,5,8,11)(3,6,9,12)" + // Face 1 CW
		"(17,20,23,15)(13,18,21,24)(16,19,22,14)" + // Face 6 CCW
		"]";

	public static void mainCheckResult(String[] args) {

		ObjectOpenHashSet<State> map = new ObjectOpenHashSet<>();
		String genCandidate = "[(1,10,2)(6,19,5)(8,4,7)(12,14,11)(18,3,16)(22,9,24)]";

		genCandidate = genCandidate.substring(0, genCandidate.length() - 1) + "," + cubicPISymmetries.substring(1);

		GroupExplorer ge = new GroupExplorer(genCandidate, MemorySettings.COMPACT, map);
		IcosahedralGenerators.exploreGroup(ge, null);
	}

	public static void studyRates(String[] args) throws Exception {

		List<int[][]> generatorCandidates = new ArrayList<>();

		for (int[][] cycles : M24Generator.loadM24CategoryStates("6p 3-cycles")) {
			if (Math.random() > 0.999) {
				generatorCandidates.add(cycles);
			}
		}
		List<int[][]> sixP4Cycles = M24Generator.loadM24CategoryStates("6p 4-cycles");
		Iterator<int[][]> iter = sixP4Cycles.iterator();


		int[][] cubecandidate2 = null;
		int[][] cubecandidate3 = null;
		// For each pair of other 6p 4-cycles...


;
		while (true) {
			
			// Take a random pair selection
			cubecandidate2 = sixP4Cycles.get((int)(Math.random() * sixP4Cycles.size()));
			cubecandidate3 = sixP4Cycles.get((int)(Math.random() * sixP4Cycles.size()));

			GroupExplorer ge = new GroupExplorer(
				GroupExplorer.generatorsToString(new int[][][] {cubecandidate2, cubecandidate3}),
				MemorySettings.FASTEST);
			int iters = ge.exploreStates(false, 25, null);
			if (iters < 0 || ge.order() != 24) {
				//System.out.println("iters: " + iters + " order: " + ge.order());
				continue;
			} else {
				// FOUND
				break;
			}
		}
		
		
		
		
		AbstractGroupProperties group = new AbstractGroupProperties() {
			@Override public int elements() { return 24; }
			@Override public MemorySettings mem() { return MemorySettings.FASTEST; }
			@Override public int order() { return 244823040; }
		};
		Set<State> set = new M24StateCache();

		sixP4Cycles.clear();;

		for (int[][] candidate : generatorCandidates) {
			String genCandidate = GroupExplorer.generatorsToString(new int[][][] {candidate, cubecandidate2, cubecandidate3});

			System.out.println(genCandidate);
			GroupExplorer ge = new GroupExplorer(genCandidate, MemorySettings.DEFAULT, set);
			
			int iterations = ge.exploreStates(true, null);

			System.out.println("Iterations: " + iterations + " Order: " + ge.order());
		}

		

	}
	public static void main(String[] args) throws Exception {
		

		GroupExplorer cube = new GroupExplorer(cubicPISymmetries_2, MemorySettings.FASTEST);
		
		IcosahedralGenerators.exploreGroup(cube, null);
		

		HashSet<Integer> uniqueFaces = new HashSet<>();

		boolean[][] fixedCycleIndices = new boolean[][] {
			{true, true, true, true, true, true},
			//{true, true, true, true, true, true},
			{true, false, false, false, false, false},
		};

		
		int[][][] symm = GroupExplorer.parseOperationsArr(cubicPISymmetries);
		System.out.println(GroupExplorer.generatorsToString(GroupExplorer.renumberGenerators_fast(symm)));

		HashSet<Generator> generatorCache = new HashSet<>();

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

				int[][][] genSrc = new int[][][] {
					symm[0],
					//symm[1],
					cyclesA,
				};
				List<int[][][]> cycled = CycleInverter.generateInvertedCycles(fixedCycleIndices, genSrc);

				for (int[][][] c : cycled) {
					Generator g = new Generator(c);
					generatorCache.add(g);
				}
				
			}

		});

		ArrayList<Generator> validVertexCombinations = new ArrayList<>(generatorCache);
		Collections.shuffle(validVertexCombinations);

		// Sanity check: Good M24 gen should be valid
		validVertexCombinations.add(0, new Generator(GroupExplorer.parseOperationsArr(Generators.m24)));
		
		System.out.println("Found " + validVertexCombinations.size() + " possible generators");
		
		int[] iteration = new int[] {0};
		long combinations = validVertexCombinations.size();
		long startTime = System.currentTimeMillis();

		HashMap<Integer, Integer> orderCounts = new HashMap<>();

		Iterator<Generator> iter = validVertexCombinations.iterator();
		while (iter.hasNext()) {
			Generator g = iter.next();
			if (iteration[0] % 10 == 0) {
				printProgressEstimate(iteration[0], combinations, startTime, validVertexCombinations.size());
			}

			String genString = GroupExplorer.generatorsToString(g.generator());
			GroupExplorer candidate = new GroupExplorer(
				genString,
				MemorySettings.DEFAULT);
			
			GroupExplorer candidate_m12cache = new GroupExplorer(
				genString,
				MemorySettings.DEFAULT, new M24StateCache());

			ArrayList<String> depthPeek = new ArrayList<>();
			int startCheckingRatioIncreaseAtOrder = 313692;/// 739215;
			int limit = 30400000 / 4;
			int[] stateCount = new int[2];
			int[] stateCountB = new int[1];
			int iters = -2;

			//System.out.println(genString);
			
			double lastRatio = 0;
			candidate.initIterativeExploration();
			candidate_m12cache.initIterativeExploration();
			while (iters == -2) {
				int[] depthA = new int[] {0};
				iters = candidate.iterateExploration(false, limit, (states, depth) -> {
					stateCount[0] = stateCount[1];
					stateCount[1] += states.size();
					depthA[0] = depth;
				});
				candidate_m12cache.iterateExploration(false, limit, (states, depth) -> {
					stateCountB[0] += states.size();
				});
				int depth = depthA[0];
				double ratio = depth < 2 ? 10000 : stateCount[1]/(double)stateCount[0];
				if (depth > 5 ) depthPeek.add(ratio+" " + stateCount[1] + ",");

				//System.out.println(stateCountB[0] + " " + stateCount[1]);

				// Heuristic 1: If the ratio of states is increasing, it's not going to converge quickly
				boolean isDecreasing = ratio - lastRatio < 0.01;
				if (!isDecreasing && stateCount[1] > startCheckingRatioIncreaseAtOrder) {
					iters = -2;
					//System.out.println("Ratio rate increase");
					break;
				}
				
				// Heuristic 2: If the M24 cache is different we've generated a non-M24 group
				if (stateCountB[0] != stateCount[1]) {
					// M24 cache is different
					// fail because this doesn't happen for valid M24 generators
					iters = -2;
					//System.out.println("M24 cache is different");
					break;
				}
				lastRatio = ratio;
			}

			if (iters == -2) {
				/*if (depthPeek.size() > 4) {
					System.out.println("Reject " + iters + " Last iter states: " + depthPeek.get(depthPeek.size() - 1) + " Depth: " + depthPeek.size());
					System.out.println(depthPeek.toString());
				}*/
				iter.remove();
			} else if (iters == -1) {
				System.out.println("Iters: " + iters + " Last iter states: " + depthPeek.get(depthPeek.size() - 1) + " Depth: " + depthPeek.size());
				System.out.println( GroupExplorer.generatorsToString(g.generator()));
				System.out.println(depthPeek.toString());
			} else{
				orderCounts.merge(candidate.order(), 1, Integer::sum);
				System.out.println("Found order " + (candidate.order()) + ": " + GroupExplorer.generatorsToString(new int[][][] {g.generator()[g.generator.length -1]}));
				iter.remove();
			}
			

			




			iteration[0]++;
		}

		System.out.println("Filtered down to " + validVertexCombinations.size() + " valid generators");
		// Write to file 

		Files.deleteIfExists(Paths.get("valid_generators.txt"));
		PrintStream out = new PrintStream("valid_generators.txt");
		for (Generator g : validVertexCombinations) {
			out.println(GroupExplorer.generatorsToString(g.generator()));
		}
		out.close();

		System.out.println("Order counts: " + orderCounts.toString());
	}


    private static void printProgressEstimate(int currentIteration, long totalCombinations, long startTime, int validCombinations) {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;
        long estimatedTotalTime = (long) ((double) elapsedTime / currentIteration * totalCombinations);
        long remainingTime = estimatedTotalTime - elapsedTime;
        
        String remainingTimeStr = String.format("%d hours, %d minutes, %d seconds",
            remainingTime / 3600000,
            (remainingTime % 3600000) / 60000,
            (remainingTime % 60000) / 1000);
        
        System.out.println(currentIteration + " / " + totalCombinations + " -> " + validCombinations +
            " | Estimated time remaining: " + remainingTimeStr);
    }
}
