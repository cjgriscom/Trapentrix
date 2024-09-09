package io.chandler.gap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.chandler.gap.GroupExplorer.Generator;

public class GeneratorMethodConsistencyTest {
	public static void main(String[] args) {
		List<int[][]> generatorCandidatesAll = new ArrayList<>();

		GroupExplorer.MemorySettings mem = GroupExplorer.MemorySettings.DEFAULT;
		GroupExplorer m12 = new GroupExplorer(Generators.m12, mem);
		m12.exploreStates(false, (states, depth) -> {
			for (int[] state : states) {
				if (GroupExplorer.describeState(m12.nElements, state)
						.equals("triple 3-cycles")) {
					generatorCandidatesAll.add(GroupExplorer.stateToCycles(state));
				}
			}

		});

		System.out.println(generatorCandidatesAll.size());

        List<int[][]> generatorCandidates1 = generatorCandidatesAll.subList(0, 20);
        List<int[][]> generatorCandidates2 = generatorCandidatesAll.subList(100, 120);

        
		Map<Generator, Integer> generatorPairs_Cached =
				GeneratorPairSearch.findGeneratorPairs(
					m12, generatorCandidates1, generatorCandidates2);

		// Invert map
		Map<Integer, Generator> generatorPairs_Cached_Inv = new HashMap<>();
		for (Map.Entry<Generator, Integer> entry : generatorPairs_Cached.entrySet()) {
			generatorPairs_Cached_Inv.put(entry.getValue(), entry.getKey());
		}
		System.out.println(generatorPairs_Cached_Inv.size());

		
        ArrayList<IsomorphicGenerator> generatorPairs_NoCache =
				GeneratorPairSearch.findGeneratorPairs_NoCache(
					m12,
					100, // TODO tune, Max order before breaking to check isomorphism 
					95040/2, // Max order before we consider this M12
					generatorCandidates1, generatorCandidates2, false);

		System.out.println("Cache method produced " + generatorPairs_Cached_Inv.size());
		for (Generator g : generatorPairs_Cached_Inv.values()) {
			System.out.println(GroupExplorer.generatorsToString(g.generator()));
		}

		System.out.println("No cache method produced " + generatorPairs_NoCache.size());
        for (IsomorphicGenerator g : generatorPairs_NoCache) {
            System.out.println(GroupExplorer.generatorsToString(GroupExplorer.renumberGenerators(g.generator())));
        }
	}
}
