package io.chandler.gap;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.AbstractSet;
import java.util.Iterator;

public class M23StateCache extends AbstractSet<State> {
    private final LongOpenHashSet map;

    public M23StateCache() {
        this.map = new LongOpenHashSet();
    }

    /**
     * @param state
     * @return
     */
    long cvt(int[] state) {
        long value = 0;
        int x = 25;
        for (int i = 0; i < 6; i++) {
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
