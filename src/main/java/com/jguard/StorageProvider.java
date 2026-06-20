package com.jguard;

public interface StorageProvider {
    int incrementAndGetIpCount(String ip, long windowMs);
    void blacklist(String ip, long durationMs);
    boolean isBlacklisted(String ip);
    int incrementAndGetGlobalCount(long windowMs);
    void resetGlobal();
    void shutdown();
}
