package io.chandler.gap;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import io.chandler.gap.GroupExplorer.Generator;
import io.chandler.gap.GroupExplorer.MemorySettings;
import io.chandler.gap.cache.M24StateCache;
import io.chandler.gap.cache.ParityStateCache;
import io.chandler.gap.cache.State;

public class PHGenerators {

    public static final String pentPHSymmetryF1 = "[" +
            "(1,4,7,10,13)" +
            "(2,5,8,11,14)" +
            "(3,6,9,12,15)" +
            "(17,22,39,43,55)" +
            "(38,45,57,16,24)" +
            "(33,36,54,58,19)" +
            "(37,44,56,18,23)" +
            "(34,52,59,20,31)" +
            "(35,53,60,21,32)" +
            "(40,51,48,26,28)" +
            "(25,30,42,50,47)" +
            "(29,41,49,46,27)" +
        "]";

    public static final String triPHSymmetryF1 = "[" +
            "(1,2,3)(5,13,22)" +
            "(4,15,24)(10,19,39)" +
            "(7,16,33)(6,14,23)" +
            "(8,17,31)(12,21,38)" +
            "(11,20,37)(9,18,32)" +
            "(28,43,58)(57,25,36)" +
            "(26,34,55)(45,60,30)" +
            "(40,54,47)(59,29,44)" +
            "(35,56,27)(41,52,48)" +
            "(53,46,42)(50,49,51)" +
        "]";



	public static void main(String[] args) throws Exception {
		    //PentagonalHexecontahedron.printVertexGeneratorNotations(new Generator(GroupExplorer.parseOperationsArr("(23,15,24)(14,12,13)(11,10,7)(22,8,9)(20,17,18)(2,16,3)(5,1,4)(6,19,21)")).generator());
        
        
        getDodecahedralSymm(); // Order 60 (12p 5-cycles + 20p 3-cycles)
        get3D_180Symm();       // Order 3 (30p 2-cycles)
        getTetrahedralSymm();  // Order 12 (20p 3-cycles)

        get5FoldSymm();


        int tests = 10000;

        // Make sure they all generate the same group
        Map<Generator, Generator> pairs = findUniqueSubgroupPairs();
        for (Entry<Generator, Generator> e : pairs.entrySet()) {
            
            Generator combined = Generator.combine(e.getKey(), e.getValue());
            GroupExplorer ge = new GroupExplorer(GroupExplorer.generatorsToString(combined.generator()), MemorySettings.DEFAULT);
            ge.exploreStates(false, null);
            if (ge.order() != 60) {
                System.out.println("Found non-60 order: " + ge.order());
            } else {
                int a = findSymmetryOfPHVertex(e.getKey(), 1, null).size();
                int b = findSymmetryOfPHVertex(e.getValue(), 1, null).size();
                
                System.out.println("Symmetry : " + a + " " + b + " " + (a * b));
                tests--;
            }
            if (tests == 0) break;
        }

        
	}

    private static Map<Generator, Generator> findUniqueSubgroupPairs() {

        ArrayList<Entry<Integer, int[][][]>> subgroups = findSubgroups(getDodecahedralSymm());
        
        int subgroupPairsTotal = 0;
        HashMap<Generator, Generator> subgroupPairsIsomorphic = new HashMap<>();

        // Find all pairs whose orders multiply to 60 (?)
        for (Entry<Integer, int[][][]> e : subgroups) {
            for (Entry<Integer, int[][][]> e2 : subgroups) {
                if (e.getKey() * e2.getKey() == 60) {
                    Generator g1 = new Generator(e.getValue());
                    Generator g2 = new Generator(e2.getValue());
                    subgroupPairsTotal++;
                    if (subgroupPairsIsomorphic.containsKey(g1) || subgroupPairsIsomorphic.containsKey(g2)) {
                        continue;
                    }
                    if (e.getKey() < e2.getKey()) {
                        subgroupPairsIsomorphic.put(g1, g2);
                    } else {
                        subgroupPairsIsomorphic.put(g2, g1);
                    }
                }
            }
        }

        System.out.println("Total subgroup pairs: " + subgroupPairsTotal);
        System.out.println("Unique subgroup pairs: " + subgroupPairsIsomorphic.size());

        return subgroupPairsIsomorphic;
    }
    
