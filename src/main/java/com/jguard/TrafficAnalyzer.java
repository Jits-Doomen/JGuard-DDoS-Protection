package com.jguard;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TrafficAnalyzer {

    private static final int MAX_REQUESTS_PER_IP = 50;
    private static final int GLOBAL_THRESHOLD = 2000;
    private static final long MAX_UNIQUE_IPS = 10000;
    private static final Set<String> whitelist = new HashSet<>();
    private static final AtomicInteger globalCounter = new AtomicInteger(0);

    private static final LoadingCache<String, AtomicInteger> ipRegistry = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .maximumSize(MAX_UNIQUE_IPS)
            .build(new CacheLoader<>() {
                public AtomicInteger load(String key) { return new AtomicInteger(0); }
            });

    private static final Cache<String, Boolean> blacklist = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(MAX_UNIQUE_IPS)
            .build();

    private static final Cache<String, Boolean> globalLock = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    static {
        whitelist.add("127.0.0.1");

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10000);
                    globalCounter.set(0);
                } catch (InterruptedException e) { break; }
            }
        }).start();
    }

    public static boolean shouldAllowRequest(String ipAddress) {
        if (whitelist.contains(ipAddress)) return true;

        if (globalLock.getIfPresent("SYSTEM_LOCK") != null) {
            return false;
        }

        if (blacklist.getIfPresent(ipAddress) != null) return false;

        if (globalCounter.incrementAndGet() > GLOBAL_THRESHOLD) {
            globalLock.put("SYSTEM_LOCK", true);
            System.err.println("!!! GLOBAL SECURITY LOCKDOWN TRIGGERED !!!");
            return false;
        }

        try {
            int currentCount = ipRegistry.get(ipAddress).incrementAndGet();
            if (currentCount > MAX_REQUESTS_PER_IP) {
                blacklist.put(ipAddress, true);
                logPotentialAttack(ipAddress, currentCount);
                return false;
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    private static void logPotentialAttack(String ip, int count) {
        System.err.println("--- SECURITY ALERT ---");
        System.err.println("Source IP: " + ip + " (BANNED FOR 5 MIN)");
        System.err.println("Request Rate: " + count + " per 10s");
    }

    public static void addToWhitelist(String ip) { whitelist.add(ip); }
    public static void manuallyUnblock(String ip) { blacklist.invalidate(ip); ipRegistry.invalidate(ip); }
    public static Set<String> getBannedIPs() { return blacklist.asMap().keySet(); }
}