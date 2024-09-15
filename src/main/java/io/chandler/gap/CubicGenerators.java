package io.chandler.gap;

import static io.chandler.gap.IcosahedralGenerators.printCycleDescriptions;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import io.chandler.gap.GroupExplorer.Generator;
import io.chandler.gap.GroupExplorer.MemorySettings;
import io.chandler.gap.cache.InteractiveCachePair;
import io.chandler.gap.cache.LMDBCache;
import io.chandler.gap.cache.LongStateCache;
import io.chandler.gap.cache.M24StateCache;
import io.chandler.gap.cache.ParityStateCache;
import io.chandler.gap.cache.State;

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

	public static int[][] getFace180Symm(int faceIndex) {
		String face1Symm = GroupExplorer.generatorsToString(new int[][][] {
			GroupExplorer.parseOperations(CubicGenerators.cubicPISymmetries3).get(faceIndex)});

		GroupExplorer ge_face1 = new GroupExplorer(face1Symm, MemorySettings.DEFAULT, new HashSet<>());
		ge_face1.resetElements(true);
		ge_face1.applyOperation(0);
		ge_face1.applyOperation(0); // 180 deg
		int[] turn180 = ge_face1.copyCurrentState();
		return GroupExplorer.stateToCycles(turn180);
	}


	// 180 8p doesn't work...
	// Cube 8p doesn't work
	

	public static void main(String[] args) throws Exception {
        PentagonalIcositrahedron.printVertexGeneratorNotations(new Generator(GroupExplorer.parseOperationsArr("(23,15,24)(14,12,13)(11,10,7)(22,8,9)(20,17,18)(2,16,3)(5,1,4)(6,19,21)")).generator());
        
        // Predictions

        int[][][] piCubicSymm = GroupExplorer.parseOperationsArr(CubicGenerators.cubicPISymmetries_2);
        VertexColorSearch vcs = new VertexColorSearch(piCubicSymm, 24, PentagonalIcositrahedron::getFacesFromVertex, PentagonalIcositrahedron::getMatchingVertexFromFaces);

        vcs.searchForGenerators();


        //findCube_8p();
        //checkCube_8p();


	}

    public static void checkCube_8p() throws Exception {

        String genCandidate = "[(1,4,7,10)(2,5,8,11)(3,6,9,12)(17,20,23,15)(13,18,21,24)(16,19,22,14),(2,12,13,16)(3,10,14,17)(1,11,15,18)(6,8,22,21)(4,9,23,19)(5,7,24,20),(23,15,24)(14,12,13)(11,10,7)(22,8,9)(20,17,18)(2,16,3)(5,1,4)(6,19,21)]";
        

        int nElements = 24;
        int cacheGB = 32;
        int batch = 10_000_000;
        try (
            Scanner scanner = new Scanner(System.in);
            InteractiveCachePair cachePair =
                new InteractiveCachePair(scanner , cacheGB, nElements, batch)) {

            if (false) {
                int totalStates = cachePair.cache.size() + cachePair.cache_incomplete.size();

                System.out.println("Looping through " + totalStates + " states");

                HashMap<String, Integer> cycleDescriptions = new HashMap<>();
                for (State state : cachePair.cache) {
                    String cycleDescription = GroupExplorer.describeState(nElements, state.state());
                    cycleDescriptions.merge(cycleDescription, 1, Integer::sum);
                    if (cycleDescriptions.size() % 1_000_000 == 0) {
                        System.out.println("Processed " + cycleDescriptions.size() + " / " + totalStates + " states");
                    }
                }
                for (State state : cachePair.cache_incomplete) {
                    String cycleDescription = GroupExplorer.describeState(nElements, state.state());
                    cycleDescriptions.merge(cycleDescription, 1, Integer::sum);
                }

                printCycleDescriptions(cycleDescriptions);
            }
            
            cachePair.cache.debug(true);
            cachePair.cache_incomplete.debug(true);

            if (cachePair.cache.size() > 0) {
                System.out.println("Pre-initializing incomplete states to size " + cachePair.cache.size());
                cachePair.cache_incomplete.addAll(cachePair.cache);
            }
            GroupExplorer ge_ord = new GroupExplorer(genCandidate, MemorySettings.COMPACT,
                    cachePair.cache, cachePair.cache_incomplete, cachePair.cache_tmp, true);
			ge_ord.exploreStates(true, null);

            System.out.println("Order: " + ge_ord.order());
		}
		
    }
    public static void findHalfCube_8p() throws Exception {
        
        int[][] symm0 = GroupExplorer.parseOperationsArr(cubicPISymmetries_2)[0];
        int[][] symm1 = GroupExplorer.parseOperationsArr(cubicPISymmetries_2)[1];
        

        System.out.println("Starting cube 8p search");
        System.out.println("Press Q + Enter at any time to interrupt");

        Thread.sleep(1000);

        ArrayList<Generator> validVertexCombinations = new ArrayList<>();
        int[][] axes = new int[][] {
            {23, 15, 24},
            {13, 12, 14},
            {11, 10, 7},
            {9, 8, 22},
            {20, 17, 18},
            {3, 16, 2},
            {5, 1, 4},
            {21, 19, 6}
        };


        boolean[][] fixedCycleIndices = new boolean[][] {
            {true, false, false, false},
        };
        // Choose 4
        PermuCallback.generateCombinations(8, 4, (b) -> {

            int[][] axesPermute = new int[][] {
                axes[b[0]],
                axes[b[1]],
                axes[b[2]],
                axes[b[3]],
            };

            List<int[][][]> inverted = CycleInverter.generateInvertedCycles(fixedCycleIndices, new int[][][] {axesPermute});

            for (int[][][] g : inverted) {
                validVertexCombinations.add(new Generator(g));
            }

        });

        
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

            checkGenerator(g, results, smallGroupGenerators, new LongStateCache(8, 24));

                
            
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

    public static void findCube_8p() throws Exception {
        
        int[][] symm0 = GroupExplorer.parseOperationsArr(cubicPISymmetries_2)[0];
        int[][] symm1 = GroupExplorer.parseOperationsArr(cubicPISymmetries_2)[1];
        

        System.out.println("Starting cube 8p search");
        System.out.println("Press Q + Enter at any time to interrupt");

        Thread.sleep(1000);

        ArrayList<Generator> validVertexCombinations = new ArrayList<>();
        int[][] axes = new int[][] {
            {23, 15, 24},
            {13, 12, 14},
            {11, 10, 7},
            {9, 8, 22},
            {20, 17, 18},
            {3, 16, 2},
            {5, 1, 4},
            {21, 19, 6}
        };
        boolean[][] fixedCycleIndices = new boolean[][] {
            {true, false, false, false, false ,false, false ,false},
        };
        List<int[][][]> inverted = CycleInverter.generateInvertedCycles(fixedCycleIndices, new int[][][] {axes});

        for (int[][][] g : inverted) {
            validVertexCombinations.add(new Generator(g));
        }

        
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

            checkGenerator(g, results, smallGroupGenerators, new LongStateCache(8, 24));

                
            
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

	// 180 6p works
    public static void find180_6p() throws Exception {
        int[][] symm0 = getFace180Symm(1);
        int[][] symm1 = getFace180Symm(2);

        System.out.println("Starting 180 search");
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

            if (iteration[0] % 10 == 0) {
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

    public static void fullPairSearch() throws Exception {
        System.out.println("Starting full pair search");
        System.out.println("Press Q + Enter at any time to interrupt");

        Thread.sleep(3000);

        ArrayList<Generator> validVertexCombinations = get6pVertexCombinations();
        Collections.shuffle(validVertexCombinations);
        
        System.out.println("Found " + validVertexCombinations.size() + " possible generators");
        
        int[] iteration = new int[] {0};
        long combinations = validVertexCombinations.size();
        long startTime = System.currentTimeMillis();

        // Synchronized
        List<String> results = Collections.synchronizedList(new ArrayList<String>());
        Map<Integer, List<String>> smallGroupGenerators =Collections.synchronizedMap(new TreeMap<>());

        for (Generator vertices : validVertexCombinations) {

            if (checkQuit() == -1) {
                System.out.println("QUITTING");
                break;
            }

            if (iteration[0] % 10 == 0) {
                checkProgressEstimate(iteration[0], combinations, startTime, validVertexCombinations.size(), results.size());
            }

            List<Generator> vertices2 = new ArrayList<>();

            tryagain: for (Generator v : validVertexCombinations) {
                Generator g = new Generator(new int[][][] {
                    v.generator()[0],
                    vertices.generator()[0]});

                // Check coverage of faces is greater than 21
                int[] faceCoverage = new int[24];
                for (int[][] cycles : g.generator()) {
                    for (int[] cycle : cycles) {
                        for (int f : cycle) faceCoverage[f - 1]++;
                    }
                }
                // Count how many faces have coverage of 1 or more
                int coverageCount = 0;
                for (int f : faceCoverage) {
                    if (f >= 1) coverageCount++;
                }
                if (coverageCount <= 23) {
                    //System.out.println("Skip");
                    continue tryagain;
                }

                // Make sure none of the cycles are the same
                for (int[] cycleA : g.generator()[0]) {
                    int[] sortedCycleA = Arrays.copyOf(cycleA, cycleA.length);
                    Arrays.sort(sortedCycleA); // Sort the elements of cycleA

                    for (int[] cycleB : g.generator()[1]) {
                        int[] sortedCycleB = Arrays.copyOf(cycleB, cycleB.length);
                        Arrays.sort(sortedCycleB); // Sort the elements of cycleB

                        if (Arrays.equals(sortedCycleA, sortedCycleB)) {
                            continue tryagain; // Skip if any cycle contains the same elements
                        }
                    }
                }

                vertices2.add(g);
            }

            System.out.println("Checking " + vertices2.size() + " matches");

            vertices2.parallelStream().forEach(g -> {
                checkGenerator(g, results, smallGroupGenerators);
            
            });


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

    private static ArrayList<Generator> get6pVertexCombinations() {

        HashSet<Generator> generatorCache = new HashSet<>();

        boolean[][] fixedCycleIndices = new boolean[][] {
            {true, false, false, false, false, false},
        };

        HashSet<Integer> uniqueFaces = new HashSet<>();
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

    private static ArrayList<Generator> get8pVertexCombinations() {

        HashSet<Generator> generatorCache = new HashSet<>();

        boolean[][] fixedCycleIndices = new boolean[][] {
            {true, false, false, false, false, false, false, false},
        };

        HashSet<Integer> uniqueFaces = new HashSet<>();
        PermuCallback.generateCombinations(32, 8, (b) -> {
    

            int[][] cyclesA = new int[][] {
                PentagonalIcositrahedron.getFacesFromVertex(b[0] + 1),
                PentagonalIcositrahedron.getFacesFromVertex(b[1] + 1),
                PentagonalIcositrahedron.getFacesFromVertex(b[2] + 1),
                PentagonalIcositrahedron.getFacesFromVertex(b[3] + 1),
                PentagonalIcositrahedron.getFacesFromVertex(b[4] + 1),
                PentagonalIcositrahedron.getFacesFromVertex(b[5] + 1),
                PentagonalIcositrahedron.getFacesFromVertex(b[6] + 1),
                PentagonalIcositrahedron.getFacesFromVertex(b[7] + 1),
            };


            uniqueFaces.clear();
            for (int[] cycle : cyclesA) {
                for (int face : cycle) {
                    uniqueFaces.add(face);
                }
            }
            
            if (uniqueFaces.size() == 6*4) {

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
}
