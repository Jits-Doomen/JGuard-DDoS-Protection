package com.shieldguard;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
public class TrafficAnalyzer {

    private static final int MAX_REQUESTS = 50;

    private static final LoadingCache<String, AtomicInteger> ipRegistry = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .build(new CacheLoader<>() {
                public AtomicInteger load(String key) {
                    return new AtomicInteger(0);
                }
            });

    /**
     * @param ipAddress Incoming connection's IP
     * @return boolean True if traffic is safe, False if it should be dropped.
     */
    public static boolean shouldAllowRequest(String ipAddress) {
        try {
            int currentCount = ipRegistry.get(ipAddress).incrementAndGet();

            if (currentCount > MAX_REQUESTS) {
                System.err.println("[SHIELD ALERT] Dropping flood traffic from: " + ipAddress);
                return false;
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }
}