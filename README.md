# JGuard-DDoS-Protection

A lightweight, Java-based defensive utility designed to mitigate Layer 7 (Application Layer) DDoS attacks using rate-limiting.

## Purpose

This tool helps developers protect their applications by identifying and throttling IP addresses that send too many requests in a short period.

## Features

- **Rate Limiting:** Sliding-window per-IP and global request throttling to protect against volumetric floods.
- **Auto-Expiring State:** IP counters and blacklist entries clean themselves up automatically once their window/duration elapses, with a background sweep removing stale entries.
- **Thread-Safe:** Built entirely on atomics and `ConcurrentHashMap`; designed for high-concurrency environments.
- **Pluggable Storage:** Default in-memory storage out of the box, with a `StorageProvider` interface so you can back it with Redis or another store for multi-instance deployments.
- **Global Lockdown:** A configurable global request threshold triggers a temporary, time-bounded lockdown if the whole system is being flooded, not just a single IP.
- **No Heavy Dependencies:** Core logic has zero third-party runtime dependencies beyond SLF4J for logging.

## Integration

Add the following to your `pom.xml` if you are using Maven:

```xml
<dependency>
    <groupId>com.jguard</groupId>
    <artifactId>jguard-core</artifactId>
    <version>3.0.0</version>
</dependency>
```

## Usage

```java
GuardConfig config = GuardConfig.builder()
        .maxIPRequests(50)
        .globalThreshold(100_000)
        .window(10, TimeUnit.SECONDS)
        .blacklistDuration(5, TimeUnit.MINUTES)
        .build();

JGuard guard = new JGuard(config);
guard.whitelist("10.0.0.1");

if (guard.allow(requestIp)) {
} else {
}
```

When you're done with a `JGuard` instance (e.g. on application shutdown), call:

```java
guard.shutdown();
```

This stops the background cleanup thread.

### Custom storage

To back rate-limiting state with something other than in-memory maps (e.g. Redis, for use across multiple app instances), implement `StorageProvider` and pass it in:

```java
StorageProvider storage = new MyRedisStorageProvider(redisClient);
JGuard guard = new JGuard(config, storage);
```

## Configuration

| Option | Default | Description |
|---|---|---|
| `maxIPRequests` | 50 | Max requests a single IP can make per window before being blacklisted |
| `globalThreshold` | 100,000 | Max total requests across all IPs per window before triggering a global lockdown |
| `windowMs` | 10,000 (10s) | Length of the rate-limiting window |
| `blacklistDurationMs` | 300,000 (5m) | How long a banned IP stays blacklisted |

## Requirements

- Java 17+
- SLF4J + a logging backend (e.g. Logback)
