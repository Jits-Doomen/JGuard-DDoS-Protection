package com.jguard;

import java.util.concurrent.atomic.AtomicInteger;

public interface StorageProvider {
    AtomicInteger getRequestCount(String ip);
    void blacklist(String ip);
    boolean isBlacklisted(String ip);
    void resetGlobal();
}