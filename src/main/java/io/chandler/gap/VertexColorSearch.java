package io.chandler.gap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import io.chandler.gap.GroupExplorer.Generator;
import io.chandler.gap.GroupExplorer.MemorySettings;
import io.chandler.gap.cache.State;

public class VertexColorSearch {

	int[][][] fullSymmetryGroup;
	Function<Integer, int[]> getFacesFromVertex;
	Function<int[], Integer> getMatchingVertexFromFaces;
	HashSet<State> fullSymmetryStates;
	int symmetryOrder;
	int nVertices;
	Generator colorSymmGen;
	SubgroupKey colorSymmSubgroup;

	public VertexColorSearch(int[][][] fullSymmetryGroup, int nVertices, Function<Integer, int[]> getFacesFromVertex, Function<int[], Integer> getMatchingVertexFromFaces) {
		this.fullSymmetryGroup = fullSymmetryGroup;
		this.nVertices = nVertices;
		this.getFacesFromVertex = getFacesFromVertex;
		this.getMatchingVertexFromFaces = getMatchingVertexFromFaces;

		fullSymmetryStates = new HashSet<>();
        GroupExplorer ge = new GroupExplorer(GroupExplorer.generatorsToString(fullSymmetryGroup), MemorySettings.DEFAULT, fullSymmetryStates);
        ge.resetElements(true);
        ge.exploreStates(false, null);
		this.symmetryOrder = ge.order();

		this.colorSymmGen = new Generator(fullSymmetryGroup);
		this.colorSymmSubgroup = new SubgroupKey(symmetryOrder, findSymmetryCopiesOfVertex(colorSymmGen, 1, null).keySet());
	}

	public LinkedHashMap<Integer, int[]> findSymmetryCopiesOfVertex(Generator gen, int vertex, Map<Integer, LinkedHashMap<Integer, int[]>> vertexSymmetryCache) {
		
        if (vertexSymmetryCache != null && vertexSymmetryCache.containsKey(vertex)) {
            return vertexSymmetryCache.get(vertex);
        }
        
        HashSet<State> cache = new HashSet<>();
        GroupExplorer ge = new GroupExplorer(GroupExplorer.generatorsToString(gen.generator()), MemorySettings.DEFAULT, cache);

        int[] vertexFaces = getFacesFromVertex.apply(vertex);
        LinkedHashMap<Integer, int[]> vertexMapping = new LinkedHashMap<>();

        List<int[]> states = new ArrayList<>();
        states.add(ge.copyCurrentState());
        ge.exploreStates(false, (states2, depth) -> {
            states.addAll(states2);
        });
        for (int[] s : states) {
            int[] analogousFaces = new int[3];
            for (int fi = 0; fi < 3; fi++) {
                int face = vertexFaces[fi];
                analogousFaces[fi] = s[face - 1];
            }
            int cacheIndex = getMatchingVertexFromFaces.apply(analogousFaces);
            vertexMapping.put(cacheIndex, analogousFaces);
        }
        
        if (vertexSymmetryCache != null) {
            vertexSymmetryCache.put(vertex, vertexMapping);
        }
        return vertexMapping;
    }

    public class ColorMapping {
        SubgroupKey axesSubgroup;
        Generator axesGen;

        public ColorMapping(SubgroupKey axesSubgroup, Generator axesGen) {
            this.axesSubgroup = axesSubgroup;
            this.axesGen = axesGen;
        }

        @Override
        public int hashCode() {
            return axesSubgroup.hashCode() * 31;
        }
        @Override
        public boolean equals(Object obj) {
            return axesSubgroup.equals(((ColorMapping)obj).axesSubgroup);
        }

