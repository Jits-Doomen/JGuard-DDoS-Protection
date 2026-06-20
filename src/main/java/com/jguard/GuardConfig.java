package com.jguard;

public final class GuardConfig {

    public final int maxIPRequests;
    public final int globalThreshold;
    public final long blacklistDurationMs;
    public final long windowMs;

    private GuardConfig(Builder b) {
        this.maxIPRequests = b.maxIPRequests;
        this.globalThreshold = b.globalThreshold;
        this.blacklistDurationMs = b.blacklistDurationMs;
        this.windowMs = b.windowMs;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static GuardConfig defaultSettings() {
        return builder().build();
    }

    public static final class Builder {
        private int maxIPRequests = 50;
        private int globalThreshold = 100_000;
        private long blacklistDurationMs = 5 * 60_000L;
        private long windowMs = 10_000L;

        public Builder maxIPRequests(int v) {
            this.maxIPRequests = v;
            return this;
        }

        public Builder globalThreshold(int v) {
            this.globalThreshold = v;
            return this;
        }

        public Builder blacklistDuration(long amount, java.util.concurrent.TimeUnit unit) {
            this.blacklistDurationMs = unit.toMillis(amount);
            return this;
        }

        public Builder window(long amount, java.util.concurrent.TimeUnit unit) {
            this.windowMs = unit.toMillis(amount);
            return this;
        }

        public GuardConfig build() {
            if (maxIPRequests <= 0) throw new IllegalArgumentException("maxIPRequests must be > 0");
            if (globalThreshold <= 0) throw new IllegalArgumentException("globalThreshold must be > 0");
            if (blacklistDurationMs <= 0) throw new IllegalArgumentException("blacklistDurationMs must be > 0");
            if (windowMs <= 0) throw new IllegalArgumentException("windowMs must be > 0");
            return new GuardConfig(this);
        }
    }
}
