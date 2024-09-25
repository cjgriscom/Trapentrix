package io.chandler.gap;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import io.chandler.gap.GroupExplorer.Generator;
import io.chandler.gap.GroupExplorer.MemorySettings;
import io.chandler.gap.cache.*;
import io.chandler.gap.util.TimeEstimator;

public class OctaSearch {

/*

VTO 
W axis: {{WR,WB,WP,WG},{GB,RP,BG,OR},{RB,BP,OG,GR},{RY,BO,OC,GW},{GC,RW,BY,OO},{YW,YY,YO,YC}}
B axis:{{BY,BO,BG,BP}{WP,RY,YO,OG}{RP,YY,OO,WG}{RW,YC,OR,WB}{RB,YW,OC,WR}{GR,GB,GW,GC}}
G axis: {{GC,GW,GB,GR},{OC,YW,RB,WR},{WB,OR,YC,RW},{RP,WG,OO,YY},{RY,WP,OG,YO},{BO,BY,BP,BG}}

FTO
P axis:{{RP,WP,BP}{RY,WB,BG}{BY,RB,WG}{WR,BO,RW}{YY,GB,OG}{OR,YO,GW}{YW,GR,OO}{OC,YC,GC}}
B acis:{{RB,GB,WB},{WR,RP,GW},{GR,WP,RW},{RY,GC,WG},{BP,YW,OR},{OC,BG,YY},{BY,YC,OG},{OO,BO,YO}}
*/

    private static final int[][][] vertexSymmetries = {
        {{10,5,2,9},{14,1,6,16},{8,3,15,20},{4,11,22,18},{24,12,7,21},{19,13,17,23}},
        {{7,11,6,3},{2,4,17,15},{1,13,21,9},{12,23,16,5},{8,19,22,10},{20,14,18,24}},
        {{24,18,14,20},{22,19,8,10},{5,16,23,12},{1,9,21,13},{4,2,15,17},{11,7,3,6}}
    };
    private static final int[][][][] vertex180Symmetries = {
        {{{10,2},{5,9}}, {{14,6},{1,16}}, {{8,15},{3,20}}, {{4,22},{11,18}}, {{24,7},{12,21}}, {{19,17},{13,23}}},
        {{{7,6},{11,3}}, {{2,17},{4,15}}, {{1,21},{13,9}}, {{12,16},{23,5}}, {{8,22},{19,10}}, {{20,18},{14,24}}},
    };

    // THese can represent any 2 adjacent faces
    public static final int[][][] faceSymmetries = {
        {{1,2,3},{4,5,6},{7,8,9},{10,11,12},{13,14,15},{16,17,18},{19,20,21},{22,23,24}},
        {{8,14,5},{10,1,18},{20,2,12},{4,24,9},{3,19,16},{22,6,13},{7,23,15},{21,11,17}}
    };

	public static void main(String[] args) throws IOException {


        // Pattern 
/*
        GroupExplorer ge = new GroupExplorer(
            Generator.combine(new Generator(vertexSymmetries), new Generator(faceSymmetries)), 24,
            MemorySettings.FASTEST, new M24StateCache());

        IcosahedralGenerators.exploreGroup(ge,(state, d) -> {
            if (d.contains("8p 3-cycles")) {
                System.out.println(GroupExplorer.cyclesToNotation(GroupExplorer.stateToCycles(state)));
            }
        });


        System.out.println(ge.order());

        System.exit(0);
        */


        // No inversions
        boolean noInversions = false;

        String outFile = "matching_oct_generators_full.txt";

		System.out.println("Writing to " + outFile);
		try (PrintStream file = new PrintStream(outFile)) {
            for (int i = 2; i <= 6; i++) {
                for (int j = 2; j <= 8; j++) {
                    int[][] axisLeft = vertexSymmetries[0];
                    int[][] axisRight = faceSymmetries[1];
                    int tierDepthLeft = i;
                    int tierDepthRight = j;
                    boolean fixLeft = false || noInversions;
                    boolean fixRight = false || noInversions;


                    TrapentrixFinder(file, axisLeft, axisRight, tierDepthLeft, tierDepthRight, fixLeft, fixRight);
                    
                }  
            }

            for (int i = 2; i <= 8; i++) {
                for (int j = i; j <= 8; j++) { // Symmetrical, so j >= i
                    int[][] axisLeft = faceSymmetries[0];
                    int[][] axisRight = faceSymmetries[1];
                    int tierDepthLeft = i;
                    int tierDepthRight = j;
                    boolean fixLeft = false || noInversions;
                    boolean fixRight = false || noInversions;


                    TrapentrixFinder(file, axisLeft, axisRight, tierDepthLeft, tierDepthRight, fixLeft, fixRight);
                    
                }  
            }

            for (int i = 2; i <= 6; i++) {
                for (int j = i; j <= 6; j++) { // Symmetrical, so j >= i
                    int[][] axisLeft = vertexSymmetries[0];
                    int[][] axisRight = vertexSymmetries[1];
                    int tierDepthLeft = i;
                    int tierDepthRight = j;
                    boolean fixLeft = false || noInversions;
                    boolean fixRight = false || noInversions;


                    TrapentrixFinder(file, axisLeft, axisRight, tierDepthLeft, tierDepthRight, fixLeft, fixRight);
                    
                }  
            }
        }

	}
	
