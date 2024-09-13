package io.chandler.gap.cache;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Scanner;

import io.chandler.gap.GroupExplorer.MemorySettings;

public class InteractiveCachePair implements AutoCloseable {
	public final LMDBManager manager;
	public final LMDBCache cache;
	public final LMDBCache cache_incomplete;
	public final LMDBCache cache_tmp;

	final Path cachePath = Paths.get("cache.lmdb");

	final int completeCacheGB, nElements, operationsTillFlush;

	public InteractiveCachePair(Scanner scanner, int completeCacheGB, int nElements, int operationsTillFlush) throws IOException {
		this.completeCacheGB = completeCacheGB;
		this.nElements = nElements;
		this.operationsTillFlush = operationsTillFlush;

		// Check if the cache directories exist and prompt for deletion
		if (Files.exists(cachePath)) {
			System.out.println("Cache directory exists. Do you want to delete it? (Y/n)");
			String response = scanner.nextLine().trim().toLowerCase();
			if (!"n".equals(response.trim().toLowerCase())) {
				Files.walk(cachePath)
					.sorted(Comparator.reverseOrder())
					.map(Path::toFile)
					.forEach(File::delete);
				System.out.println("Cache directories deleted.");
			}
		}

		Files.createDirectories(cachePath);

		manager = new LMDBManager(cachePath, completeCacheGB);

        cache = new LMDBCache(manager, "main", nElements, MemorySettings.COMPACT, operationsTillFlush);
        
        cache_incomplete = new LMDBCache(manager, "tmp1", nElements, MemorySettings.COMPACT, operationsTillFlush);
        cache_tmp = new LMDBCache(manager, "tmp2", nElements, MemorySettings.COMPACT, operationsTillFlush);
	
	}

	@Override
	public void close() throws Exception {
		cache.close();
		cache_incomplete.close();
	}
}
