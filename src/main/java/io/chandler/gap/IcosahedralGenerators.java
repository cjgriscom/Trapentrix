package io.chandler.gap;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;

import io.chandler.gap.GroupExplorer.Generator;
import io.chandler.gap.GroupExplorer.MemorySettings;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class IcosahedralGenerators {

    static final int[][][] icosahedronSliceEdges = new int[][][] {
        /*1*/ {{3,5}, {10,8}, {2,7}, {6,1}},
        //TODO
    };

    static final int[][] dodecahedronFaceAboutFaceSymmetries = new int[][] {
        /*1*/ {2,12,5,8,9},
        /*2*/ {1,9,10,3,12},
        /*3*/ {2,10,6,4,12},
        /*4*/ {7,5,12,3,6},
        /*5*/ {4,7,8,1,12},
        /*6*/ {7,4,3,10,11},
        /*7*/ {5,4,6,11,8},
        /*8*/ {1,5,7,11,9},
        /*9*/ {1,8,11,10,2},
        /*10*/ {2,9,11,6,3},
        /*11*/ {9,8,7,6,10},
        /*12*/ {1,2,3,4,5},
    };
    
    static final int[][] dodecahedronFaceAboutVertex = new int[][] {
        // Shallow    Deep       Deep      Shallow
        {1, 2,12}, {3, 5, 9}, {8, 10, 4}, {6, 7, 11},
        {2, 3,12}, {4, 1,10}, {9,  6, 5}, {7, 8, 11},
        {3, 4,12}, {5, 2, 6}, {10, 7, 1}, {8, 9, 11},
        {4, 5,12}, {1, 3, 7}, {6,  8, 2}, {9, 10,11},
        {5, 1,12}, {2, 4, 8}, {7,  9, 3}, {10, 6,11},

        {4, 5, 7}, {6, 8,12}, {1, 3,11}, {2, 9,10},
        {3, 4, 6}, {10,7,12}, {5, 2,11}, {1, 8, 9},
        {2, 3,10}, {9, 6,12}, {4, 1,11}, {5, 7, 8},
        {1, 2, 9}, {8,10,12}, {3, 5,11}, {4, 6, 7},
        {5, 1, 8}, {7, 9,12}, {2, 4,11}, {3,10, 6}
    };
    
    static final int[][] dodecahedronFaceAboutVertex_Shallow = new int[][] {
        // Shallow    Shallow
        {1, 2,12},  {6, 7, 11},
        {2, 3,12},  {7, 8, 11},
        {3, 4,12},  {8, 9, 11},
        {4, 5,12},  {9, 10,11},
        {5, 1,12},  {10, 6,11},

        {4, 5, 7},  {2, 9,10},
        {3, 4, 6},  {1, 8, 9},
        {2, 3,10},   {5, 7, 8},
        {1, 2, 9},  {4, 6, 7},
        {5, 1, 8},   {3,10, 6}
    };
    
    static final int[][] dodecahedronFaceAboutVertex_Deep = new int[][] {
        // Deep       Deep
        {3, 5, 9}, {8, 10, 4},
        {4, 1,10}, {9,  6, 5},
        {5, 2, 6}, {10, 7, 1},
        {1, 3, 7}, {6,  8, 2},
        {2, 4, 8}, {7,  9, 3},

        {6, 8,12}, {1, 3,11},
        {10,7,12}, {5, 2,11},
        {9, 6,12}, {4, 1,11},
        {8,10,12}, {3, 5,11},
        {7, 9,12}, {2, 4,11},
    };

    public static void main(String[] args) {
        //M11_Slice_and_Vertex_Dodec();
        //M12_Deep_and_Shallow_Icosahedron();
        //M12_Vertex_Shallow();
        //M12_Vertex_Shallow_Threefold();
        //M11_Vertex_Shallow_Fivefold();
        // M12_ThreeAxis();
        //M12_5_11();
        TrapentrixFinder();

    }


    public static void TrapentrixFinder() {
        // 1/2 of the trapentrix points correspond to 9 faces on a rhombic triacontahedron

        GroupExplorer group = new GroupExplorer(Generators.ree3, MemorySettings.FASTEST);
        
        ArrayList<int[][]> generatorCandidates = new ArrayList<>();
        ArrayList<int[][]> generatorCandidates2 = new ArrayList<>();
        exploreGroup(group, (state, cycleDescription) -> {
           
            if (cycleDescription.equals("dual 3-cycles")) {
                if (Math.random() > 0.0) generatorCandidates.add(GroupExplorer.stateToCycles(state));
            }
            if (cycleDescription.equals("dual 3-cycles")) {
                if (Math.random() > 0.0) generatorCandidates2.add(GroupExplorer.stateToCycles(state));
            }

        });
        
        Map<Generator, Integer> generatorPairs = findGeneratorPairs(group, generatorCandidates, generatorCandidates2);

        reportReducedIsomorphisms(generatorPairs);

        System.out.println("Searching for icosahedral generators");

        boolean foundMatch = false;
        int checkedIcosahedralGenerators = 0;
        HashMap<String, Integer> matchingGenerators = new HashMap<>();

        // List tiers of symmetries for easier reporting
        String[] tierLabels = new String[] {
            /* 0    1     2       3     4      5     6      7      8     9 */
            "D1", "D2L", "D2R", "D3", "D4L", "D4R", "D5", "D6L", "D6R", "D7",
        };
        int[] tierDepths = new int[] {
            1, 2, 2, 3, 4, 4, 5, 6, 6, 7,
        };
        Map<Integer, String> tierNames = new HashMap<>();
        for (int i = 1; i <= 20; i++) {
            int[][] axis1 = Dodecahedron.getEdgeSymmetriesAlongVertexAxis(i);
            for (int j = 0; j < 10; j++) {
                String name = "V" + i + "-" + tierLabels[j];
                int[] sorted = axis1[j].clone();
                Arrays.sort(sorted);
                int index = 25*25 * sorted[0] + 25 * sorted[1] + sorted[2];
                String old = tierNames.get(index);
                if (old != null) {
                    //tierNames.put(index, old + ", " + name);
                } else {
                    tierNames.put(index, name);
                }
            }
        }

        int axisDepth = 7; // Search Depth 1, 2L, 2R, 3, 4L, 4R, 5

        // Select 3 vertex axes
        int vertex0 = 1;
        int[][] axisA = Dodecahedron.getEdgeSymmetriesAlongVertexAxis(vertex0);
        for (int vertex1 = 1; vertex1 <= 20; vertex1++) {
            if (vertex1 == vertex0) continue;
            int[][] axisB = Dodecahedron.getEdgeSymmetriesAlongVertexAxis(vertex1);

            // Select pairs of 3-cycles from each axis
            for (int[] a : Permu.generateCombinations(axisDepth, 2)) {
                HashSet<Integer> aTiers = new HashSet<>();
                HashSet<Integer> aDepths = new HashSet<>();
                for (int aTier : a) aTiers.add(aTier);
                for (int aTier : a) aDepths.add(tierDepths[aTier]);
                for (int[] b : Permu.generateCombinations(axisDepth, 2)) {
                    HashSet<Integer> bTiers = new HashSet<>();
                    HashSet<Integer> bDepths = new HashSet<>();
                    for (int bTier : b) bTiers.add(bTier);
                    for (int bTier : b) bDepths.add(tierDepths[bTier]);

                    // Remove asymmetrical generators
                    if (!aDepths.equals(bDepths)) continue;

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
                    if (conflicts) continue;


                    int[][][] generator = new int[2][][];
                    boolean[][] fixedCycleIndices = new boolean[][] {
                        {true, true},
                        {true, true}
                    };
        
                    generator[0] = new int[][] {
                        axisA[a[0]],
                        axisA[a[1]],
                    };
                    generator[1] = new int[][] {
                        axisB[b[0]],
                        axisB[b[1]],
                    };
        
                    for (int[][][] genCandidate : CycleInverter.generateInvertedCycles(fixedCycleIndices, generator)) {
                        Generator g = new Generator(GroupExplorer.renumberGenerators(genCandidate));
                        checkedIcosahedralGenerators++;
                        if (generatorPairs.containsKey(g)) {
                            if (!foundMatch) {
                                System.out.println("Found a match! #" + generatorPairs.get(g));
                                System.out.println(GroupExplorer.generatorsToString(genCandidate));
                                foundMatch = true;
                            }
                            matchingGenerators.put(GroupExplorer.generatorsToString(genCandidate), generatorPairs.get(g));
                        }
                    }
                }
            }
        }

        System.out.println("Checked " + checkedIcosahedralGenerators + " icosahedral generators");
        
        System.out.println("Matching generators: " + matchingGenerators.size());

        for (String s : matchingGenerators.keySet()) {
            int[][][] gen = GroupExplorer.parseOperationsArr(s);
            boolean first = true;
            for (int[][] g : gen) {
                if (!first) System.out.print(",");
                first = false;
                for (int[] h : g) {
                    System.out.print("(");
                    int[] sorted = h.clone();
                    Arrays.sort(sorted);
                    System.out.print(tierNames.get(25*25 * sorted[0] + 25 * sorted[1] + sorted[2]));
                    System.out.print(")");
                }
            }
            System.out.println("\t" + s);
        }
    }

    public static void M12_5_11() {
        // M10 Antiprism ["(3,12)(9,6)(2,11)(1,10),(6,5,4,3,2,1)(7,8,9,10,11,12)] or [(7,8)(3,4),(6,5,4,3,2,1)(7,8,9,10,11,12)]


        // This generates M12
        // Removing one of the 3x3s generates M11
        // Pretty cool
        // [(7,8,11)(2,9,10)(3,4,6),(11,10,6)(12,3,4)(5,8,7),(6,4,7)(1,5,8)(9,10,11)]

        // This generates M12
        // [(1,2,12)(3,5,9)(10,8,4)(6,11,7),(4,7,8,1,12)(3,6,11,9,2)]
        // It's a 5-fold alternating chopasaurus that can only turn around the center band
        
        GroupExplorer group = new GroupExplorer(Generators.m12, MemorySettings.FASTEST);
        

        // (11,1)(6,10)(12,2)(5,9),
        group = new GroupExplorer(Generators._2_m12, MemorySettings.COMPACT);
        //group = new GroupExplorer("[(2,3)(5,6)(8,9)(11,12),(1,2,4)(3,5,7)(6,8,10)(9,11,12)]");
        exploreGroup(group, null);

        /*try (GZIPOutputStream out = new GZIPOutputStream(new BufferedOutputStream(
                new FileOutputStream("m24.gap.gz")))) {
            group.serialize(out);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        System.exit(0);

        
        HashMap<Generator, Integer> generatorCandidates11 = new HashMap<>();
        HashMap<Generator, Integer> generatorCandidates6 = new HashMap<>();
        int[] count = new int[]{0};

        exploreGroup(group, (state, cycleDescription) -> {
           
            if (cycleDescription.equals("quadruple 2-cycles")) {
                Generator g = new Generator(new int[][][]{GroupExplorer.stateToCycles(state)});
                if (generatorCandidates6.containsKey(g)) {
                    generatorCandidates6.put(g, generatorCandidates6.get(g));
                } else {
                    GroupExplorer.genIsomorphisms_Callback(g.generator, false, (iso) -> {
                        generatorCandidates6.put(new Generator(iso), count[0]);
                    });
                }
            } if (cycleDescription.equals("quadruple 2-cycles")) {
                Generator g = new Generator(new int[][][]{GroupExplorer.stateToCycles(state)});
                if (generatorCandidates11.containsKey(g)) {
                    generatorCandidates11.put(g, generatorCandidates11.get(g));
                } else {
                    GroupExplorer.genIsomorphisms_Callback(g.generator, false, (iso) -> {
                        generatorCandidates11.put(new Generator(iso), count[0]);
                    });
                }
            }
            count[0]++;
        });

        HashMap<Integer, Generator> reducedGenerators11 = new HashMap<>();
        for (Entry<Generator, Integer> entry : generatorCandidates11.entrySet()) {
            reducedGenerators11.put(entry.getValue(), entry.getKey());
        }
        HashMap<Integer, Generator> reducedGenerators6 = new HashMap<>();
        for (Entry<Generator, Integer> entry : generatorCandidates6.entrySet()) {
            reducedGenerators6.put(entry.getValue(), entry.getKey());
        }
        
        
        ArrayList<Generator> reducedGeneratorsList11 = new ArrayList<>(reducedGenerators11.values());
        ArrayList<Generator> reducedGeneratorsList6 = new ArrayList<>(reducedGenerators6.values());

        ArrayList<int[][]> generators11 = new ArrayList<>();
        ArrayList<int[][]> generators6 = new ArrayList<>();

        // 19245600
        for (Generator g : reducedGeneratorsList11) {
           if (Math.random() > 0.98) generators11.add(g.generator[0]);
        }
        for (Generator g : reducedGeneratorsList6) {
            if (Math.random() > 0.98) generators6.add(g.generator[0]);
         }

        System.out.println("Found " + reducedGeneratorsList11.size() + " / " + reducedGeneratorsList6.size() + " reduced generators");
        
        Map<Generator, Integer> generatorPairs = findGeneratorPairs(group, generators11, generators6);

        reportReducedIsomorphisms(generatorPairs);

        System.out.println("Searching for icosahedral generators");

        boolean foundMatch = false;
        int checkedIcosahedralGenerators = 0;
        HashMap<String, Integer> matchingGenerators = new HashMap<>();

        // Loop through each possible combination of 2 vertices from the icosahedron as generator 1
        for (int[] c : Permu.generateCombinations(3, 3)) {
            
            int[][][] generator = new int[2][][];
            boolean[][] fixedCycleIndices = new boolean[][] {{true, true, true, true}, {false, false, false, false}};

            generator[0] = new int[][] {
                {1,2,12}, // Shallow vertex
                {3,5,9},
                {4,8,10},//{4,8,10},
                {7,11,6},//{7,11,6},
            };
            generator[1] = icosahedronSliceEdges[0];
            

            for (int[][][] genCandidate : CycleInverter.generateInvertedCycles(fixedCycleIndices, generator)) {
                Generator g = new Generator(GroupExplorer.renumberGenerators(genCandidate));
                checkedIcosahedralGenerators++;
                if (generatorPairs.containsKey(g)) {
                    if (!foundMatch) {
                        System.out.println("Found a match! #" + generatorPairs.get(g));
                        System.out.println(GroupExplorer.generatorsToString(genCandidate));
                        foundMatch = true;
                    }
                    matchingGenerators.put(GroupExplorer.generatorsToString(genCandidate), generatorPairs.get(g));
                }
            }
        }

        System.out.println("Checked " + checkedIcosahedralGenerators + " icosahedral generators");
        
        reportMatchingGenerators(matchingGenerators);
    }

    public static void M12_ThreeAxis() {
        int stateLimit = 100000;
        GroupExplorer group = new GroupExplorer(Generators.m12, MemorySettings.FASTEST);
        
        HashMap<Generator, Integer> generatorCandidates = new HashMap<>();
        int[] count = new int[]{0};
        exploreGroup(group, (state, cycleDescription) -> {
           
            if (cycleDescription.equals("dual 6-cycles")) {
                Generator g = new Generator(new int[][][]{GroupExplorer.stateToCycles(state)});
                if (generatorCandidates.containsKey(g)) {
                    generatorCandidates.put(g, generatorCandidates.get(g));
                } else {
                    GroupExplorer.genIsomorphisms_Callback(g.generator, false, (iso) -> {
                        generatorCandidates.put(new Generator(iso), count[0]);
                    });
                }
            }
            count[0]++;
        });

        HashMap<Integer, Generator> reducedGenerators = new HashMap<>();
        for (Entry<Generator, Integer> entry : generatorCandidates.entrySet()) {
            reducedGenerators.put(entry.getValue(), entry.getKey());
        }

        ArrayList<Generator> reducedGeneratorsList = new ArrayList<>(reducedGenerators.values());

        ArrayList<int[][]> generatorCandidates0 = new ArrayList<>();
        ArrayList<int[][]> generatorCandidates1 = new ArrayList<>();
        ArrayList<int[][]> generatorCandidates2 = new ArrayList<>();

        boolean[] first = new boolean[]{true};
        for (Generator g : reducedGeneratorsList) {
           if (first[0]) {
            generatorCandidates0.add(g.generator[0]);
            first[0] = false;
           }
           else if (Math.random() > 0.995) generatorCandidates1.add(g.generator[0]);
           else if (Math.random() > 0.995) generatorCandidates2.add(g.generator[0]);
        }

        System.out.println("Found " + reducedGeneratorsList.size() + " reduced generators");
        
        HashMap<Generator, Integer> generatorPairs = findGeneratorTriples(stateLimit, group, generatorCandidates0, generatorCandidates1, generatorCandidates2);

        reportReducedIsomorphisms(generatorPairs);

    }

    public static void M11_Vertex_Shallow_Fivefold() {
        
        GroupExplorer group = new GroupExplorer(Generators.m11, MemorySettings.FASTEST);
        
        ArrayList<int[][]> generatorCandidates = new ArrayList<>();
        ArrayList<int[][]> generatorCandidates2 = new ArrayList<>();
        exploreGroup(group, (state, cycleDescription) -> {
           
            if (cycleDescription.equals("dual 5-cycles")) {
                if (Math.random() > 0.98) generatorCandidates.add(GroupExplorer.stateToCycles(state));
            }
            if (cycleDescription.equals("triple 3-cycles")) {
                if (Math.random() > 0.95) generatorCandidates2.add(GroupExplorer.stateToCycles(state));
            }

        });
        
        Map<Generator, Integer> generatorPairs = findGeneratorPairs(group, generatorCandidates, generatorCandidates2, true);

        reportReducedIsomorphisms(generatorPairs);

        System.out.println("Searching for icosahedral generators");

        boolean foundMatch = false;
        int checkedIcosahedralGenerators = 0;
        HashMap<String, Integer> matchingGenerators = new HashMap<>();

        // Loop through each possible combination of 2 vertices from the icosahedron as generator 1
        for (int[] c : Permu.generateCombinations(dodecahedronFaceAboutVertex_Shallow.length, 3)) {
            
            int[][][] generator = new int[2][][];
            boolean[][] fixedCycleIndices = new boolean[][] {
                {true, true},
                {false, false, false}
            };

            generator[0] = new int[][] {
                {1,2,3,4,5},{6,7,8,9,10}
            };
            generator[1] = new int[][] {
                dodecahedronFaceAboutVertex_Shallow[c[0]],
                dodecahedronFaceAboutVertex_Shallow[c[1]],
                dodecahedronFaceAboutVertex_Shallow[c[2]],
            };

            for (int[][][] genCandidate : CycleInverter.generateInvertedCycles(fixedCycleIndices, generator)) {
                Generator g = new Generator(GroupExplorer.renumberGenerators(genCandidate));
                checkedIcosahedralGenerators++;
                if (generatorPairs.containsKey(g)) {
                    if (!foundMatch) {
                        System.out.println("Found a match! #" + generatorPairs.get(g));
                        System.out.println(GroupExplorer.generatorsToString(genCandidate));
                        foundMatch = true;
                    }
                    matchingGenerators.put(GroupExplorer.generatorsToString(genCandidate), generatorPairs.get(g));
                }
            }
        }

        System.out.println("Checked " + checkedIcosahedralGenerators + " icosahedral generators");
        
        reportMatchingGenerators(matchingGenerators);
    }

    public static void M12_Vertex_Shallow_Threefold() {
        
        GroupExplorer group = new GroupExplorer(Generators.m12, MemorySettings.FASTEST);
        
        ArrayList<int[][]> generatorCandidates = new ArrayList<>();
        ArrayList<int[][]> generatorCandidates2 = new ArrayList<>();
        exploreGroup(group, (state, cycleDescription) -> {
           
            if (cycleDescription.equals("quadruple 3-cycles")) {
                if (Math.random() > 0.9) generatorCandidates.add(GroupExplorer.stateToCycles(state));
            }
            if (cycleDescription.equals("triple 3-cycles")) {
                if (Math.random() > 0.75) generatorCandidates2.add(GroupExplorer.stateToCycles(state));
            }

        });
        
        Map<Generator, Integer> generatorPairs = findGeneratorPairs(group, generatorCandidates, generatorCandidates2, true);

        reportReducedIsomorphisms(generatorPairs);

        System.out.println("Searching for icosahedral generators");

        boolean foundMatch = false;
        int checkedIcosahedralGenerators = 0;
        HashMap<String, Integer> matchingGenerators = new HashMap<>();

        // Loop through each possible combination of 2 vertices from the icosahedron as generator 1
        for (int[] c : Permu.generateCombinations(dodecahedronFaceAboutVertex_Shallow.length, 3)) {
            
            int[][][] generator = new int[2][][];
            boolean[][] fixedCycleIndices = new boolean[][] {
                {true, true, true, true},
                {false, false, false}
            };

            generator[0] = new int[][] {
                {1, 2,12}, {3, 5, 9}, {4, 10, 8}, {11, 7, 6}
            };
            generator[1] = new int[][] {
                dodecahedronFaceAboutVertex_Shallow[c[0]],
                dodecahedronFaceAboutVertex_Shallow[c[1]],
                dodecahedronFaceAboutVertex_Shallow[c[2]],
            };

            for (int[][][] genCandidate : CycleInverter.generateInvertedCycles(fixedCycleIndices, generator)) {
                Generator g = new Generator(GroupExplorer.renumberGenerators(genCandidate));
                checkedIcosahedralGenerators++;
                if (generatorPairs.containsKey(g)) {
                    if (!foundMatch) {
                        System.out.println("Found a match! #" + generatorPairs.get(g));
                        System.out.println(GroupExplorer.generatorsToString(genCandidate));
                        foundMatch = true;
                    }
                    matchingGenerators.put(GroupExplorer.generatorsToString(genCandidate), generatorPairs.get(g));
                }
            }
        }

        System.out.println("Checked " + checkedIcosahedralGenerators + " icosahedral generators");
        
        reportMatchingGenerators(matchingGenerators);
    }

    public static void M12_Vertex_Shallow() {
        
        GroupExplorer group = new GroupExplorer(Generators.m12, MemorySettings.FASTEST);
        
        ArrayList<int[][]> generatorCandidates = new ArrayList<>();
        ArrayList<int[][]> generatorCandidates2 = new ArrayList<>();
        exploreGroup(group, (state, cycleDescription) -> {
           
            if (cycleDescription.equals("triple 3-cycles")) {
                if (Math.random() > 0.98) generatorCandidates.add(GroupExplorer.stateToCycles(state));
            }
            if (cycleDescription.equals("triple 3-cycles")) {
                if (Math.random() > 0.98) generatorCandidates2.add(GroupExplorer.stateToCycles(state));
            }

        });
        
        Map<Generator, Integer> generatorPairs = findGeneratorPairs(group, generatorCandidates, generatorCandidates2);

        reportReducedIsomorphisms(generatorPairs);

        System.out.println("Searching for icosahedral generators");

        boolean foundMatch = false;
        int checkedIcosahedralGenerators = 0;
        HashMap<String, Integer> matchingGenerators = new HashMap<>();

        // Loop through each possible combination of 2 vertices from the icosahedron as generator 1
        for (int[] c : Permu.generateCombinations(dodecahedronFaceAboutVertex_Shallow.length, 5)) {
            
            int[][][] generator = new int[2][][];
            boolean[][] fixedCycleIndices = new boolean[2][3];

            generator[0] = new int[][] {
                {5,7,8}, // Shallow vertex
                dodecahedronFaceAboutVertex_Shallow[c[0]],
                dodecahedronFaceAboutVertex_Shallow[c[1]],
            };
            fixedCycleIndices[0][0] = true;
            fixedCycleIndices[0][1] = true;
            generator[1] = new int[][] {
                dodecahedronFaceAboutVertex_Shallow[c[2]],
                dodecahedronFaceAboutVertex_Shallow[c[3]],
                dodecahedronFaceAboutVertex_Shallow[c[4]],
            };
            fixedCycleIndices[1][0] = true;

            for (int[][][] genCandidate : CycleInverter.generateInvertedCycles(fixedCycleIndices, generator)) {
                Generator g = new Generator(GroupExplorer.renumberGenerators(genCandidate));
                checkedIcosahedralGenerators++;
                if (generatorPairs.containsKey(g)) {
                    if (!foundMatch) {
                        System.out.println("Found a match! #" + generatorPairs.get(g));
                        System.out.println(GroupExplorer.generatorsToString(genCandidate));
                        foundMatch = true;
                    }
                    matchingGenerators.put(GroupExplorer.generatorsToString(genCandidate), generatorPairs.get(g));
                }
            }
        }

        System.out.println("Checked " + checkedIcosahedralGenerators + " icosahedral generators");
        
        reportMatchingGenerators(matchingGenerators);
    }

    public static void M11_Slice_and_Vertex_Dodec() {
        
        GroupExplorer group = new GroupExplorer(Generators.m11, MemorySettings.FASTEST);
        
        ArrayList<int[][]> generatorCandidates = new ArrayList<>();
        ArrayList<int[][]> generatorCandidates2 = new ArrayList<>();
        exploreGroup(group, (state, cycleDescription) -> {
           
            if (cycleDescription.equals("dual 5-cycles")) {
                if (Math.random() > 0.95) generatorCandidates.add(GroupExplorer.stateToCycles(state));
            }
            if (cycleDescription.equals("triple 3-cycles")) {
                if (Math.random() > 0.9) generatorCandidates2.add(GroupExplorer.stateToCycles(state));
            }

        });
        
        Map<Generator, Integer> generatorPairs = findGeneratorPairs(group, generatorCandidates, generatorCandidates2);

        reportReducedIsomorphisms(generatorPairs);

        System.out.println("Searching for icosahedral generators");

        boolean foundMatch = false;
        int checkedIcosahedralGenerators = 0;
        HashMap<String, Integer> matchingGenerators = new HashMap<>();

        // Start with slice moves
        int[][][] generator = new int[2][][];
        generator[0] = new int[][] {
            {1,2,3,4,5},
            {6,7,8,9,10}
        };
        boolean[][] fixedCycleIndices = new boolean[][] {
            {true, true}, // Hold the pentagons stationary
            {false, false, false} // Allow the vertices to invert
        };

        // Loop through each possible combination of 3 vertices from the icosahedron as generator 1
        for (int[] c : Permu.generateCombinations(dodecahedronFaceAboutVertex.length, 3)) {
            
            generator[1] = new int[][] {
                dodecahedronFaceAboutVertex[c[0]],
                dodecahedronFaceAboutVertex[c[1]],
                dodecahedronFaceAboutVertex[c[2]]
            };

            for (int[][][] genCandidate : CycleInverter.generateInvertedCycles(fixedCycleIndices, generator)) {
                Generator g = new Generator(GroupExplorer.renumberGenerators(genCandidate));
                checkedIcosahedralGenerators++;
                if (generatorPairs.containsKey(g)) {
                    if (!foundMatch) {
                        System.out.println("Found a match! #" + generatorPairs.get(g));
                        System.out.println(GroupExplorer.generatorsToString(genCandidate));
                        foundMatch = true;
                    }
                    matchingGenerators.put(GroupExplorer.generatorsToString(genCandidate), generatorPairs.get(g));
                }
            }
        }

        System.out.println("Checked " + checkedIcosahedralGenerators + " icosahedral generators");
        
        reportMatchingGenerators(matchingGenerators);
    }

    public static void M12_Deep_and_Shallow_Icosahedron() {
        
        GroupExplorer group = new GroupExplorer(Generators.m12, MemorySettings.FASTEST);
        
        ArrayList<int[][]> generatorCandidates = new ArrayList<>();
        ArrayList<int[][]> generatorCandidates2 = new ArrayList<>();
        exploreGroup(group, (state, cycleDescription) -> {
           
            if (cycleDescription.equals("triple 3-cycles")) {
                if (Math.random() > 0.98) generatorCandidates.add(GroupExplorer.stateToCycles(state));
            }
            if (cycleDescription.equals("triple 3-cycles")) {
                if (Math.random() > 0.98) generatorCandidates2.add(GroupExplorer.stateToCycles(state));
            }

        });
        
        Map<Generator, Integer> generatorPairs = findGeneratorPairs(group, generatorCandidates, generatorCandidates2);

        reportReducedIsomorphisms(generatorPairs);

        System.out.println("Searching for icosahedral generators");

        boolean foundMatch = false;
        int checkedIcosahedralGenerators = 0;
        HashMap<String, Integer> matchingGenerators = new HashMap<>();

        // Loop through each possible combination of 2 vertices from the icosahedron as generator 1
        for (int[] c : Permu.generateCombinations(dodecahedronFaceAboutVertex.length, 2)) {
            
            int[][][] generator = new int[2][][];
            boolean[][] fixedCycleIndices = new boolean[2][3];

            generator[0] = new int[][] {
                {1,2,12}, // Shallow vertex
                {3,5,9}, // Deep cut on same vertex
                dodecahedronFaceAboutVertex[c[0]]
            };
            fixedCycleIndices[0][0] = true;
            fixedCycleIndices[0][1] = true;
            generator[1] = new int[][] {
                {5,7,8}, // Another shallow vertex
                {1,4,11}, // Deep cut on same vertex
                dodecahedronFaceAboutVertex[c[1]]
            };
            fixedCycleIndices[1][0] = true;
            fixedCycleIndices[1][1] = true;

            for (int[][][] genCandidate : CycleInverter.generateInvertedCycles(fixedCycleIndices, generator)) {
                Generator g = new Generator(GroupExplorer.renumberGenerators(genCandidate));
                checkedIcosahedralGenerators++;
                if (generatorPairs.containsKey(g)) {
                    if (!foundMatch) {
                        System.out.println("Found a match! #" + generatorPairs.get(g));
                        System.out.println(GroupExplorer.generatorsToString(genCandidate));
                        foundMatch = true;
                    }
                    matchingGenerators.put(GroupExplorer.generatorsToString(genCandidate), generatorPairs.get(g));
                }
            }
        }

        System.out.println("Checked " + checkedIcosahedralGenerators + " icosahedral generators");
        
        reportMatchingGenerators(matchingGenerators);
    }

    public static void exploreGroup(GroupExplorer gap,
            BiConsumer<int[], String> peekCyclesAndDescriptions) {

        long nPermutations = 1;
        for (int i = 1; i <= gap.nElements; i++) {
            nPermutations *= i;
        }

        HashMap<String, Integer> cycleDescriptions = new HashMap<>();

        int iterations = gap.exploreStates(true, (states, depth) -> {
            for (int[] state : states) {
                String cycleDescription = GroupExplorer.describeState(gap.nElements, state);
                if (peekCyclesAndDescriptions != null) peekCyclesAndDescriptions.accept(state, cycleDescription);
                cycleDescriptions.merge(cycleDescription, 1, Integer::sum);
            }
        });
        
        System.out.println("Elements: " + gap.nElements);
        System.out.println("Total unique permutations: " + nPermutations);
        System.out.println("Total group permutations: " + gap.order());

        System.out.println("Subset: 1/" + ((double)nPermutations / gap.order()));
        System.out.println("Iterations: " + iterations);


        // Print sorted cycle descriptions
        System.out.println("Cycle structure frequencies:");
        cycleDescriptions.entrySet().stream()
            .sorted((e1, e2) -> {
                int comp = Integer.compare(e2.getValue(), e1.getValue()); // Sort by frequency descending
                if (comp == 0) {
                    return e1.getKey().compareTo(e2.getKey()); // If frequencies are equal, sort alphabetically
                }
                return comp;
            })
            .forEach(entry -> System.out.println(entry.getValue() + ": " + entry.getKey()));

    }

    public static Map<Generator, Integer> findGeneratorPairs(GroupExplorer group, ArrayList<int[][]> generatorCandidates, ArrayList<int[][]> generatorCandidates2) {
        return findGeneratorPairs(group, generatorCandidates, generatorCandidates2, false);
    }
    public static Map<Generator, Integer> findGeneratorPairs(GroupExplorer group, ArrayList<int[][]> generatorCandidates, ArrayList<int[][]> generatorCandidates2, boolean verbose) {
        
        System.out.println("Generator candidates: " + generatorCandidates.size());
        System.out.println("Generator candidates2: " + generatorCandidates2.size());

        // Make a list of generator pairs : src index
        //HashMap<Generator, Integer> generatorPairs = new HashMap<>();

        Object2IntOpenHashMap<Generator> generatorPairs = new Object2IntOpenHashMap<>();
        int lastSize = 0;
        // Loop thru pairs of generator candidates
        for (int i = 0; i < generatorCandidates.size(); i++) {
            if (i % 10 == 0) System.out.println("Checking generator "+i+"/"+0+" of " + generatorCandidates.size() + " - " + generatorPairs.size() + " pairs found");
            int[][] aCycles = generatorCandidates.get(i);
            int j0 = generatorCandidates == generatorCandidates2 ? i + 1 : 0;
            for (int j = j0; j < generatorCandidates2.size(); j++) {
                int[][] bCycles = generatorCandidates2.get(j);

                if (!GroupExplorer.cyclesContainsAllElements(group.nElements, aCycles, bCycles)) continue;

                int[][][] generator = new int[][][] { aCycles, bCycles };
                generator = GroupExplorer.renumberGenerators(generator);
                if (generatorPairs.containsKey(new Generator(generator))) continue;

                String composite = "[" + GroupExplorer.cyclesToNotation(aCycles) + "," + GroupExplorer.cyclesToNotation(bCycles) + "]";
                GroupExplorer compositeGAP = new GroupExplorer(composite, group.mem);
                compositeGAP.exploreStates(false, null);
                int order = compositeGAP.order();
                if (order == group.order()) {
                    for (int[][][] iso : GroupExplorer.genIsomorphisms(generator)) {
                        generatorPairs.put(new Generator(iso), i * generatorCandidates.size() + j);
                    }
                }
                if (generatorPairs.size() > lastSize) {
                    if (verbose) System.out.println("Checking generator "+i+"/"+j+" of " + generatorCandidates.size() + " - " + generatorPairs.size() + " pairs found");
                    lastSize = generatorPairs.size();
                }
            }
        }

        System.out.println("Isomorphic Generator pairs: " + generatorPairs.size());
        return generatorPairs;
    }
   
    public static HashMap<Generator, Integer> findGeneratorTriples(int stateLimit, GroupExplorer group, ArrayList<int[][]> generatorCandidates, ArrayList<int[][]> generatorCandidates2, ArrayList<int[][]> generatorCandidates3) {
        
        System.out.println("Generator candidates: " + generatorCandidates.size());
        System.out.println("Generator candidates2: " + generatorCandidates2.size());
        System.out.println("Generator candidates3: " + generatorCandidates3.size());

        // Make a list of generator triples : src index
        HashMap<Generator, Integer> generatorTriples = new HashMap<>();
        int lastSize = 0;

        // Loop thru triples of generator candidates
        for (int i = 0; i < generatorCandidates.size(); i++) {
            int[][] aCycles = generatorCandidates.get(i);
            int j0 = generatorCandidates == generatorCandidates2 ? i + 1 : 0;
            for (int j = j0; j < generatorCandidates2.size(); j++) {
                int[][] bCycles = generatorCandidates2.get(j);
                int k0 = (generatorCandidates == generatorCandidates3) ? j + 1 : 0;
                for (int k = k0; k < generatorCandidates3.size(); k++) {
                    int iF = i, jF = j, kF = k;
                    int[][] cCycles = generatorCandidates3.get(k);

                    if (!GroupExplorer.cyclesContainsAllElements(group.nElements, aCycles, bCycles, cCycles)) continue;

                    int[][][] generator = new int[][][] { aCycles, bCycles, cCycles };
                    generator = GroupExplorer.renumberGenerators(generator);
                    if (generatorTriples.containsKey(new Generator(generator))) continue;

                    String composite = "[" + GroupExplorer.cyclesToNotation(aCycles) + "," + GroupExplorer.cyclesToNotation(bCycles) + "," + GroupExplorer.cyclesToNotation(cCycles) + "]";
                    GroupExplorer compositeGAP = new GroupExplorer(composite, group.mem);
                    //System.out.println("Exploring " + composite);
                    int iterations = compositeGAP.exploreStates(false, stateLimit, null);
                    int order = compositeGAP.order();
                    //System.out.println("Generating isomorphisms for " + composite);
                    if (iterations > 0 && order % group.order() == 0) {
                        int[][][] genPartial = new int[][][] {generator[1], generator[2]};
                        int[][] gen0 = generator[0];
                        GroupExplorer.genIsomorphisms_Callback(genPartial, false, (iso) -> {
                            int[][][] isoFull = GroupExplorer.renumberGenerators(new int[][][] {gen0, iso[0], iso[1]});
                            generatorTriples.put(new Generator(isoFull), iF * generatorCandidates.size() * generatorCandidates2.size() + jF * generatorCandidates2.size() + kF);
                        });
                        generatorTriples.put(new Generator(generator), iF * generatorCandidates.size() * generatorCandidates2.size() + jF * generatorCandidates2.size() + kF);
                        //System.out.println(GroupExplorer.generatorsToString(generator) + " -> " + composite);


                    }
                    if (generatorTriples.size() > lastSize) {
                        System.out.println("Checking generator "+i+"/"+j+"/"+k+" of " + generatorCandidates.size() + " - " + generatorTriples.size() + " triples found");
                        lastSize = generatorTriples.size();
                    }

                }
            }
        }

        System.out.println("Isomorphic Generator triples: " + generatorTriples.size());
        return generatorTriples;
    }

    public static void reportMatchingGenerators(HashMap<String, Integer> matchingGenerators) {
        System.out.println("Matching generators: " + matchingGenerators.size());

        for (String s : matchingGenerators.keySet()) {
            System.out.println(s);
        }
    }

    public static void reportReducedIsomorphisms(Map<Generator, Integer> generators) {
        HashMap<Integer, Generator> filteredGenerators = new HashMap<>();
        for (Entry<Generator, Integer> entry : generators.entrySet()) {
            filteredGenerators.put(entry.getValue(), entry.getKey());
        }
        System.out.println("Generators with isomorphisms removed: " + filteredGenerators.size());

        ArrayList<int[][][]> reducedGenerators = new ArrayList<>();
        for (Generator g : filteredGenerators.values()) {
            reducedGenerators.add(g.generator);
        }

        // Print generators
        for (int[][][] g : reducedGenerators) {
            System.out.println(GroupExplorer.renumberGeneratorNotation(GroupExplorer.generatorsToString(g)));
        }
    }
}
