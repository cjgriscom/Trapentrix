package io.chandler.gap;

import io.chandler.gap.GroupExplorer.MemorySettings;

public interface AbstractGroupProperties {
	public MemorySettings mem();
	public int order();
	public int elements();
}