    private static ArrayList<Entry<Integer, int[][][]>> findSubgroups(int[][][] gen) {
        ArrayList<Entry<Integer, int[][][]>> subgroups = new ArrayList<>();
       
        HashSet<State> stateCache = new HashSet<>();
        GroupExplorer ge = new GroupExplorer(GroupExplorer.generatorsToString(gen), MemorySettings.DEFAULT, stateCache);
        ge.exploreStates(false,  null);

        System.out.println("Found " + stateCache.size() + " states");
        for (State a : stateCache) {
            for (State b : stateCache) {
                int[][][] generator;
                if (a.equals(b)) {
                    generator = new int[][][] {
                        GroupExplorer.stateToCycles(a.state()),
                    };
                } else {
                    generator = new int[][][] {
                        GroupExplorer.stateToCycles(a.state()),
                        GroupExplorer.stateToCycles(b.state()),
                    };
                }

                GroupExplorer ge2 = null;
                String genString = GroupExplorer.generatorsToString(generator);
                if (genString.equals("[]")) continue; // Idendity
                ge2 = new GroupExplorer(genString, MemorySettings.DEFAULT);
                ge2.exploreStates(false, null);
                if (ge2.order() == ge.order()) continue; // Not a subgroup
                subgroups.add(new AbstractMap.SimpleEntry<>(ge2.order(), generator));
            }
            
        }
        System.out.println("Order " + ge.order());
        return subgroups;
    }


    private static LinkedHashMap<Integer, int[]> findSymmetryOfPHVertex(Generator gen, int vertex, Map<Integer, LinkedHashMap<Integer, int[]>> vertexSymmetryCache) {
        
        if (vertexSymmetryCache != null && vertexSymmetryCache.containsKey(vertex)) {
            return vertexSymmetryCache.get(vertex);
        }
        
        HashSet<State> cache = new HashSet<>();
        GroupExplorer ge = new GroupExplorer(GroupExplorer.generatorsToString(gen.generator()), MemorySettings.DEFAULT, cache);

        int[] vertexFaces = PentagonalHexecontahedron.getFacesFromVertex(vertex);
        LinkedHashMap<Integer, int[]> vertexMapping = new LinkedHashMap<>();

        List<int[]> states = new ArrayList<>();
        states.add(ge.copyCurrentState());
        ge.exploreStates(false, (states2, depth) -> {
            states.addAll(states2);
        });
        for (int[] s : states) {
            int[] analogousFaces = new int[3];
            for (int fi = 0; fi < 3; fi++) {
                int face = vertexFaces[fi];
                analogousFaces[fi] = s[face - 1];
            }
            int cacheIndex = PentagonalHexecontahedron.getMatchingVertexFromFaces(analogousFaces);
            vertexMapping.put(cacheIndex, analogousFaces);
        }
        
        if (vertexSymmetryCache != null) {
            vertexSymmetryCache.put(vertex, vertexMapping);
        }
        return vertexMapping;
    }


