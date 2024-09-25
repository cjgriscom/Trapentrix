package io.chandler.gap;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import io.chandler.gap.GroupExplorer.Generator;
import io.chandler.gap.GroupExplorer.MemorySettings;
import io.chandler.gap.cache.ParityStateCache;
import io.chandler.gap.cache.State;
import io.chandler.gap.util.TimeEstimator;

public class PyraSearch {

    private static final int[][][] vertexEdgeSymmetries = {
        {{1, 10, 7}, {3, 11, 8}, {2, 12, 9}, {5, 6, 4}},
        {{6, 9, 11}, {4, 7, 12}, {5, 8, 10}, {2, 1, 3}},
        {{2, 8, 4}, {1, 9, 5}, {3, 7, 6}, {10, 11, 12}},
        {{3, 5, 12}, {2, 6, 10}, {1, 4, 11}, {8, 9, 7}},
    };

	public static void main(String[] args) throws IOException {

        int tiersLeft = 4, tiersRight = 3;
        String outFile = "matching_py_generators_m12.txt";
        int tierDepth = 4;

		TrapentrixFinder(outFile, vertexEdgeSymmetries, tierDepth, tiersLeft, tiersRight);
	}
	
    public static void TrapentrixFinder(String filename, int[][][] vertexSymmetries, int axisDepth, int nCyclesA, int nCyclesB) throws IOException {
        // 1/2 of the trapentrix points correspond to 9 faces on a rhombic triacontahedron

        
        System.out.println("Searching for icosahedral generators");

        boolean foundMatch = false;
        int checkedIcosahedralGenerators = 0;
        HashMap<String, Integer> matchingGenerators = new HashMap<>();

        // List tiers of symmetries for easier reporting
        String[] tierLabels = new String[] {
            "D1", "D2", "D3", "D4"
        };
        int[] tierDepths = new int[] {
            1, 2, 3, 4
        };
        Map<Integer, String> tierNames = new HashMap<>();
        for (int i = 4; i >= 1; i--) {
            int[][] axis1 = vertexSymmetries[i - 1];
            for (int j = 0; j < 4; j++) {
                String name = "V" + i + "-" + tierLabels[j];
                int[] sorted = axis1[j].clone();
                
               
                tierNames.put(25*25 * sorted[0] + 25 * sorted[1] + sorted[2], name);
                ArrayRotator.rotateRight(sorted);
                tierNames.put(25*25 * sorted[0] + 25 * sorted[1] + sorted[2], name);
                ArrayRotator.rotateRight(sorted);
                tierNames.put(25*25 * sorted[0] + 25 * sorted[1] + sorted[2], name);

                sorted = CycleInverter.invertArray(sorted);
                tierNames.put(25*25 * sorted[0] + 25 * sorted[1] + sorted[2], name+"*");
                ArrayRotator.rotateRight(sorted);
                tierNames.put(25*25 * sorted[0] + 25 * sorted[1] + sorted[2], name+"*");
                ArrayRotator.rotateRight(sorted);
                tierNames.put(25*25 * sorted[0] + 25 * sorted[1] + sorted[2], name+"*");

            }
        }

		boolean removeAsymmetricalGenerators = false;

		List<int[]> permutationsA = Permu.generateCombinations(axisDepth, nCyclesA);
		List<int[]> permutationsB = Permu.generateCombinations(axisDepth, nCyclesB);

		System.out.println("Permutations A: " + permutationsA.size());
		System.out.println("Permutations B: " + permutationsB.size());
		int total = permutationsA.size() * permutationsB.size() * 20;
		System.out.println("Total: " + total);

		TimeEstimator te = new TimeEstimator(total);
		int iteration = 0;

        // Select 3 vertex axes
        int vertex0 = 1;
        int[][] axisA = vertexSymmetries[vertex0 - 1];

		checkQuit:
        for (int vertex1 = 1; vertex1 <= 2; vertex1++) {
            if (vertex1 == vertex0) continue;
            int[][] axisB = vertexSymmetries[vertex1 - 1];

            // Select pairs of 3-cycles from each axis
            for (int[] a : permutationsA) {
                HashSet<Integer> aTiers = new HashSet<>();
                HashSet<Integer> aDepths = new HashSet<>();
                for (int aTier : a) aTiers.add(aTier);
                for (int aTier : a) aDepths.add(tierDepths[aTier]);
                for (int[] b : permutationsB) {

					iteration++;
					te.checkProgressEstimate(iteration, matchingGenerators.size());
					if (checkQuit() == -1) break checkQuit;
                    HashSet<Integer> bTiers = new HashSet<>();
                    HashSet<Integer> bDepths = new HashSet<>();
                    for (int bTier : b) bTiers.add(bTier);
                    for (int bTier : b) bDepths.add(tierDepths[bTier]);

                    // Remove asymmetrical generators
                    if (removeAsymmetricalGenerators && !aDepths.equals(bDepths)) continue;

                    /*
                    // Check if there are any mirror conflicts
                    HashSet<Integer> sideEffectsA = new HashSet<>();
                    HashSet<Integer> sideEffectsB = new HashSet<>();

                    for (int aTier : aTiers) {
                        if (aTier == 1 && !aTiers.contains(2)) for (int x : axisA[2]) sideEffectsA.add(x);
                        if (aTier == 2 && !aTiers.contains(1)) for (int x : axisA[1]) sideEffectsA.add(x);
                        if (aTier == 4 && !aTiers.contains(5)) for (int x : axisA[5]) sideEffectsA.add(x);
                        if (aTier == 5 && !aTiers.contains(4)) for (int x : axisA[4]) sideEffectsA.add(x);
                        if (aTier == 7 && !aTiers.contains(8)) for (int x : axisA[8]) sideEffectsA.add(x);
                        if (aTier == 8 && !aTiers.contains(7)) for (int x : axisA[7]) sideEffectsA.add(x);
                    }
                    for (int bTier : bTiers) {
                        if (bTier == 1 && !bTiers.contains(2)) for (int x : axisB[2]) sideEffectsB.add(x);
                        if (bTier == 2 && !bTiers.contains(1)) for (int x : axisB[1]) sideEffectsB.add(x);
                        if (bTier == 4 && !bTiers.contains(5)) for (int x : axisB[5]) sideEffectsB.add(x);
                        if (bTier == 5 && !bTiers.contains(4)) for (int x : axisB[4]) sideEffectsB.add(x);
                        if (bTier == 7 && !bTiers.contains(8)) for (int x : axisB[8]) sideEffectsB.add(x);
                        if (bTier == 8 && !bTiers.contains(7)) for (int x : axisB[7]) sideEffectsB.add(x);
                    }
                    

                    boolean conflicts = false;
                    // Now make sure axisB doesn't contains anything in sideEffectsA
                    for (int bb : bTiers) {
                        int[] x = axisB[bb];
                        for (int y : x) {
                            if (sideEffectsA.contains(y)) {
                                conflicts = true;
                            }
                        }
                    }
                    // And vv
                    for (int aa : aTiers) {
                        int[] x = axisA[aa];
                        for (int y : x) {
                            if (sideEffectsB.contains(y)) {
                                conflicts = true;
                            }
                        }
                    }
                        */
                    /*
                    if (("Tiers: A" + aTiers + " B" + bTiers).equals("Tiers: A[2, 5] B[1, 4]")) {
                    System.out.println(vertex0 + " " + vertex1);
                    System.out.println("Tiers: A" + aTiers + " B" + bTiers);
                    System.out.println("Side effects: A" + sideEffectsA + " B" + sideEffectsB);
                    System.out.println("Axis A: " + Arrays.toString(axisA[a[0]]) + " " + Arrays.toString(axisA[a[1]]));
                    System.out.println("Axis B: " + Arrays.toString(axisB[b[0]]) + " " + Arrays.toString(axisB[b[1]]));
                    System.out.println("Conflicts: " + conflicts);
                    }
                    */
                    //if (conflicts) continue;


                    int[][][] generator = new int[2][][];
        
                    generator[0] = new int[a.length][];
					for (int i = 0; i < a.length; i++) {
						generator[0][i] = axisA[a[i]];
					}
                    generator[1] = new int[b.length][];
					for (int i = 0; i < b.length; i++) {
						generator[1][i] = axisB[b[i]];
					}
                    
                    for (int[][][] genCandidate : CycleInverter.generateInvertedCycles(null, generator)) {
                        Generator g = new Generator(GroupExplorer.renumberGenerators(genCandidate));
                        checkedIcosahedralGenerators++;
						ArrayList<String> lgGroupResults = new ArrayList<>();
						HashMap<Integer, List<String>> smallGroupGenerators = new HashMap<>();
						Set<State> cache = new HashSet<>();
						checkGenerator(false, g, lgGroupResults, smallGroupGenerators, cache);
                        if (lgGroupResults.size() > 0 || smallGroupGenerators.size() > 0) {
							Integer order = smallGroupGenerators.size() > 0 ?smallGroupGenerators.keySet().iterator().next() : null;
                            if (!foundMatch) {
                                System.out.println("Found a match! (" + order + ")");
                                System.out.println(GroupExplorer.generatorsToString(genCandidate));
                                foundMatch = true;
                            }
                            matchingGenerators.put(GroupExplorer.generatorsToString(genCandidate), order == null ? -1 : order);
                        }
                    }
                }
            }
        }

        System.out.println("Checked " + checkedIcosahedralGenerators + " icosahedral generators");
        
        System.out.println("Matching generators: " + matchingGenerators.size());



		Map<Integer, List<String>> orderMap = new TreeMap<>();
		for (Map.Entry<String, Integer> entry : matchingGenerators.entrySet()) {
			int order = entry.getValue();
			List<String> gens = orderMap.computeIfAbsent(order, k -> new ArrayList<>());
			gens.add(entry.getKey());
		}

		System.out.println("Writing to " + filename);
		PrintStream file = new PrintStream(filename);
		for (Map.Entry<Integer, List<String>> entry : orderMap.entrySet()) {
			int order = entry.getKey();
			List<String> gens = entry.getValue();
			System.out.println("Order " + order + ": (" + GetPrimeFactors.getPrimeFactorsSet(order) + ") " + gens.size());
			file.println("Order " + order + ": (" + GetPrimeFactors.getPrimeFactorsSet(order) + ") " + gens.size());
			for (String s : gens) {
				int[][][] gen = GroupExplorer.parseOperationsArr(s);
				boolean first = true;
				for (int[][] g : gen) {
					if (!first) file.print(",");
					first = false;
					for (int[] h : g) {
						file.print("(");
						file.print(tierNames.get(25*25 * h[0] + 25 * h[1] + h[2]));
						file.print(")");
					}
				}
				file.println("\t" + s);
			}
		}
        file.close();
    }




