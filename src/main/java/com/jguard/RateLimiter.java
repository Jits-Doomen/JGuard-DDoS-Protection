package com.jguard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public class RateLimiter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiter.class);

    private final GuardConfig config;
    private final StorageProvider storage;
    private final AtomicLong lockdownUntil = new AtomicLong(0);

    public RateLimiter(GuardConfig config) {
        this(config, new InMemoryStorageProvider(config.windowMs));
    }

    public RateLimiter(GuardConfig config, StorageProvider storage) {
        this.config = config;
        this.storage = storage;
    }

    public boolean check(String ip, boolean isWhitelisted) {
        if (isWhitelisted) return true;

        if (System.currentTimeMillis() < lockdownUntil.get()) return false;
        if (storage.isBlacklisted(ip)) return false;

        int globalCount = storage.incrementAndGetGlobalCount(config.windowMs);
        if (globalCount > config.globalThreshold) {
            lockdownUntil.set(System.currentTimeMillis() + config.windowMs);
            logger.error("Global security lockdown triggered at {} requests", globalCount);
            return false;
        }

        int ipCount = storage.incrementAndGetIpCount(ip, config.windowMs);
        if (ipCount > config.maxIPRequests) {
            storage.blacklist(ip, config.blacklistDurationMs);
            logger.warn("IP banned: {} ({} requests in {}ms)", ip, ipCount, config.windowMs);
            return false;
        }

        return true;
    }

    public void shutdown() {
        storage.shutdown();
    }
}
