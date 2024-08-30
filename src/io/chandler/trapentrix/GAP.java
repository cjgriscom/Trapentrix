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
    private static Map<State, Boolean> stateMap = new HashMap<>();
    private static int[] elements;
    private static List<int[][]> parsedOperations;

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

    public static void main(String[] args) {
        //String cycleNotation = "[(1,2,3,4)(5,6,7,8),(1,5)(2,9)(4,10))]";
        String m11 = "[(1,2,3,4,5,6,7,8,9,10,11),(3,7,11,8)(4,10,5,6)]"; // M11
        //String cycleNotation = "[(1,3,11,7)(2,4,9,6),(1,11)(2,9)(3,7)(4,6),(8,3)(7,4)(5,6)(9,10)]"; // M11 Face turn metric
        //String cycleNotation = "[(1,3,11,7)(2,4,9,6),(8,3)(7,4)(5,6)(9,10)]"; // M11 Q turn metric
        //String cycleNotation = "[(1,2,3,4,5,6,7,8,9,10,11),(3,7,11,8)(4,10,5,6),(1,12)(2,11)(3,6)(4,8)(5,9)(7,10)]"; // M12
        String trapentrix = "[(1,2,3)(5,6,7),(2,4,5)(6,8,9)]"; // Trapentrix
        
        String cycleNotation = m11;
        //cycleNotation = "[(1,2,3,4,5,6,7,8,9,10,11),(1,7,6,5,10,8,9,11)(3,4)]";

        cycleNotation = trapentrix;
        
        // This is order 660, PSL(2,11) ?
        cycleNotation = "[(1,2,3,4,5,6,7,8,9,10,11),(2,4)(3,9)(5,10)(7,11)]";

        int nElements = 0;
        for (String s : cycleNotation.split("\\(|\\)|,|\\[|\\]")) {
            if (!s.trim().isEmpty()) nElements = Math.max(nElements, Integer.parseInt(s.trim()));
        }
        long nPermutations = 1;
        for (int i = 1; i <= nElements; i++) {
            nPermutations *= i;
        }

        ArrayList<String> interestingStates = new ArrayList<>();

        elements = initializeElements(nElements);
        parsedOperations = parseOperations(cycleNotation);

        int nF = nElements;
        HashMap<String, Integer> cycleDescriptions = new HashMap<>();

        int iterations = exploreStates(elements, (state, depth) -> {
            
            String cycleDescription = describeState(nF, state.state);
            cycleDescriptions.merge(cycleDescription, 1, Integer::sum);
        });
        
        System.out.println("Elements: " + nElements);
        System.out.println("Total unique permutations: " + nPermutations);
        System.out.println("Total group permutations: " + stateMap.size());

        System.out.println("Subset: 1/" + ((double)nPermutations / stateMap.size()));
        System.out.println("Iterations: " + iterations);

        System.out.println("Initial state in cycle notation: " + stateToNotation(elements));

        System.out.println("Interesting states: " + interestingStates.size());
        int interestingStateCount = 0;

        // Sort interesting states by length
        Collections.sort(interestingStates, (a, b) -> a.length() - b.length());


        for (String s : interestingStates) {
            System.out.println(s);
            interestingStateCount++;
            if (interestingStateCount > 10) break;
        }

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

    // New method to convert a state to cycle notation
    public static String stateToNotation(int[] state) {
        boolean[] visited = new boolean[state.length];
        StringBuilder notation = new StringBuilder();

        for (int i = 0; i < state.length; i++) {
            if (!visited[i]) {
                int current = i;
                StringBuilder cycle = new StringBuilder("(");
                
                do {
                    visited[current] = true;
                    cycle.append(current + 1);
                    current = state[current] - 1;
                    
                    if (current != i) {
                        cycle.append(",");
                    }
                } while (current != i);

                cycle.append(")");
                
                // Only add non-trivial cycles (length > 1)
                if (cycle.length() > 3) {
                    notation.append(cycle);
                }
            }
        }

        return notation.length() > 0 ? notation.toString() : "()";
    }

    private static int[] initializeElements(int maxElement) {
        int[] result = new int[maxElement];
        for (int i = 0; i < maxElement; i++) {
            result[i] = i + 1;
        }
        return result;
    }

    private static int exploreStates(int[] initialState, BiConsumer<State, Integer> peekStateAndDepth) {
        stateMap.put(new State(initialState), false);

        int lastSize = 0;
        int iteration = 0;
        while (true) {
            System.out.println("Depth: " + iteration + " - " + (stateMap.size() - lastSize));
            
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
                        peekStateAndDepth.accept(s, iteration);
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

    private static List<int[][]> parseOperations(String groupNotation) {
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

    private static int[][] reverseOperation(int[][] operation) {
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

    private static int[] applyOperation(int[] state, int[][] operation) {
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
    
    private static String describeState(int nF, int[] state) {
        String s = stateToNotation(state);
        int[] nCycles = new int[nF + 1];
        String[] cycles = s.split("\\(");
        
        for (String cycle : cycles) {
            int length = cycle.split(",").length;
            if (length > 1) { // Ignore 1-cycles (fixed points)
                nCycles[length]++;
            }
        }
        
        StringBuilder description = new StringBuilder();
        boolean first = true;
        for (int i = 2; i <= nF; i++) {
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
    private static String getMultiplicityDescription(int count) {
        switch (count) {
            case 1: return "single";
            case 2: return "dual";
            case 3: return "triple";
            case 4: return "quadruple";
            case 5: return "quintuple";
            default: return count + "p";
        }
    }
}