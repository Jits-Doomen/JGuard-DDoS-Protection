package com.jguard;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class JGuard {

    private final RateLimiter limiter;
    private final Set<String> whitelist = ConcurrentHashMap.newKeySet();

    public JGuard() {
        this(GuardConfig.defaultSettings());
    }

    public JGuard(GuardConfig config) {
        this(config, new InMemoryStorageProvider(config.windowMs));
    }

    public JGuard(GuardConfig config, StorageProvider storage) {
        this.limiter = new RateLimiter(config, storage);
        whitelist.add("127.0.0.1");
    }

    public boolean allow(String ipAddress) {
        return limiter.check(ipAddress, whitelist.contains(ipAddress));
    }

    public void whitelist(String ip) {
        whitelist.add(ip);
    }

    public void unwhitelist(String ip) {
        whitelist.remove(ip);
    }

    public boolean isWhitelisted(String ip) {
        return whitelist.contains(ip);
    }

    public void shutdown() {
        limiter.shutdown();
    }
}
