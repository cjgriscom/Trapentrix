package io.chandler.gap;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.chandler.gap.GroupExplorer.Generator;
import io.chandler.gap.GroupExplorer.MemorySettings;

public class MirrorGenerators {

    private static final int[][] piMirror3 = {
        //{2, 10, 20}, {20, 23, 21}, // Tentative 1
        //{12, 11, 10}, {23, 24, 22}, // Tentative 2
        {17, 18, 1}, {5, 4, 1},
        {1, 18, 5}, {3, 20, 19},
        {2, 20, 3}, {20, 21, 19},
        {11, 12, 14}, {24, 9, 22},
        {13, 14, 12}, {9, 8, 22},
        {13, 15, 14}, {7, 8, 9},
        {17, 15, 13}, {4, 8, 7},
        {17, 16, 18}, {4, 5, 6},
        {3, 18, 16}, {5, 19, 6},
        {2, 3, 16}, {6, 19, 21},
        {2, 12, 10}, {22,21,23},
        {13, 16, 17}, {4, 6, 8},
    };

    public static void main(String[] args) throws Exception{
        fullPairSearch();
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
                if (coverageCount != 22 && coverageCount != 23) {
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

            //System.out.println("Checking " + vertices2.size() + " matches");

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
        PermuCallback.generateCombinations(piMirror3.length, 6, (b) -> {
    

            int[][] cyclesA = new int[][] {
                piMirror3[b[0]],
                piMirror3[b[1]],
                piMirror3[b[2]],
                piMirror3[b[3]],
                piMirror3[b[4]],
                piMirror3[b[5]],
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
        ParityStateCache cache = new ParityStateCache(new M23StateCache());
        String genString = GroupExplorer.generatorsToString(g.generator());
        GroupExplorer candidate = new GroupExplorer(
            genString,
            MemorySettings.DEFAULT, cache);
            

        ArrayList<String> depthPeek = new ArrayList<>();
        int startCheckingRatioIncreaseAtOrder = 313692;/// 739215;
        int limit = 443520 + 10;//10200960 + 1;
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
        } else if (candidate.order() >= 443520) {
            // Add genString to smallGroupGenerators
            List<String> gens = smallGroupGenerators.computeIfAbsent(candidate.order(), k -> Collections.synchronizedList(new ArrayList<String>()));
            gens.add(genString);
            System.out.println("Found order " + (candidate.order()) + ": " + GroupExplorer.generatorsToString(g.generator()));
        }
        

    }
}
