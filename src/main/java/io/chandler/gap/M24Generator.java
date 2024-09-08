package io.chandler.gap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.function.BiConsumer;

import io.chandler.gap.GroupExplorer.MemorySettings;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;

public class M24Generator {
	public static void main(String[] args) throws Exception {
		File m24States = new File("m24.gap.lz4");
		//GenerateM24(m24States);
		exploreM24(m24States, null);
	}

	public static void exploreM24(File file,
			BiConsumer<int[], String> peekCyclesAndDescriptions) throws Exception {

		HashMap<String, Integer> cycleDescriptions = new HashMap<>();
		int elements, order;
        
		int progress0 = 0;
		
		try (DataInputStream dis = new DataInputStream(new LZ4BlockInputStream(new FileInputStream(file)))) {
			elements = dis.readInt();
			order = dis.readInt();
			System.out.println("Elements: " + elements);
			System.out.println("Order: " + order);
			for (int i = 0; i < order - 1; i++) {
				int[] state = new int[elements];
				for (int j = 0; j < elements; j++) {
					state[j] = dis.readInt();
				}
				
                String cycleDescription = GroupExplorer.describeState(elements, state);
                if (peekCyclesAndDescriptions != null) peekCyclesAndDescriptions.accept(state, cycleDescription);
                cycleDescriptions.merge(cycleDescription, 1, Integer::sum);

				int progress = (int)((long)i*100 / order);
				if (progress != progress0 && progress % 5 == 0) {
					System.out.println("Loading, " + progress + "%");
					progress0 = progress;
				}
			}
		}


        
        System.out.println("Elements: " + elements);
        System.out.println("Total group permutations: " + order);

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
	/**
	 * Use a reduced-size cache to find the unique states, while
	 *   storing each new full state to disk
	 * 
	 * Note the start state (1,2,3,4,...,24) is not included
 	 * 
	 * @param file
	 * @throws Exception
	 */
    public static void GenerateM24(File file) throws Exception {
        Set<State> set = new M24StateCache();
        int elements = 24;
        int order = 244823040;
        GroupExplorer group = new GroupExplorer(
            Generators.m24, MemorySettings.DEFAULT, set);

        int[] totalStates = new int[]{0};

        try (DataOutputStream dos = new DataOutputStream(new LZ4BlockOutputStream(
                new FileOutputStream(file), 32*1024*1024))) {
            
            dos.writeInt(elements);
            dos.writeInt(order);
                
            group.exploreStates(true, (states, depth) -> {
                totalStates[0] += states.size();
                try {
                    for (int[] state : states) {
                        for (int i : state) dos.writeInt(i);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