		public int[] getVertexToColorMap() {
            int[] vertexToColor = new int[nVertices];

			HashMap<Integer, HashSet<Integer>> congruentColors = new HashMap<>();

            int color = 1;
            for (int vertex : colorSymmSubgroup.vertex1Positions) {
                for (int axis : findSymmetryCopiesOfVertex(this.axesGen, vertex, null).keySet()) {
					if (vertexToColor[axis - 1] == 0) {
						// Assign color
						vertexToColor[axis - 1] = color;
						congruentColors.put(color, new HashSet<>(color));
					} else if (vertexToColor[axis - 1] != color) {
						congruentColors.get(vertexToColor[axis - 1]).add(color);
					}
                }
                color++;
            }

            for (int i = 0; i < vertexToColor.length; i++) {
                if (vertexToColor[i] == 0) {
                    vertexToColor[i] = -1; // Missing vertex
                    return null;
                }
            }

			// Check make sure there are multiple disconnected color sets
			if (congruentColors.size() == 1) {
				return null;
			}

            // Make sure all the color sets are the same size
            int size = congruentColors.get(1).size();
            for (int c : congruentColors.keySet()) {
                if (congruentColors.get(c).size() != size) {
                    return null;
                }
            }

			// Renumber
			int[] vertexToColor2 = new int[vertexToColor.length];
			HashMap<Integer, Integer> colorMap = new HashMap<>();
			int nextColor = 1;
			for (int i = 0; i < vertexToColor.length; i++) {
				int originalColor = vertexToColor[i];
				if (!colorMap.containsKey(originalColor)) {
					colorMap.put(originalColor, nextColor++);
				}
				vertexToColor2[i] = colorMap.get(originalColor);
			}
			return vertexToColor2;
			
		}
    }



    public ArrayList<ColorMapping> searchForGenerators() {

        int rejects = 0;

        ArrayList<ColorMapping> colorMappings = new ArrayList<>();

        HashMap<SubgroupKey, int[][][]> subgroups = findSubgroups(fullSymmetryGroup);
        for (Entry<SubgroupKey, int[][][]> e : subgroups.entrySet()) {
            int[][][] gArr1 = e.getValue();
            {
                // Re-order the symmetries so that the first has non-colliding cycles
                // If both have colliding cycles, exclude from results

                boolean gArr1HasCollidingCycles = hasCollidingCycles(gArr1);

                if (gArr1HasCollidingCycles) {
                    //System.out.print("Reject ");
                    rejects++;
                    continue;
                } else {
                    ColorMapping cm1 = new ColorMapping(e.getKey(), new Generator(e.getValue()));
                    colorMappings.add(cm1);
                }

            }
        }

        System.out.println("Rejects: " + rejects);
        System.out.println("Color mappings: " + colorMappings.size());

        reduceIsomorphicAndInvalidColorMappings(colorMappings);
        System.out.println("Unique color mappings: " + colorMappings.size());

        for (ColorMapping cm : colorMappings) {
			System.out.print("Colors: " + Arrays.stream(cm.getVertexToColorMap()).distinct().count());
            System.out.print(" Axes: " + cm.axesSubgroup.order);
            System.out.print(", cycle lengths");
			int[][][] gen = cm.axesGen.generator();
			for (int[][] cycles : gen) {
				System.out.print(" " + cycles[0].length);
			}
			System.out.print(",    \t");

			SubgroupKey k = cm.axesSubgroup;
			System.out.println(k.order + " " + Arrays.toString(k.vertex1Positions));
                    
        }

        return colorMappings;
    }

	private static int countDistinctColors(int[] elementToColor) {
		//(int)Arrays.stream(cacheItem).distinct().count();
		HashSet<Integer> colors = new HashSet<>();
		for (int c : elementToColor) {
			if (c != -1) {
				colors.add(c);
			}
		}
		return colors.size();
	}

