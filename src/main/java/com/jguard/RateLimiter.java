package com.jguard;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimiter {
    private static final Logger logger = LoggerFactory.getLogger(RateLimiter.class);
    private final GuardConfig config;
    private final LoadingCache<String, AtomicInteger> ipRegistry;
    private final Cache<String, Boolean> blacklist;
    private final AtomicInteger globalCounter = new AtomicInteger(0);
    private final ScheduledExecutorService scheduler;
    private boolean systemLockdown = false;

    public RateLimiter(GuardConfig config) {
        this.config = config;

        this.ipRegistry = CacheBuilder.newBuilder()
                .expireAfterWrite(config.windowSec, TimeUnit.SECONDS)
                .maximumSize(10000)
                .build(new CacheLoader<String, AtomicInteger>() {
                    @Override
                    public AtomicInteger load(String key) {
                        return new AtomicInteger(0);
                    }
                });

        this.blacklist = CacheBuilder.newBuilder()
                .expireAfterWrite(config.blacklistDurationMin, TimeUnit.MINUTES)
                .build();

        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.scheduler.scheduleAtFixedRate(() -> {
            globalCounter.set(0);
            systemLockdown = false;
        }, config.windowSec, config.windowSec, TimeUnit.SECONDS);
    }

    public boolean check(String ip, boolean isWhitelisted) {
        if (isWhitelisted) return true;
        if (systemLockdown || blacklist.getIfPresent(ip) != null) return false;

        if (globalCounter.incrementAndGet() > config.globalThreshold) {
            systemLockdown = true;
            logger.error("GLOBAL SECURITY LOCKDOWN TRIGGERED");
            return false;
        }

        try {
            int currentCount = ipRegistry.get(ip).incrementAndGet();
            if (currentCount > config.maxIPRequests) {
                blacklist.put(ip, true);
                logger.warn("IP Banned: {} | Rate: {}/{}s", ip, currentCount, config.windowSec);
                return false;
            }
            return true;
        } catch (ExecutionException e) {
            return true;
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}