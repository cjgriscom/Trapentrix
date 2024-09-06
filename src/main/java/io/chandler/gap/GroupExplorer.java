package io.chandler.gap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

public class GroupExplorer {
    
    private Map<State, Boolean> stateMap = new HashMap<>();
    private int[] elements;
    private List<int[][]> parsedOperations;
    public int nElements;

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

    public GroupExplorer(String cycleNotation) {
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

    public static int[][] stateToCycles(int[] state) {
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

    public static String cyclesToNotation(int[][] cycles) {
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

    public int exploreStates(boolean debug, BiConsumer<State, Integer> peekStateAndDepth) {
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
            //operations.add(reverseOperation(operation));
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
    
    public String describeState(int[] state) {
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

    public static boolean cyclesContainsAllElements(int nElements, int[][]... cyclesList) {
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

    public static List<int[][][]> genIsomorphisms(int[][][] a) {
        List<int[][][]> checks = new ArrayList<>();
        for (int[][][] aPerm : Permu.applyGeneratorPermutationsAndRotations(a)) {
            checks.add(renumberGenerators(aPerm));
        }
        return checks;
    }

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
