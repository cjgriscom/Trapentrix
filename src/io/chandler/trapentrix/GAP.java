package io.chandler.trapentrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
        String cycleNotation = "[(1,2,3,4)(5,6,7,8),(1,5)(2,9)(4,10))]";
        int nElements = 0;
        for (String s : cycleNotation.split("\\(|\\)|,|\\[|\\]")) {
            if (!s.trim().isEmpty()) nElements = Math.max(nElements, Integer.parseInt(s.trim()));
        }
        System.out.println("nElements: " + nElements);
        long nPermutations = 1;
        for (int i = 1; i <= nElements; i++) {
            nPermutations *= i;
        }
        System.out.println("Total unique permutations: " + nPermutations);

        elements = initializeElements(nElements);
        parsedOperations = parseOperations(cycleNotation);
        int iterations = exploreStates(elements);
        System.out.println("Total group permutations: " + stateMap.size());

        System.out.println("Subset: " + ((double)nPermutations / stateMap.size()));
        System.out.println("Iterations: " + iterations);
    }

    private static int[] initializeElements(int maxElement) {
        int[] result = new int[maxElement];
        for (int i = 0; i < maxElement; i++) {
            result[i] = i + 1;
        }
        return result;
    }

    private static int exploreStates(int[] initialState) {
        stateMap.put(new State(initialState), false);

        int iteration = 0;
        while (true) {
            System.out.println("Depth: " + iteration + " - " + stateMap.size());
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
                        stateMapCopy.put(new State(newState), false);
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
}