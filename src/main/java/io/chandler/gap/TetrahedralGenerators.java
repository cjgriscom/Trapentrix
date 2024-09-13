package io.chandler.gap;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import io.chandler.gap.GroupExplorer.Generator;
import io.chandler.gap.GroupExplorer.MemorySettings;
import io.chandler.gap.cache.M12StateCache;
import io.chandler.gap.cache.ParityStateCache;

import static io.chandler.gap.IcosahedralGenerators.dodecahedronFaceAboutVertex_Shallow;

public class TetrahedralGenerators {

    public static final String tet1 = "(4,3,6)(7,12,10)(1,9,8)(2,11,5)";
    public static final String tet2 = "(1,2,12)(3,5,9)(8,10,4)(6,7,11)";
    public static final String tet3 = "(5,7,8)(1,4,11)(9,12,6)(3,10,2)";
    public static final String tet4 = "(10,9,11)(2,8,6)(7,3,1)(5,4,12)";
    
    
    public static void main(String[] args) throws Exception {

        HashSet<Generator> validVertexCombinations = new HashSet<>();
        
        int[][][] symm = new int[][][] {
            GroupExplorer.parseOperationsArr("["+tet1+"]")[0],
            GroupExplorer.parseOperationsArr("["+tet2+"]")[0],
            GroupExplorer.parseOperationsArr("["+tet3+"]")[0],
            GroupExplorer.parseOperationsArr("["+tet4+"]")[0]
        };


        HashSet<Integer> uniqueFaces = new HashSet<>();

        boolean[][] fixedCycleIndices = new boolean[][] {
            {true, true, true, true},
            {true, true, true, true},
            {true, true, true, true},
            {true, true, true, true},
            {true, false, false},
        };

        int[][] d = dodecahedronFaceAboutVertex_Shallow;

        PermuCallback.generateCombinations(dodecahedronFaceAboutVertex_Shallow.length, 3, (b) -> {
    

            int[][] cyclesA = new int[][] {
                {10,6,11},
                d[b[0]],
                d[b[1]],
            };


            uniqueFaces.clear();
            for (int[] cycle : cyclesA) {
                for (int face : cycle) {
                    uniqueFaces.add(face);
                }
            }
            
            if (uniqueFaces.size() == 3*3) {

                int[][][] genSrc = new int[][][] {
                    symm[0],
                    symm[1],
                    symm[2],
                    symm[3],
                    cyclesA,
                };
                List<int[][][]> cycled = CycleInverter.generateInvertedCycles(fixedCycleIndices, genSrc);

                for (int[][][] c : cycled) {
                    Generator g = new Generator(c);
                    validVertexCombinations.add(g);
                }
                
            }

        });

        System.out.println("Found " + validVertexCombinations.size() + " possible generators");
        //System.exit(0);
        int[] iteration = new int[] {0};
        long combinations = validVertexCombinations.size();
        long startTime = System.currentTimeMillis();

        HashMap<Integer, Integer> orderCounts = new HashMap<>();

        Iterator<Generator> iter = validVertexCombinations.iterator();
        while (iter.hasNext()) {
            Generator g = iter.next();
            if (iteration[0] % 100 == 0) {
                printProgressEstimate(iteration[0], combinations, startTime, validVertexCombinations.size());
            }

            GroupExplorer candidate = new GroupExplorer(
                GroupExplorer.generatorsToString(g.generator()),
                MemorySettings.FASTEST, new ParityStateCache(new M12StateCache()));
            
            ArrayList<String> depthPeek = new ArrayList<>();
            int limit = 99000;
            int[] stateCount = new int[2];
            int iters;
            
            double[] lastRatio = new double[] {0};
            try { iters = candidate.exploreStates(false, limit, (states, depth) -> {
                stateCount[0] = stateCount[1];
                stateCount[1] += states.size();
                double ratio = depth < 2 ? 10000 : stateCount[1]/(double)stateCount[0];
                depthPeek.add(ratio+" " + stateCount[1] + ",");

                /*if (depth == 12 && stateCount[0] > 1049999) {
                    throw new RuntimeException();
                }*/
                if (depth > 3 && ratio > lastRatio[0]) {
                    throw new RuntimeException();
                }
                lastRatio[0] = ratio;
            }); } catch (RuntimeException e) {
                iters = -2;
            }

            if (iters == -2) {
                /*if (depthPeek.size() > 4) {
                    System.out.println("Reject " + iters + " Last iter states: " + depthPeek.get(depthPeek.size() - 1) + " Depth: " + depthPeek.size());
                    System.out.println(depthPeek.toString());
                }*/
                iter.remove();
            } else if (iters == -1) {
                //System.out.println("Iters: " + iters + " Last iter states: " + depthPeek.get(depthPeek.size() - 1) + " Depth: " + depthPeek.size());
                //System.out.println( GroupExplorer.generatorsToString(new int[][][] {g.generator()[g.generator.length -1]}));
                //System.out.println(depthPeek.toString());
            } else{
                orderCounts.merge(candidate.order(), 1, Integer::sum);
                if (candidate.order() == 95040) {
                    System.out.println("Found order " + (candidate.order()) + ": " + GroupExplorer.generatorsToString(new int[][][] {g.generator()[g.generator.length -1]}));
                } else {
                    iter.remove();
                }
            }
            
            iteration[0]++;
        }

        System.out.println("Filtered down to " + validVertexCombinations.size() + " valid generators");
        // Write to file 

        Files.deleteIfExists(Paths.get("valid_generators_tetrahedral.txt"));
        PrintStream out = new PrintStream("valid_generators_tetrahedral.txt");
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
