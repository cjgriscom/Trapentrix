package io.chandler.gap;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

import java.util.AbstractSet;
import java.util.Iterator;

public class M12StateCache extends AbstractSet<State> {
    private final IntOpenHashSet map;

    public M12StateCache() {
        this.map = new IntOpenHashSet();
    }

    /**
     * @param state
     * @return
     */
    int cvt(int[] state) {
        int value = 0;
        int x = 13;
        for (int i = 0; i < 5; i++) {
            value = value * x + (state[i]);
            //x--;
        }
        return value;
    }

    @Override
    public boolean add(State state) {
        int[] s = state.state();

        return map.add(cvt(s));
    }

    @Override
    public boolean contains(Object o) {
        return map.contains(cvt(((State)o).state()));
    }

    @Override
    public int size() {
        return map.size();
    }

	@Override
    public Iterator<State> iterator() {
        // Since the full state is not stored we can't retrieve the original states
        throw new UnsupportedOperationException("Not implemented");
    }

}
