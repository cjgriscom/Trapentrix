package io.chandler.gap;

import org.lmdbjava.*;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class LMDBCache extends AbstractSet<State> {
    private final Env<ByteBuffer> env;
    private final Dbi<ByteBuffer> db;

    public LMDBCache(String filePath, int gigaBytes) {
        File dbFile = new File(filePath);
        env = Env.create()
                .setMapSize(gigaBytes * 1024L * 1024L * 1024L)
                .setMaxDbs(1)
                .open(dbFile);
        db = env.openDbi("lmdb-cache", DbiFlags.MDB_CREATE);
    }

    @Override
    public boolean add(State state) {
        byte[] key = cvt(state.state());
        ByteBuffer keyBuffer = ByteBuffer.allocateDirect(key.length);
        keyBuffer.put(key).flip();

        ByteBuffer valueBuffer = ByteBuffer.allocateDirect(0); // Empty value

        try (Txn<ByteBuffer> txn = env.txnWrite()) {
            boolean added = db.put(txn, keyBuffer, valueBuffer);
            txn.commit();
            return added;
        }
    }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof State)) {
            return false;
        }
        State state = (State) o;
        byte[] key = cvt(state.state());
        ByteBuffer keyBuffer = ByteBuffer.allocateDirect(key.length);
        keyBuffer.put(key).flip();
    
        try (Txn<ByteBuffer> txn = env.txnRead()) {
            return db.get(txn, keyBuffer) != null;
        }
    }

    @Override
    public int size() {
        try (Txn<ByteBuffer> txn = env.txnRead()) {
            return (int) db.stat(txn).entries;
        }
    }

    @Override
    public Iterator<State> iterator() {
        return new LMDBIterator();
    }

    private class LMDBIterator implements Iterator<State> {
        private final Txn<ByteBuffer> txn;
        private final Cursor<ByteBuffer> cursor;
        private boolean hasNext;

        public LMDBIterator() {
            this.txn = env.txnRead();
            this.cursor = db.openCursor(txn);
            this.hasNext = cursor.first();
        }

        @Override
        public boolean hasNext() {
            if (!hasNext) {
                // Close the cursor and transaction when we reach the end
                cursor.close();
                txn.close();
            }
            return hasNext;
        }

        @Override
        public State next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            ByteBuffer keyBuffer = cursor.key();
            byte[] keyBytes = new byte[keyBuffer.remaining()];
            keyBuffer.get(keyBytes);
            int[] arr = cvtBack(keyBytes);
            
            // Move cursor to next entry
            hasNext = cursor.next();
            
            return State.of(arr, 0, GroupExplorer.MemorySettings.FASTEST);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove operation is not supported");
        }
    }

    private int[] cvtBack(byte[] bytes) {
        int[] state = new int[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            state[i] = bytes[i] & 0xFF;
        }
        return state;
    }

    private byte[] cvt(int[] state) {
        byte[] conversion = new byte[state.length];
        for (int i = 0; i < state.length; i++) {
            conversion[i] = (byte) state[i];
        }
        return conversion;
    }

    public void close() {
        db.close();
        env.close();
    }
}