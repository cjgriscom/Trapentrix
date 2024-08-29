package io.chandler.trapentrix;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class GAP {
	// M6 / Eliac: [ (1,2,3,4)(5,6,7,8), (1,5)(2,9)(4,10) ]
    private static Map<State, Boolean> stateMap = new HashMap<>();
    private static int[] elements;

    private static final int MAX_STACK_SIZE = 100000;

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
        String cycleNotation = "[(1,2,3,4)(5,6,7,8),(1,5)(2,9)(4,10),(7,3)(11,4)(2,12))]";
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
        int iterations = exploreStates(cycleNotation, elements);
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

    private static int exploreStates(String groupNotation, int[] initialState) {
        stateMap.put(new State(initialState), false);

        List<String> operations = parseOperations(groupNotation);

        int iteration = 0;
        while (true) {
            iteration++;
            System.out.println("Iteration: " + iteration + " - " + stateMap.size());
            HashMap<State, Boolean> stateMapCopy = new HashMap<>(stateMap);

            for (Entry<State, Boolean> entry : stateMap.entrySet()) {
                if (entry.getValue()) {
                    continue;
                }
                State state = entry.getKey();
                int[] currentState = state.state;

                boolean added = false;
                for (String operation : operations) {
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

    private static List<String> parseOperations(String groupNotation) {
        List<String> operations = new ArrayList<>();
        String[] parts = groupNotation.substring(1, groupNotation.length() - 1).split("\\),\\(");
        for (String part : parts) {
            operations.add("(" + part + ")");
        }
        return operations;
    }

    private static int[] applyOperation(int[] state, String operation) {
        int[] newState = Arrays.copyOf(state, state.length);
        String[] cycles = operation.split("\\)\\(");
        for (String cycle : cycles) {
            cycle = cycle.replaceAll("[()]", "");
            String[] elements = cycle.split(",");
            if (elements.length > 1) {
                int first = Integer.parseInt(elements[0]);
                for (int i = 0; i < elements.length - 1; i++) {
                    int current = Integer.parseInt(elements[i]);
                    int next = Integer.parseInt(elements[i + 1]);
                    newState[current - 1] = state[next - 1];
                }
                newState[Integer.parseInt(elements[elements.length - 1]) - 1] = state[first - 1];
            }
        }
        return newState;
    }
}