package io.chandler.trapentrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

public class GAP {
    // M6 / Eliac: [ (1,2,3,4)(5,6,7,8), (1,5)(2,9)(4,10) ]
    private Map<State, Boolean> stateMap = new HashMap<>();
    private int[] elements;
    private List<int[][]> parsedOperations;
    private int nElements;

    public static class State {
        int[] state;
        public State(int[] state) {
            this.state = state;
        }
        public int hashCode() {
            return Arrays.hashCode(state);
        }
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            State state1 = (State) obj;
            return Arrays.equals(state, state1.state);
        }
    }

    public static class Generator {
        int[][][] generator;
        public Generator(int[][][] generator) {
            this.generator = generator;
        }
        public int hashCode() {
            return Arrays.deepHashCode(generator);
        }
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Generator generator1 = (Generator) obj;
            return Arrays.deepEquals(generator, generator1.generator);
        }
    }

    public static void main(String[] args) {
        //https://www.sciencedirect.com/science/article/pii/0012365X9390222F?ref=cra_js_challenge&fr=RR-1
        String m12 = "[(1,2,3)(4,5,6)(7,8,9)(10,11,12),(3,4)(6,7)(9,10)(11,12)]";
        //String cycleNotation = "[(1,2,3,4)(5,6,7,8),(1,5)(2,9)(4,10))]";
        String m11 = "[(1,2,3,4,5,6,7,8,9,10,11),(3,7,11,8)(4,10,5,6)]"; // M11
        //String m11 = "[(1,2,3,4)(5,6,7,8),(9,8)(10,3)(2,6)(4,11)]"; // M11

        String m11_12pt = "[(1,6)(2,9)(5,7)(8,10),(1,6,7,4)(2,8)(3,9)(5,11,12,10)]";
        //String cycleNotation = "[(1,3,11,7)(2,4,9,6),(1,11)(2,9)(3,7)(4,6),(8,3)(7,4)(5,6)(9,10)]"; // M11 Face turn metric
        //String cycleNotation = "[(1,3,11,7)(2,4,9,6),(8,3)(7,4)(5,6)(9,10)]"; // M11 Q turn metric
        //String cycleNotation = "[(1,2,3,4,5,6,7,8,9,10,11),(3,7,11,8)(4,10,5,6),(1,12)(2,11)(3,6)(4,8)(5,9)(7,10)]"; // M12
        String trapentrix = "[(1,2,3)(5,6,7),(2,4,5)(6,8,9)]"; // Trapentrix
        
        String cycleNotation = m11_12pt;
        //cycleNotation = "[(1,2,3,4,5,6,7,8,9,10,11),(1,7,6,5,10,8,9,11)(3,4)]";

        //cycleNotation = trapentrix;

        // This is order 660, PSL(2,11) ?
        //cycleNotation = "[(1,2,3,4,5,6,7,8,9,10,11),(2,4)(3,9)(5,10)(7,11)]";

        // Oops V2
        //cycleNotation = "[(2,5,3)(4,7,9)(6,12,11),(1,7,6)(2,4,8)(9,12,10)]";
        //cycleNotation = "[(1,4,2)(3,6,8)(5,11,10),(4,6,5)(1,3,7)(8,11,9)]";

        // M11 Icosahedral
        cycleNotation = "[(1,2,3,4,5)(6,7,8,9,10),(11,10,9)(1,5,6)(2,8,3)]";
        

        // Alternating pentultimate
        //cycleNotation = "[(1,2,3,4,5)(6,7,8,9,10),(1,11,4,10,9)(2,8,12,6,3)]";

        // Mildly interesting - upper vertices on pentultimate with edge turn
        //cycleNotation = "[(1,2,3,4,5)(6,7,8,9,10),(4,11)(5,3)(1,2)(6,7)]";

        
        //cycleNotation = m11;

        int[][] icosahedronVertexSymmetries = new int[][] {
            {1, 2, 12}, {3, 5, 9}, {8, 10, 4}, {6, 7, 11},
            {2, 3, 12}, {4, 1, 10},{9,  6, 5}, {7, 8, 11},
            {3, 4, 12}, {5, 2, 6}, {10, 7, 1}, {8, 9, 11},
            {4, 5, 12}, {1, 3, 7}, {6,  8, 2}, {9, 10, 11},
            {5, 1, 12}, {2, 4, 8}, {7,  9, 3}, {10, 6, 11}
        };


        GAP gap = new GAP(cycleNotation);
        
        long nPermutations = 1;
        for (int i = 1; i <= gap.nElements; i++) {
            nPermutations *= i;
        }

        HashMap<String, Integer> cycleDescriptions = new HashMap<>();

        ArrayList<State> generatorCandidates = new ArrayList<>();
        ArrayList<State> generatorCandidates2 = new ArrayList<>();

        int iterations = gap.exploreStates(true, (state, depth) -> {
            String cycleDescription = gap.describeState(state.state);

            if (cycleDescription.equals("dual 5-cycles")) {
                if (Math.random() > 0.9) generatorCandidates.add(state);
            }
            if (cycleDescription.equals("triple 3-cycles")) {
                if (Math.random() > 0.5) generatorCandidates2.add(state);
            }

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


        System.out.println("Generator candidates: " + generatorCandidates.size());
        System.out.println("Generator candidates2: " + generatorCandidates2.size());

        // Make a list of generator pairs : src index
        HashMap<Generator, Integer> generatorPairs = new HashMap<>();

        // Loop thru pairs of generator candidates
        for (int i = 0; i < generatorCandidates.size(); i++) {
            if (i % 10 == 0) System.out.println("Checking generator " + i + " of " + generatorCandidates.size() + " - " + generatorPairs.size() + " pairs found");
            State a = generatorCandidates.get(i);
            int[][] aCycles = stateToCycles(a.state);
            int j0 = generatorCandidates == generatorCandidates2 ? i + 1 : 0;
            for (int j = j0; j < generatorCandidates2.size(); j++) {
                State b = generatorCandidates2.get(j);
                int[][] bCycles = stateToCycles(b.state);

                if (!cyclesContainsAllElements(gap.nElements, aCycles, bCycles)) continue;

                int[][][] generator = new int[][][] { aCycles, bCycles };
                generator = renumberGenerators(generator);
                if (generatorPairs.containsKey(new Generator(generator))) continue;

                String composite = "[" + cyclesToNotation(aCycles) + "," + cyclesToNotation(bCycles) + "]";
                GAP compositeGAP = new GAP(composite);
                compositeGAP.exploreStates(false, null);
                int order = compositeGAP.order();
                if (order == gap.order()) {
                    for (int[][][] iso : genIsomorphisms(generator)) {
                        generatorPairs.put(new Generator(iso), i * generatorCandidates.size() + j);
                    }
                }
            }
        }

        System.out.println("Contains original generator? " + generatorPairs.containsKey(new Generator(GAP.parseOperationsArr(cycleNotation))));
        System.out.println("Isomorphic Generator pairs: " + generatorPairs.size());

        boolean foundMatch = false;
        int checkedIcosahedralGenerators = 0;
        HashMap<Integer, String> matchingGenerators = new HashMap<>();
        for (int[] c : Permu.generateCombinations(20, 3)) {
            int[][][] generator = new int[2][][];
            generator[0] = new int[][] { {1,2,3,4,5}, {6,7,8,9,10} };
            generator[1] = new int[][] {
                icosahedronVertexSymmetries[c[0]],
                icosahedronVertexSymmetries[c[1]],
                icosahedronVertexSymmetries[c[2]]
            };
            //for (int[][][] genCandidateP : Permu.applyGeneratorPermutationsAndRotations(generator)) {
                for (int[][][] genCandidate : CycleInverter.generateInvertedCycles(generator)) {
                    Generator g = new Generator(renumberGenerators(genCandidate));
                    checkedIcosahedralGenerators++;
                    if (generatorPairs.containsKey(g)) {
                        if (!foundMatch) {
                            System.out.println("Found a match! " + generatorPairs.get(g));
                            System.out.println(GAP.generatorsToString(genCandidate));
                            foundMatch = true;
                        }
                        matchingGenerators.put(generatorPairs.get(g), GAP.generatorsToString(genCandidate));
                    }
                }
            //}
        }

        System.out.println("Checked " + checkedIcosahedralGenerators + " icosahedral generators");
        
        HashMap<Integer, Generator> filteredGeneratorPairs = new HashMap<>();
        for (Entry<Generator, Integer> entry : generatorPairs.entrySet()) {
            filteredGeneratorPairs.put(entry.getValue(), entry.getKey());
        }
        System.out.println("Removed isomorphisms: " + filteredGeneratorPairs.size());

        ArrayList<int[][][]> attempt2Removal = new ArrayList<>();
        for (Generator g : filteredGeneratorPairs.values()) {
            attempt2Removal.add(g.generator);
        }

        // Remove isomorphisms
        /*
        int pairs = filteredGeneratorPairs.size();
        System.out.println("Discovered generator pairs: " + pairs);
        Iterator<int[][][]> outerIter = attempt2Removal.iterator();
        int counter = 0;
        int lastPercent = 0;
        for (; outerIter.hasNext();) {
            counter++;
            int[][][] pair = outerIter.next();
            for (int i = 0; i < attempt2Removal.size(); i++) {
                int[][][] otherPair = attempt2Removal.get(i);
                if (pair != otherPair && isIsomorphic_Simple(pair, otherPair)) {
                    outerIter.remove();
                    break;
                }
            }
            int percent = (int)(100 * counter / (double)pairs);
            if (percent != lastPercent && percent % 10 == 0) {
                System.out.println("Removed isomorphisms: " + percent + "%");
                lastPercent = percent;
            }
        }
        System.out.println("Removed isomorphisms: " + attempt2Removal.size());
        */

        // Print generator pairs
        for (int[][][] pair : attempt2Removal) {
            System.out.println(renumberGeneratorNotation("["+cyclesToNotation(pair[0]) + "," + cyclesToNotation(pair[1]) + "]"));
        }
    }

    public GAP(String cycleNotation) {
        for (String s : cycleNotation.split("\\(|\\)|,|\\[|\\]")) {
            if (!s.trim().isEmpty()) nElements = Math.max(nElements, Integer.parseInt(s.trim()));
        }

        elements = initializeElements(nElements);
        parsedOperations = parseOperations(cycleNotation);
    }

    public int order() {
        return stateMap.size();
    }

    public static String stateToNotation(int[] state) {
        int[][] cycles = stateToCycles(state);
        return cyclesToNotation(cycles);
    }

    private static int[][] stateToCycles(int[] state) {
        boolean[] visited = new boolean[state.length];
        List<List<Integer>> cyclesList = new ArrayList<>();

        for (int i = 0; i < state.length; i++) {
            if (!visited[i]) {
                int current = i;
                List<Integer> cycle = new ArrayList<>();
                
                do {
                    visited[current] = true;
                    cycle.add(current + 1);
                    current = state[current] - 1;
                } while (current != i);

                // Only add non-trivial cycles (length > 1)
                if (cycle.size() > 1) {
                    cyclesList.add(cycle);
                }
            }
        }

        return cyclesList.stream()
                .map(cycle -> cycle.stream().mapToInt(Integer::intValue).toArray())
                .toArray(int[][]::new);
    }

    private static String cyclesToNotation(int[][] cycles) {
        if (cycles.length == 0) {
            return "()";
        }

        StringBuilder notation = new StringBuilder();
        for (int[] cycle : cycles) {
            notation.append("(");
            for (int i = 0; i < cycle.length; i++) {
                notation.append(cycle[i]);
                if (i < cycle.length - 1) {
                    notation.append(",");
                }
            }
            notation.append(")");
        }

        return notation.toString();
    }

    private int[] initializeElements(int maxElement) {
        int[] result = new int[maxElement];
        for (int i = 0; i < maxElement; i++) {
            result[i] = i + 1;
        }
        return result;
    }

    private int exploreStates(boolean debug, BiConsumer<State, Integer> peekStateAndDepth) {
        stateMap.put(new State(elements.clone()), false);

        int lastSize = 0;
        int iteration = 0;
        while (true) {
            if (debug) System.out.println("Depth: " + iteration + " - " + (stateMap.size() - lastSize));
            
            iteration++;
            HashMap<State, Boolean> stateMapCopy = new HashMap<>(stateMap);

            for (Entry<State, Boolean> entry : stateMap.entrySet()) {
                if (entry.getValue()) {
                    continue;
                }
                State state = entry.getKey();
                int[] currentState = state.state;

                boolean added = false;
                for (int[][] operation : parsedOperations) {
                    int[] newState = applyOperation(currentState, operation);

                    Boolean exhausted = stateMapCopy.get(new State(newState));
                    if (exhausted == null) {
                        State s = new State(newState);
                        stateMapCopy.put(s, false);
                        if (peekStateAndDepth != null) peekStateAndDepth.accept(s, iteration);
                        added = true;
                    }
                }
                if (!added) {
                    stateMapCopy.put(state, true);
                }
            }
            if (stateMapCopy.size() == stateMap.size()) {
                return iteration;
            }
            lastSize = stateMap.size();
            stateMap = stateMapCopy;
        }
    }

    public static int[][][] parseOperationsArr(String groupNotation) {
        List<int[][]> operations = parseOperations(groupNotation);
        return operations.toArray(new int[operations.size()][][]);
    }

    public static List<int[][]> parseOperations(String groupNotation) {
        List<int[][]> operations = new ArrayList<>();
        String[] parts = groupNotation.substring(1, groupNotation.length() - 1).split("\\),\\(");
        for (String part : parts) {
            String[] cycles = part.split("\\)\\(");
            int[][] operation = new int[cycles.length][];
            for (int i = 0; i < cycles.length; i++) {
                String[] elements = cycles[i].replaceAll("[()]", "").split(",");
                operation[i] = Arrays.stream(elements).mapToInt(Integer::parseInt).toArray();
            }
            operations.add(operation);
            operations.add(reverseOperation(operation));
        }
        return operations;
    }

    public static int[][] reverseOperation(int[][] operation) {
        int[][] reversedOps = new int[operation.length][];
        for (int o = 0; o < operation.length; o++) {
            int[] cycle = operation[o];
            int[] reversed = new int[cycle.length];
            for (int i = 0; i < cycle.length; i++) {
                reversed[i] = cycle[cycle.length - i - 1];
            }
            reversedOps[o] = reversed;
        }
        return reversedOps;
    }
    
    public static int[][] repeatOperation(int[][] operation, int n) {
        n++;
        int[][] result = new int[operation.length][];
        for (int i = 0; i < operation.length; i++) {
            int[] cycle = operation[i];
            int[] cycleRepeated = new int[cycle.length];
            for (int j = 0; j < cycle.length; j++) {
                cycleRepeated[j] = cycle[(j * n) % cycle.length];
            }
            result[i] = cycleRepeated;
        }
        return result;
    }

    private int[] applyOperation(int[] state, int[][] operation) {
        int[] newState = Arrays.copyOf(state, state.length);
        for (int[] cycle : operation) {
            if (cycle.length > 1) {
                int first = cycle[0];
                for (int i = 0; i < cycle.length - 1; i++) {
                    int current = cycle[i];
                    int next = cycle[i + 1];
                    newState[current - 1] = state[next - 1];
                }
                newState[cycle[cycle.length - 1] - 1] = state[first - 1];
            }
        }
        return newState;
    }
    
    private String describeState(int[] state) {
        String s = stateToNotation(state);
        int[] nCycles = new int[nElements + 1];
        String[] cycles = s.split("\\(");
        
        for (String cycle : cycles) {
            int length = cycle.split(",").length;
            if (length > 1) { // Ignore 1-cycles (fixed points)
                nCycles[length]++;
            }
        }
        
        StringBuilder description = new StringBuilder();
        boolean first = true;
        for (int i = 2; i <= nElements; i++) {
            if (nCycles[i] > 0) {
                if (!first) {
                    description.append(", ");
                }
                String multiplicity = getMultiplicityDescription(nCycles[i]);
                description.append(multiplicity).append(" ").append(i).append("-cycle");
                if (nCycles[i] > 1) {
                    description.append("s");
                }
                first = false;
            }
        }
        return description.toString();
    }
    private String getMultiplicityDescription(int count) {
        switch (count) {
            case 1: return "single";
            case 2: return "dual";
            case 3: return "triple";
            case 4: return "quadruple";
            case 5: return "quintuple";
            default: return count + "p";
        }
    }

    private static boolean cyclesContainsAllElements(int nElements, int[][]... cyclesList) {
        int[] missingElements = new int[nElements];
        for (int[][] cycles : cyclesList) {
            for (int[] cycle : cycles) {
                for (int element : cycle) { missingElements[element - 1]++; }
            }
        }
        for (int missing : missingElements) {
            if (missing == 0) {
                return false;
            }
        }
        return true;
    }

    private static List<int[][][]> genIsomorphisms(int[][][] a) {
        List<int[][][]> checks = new ArrayList<>();
        for (int[][][] aPerm : Permu.applyGeneratorPermutationsAndRotations(a)) {
            checks.add(renumberGenerators(aPerm));
        }
        return checks;
    }

    // I didn't write this and I don't trust it
    /*
    private static boolean isIsomorphic_Simple(int[][][] a, int[][][] b) {
        if (a.length != b.length) return false;
        
        // Sort generators by number of cycles and cycle lengths
        Arrays.sort(a, Comparator.<int[][]>comparingInt(gen -> gen.length)
                .thenComparing(gen -> Arrays.stream(gen).mapToInt(cycle -> cycle.length).sum()));
        Arrays.sort(b, Comparator.<int[][]>comparingInt(gen -> gen.length)
                .thenComparing(gen -> Arrays.stream(gen).mapToInt(cycle -> cycle.length).sum()));
        
        // Check if generator structures match
        for (int i = 0; i < a.length; i++) {
            if (a[i].length != b[i].length) return false;
            for (int j = 0; j < a[i].length; j++) {
                if (a[i][j].length != b[i][j].length) return false;
            }
        }
        
        // Try all possible mappings
        return tryMapping(a, b, new HashMap<>(), new HashSet<>(), 0, 0);
    }
    
    private static boolean tryMapping(int[][][] a, int[][][] b, Map<Integer, Integer> mapping, Set<Integer> used, int genIndex, int cycleIndex) {
        if (genIndex == a.length) return true;
        
        if (cycleIndex == a[genIndex].length) {
            return tryMapping(a, b, mapping, used, genIndex + 1, 0);
        }
        
        int[] cycleA = a[genIndex][cycleIndex];
        int[] cycleB = b[genIndex][cycleIndex];
        
        // Try all rotations of cycleB
        for (int rotation = 0; rotation < cycleB.length; rotation++) {
            boolean validMapping = true;
            Map<Integer, Integer> newMapping = new HashMap<>(mapping);
            Set<Integer> newUsed = new HashSet<>(used);
            
            for (int i = 0; i < cycleA.length; i++) {
                int elementA = cycleA[i];
                int elementB = cycleB[(i + rotation) % cycleB.length];
                
                if (newMapping.containsKey(elementA)) {
                    if (newMapping.get(elementA) != elementB) {
                        validMapping = false;
                        break;
                    }
                } else if (newUsed.contains(elementB)) {
                    validMapping = false;
                    break;
                } else {
                    newMapping.put(elementA, elementB);
                    newUsed.add(elementB);
                }
            }
            
            if (validMapping && tryMapping(a, b, newMapping, newUsed, genIndex, cycleIndex + 1)) {
                return true;
            }
        }
        
        return false;
    }
    */

    public static String renumberGeneratorNotation(String gapNotation) {
        Map<Integer, Integer> newIndices = new HashMap<>();
        int nextIndex = 1;
        StringBuilder result = new StringBuilder("[");
        String[] generators = gapNotation.substring(1, gapNotation.length() - 1).split(",(?=\\()");
    
        for (int i = 0; i < generators.length; i++) {
            String generator = generators[i].trim();
            StringBuilder newGenerator = new StringBuilder("");
            String[] cycles = generator.split("\\)\\(");
    
            for (int j = 0; j < cycles.length; j++) {
                newGenerator.append("(");
                String cycle = cycles[j].replaceAll("[()]", "");
                String[] elements = cycle.split(",");
                
                for (int k = 0; k < elements.length; k++) {
                    int oldIndex = Integer.parseInt(elements[k].trim());
                    if (!newIndices.containsKey(oldIndex)) {
                        newIndices.put(oldIndex, nextIndex++);
                    }
                    newGenerator.append(newIndices.get(oldIndex));
                    if (k < elements.length - 1) {
                        newGenerator.append(",");
                    }
                }
                newGenerator.append(")");
            }
    
            result.append(newGenerator);
            if (i < generators.length - 1) {
                result.append(",");
            }
        }
        result.append("]");
        return result.toString();
    }

    public static int[][][] renumberGenerators(int[][][] generators) {
        Map<Integer, Integer> newIndices = new HashMap<>();
        int nextIndex = 1;
        int[][][] result = new int[generators.length][][];

        for (int i = 0; i < generators.length; i++) {
            int[][] generator = generators[i];
            int[][] newGenerator = new int[generator.length][];

            for (int j = 0; j < generator.length; j++) {
                int[] cycle = generator[j];
                int[] newCycle = new int[cycle.length];

                for (int k = 0; k < cycle.length; k++) {
                    int oldIndex = cycle[k];
                    if (!newIndices.containsKey(oldIndex)) {
                        newIndices.put(oldIndex, nextIndex++);
                    }
                    newCycle[k] = newIndices.get(oldIndex);
                }

                newGenerator[j] = newCycle;
            }

            result[i] = newGenerator;
        }

        return result;
    }

    // Helper method to convert int[][][] to String (for debugging or display purposes)
    public static String generatorsToString(int[][][] generators) {
        StringBuilder result = new StringBuilder("[");
        for (int i = 0; i < generators.length; i++) {
            for (int[] cycle : generators[i]) {
                result.append("(");
                for (int j = 0; j < cycle.length; j++) {
                    result.append(cycle[j]);
                    if (j < cycle.length - 1) {
                        result.append(",");
                    }
                }
                result.append(")");
            }
            if (i < generators.length - 1) {
                result.append(",");
            }
        }
        result.append("]");
        return result.toString();
    }

}
