package io.chandler.gap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import io.chandler.gap.GroupExplorer.Generator;
import io.chandler.gap.GroupExplorer.State;

public class IcosahedralGenerators {
	
	static final int[][] icosahedronVertexSymmetries = new int[][] {
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

    public static void main(String[] args) {
        M12_Deep_and_Shallow_Icosahedron();
    }

    public static void M12_Deep_and_Shallow_Icosahedron() {
        
        GroupExplorer group = new GroupExplorer(Generators.m12);
        
        ArrayList<State> generatorCandidates = new ArrayList<>();
        ArrayList<State> generatorCandidates2 = new ArrayList<>();
        exploreGroup(group, (state, cycleDescription) -> {
           
            if (cycleDescription.equals("triple 3-cycles")) {
                if (Math.random() > 0.98) generatorCandidates.add(state);
            }
            if (cycleDescription.equals("triple 3-cycles")) {
                if (Math.random() > 0.98) generatorCandidates2.add(state);
            }

        });
        
        HashMap<Generator, Integer> generatorPairs = findGeneratorPairs(group, generatorCandidates, generatorCandidates2);

        boolean foundMatch = false;
        int checkedIcosahedralGenerators = 0;
        HashMap<String, Integer> matchingGenerators = new HashMap<>();
        for (int[] c : Permu.generateCombinations(icosahedronVertexSymmetries.length, 3)) {
            int[][][] generator = new int[2][][];
            generator[0] = new int[][] {
                icosahedronVertexSymmetries[0],
                icosahedronVertexSymmetries[1],
                {11,8,7}
            };
            generator[1] = new int[][] {
                icosahedronVertexSymmetries[c[0]],
                icosahedronVertexSymmetries[c[1]],
                icosahedronVertexSymmetries[c[2]]
            };

            int startAtGeneratorIndex = 1;

			for (int[][][] genCandidate : CycleInverter.generateInvertedCycles(startAtGeneratorIndex, generator)) {
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
        reportReducedIsomorphisms(generatorPairs);
    }

    public static void exploreGroup(GroupExplorer gap,
            BiConsumer<State, String> peekCyclesAndDescriptions) {

        long nPermutations = 1;
        for (int i = 1; i <= gap.nElements; i++) {
            nPermutations *= i;
        }

        HashMap<String, Integer> cycleDescriptions = new HashMap<>();

        int iterations = gap.exploreStates(true, (state, depth) -> {
            String cycleDescription = gap.describeState(state.state);
            peekCyclesAndDescriptions.accept(state, cycleDescription);
            cycleDescriptions.merge(cycleDescription, 1, Integer::sum);
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
        

    public static HashMap<Generator, Integer> findGeneratorPairs(GroupExplorer group, ArrayList<State> generatorCandidates, ArrayList<State> generatorCandidates2) {
        
        System.out.println("Generator candidates: " + generatorCandidates.size());
        System.out.println("Generator candidates2: " + generatorCandidates2.size());

        // Make a list of generator pairs : src index
        HashMap<Generator, Integer> generatorPairs = new HashMap<>();

        // Loop thru pairs of generator candidates
        for (int i = 0; i < generatorCandidates.size(); i++) {
            if (i % 10 == 0) System.out.println("Checking generator " + i + " of " + generatorCandidates.size() + " - " + generatorPairs.size() + " pairs found");
            State a = generatorCandidates.get(i);
            int[][] aCycles = GroupExplorer.stateToCycles(a.state);
            int j0 = generatorCandidates == generatorCandidates2 ? i + 1 : 0;
            for (int j = j0; j < generatorCandidates2.size(); j++) {
                State b = generatorCandidates2.get(j);
                int[][] bCycles = GroupExplorer.stateToCycles(b.state);

                if (!GroupExplorer.cyclesContainsAllElements(group.nElements, aCycles, bCycles)) continue;

                int[][][] generator = new int[][][] { aCycles, bCycles };
                generator = GroupExplorer.renumberGenerators(generator);
                if (generatorPairs.containsKey(new Generator(generator))) continue;

                String composite = "[" + GroupExplorer.cyclesToNotation(aCycles) + "," + GroupExplorer.cyclesToNotation(bCycles) + "]";
                GroupExplorer compositeGAP = new GroupExplorer(composite);
                compositeGAP.exploreStates(false, null);
                int order = compositeGAP.order();
                if (order == group.order()) {
                    for (int[][][] iso : GroupExplorer.genIsomorphisms(generator)) {
                        generatorPairs.put(new Generator(iso), i * generatorCandidates.size() + j);
                    }
                }
            }
        }

        System.out.println("Isomorphic Generator pairs: " + generatorPairs.size());
        return generatorPairs;
    }

    public static void reportMatchingGenerators(HashMap<String, Integer> matchingGenerators) {
        System.out.println("Matching generators: " + matchingGenerators.size());

        for (String s : matchingGenerators.keySet()) {
            System.out.println(s);
        }
    }

    public static void reportReducedIsomorphisms(HashMap<Generator, Integer> generatorPairs) {
        HashMap<Integer, Generator> filteredGeneratorPairs = new HashMap<>();
        for (Entry<Generator, Integer> entry : generatorPairs.entrySet()) {
            filteredGeneratorPairs.put(entry.getValue(), entry.getKey());
        }
        System.out.println("Reduced isomorphisms: " + filteredGeneratorPairs.size());

        ArrayList<int[][][]> reducedGenerators = new ArrayList<>();
        for (Generator g : filteredGeneratorPairs.values()) {
            reducedGenerators.add(g.generator);
        }

        // Print generator pairs
        for (int[][][] pair : reducedGenerators) {
            System.out.println(GroupExplorer.renumberGeneratorNotation("["+GroupExplorer.cyclesToNotation(pair[0]) + "," + GroupExplorer.cyclesToNotation(pair[1]) + "]"));
        }
    }
}
