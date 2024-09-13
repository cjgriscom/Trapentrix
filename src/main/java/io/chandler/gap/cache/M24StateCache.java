package io.chandler.gap.cache;

/**
 * This is perhaps non-optimal but it allows the M24 cache to fit in my 32gb of RAM
 * 
 * Once you've chosen 7 positions there is only one way to permute the rest
 * 
 * TODO I think maybe you can squeeze 7 states into an int32
 * 
 * @param state
 * @return
 */
public class M24StateCache extends LongStateCache {
    public M24StateCache() {
        super(7, 25);
    }

}
