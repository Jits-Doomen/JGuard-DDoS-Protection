package com.jguard;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryStorageProvider implements StorageProvider {

    private static final class Window {
        final AtomicLong start = new AtomicLong(System.currentTimeMillis());
        final AtomicInteger count = new AtomicInteger(0);

        int incrementAndGet(long windowMs) {
            long now = System.currentTimeMillis();
            long s = start.get();
            if (now - s >= windowMs && start.compareAndSet(s, now)) {
                count.set(0);
            }
            return count.incrementAndGet();
        }

        boolean expired(long windowMs) {
            return System.currentTimeMillis() - start.get() >= windowMs * 2;
        }
    }

    private final Map<String, Window> ipWindows = new ConcurrentHashMap<>();
    private final Map<String, Long> blacklistExpiry = new ConcurrentHashMap<>();
    private final Window globalWindow = new Window();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "jguard-cleaner");
        t.setDaemon(true);
        return t;
    });

    public InMemoryStorageProvider(long windowMs) {
        cleaner.scheduleAtFixedRate(() -> cleanup(windowMs), windowMs, windowMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public int incrementAndGetIpCount(String ip, long windowMs) {
        return ipWindows.computeIfAbsent(ip, k -> new Window()).incrementAndGet(windowMs);
    }

    @Override
    public void blacklist(String ip, long durationMs) {
        blacklistExpiry.put(ip, System.currentTimeMillis() + durationMs);
    }

    @Override
    public boolean isBlacklisted(String ip) {
        Long expiry = blacklistExpiry.get(ip);
        if (expiry == null) return false;
        if (System.currentTimeMillis() >= expiry) {
            blacklistExpiry.remove(ip, expiry);
            return false;
        }
        return true;
    }

    @Override
    public int incrementAndGetGlobalCount(long windowMs) {
        return globalWindow.incrementAndGet(windowMs);
    }

    @Override
    public void resetGlobal() {
        globalWindow.count.set(0);
        globalWindow.start.set(System.currentTimeMillis());
    }

    private void cleanup(long windowMs) {
        long now = System.currentTimeMillis();
        ipWindows.entrySet().removeIf(e -> e.getValue().expired(windowMs));
        blacklistExpiry.entrySet().removeIf(e -> now >= e.getValue());
    }

    @Override
    public void shutdown() {
        cleaner.shutdown();
    }
}