    private static void checkGenerator(boolean debug, Generator g, List<String> lgGroupResults, Map<Integer, List<String>> smallGroupGenerators, Set<State> cache) {
        String genString = GroupExplorer.generatorsToString(g.generator());
        GroupExplorer candidate = new GroupExplorer(
            genString,
            MemorySettings.FASTEST, cache);
            

        ArrayList<String> depthPeek = new ArrayList<>();
        int limit = 99000;
        int[] stateCount = new int[2];
        int iters = -2;

        //System.out.println(genString);
        
        double lastRatio = 0;
        candidate.initIterativeExploration();
        HashMap<String, Integer> cycleDescriptions = new HashMap<>();
        while (iters == -2) {
            int[] depthA = new int[] {0};
            try {
                iters = candidate.iterateExploration(debug, limit, (states, depth) -> {
                    stateCount[0] = stateCount[1];
                    stateCount[1] += states.size();
                    depthA[0] = depth;
                    for (int[] s : states) {
                        String desc = GroupExplorer.describeStateForCache(candidate.nElements, s);
                        cycleDescriptions.merge(desc, 1, Integer::sum);
                        // Heuristic 3: If there are more than 50 cycle descriptions, it's the alternating group
                        if (cycleDescriptions.size() > 20) {
                            throw new RuntimeException("Too many cycle descriptions");
                        }
                    }
                });
            } catch (ParityStateCache.StateRejectedException e) {
                // Heuristic 1:
                //    If the 7-transitive cache is too small we've generated a non-M24 group
                
                // Fail because this can't happen for valid M24 generators
                iters = -2;
                //System.out.println("M24 cache is different");
                break;
            } catch (RuntimeException e) {
                iters = -2;
                break;
            }
            int depth = depthA[0];
            double ratio = depth < 2 ? 10000 : stateCount[1]/(double)stateCount[0];
            if (depth > 5 ) depthPeek.add(ratio+" " + stateCount[1] + ",");

            //System.out.println(stateCountB[0] + " " + stateCount[1]);

            
            
            
            lastRatio = ratio;
        }

        if (iters == -2) {
            /*if (depthPeek.size() > 4) {
                System.out.println("Reject " + iters + " Last iter states: " + depthPeek.get(depthPeek.size() - 1) + " Depth: " + depthPeek.size());
                System.out.println(depthPeek.toString());
            }*/
        } else if (iters == -1) {
            System.out.println("Iters: " + iters + " Last iter states: " + depthPeek.get(depthPeek.size() - 1) + " Depth: " + depthPeek.size());
            System.out.println( GroupExplorer.generatorsToString(g.generator()));
            System.out.println(depthPeek.toString());
            lgGroupResults.add(GroupExplorer.generatorsToString(g.generator()));


            /*
            HashMap<String, Integer> cycleDescriptions = new HashMap<>();
            for (State state : cache) {
                String cycleDescription = GroupExplorer.describeState(candidate.nElements, state.state());
                cycleDescriptions.merge(cycleDescription, 1, Integer::sum);
                if (cycleDescriptions.size() % 100_000 == 0) {
                    System.out.println("Processed " + cycleDescriptions.size() + " / " + cache.size() + " states");
                }
            }

            printCycleDescriptions(cycleDescriptions);
            */
        } else if (candidate.order() > 10) {
            // Add genString to smallGroupGenerators
            List<String> gens = smallGroupGenerators.computeIfAbsent(candidate.order(), k -> Collections.synchronizedList(new ArrayList<String>()));
            gens.add(genString);
            //System.out.println("Found order " + (candidate.order()) + ": " + GroupExplorer.generatorsToString(g.generator()));
        }
        

    }


    private static int checkQuit() {
        try {
            // Check for key press
            while (System.in.available() > 0) {
                int i = System.in.read();
                if (i == 'q' || i == 'Q') {
                    return -1;
                }
            }
        } catch (Exception e) {}
        return 0;
    }
}
