package com.jguard;

public class GuardConfig {
    public int maxIPRequests = 50;
    public int globalThreshold = 100000;
    public long blacklistDurationMin = 5;
    public long windowSec = 10;

    public GuardConfig() {}

    public GuardConfig(int maxIPRequests, int globalThreshold) {
        this.maxIPRequests = maxIPRequests;
        this.globalThreshold = globalThreshold;
    }

    public static GuardConfig defaultSettings() {
        return new GuardConfig();
    }
}