    public void reduceIsomorphicAndInvalidColorMappings(ArrayList<ColorMapping> colorMappings) {
        HashSet<int[]> colorMappingCache = new HashSet<>();
        Iterator<ColorMapping> iter = colorMappings.iterator();
        while (iter.hasNext()) {
            ColorMapping cm = iter.next();

            int[] vertexToColor = cm.getVertexToColorMap();

            if (vertexToColor == null) {
                iter.remove();
                continue;
            }
			int nColors = countDistinctColors(vertexToColor);

            // generate the N full symmetries of the color mapping
            // Then for each, see if the colors map 1:1 to any element in the cache

            boolean found = false;
            for (State s : fullSymmetryStates) {
                int[] state = s.state();
                int[] colorMappingPermuted = new int[vertexToColor.length];
				
                for (int i = 0; i < state.length; i++) {
                    colorMappingPermuted[i] = vertexToColor[state[i] - 1];
                }
                for (int[] cacheItem : colorMappingCache) {
					int nColorsCache = countDistinctColors(cacheItem);
					if (nColorsCache != nColors) continue;

                    // Greedily try to match each element
                    int[] colorAssoc = new int[nColors+1];
                    boolean colorMapsEqual = true;
                    for (int i = 0; i < cacheItem.length; i++) {
                        int cacheColor = cacheItem[i];
                        int mappedColor = colorMappingPermuted[i];
                        
                        if (colorAssoc[cacheColor] == 0) {
                            // Assign the color and continue
                            colorAssoc[cacheColor] = mappedColor;
                        } else {
                            // Make sure they map to the same color
                            if (colorAssoc[cacheColor] != mappedColor) {
                                colorMapsEqual = false;
                                break;
                            }
                        }
                        
                    }
                    if (colorMapsEqual) {
                        found = true;
                        break;
                    }
                }
                if (found) break;
            }

            if (found) {
                iter.remove();
            } else {
                colorMappingCache.add(vertexToColor);
            }
        }
    }

    private boolean hasCollidingCycles(int[][][] gen) {
        HashSet<Integer> seen = new HashSet<>();
        for (int[] faces : findSymmetryCopiesOfVertex(new Generator(gen), 1, null).values()) {
            HashSet<Integer> current = new HashSet<>();
            for (int face : faces) {
                if (current.contains(face)) {
                    throw new IllegalStateException("Found duplicate face " + face + " in " + Arrays.deepToString(gen));
                }
                current.add(face);
            }
            for (int face : current) {
                if (seen.contains(face)) {
                    return true;
                }
                seen.add(face);
            }
        }
        return false;
    }

    public class SubgroupKey {
        int[] vertex1Positions;
        int order;
        public SubgroupKey(int order, int[][][] generator) {
            this(order, findSymmetryCopiesOfVertex(new Generator(generator), 1, null).keySet());
        }
        public SubgroupKey(int order, int[] vertex1Positions) { this.vertex1Positions = vertex1Positions; this.order = order; }
        public SubgroupKey(int order, Set<Integer> vertex1Colors) { this.order = order; this.vertex1Positions = vertex1Colors.stream().mapToInt(i -> i).toArray(); }
        @Override public int hashCode() { return order*31 + Arrays.hashCode(vertex1Positions); }
        @Override public boolean equals(Object obj) {  return order == ((SubgroupKey)obj).order && Arrays.equals(vertex1Positions, ((SubgroupKey)obj).vertex1Positions); }
    }
    
    private HashMap<SubgroupKey, int[][][]> findSubgroups(int[][][] gen) {
        HashMap<SubgroupKey, int[][][]> subgroups = new HashMap<>();
       
        HashSet<State> stateCache = new HashSet<>();
        GroupExplorer ge = new GroupExplorer(GroupExplorer.generatorsToString(gen), MemorySettings.DEFAULT);
        // Note this approach doesn't add the identity generator, which we don't want anyway
        ge.exploreStates(false, (states, x) -> {for (int[] s : states) stateCache.add(State.of(s, ge.nElements, MemorySettings.DEFAULT));});

        System.out.println("Found " + stateCache.size() + " states");
        for (State a : stateCache) {
            for (State b : stateCache) {
                int[][][] generator;
                if (a.equals(b)) {
                    generator = new int[][][] {
                        GroupExplorer.stateToCycles(a.state()),
                    };
                } else {
                    generator = new int[][][] {
                        GroupExplorer.stateToCycles(a.state()),
                        GroupExplorer.stateToCycles(b.state()),
                    };
                }

                GroupExplorer ge2 = null;
                String genString = GroupExplorer.generatorsToString(generator);
                if (genString.equals("[]")) continue; // Identity
                ge2 = new GroupExplorer(genString, MemorySettings.DEFAULT);
                ge2.exploreStates(false, null);
                if (ge2.order() == ge.order()) continue; // Not a subgroup
                subgroups.put(new SubgroupKey(ge2.order(), generator), generator);
            }
            
        }
        System.out.println("Order " + ge.order());
        return subgroups;
    }

}