    public static void TrapentrixFinder(PrintStream file, int[][] axisA, int[][] axisB, int nCyclesA, int nCyclesB, boolean fixA, boolean fixB) throws IOException {
        // 1/2 of the trapentrix points correspond to 9 faces on a rhombic triacontahedron

        file.println("OctaSearch");
        file.println();
        file.println("Axis A: " + Arrays.deepToString(axisA));
        file.println("Axis B: " + Arrays.deepToString(axisB));
        file.println("nCyclesA: " + nCyclesA);
        file.println("nCyclesB: " + nCyclesB);
        file.println("fixA: " + fixA);
        file.println("fixB: " + fixB);
        file.println();

        System.out.println("Searching for " + nCyclesA + " cycles on axis A and " + nCyclesB + " cycles on axis B");

        boolean foundMatch = false;
        int checkedIcosahedralGenerators = 0;
        HashMap<String, Integer> matchingGenerators = new HashMap<>();

        Map<Integer, String> tierNamesA = new HashMap<>();
        for (int j = 0; j < axisA.length; j++) {
            String name = "A" + (j+1);
            int[] sorted = axisA[j].clone();
            
            tierNamesA.put(25*25 * sorted[0] + 25 * sorted[1] + sorted[2], name);
            for (int i = 0; i < axisA[j].length - 1; i++) {
                ArrayRotator.rotateRight(sorted);
                tierNamesA.put(25*25 * sorted[0] + 25 * sorted[1] + sorted[2], name);
            }
            sorted = CycleInverter.invertArray(sorted);
            tierNamesA.put(25*25 * sorted[0] + 25 * sorted[1] + sorted[2], name + "*");
            for (int i = 0; i < axisA[j].length - 1; i++) {
                ArrayRotator.rotateRight(sorted);
                tierNamesA.put(25*25 * sorted[0] + 25 * sorted[1] + sorted[2], name + "*");
            }
        }


        Map<Integer, String> tierNamesB = new HashMap<>();
        for (int j = 0; j < axisB.length; j++) {
            String name = "B" + (j+1);
            int[] sorted = axisB[j].clone();
            
            tierNamesB.put(25*25 * sorted[0] + 25 * sorted[1] + sorted[2], name);
            for (int i = 0; i < axisB[j].length - 1; i++) {
                ArrayRotator.rotateRight(sorted);
                tierNamesB.put(25*25 * sorted[0] + 25 * sorted[1] + sorted[2], name);
            }
            sorted = CycleInverter.invertArray(sorted);
            tierNamesB.put(25*25 * sorted[0] + 25 * sorted[1] + sorted[2], name + "*");
            for (int i = 0; i < axisB[j].length - 1; i++) {
                ArrayRotator.rotateRight(sorted);
                tierNamesB.put(25*25 * sorted[0] + 25 * sorted[1] + sorted[2], name+ "*");
            }
        }

		List<int[]> permutationsA = Permu.generateCombinations(axisA.length, nCyclesA);
		List<int[]> permutationsB = Permu.generateCombinations(axisB.length, nCyclesB);

		System.out.println("Permutations A: " + permutationsA.size());
		System.out.println("Permutations B: " + permutationsB.size());
		int total = permutationsA.size() * permutationsB.size();
		System.out.println("Total: " + total);

		TimeEstimator te = new TimeEstimator(total);
		int iteration = 0;

        // Select pairs of 3-cycles from each axis
        checkQuit:
        for (int[] a : permutationsA) {
            for (int[] b : permutationsB) {

                iteration++;
                te.checkProgressEstimate(iteration, matchingGenerators.size());
                if (te.checkQuit() == -1) break checkQuit;

                int[][][] generator = new int[2][][];
    
                generator[0] = new int[a.length][];
                for (int i = 0; i < a.length; i++) {
                    generator[0][i] = axisA[a[i]];
                }
                generator[1] = new int[b.length][];
                for (int i = 0; i < b.length; i++) {
                    generator[1][i] = axisB[b[i]];
                }

                boolean[][] invertedCycles = new boolean[2][];
                invertedCycles[0] = new boolean[a.length];
                invertedCycles[1] = new boolean[b.length];
                for (int i = 0; i < a.length; i++) {
                    invertedCycles[0][i] = i==0 || fixA;
                }
                for (int i = 0; i < b.length; i++) {
                    invertedCycles[1][i] = i==0 || fixB;
                }
                
                for (int[][][] genCandidate : CycleInverter.generateInvertedCycles(invertedCycles, generator)) {
                    Generator g = new Generator(GroupExplorer.renumberGenerators(genCandidate));
                    checkedIcosahedralGenerators++;
                    ArrayList<String> lgGroupResults = new ArrayList<>();
                    HashMap<Integer, List<String>> smallGroupGenerators = new HashMap<>();
                    //Set<State> cache = new ParityStateCache(new LongStateCache(8,24));
                    Set<State> cache = new HashSet<>(); // Faster, probably
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

        System.out.println("Checked " + checkedIcosahedralGenerators + " icosahedral generators");
        
        System.out.println("Matching generators: " + matchingGenerators.size());



		Map<Integer, List<String>> orderMap = new TreeMap<>();
		for (Map.Entry<String, Integer> entry : matchingGenerators.entrySet()) {
			int order = entry.getValue();
			List<String> gens = orderMap.computeIfAbsent(order, k -> new ArrayList<>());
			gens.add(entry.getKey());
		}

		for (Map.Entry<Integer, List<String>> entry : orderMap.entrySet()) {
			int order = entry.getKey();
			List<String> gens = entry.getValue();
			System.out.println("Order " + order + ": (" + GetPrimeFactors.getPrimeFactorsSet(order) + ") " + gens.size());
			file.println("Order " + order + ": (" + GetPrimeFactors.getPrimeFactorsSet(order) + ") " + gens.size());
			for (String s : gens) {
				int[][][] gen = GroupExplorer.parseOperationsArr(s);
				boolean first = true;
                Map<Integer, String> tierNames = tierNamesA;
				for (int[][] g : gen) {
					if (!first) file.print(",");
					first = false;
					for (int[] h : g) {
						file.print("(");
						file.print(tierNames.get(25*25 * h[0] + 25 * h[1] + h[2]));
						file.print(")");
					}
                    tierNames = tierNamesB;
				}
				file.println("\t" + s);
			}
		}
    }




    private static void checkGenerator(boolean debug, Generator g, List<String> lgGroupResults, Map<Integer, List<String>> smallGroupGenerators, Set<State> cache) {
        String genString = GroupExplorer.generatorsToString(g.generator());
        GroupExplorer candidate = new GroupExplorer(
            genString,
            MemorySettings.FASTEST, cache);
            

        ArrayList<String> depthPeek = new ArrayList<>();
        int limit = 19958401; // A11 + 1
        int[] stateCount = new int[2];
        int iters = -2;

        //System.out.println(genString);
        
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
                        if (cycleDescriptions.size() >= 21) {
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
        } else if (candidate.order() > 100) {
            // Add genString to smallGroupGenerators
            List<String> gens = smallGroupGenerators.computeIfAbsent(candidate.order(), k -> Collections.synchronizedList(new ArrayList<String>()));
            gens.add(genString);
            //System.out.println("Found order " + (candidate.order()) + ": " + GroupExplorer.generatorsToString(g.generator()));
        }
        

    }


}
