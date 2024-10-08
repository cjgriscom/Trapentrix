package io.chandler.gap.cache;

import org.lmdbjava.*;

import io.chandler.gap.GroupExplorer;
import io.chandler.gap.GroupExplorer.MemorySettings;
import io.chandler.gap.cache.State.StateFactorial;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.AbstractSet;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LMDBCache extends AbstractSet<State> implements Closeable {
    private static final int KEY_SIZE = 11; // TODO configurable
    
    private final ReadWriteLock batchLock = new ReentrantReadWriteLock();

    private final MemorySettings retrievalSettings;
    private final int nElements;

    private final ThreadLocal<TransactionWrapper> txnThreadLocal = new ThreadLocal<>();

    private final Env<ByteBuffer> env;
    private Dbi<ByteBuffer> db;
    private final int operationsTillFlush;
    private final Map<State, Boolean> batchValues;
    private final AtomicLong size = new AtomicLong(0);
    private final String dbname;
    private boolean debug = false;

    public LMDBCache(LMDBManager manager, String dbName, int nElements, MemorySettings retrievalSettings, int operationsTillFlush) {
        this.env = manager.getEnv();
        this.db = env.openDbi(dbName, DbiFlags.MDB_CREATE);
        this.retrievalSettings = retrievalSettings;
        this.nElements = nElements;
        this.keyBuffer_tl = ThreadLocal.withInitial(() -> ByteBuffer.allocateDirect(KEY_SIZE));
        this.dbname = dbName;
        this.size.set(initialSize());
        this.operationsTillFlush = operationsTillFlush;
        this.batchValues = new HashMap<>(operationsTillFlush);
    }

    public void debug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public boolean add(State state) {
        boolean added;
        int batchSize;
    
        TransactionWrapper wrapper = getOrCreateTransaction(true);
        try {
            Boolean batchResult = batchValues.get(state);
    
            if (batchResult == null) { // Not in batch cache
                boolean dbContains = db_contains_nolock(state, wrapper.txn);
                if (!dbContains) { // Not in DB, add it
                    batchValues.put(state, true);
                    added = true;
                } else { // Already in DB, do nothing
                    added = false;
                }
            } else if (!batchResult) { // Previously removed in cache, now re-adding
                batchValues.put(state, true);
                added = true;
            } else { // Already added in cache
                added = false;
            }
    
            if (added) {
                size.incrementAndGet();
            }
            batchSize = batchValues.size();
        } finally {
            releaseTransaction(true);
        }
    
        if (batchSize >= operationsTillFlush) {
            flushBatch();
        }
    
        return added;
    }


    @Override
    public boolean remove(Object o) {
        if (!(o instanceof State)) {
            return false;
        }
        State state = (State) o;
        return remove(state);
    }

    public boolean remove(State state) {
        boolean removed;
        int batchSize;

        TransactionWrapper wrapper = getOrCreateTransaction(true);
        try {
            Boolean batchResult = batchValues.get(state);

            if (batchResult == null) { // Does not exist in cache
                boolean dbContains = db_contains_nolock(state, wrapper.txn);
                if (dbContains) { // It's in the db; remove it
                    batchValues.put(state, false);
                    removed = true;
                } else { // It's not in the db; do nothing
                    removed = false;
                }
            } else if (batchResult == true) { // Was added in cache
                batchValues.put(state, false);
                removed = true;
            } else { // Was already removed in cache
                removed = false;
            }

            batchSize = batchValues.size();

            if (removed) {
                size.decrementAndGet();
            }
        } finally {
            releaseTransaction(true);
        }

        if (batchSize >= operationsTillFlush) {
            flushBatch();
        }

        return removed;
    }

    @Override
    public void clear() {
        TransactionWrapper wrapper = getOrCreateTransaction(true, true);
        try {
            batchValues.clear();
            db.drop(wrapper.txn, false); // Drop the database but do not delete it
            wrapper.txn.commit(); // Commit the transaction
            db = env.openDbi(dbname, DbiFlags.MDB_CREATE); // Recreate the database
            size.set(0);
        } finally {
            releaseTransaction(true);
        }
    }

    private final ThreadLocal<ByteBuffer> keyBuffer_tl;    

    private void flushBatch() {
        ByteBuffer keyBuffer = ByteBuffer.allocateDirect(KEY_SIZE);
        ByteBuffer valueBuffer = ByteBuffer.allocateDirect(0);
        try (Txn<ByteBuffer> txn = env.txnWrite()) {
            batchLock.writeLock().lock();
            for (Entry<State, Boolean> entry : batchValues.entrySet()) {
                State state = entry.getKey();
                boolean add = entry.getValue();
                byte[] key = cvt(state);
                keyBuffer.clear();
                keyBuffer.put(key).flip();
                if (add) db.put(txn, keyBuffer, valueBuffer);
                else db.delete(txn, keyBuffer);
            }
            batchValues.clear();
            batchLock.writeLock().unlock();
            txn.commit();
        }
        if (debug) System.out.println("[DEBUG] Flushed batch - new size: " + size());
    }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof State)) {
            return false;
        }
        TransactionWrapper wrapper = getOrCreateTransaction();
        try {
            State state = (State) o;
            Boolean batchResult = batchValues.get(state);
            if (batchResult != null) {
                return batchResult;
            }
            return db_contains_nolock(state, wrapper.txn);
        } finally {
            releaseTransaction();
        }
    }

    private boolean db_contains_nolock(State state, Txn<ByteBuffer> txn) {
        byte[] keyBytes = cvt(state);
        ByteBuffer keyBuffer = keyBuffer_tl.get();
        keyBuffer.clear();
        keyBuffer.put(keyBytes).flip();

        return db.get(txn, keyBuffer) != null;
    }

    private int initialSize() {
        try (Txn<ByteBuffer> txn = env.txnRead()) {
            return (int) db.stat(txn).entries;
        }
    }

    @Override
    public Iterator<State> iterator() {
        // While the iterator is open, no writes allowed
        // Flush batch
        flushBatch();
        return new LMDBIterator();
    }
    private class LMDBIterator implements Iterator<State> {
        private final TransactionWrapper txnWrapper;
        private final Cursor<ByteBuffer> cursor;
        private boolean hasNext;
        private boolean closed = false;
    
        public LMDBIterator() {
            this.txnWrapper = getOrCreateTransaction();
            this.cursor = db.openCursor(txnWrapper.txn);
            this.hasNext = cursor.first();
        }
    
        @Override
        public boolean hasNext() {
            if (closed) {
                return false;
            }
            if (!hasNext) {
                close();
            }
            return hasNext;
        }
    
        @Override
        public State next() {
            if (closed || !hasNext()) {
                throw new NoSuchElementException();
            }
            ByteBuffer keyBuffer = cursor.key();
            byte[] keyBytes = new byte[keyBuffer.remaining()];
            keyBuffer.get(keyBytes);
            State state = cvtBack(keyBytes);
            
            // Move cursor to next entry
            hasNext = cursor.next();
            
            return state;
        }
    
        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove operation is not supported");
        }
    
        private void close() {
            if (batchValues.size() > 0) {
                throw new ConcurrentModificationException("Writes detected while iterating");
            }
            if (!closed) {
                cursor.close();
                releaseTransaction();
                closed = true;
            }
        }
    
        @Override
        protected void finalize() throws Throwable {
            close();
            super.finalize();
        }
    }

    private State cvtBack(byte[] bytes) {
        return State.of(StateFactorial.stateFromBytes(bytes, this.nElements), nElements, retrievalSettings);
    }

    private byte[] cvt(State state) {
        if (!(state instanceof State.StateFactorial)) {
            int[] arr = state.state();
            state = State.of(arr, arr.length, GroupExplorer.MemorySettings.COMPACT);
            
        }

        return ((StateFactorial) state).bytes();
    }

    @Override
    public int size() {
        return (int) size.get();
    }

    @Override
    public void close() {
        flushBatch(); // Ensure any remaining batch items are written
        db.close();
        env.close();
    }


    private static class TransactionWrapper {
        final Txn<ByteBuffer> txn;
        final AtomicInteger refCount;
        final Exception creatorStackTrace;
        final String threadName;

        TransactionWrapper(Txn<ByteBuffer> txn) {
            this.txn = txn;
            this.refCount = new AtomicInteger(1);
            this.creatorStackTrace = new Exception();
            this.threadName = Thread.currentThread().getName();
        }
    }

    private TransactionWrapper getOrCreateTransaction() {
        return getOrCreateTransaction(false);
    }
    
    private TransactionWrapper getOrCreateTransaction(boolean writeBatch) {
        return getOrCreateTransaction(writeBatch, false);
    }

    private TransactionWrapper getOrCreateTransaction(boolean writeBatch, boolean writeDb) {
        TransactionWrapper wrapper = txnThreadLocal.get();
        if (wrapper == null) {
            wrapper = new TransactionWrapper(writeDb ? env.txnWrite() : env.txnRead());
            txnThreadLocal.set(wrapper);
            if (writeBatch) batchLock.writeLock().lock();
            else batchLock.readLock().lock();
        } else {
            if (writeBatch) throw new ConcurrentModificationException(Thread.currentThread().getName() + " attempted to modify transaction created by " + wrapper.threadName + ": " + wrapper.refCount.get(), wrapper.creatorStackTrace);
            wrapper.refCount.incrementAndGet();
        }
        return wrapper;
    }

    private void releaseTransaction() {
        releaseTransaction(false);
    }
    private void releaseTransaction(boolean writeBatch) {
        TransactionWrapper wrapper = txnThreadLocal.get();
        if (wrapper != null) {
            if (wrapper.refCount.decrementAndGet() == 0) {
                wrapper.txn.close();
                txnThreadLocal.remove();
                if (writeBatch) batchLock.writeLock().unlock();
                else batchLock.readLock().unlock();
            }
        }
    }
}