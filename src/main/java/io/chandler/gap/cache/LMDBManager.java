package io.chandler.gap.cache;

import org.lmdbjava.Env;
import org.lmdbjava.EnvFlags;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Path;

public class LMDBManager {
    private final Env<ByteBuffer> env;

    public LMDBManager(Path directory, int gigaBytes) {
        File dbFile = directory.toFile();
        env = Env.create()
                .setMapSize(gigaBytes * 1024L * 1024L * 1024L)
                .setMaxDbs(3) // Adjust based on expected number of caches
                .setMaxReaders(100)
                .open(dbFile, EnvFlags.MDB_WRITEMAP, EnvFlags.MDB_NOTLS);
    }

    public Env<ByteBuffer> getEnv() {
        return env;
    }

    public void close() {
        env.close();
    }
}