    private static Map<Integer, LinkedHashMap<Integer, int[]>> vertexToIcosidodecSymmetryCache = new HashMap<>();
    private static LinkedHashMap<Integer, int[]> findIcosidodecSymmetryOfPHVertex(int vertex) {
        
        if (vertexToIcosidodecSymmetryCache.containsKey(vertex)) {
            return vertexToIcosidodecSymmetryCache.get(vertex);
        }
        
        int[][][] gen = getDodecahedralSymm();

        HashSet<State> cache = new HashSet<>();
        GroupExplorer ge = new GroupExplorer(GroupExplorer.generatorsToString(gen), MemorySettings.DEFAULT, cache);
        ge.resetElements(true);

        int[] vertexFaces = PentagonalHexecontahedron.getFacesFromVertex(vertex);
        LinkedHashMap<Integer, int[]> vertexMapping = new LinkedHashMap<>();

        // Find all copies of the specified vertex with tetrahedral symmetry

        ge.exploreStates(false, (states, depth) -> {
            for (int[] s : states) {
                int[] analogousFaces = new int[3];
                for (int fi = 0; fi < 3; fi++) {
                    int face = vertexFaces[fi];
                    analogousFaces[fi] = s[face - 1];
                }
                int cacheIndex = PentagonalHexecontahedron.getMatchingVertexFromFaces(analogousFaces);
                vertexMapping.put(cacheIndex, analogousFaces);
            }
        });
        
        vertexToIcosidodecSymmetryCache.put(vertex, vertexMapping);
        return vertexMapping;
    }


/*
    public static void findCube_6p() throws Exception {
		
        int[][] symm0 = GroupExplorer.parseOperationsArr(cubicPISymmetries_2)[0];
        int[][] symm1 = GroupExplorer.parseOperationsArr(cubicPISymmetries_2)[1];

        System.out.println("Starting cube 6p search");
        System.out.println("Press Q + Enter at any time to interrupt");

        Thread.sleep(1000);

        ArrayList<Generator> validVertexCombinations = get6pVertexCombinations();
        Collections.shuffle(validVertexCombinations);
        
        System.out.println("Found " + validVertexCombinations.size() + " possible generators");
        
        int[] iteration = new int[] {0};
        long combinations = validVertexCombinations.size();
        long startTime = System.currentTimeMillis();

        // Synchronized
        List<String> results = Collections.synchronizedList(new ArrayList<String>());
        Map<Integer, List<String>> smallGroupGenerators = Collections.synchronizedMap(new TreeMap<>());

        for (Generator vertices : validVertexCombinations) {

            if (checkQuit() == -1) {
                System.out.println("QUITTING");
                break;
            }

            if (iteration[0] % 50 == 0) {
                checkProgressEstimate(iteration[0], combinations, startTime, validVertexCombinations.size(), results.size());
            }

			Generator g = new Generator(new int[][][] {
				symm0,
				symm1,
				vertices.generator()[0],
			});

            //vertices2.parallelStream().forEach(g -> {
                checkGenerator(g, results, smallGroupGenerators);
            
            //});


            iteration[0]++;
        }

        System.out.println("Exited at iteration " + iteration[0] + " / " + combinations);

        System.out.println("Filtered down to " + results.size() + " valid generators");
        // Write to file 

        System.out.println("Writing results to file");

        Files.deleteIfExists(Paths.get("generators_results.txt"));

        PrintStream out2 = new PrintStream("generators_results.txt");
        for (Map.Entry<Integer, List<String>> e : smallGroupGenerators.entrySet()) {
            out2.println("Order " + e.getKey() + ":");
            for (String genString : e.getValue()) {
                out2.println(genString);
            }
        }

        out2.println("Order ?: ");
        for (String genString : results) {
            out2.println(genString);
        }
        out2.close();
    }
*/

