package com.jguard;

import java.util.HashSet;
import java.util.Set;

public class JGuard {
    private final RateLimiter limiter;
    private final Set<String> whitelist = new HashSet<>();

    public JGuard(GuardConfig config) {
        this.limiter = new RateLimiter(config);
        whitelist.add("127.0.0.1");
    }

    public boolean allow(String ipAddress) {
        return limiter.check(ipAddress, whitelist.contains(ipAddress));
    }

    public void whitelist(String ip) { whitelist.add(ip); }
}