    private static ArrayList<Generator> getNpVertexCombinations(int n) {

        HashSet<Generator> generatorCache = new HashSet<>();

        boolean[][] fixedCycleIndices = new boolean[1][n];
        fixedCycleIndices[0][0] = true;
        for (int i = 1; i < n; i++) {
            fixedCycleIndices[0][i] = false;
        }

        HashSet<Integer> uniqueFaces = new HashSet<>();
        PermuCallback.generateCombinations(60, n, (b) -> {
    
            int[][] cyclesA = new int[n][];
            for (int i = 0; i < n; i++) {
                cyclesA[i] = PentagonalHexecontahedron.getFacesFromVertex(b[i] + 1);
            }

            uniqueFaces.clear();
            for (int[] cycle : cyclesA) {
                for (int face : cycle) {
                    uniqueFaces.add(face);
                }
            }
            
            if (uniqueFaces.size() == n*3) {

                int[][][] genSrc = new int[][][] {
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
        
        return validVertexCombinations;
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
    private static void checkProgressEstimate(int currentIteration, long totalCombinations, long startTime, int validCombinations, int results) {
        
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;
        long estimatedTotalTime = (long) ((double) elapsedTime / currentIteration * totalCombinations);
        long remainingTime = estimatedTotalTime - elapsedTime;
        
        String remainingTimeStr = String.format("%d hours, %d minutes, %d seconds",
            remainingTime / 3600000,
            (remainingTime % 3600000) / 60000,
            (remainingTime % 60000) / 1000);
        
        System.out.println(currentIteration + " / " + totalCombinations + " -> " + results +
            " | Estimated time remaining: " + remainingTimeStr);

    }

    private static void checkGenerator(Generator g, List<String> lgGroupResults, Map<Integer, List<String>> smallGroupGenerators) {
        checkGenerator(g, lgGroupResults, smallGroupGenerators, new M24StateCache());
    }

    private static void checkGenerator(Generator g, List<String> lgGroupResults, Map<Integer, List<String>> smallGroupGenerators, Set<State> stateCache) {
        ParityStateCache cache = new ParityStateCache(new M24StateCache());
        String genString = GroupExplorer.generatorsToString(g.generator());
        GroupExplorer candidate = new GroupExplorer(
            genString,
            MemorySettings.DEFAULT, cache);
            

        ArrayList<String> depthPeek = new ArrayList<>();
        int startCheckingRatioIncreaseAtOrder = 313692;/// 739215;
        int limit = 30400000 / 4;
        int[] stateCount = new int[2];
        int iters = -2;

        //System.out.println(genString);
        
        double lastRatio = 0;
        candidate.initIterativeExploration();
        while (iters == -2) {
            int[] depthA = new int[] {0};
            try {
                iters = candidate.iterateExploration(false, limit, (states, depth) -> {
                    stateCount[0] = stateCount[1];
                    stateCount[1] += states.size();
                    depthA[0] = depth;
                });
            } catch (ParityStateCache.StateRejectedException e) {
                // Heuristic 1:
                //    If the 7-transitive cache is too small we've generated a non-M24 group
                
                // Fail because this can't happen for valid M24 generators
                iters = -2;
                //System.out.println("M24 cache is different");
                break;
            }
            int depth = depthA[0];
            double ratio = depth < 2 ? 10000 : stateCount[1]/(double)stateCount[0];
            if (depth > 5 ) depthPeek.add(ratio+" " + stateCount[1] + ",");

            //System.out.println(stateCountB[0] + " " + stateCount[1]);

            // Heuristic 2: If the ratio of states is increasing, it's not going to converge quickly
            boolean isDecreasing = ratio - lastRatio < 0.01;
            if (!isDecreasing && stateCount[1] > startCheckingRatioIncreaseAtOrder) {
                //iters = -2;
                System.out.println("Ratio rate increase");
                //break;
            }
            
            
            
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
        } else if (candidate.order() > 1) {
            // Add genString to smallGroupGenerators
            List<String> gens = smallGroupGenerators.computeIfAbsent(candidate.order(), k -> Collections.synchronizedList(new ArrayList<String>()));
            gens.add(genString);
            System.out.println("Found order " + (candidate.order()) + ": " + GroupExplorer.generatorsToString(g.generator()));
        }
        

    }


    public static int[][][] getDodecahedralSymm() {
        int[][] op0 = GroupExplorer.parseOperationsArr(triPHSymmetryF1)[0];
        int[][] op1 = GroupExplorer.parseOperationsArr(pentPHSymmetryF1)[0];

        int[][][] gen = new int[][][] {
            op0,
            op1,
        };

        return gen;
    }

    public static int[][][] get5FoldSymm() {
        return new int[][][] {
            GroupExplorer.parseOperationsArr(pentPHSymmetryF1)[0],
        };
    }

    //20p 3-cycles
    //12p 5-cycles
    //30p 2-cycles
    private static List<int[]> getMatchingCycles(String type) {
        List<int[]> cycles = new ArrayList<>();
        int[][][] gen = getDodecahedralSymm();

        HashSet<State> cache = new HashSet<>();
        GroupExplorer ge = new GroupExplorer(GroupExplorer.generatorsToString(gen), MemorySettings.DEFAULT, cache);
        ge.exploreStates(false, null);

        for (State s : cache) {
            if (type.equals(GroupExplorer.describeState(60, s.state()))) {
                cycles.add(s.state());
            }
        }
        return cycles;
    }

    public static int[][][] get3D_180Symm() {
        return getSymm("20p 3-cycles", 3, "180 degree");
    }

    public static int[][][] getTetrahedralSymm() {
        return getSymm("20p 3-cycles", 12, "tetrahedral");
    }

    private static int[][][] getSymm(String cyclesSrcType, int order, String name) {
        List<int[]> cycles = getMatchingCycles(cyclesSrcType);
        // Select one and loop through the rest
        int[] cycle0 = cycles.get(0);
        for (int[] cycle1 : cycles) {
            int[][][] gen = new int[][][] {
                GroupExplorer.stateToCycles(cycle0),
                GroupExplorer.stateToCycles(cycle1),
            };

            GroupExplorer ge = new GroupExplorer(GroupExplorer.generatorsToString(gen), MemorySettings.DEFAULT, new HashSet<>());
            ge.exploreStates(false, null);
            
            if (ge.order() == order) {
                //System.out.println("Found " + name + " symmetry: order " + ge.order());
                return gen;
            }
        }
        throw new RuntimeException("No " + name + " symmetry found");
    }

}
